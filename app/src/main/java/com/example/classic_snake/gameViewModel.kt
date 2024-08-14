package com.example.classic_snake

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class gameViewModel: ViewModel() {

    private val _state = MutableStateFlow(SnakeGameState())
    val state = _state.asStateFlow()


    fun onEvent(event: GameEvent) {
        when(event) {
            GameEvent.PauseGame -> {
                _state.update { it.copy(gameState = GameState.PAUSED) }
            }
            GameEvent.ResetGame -> {
                _state.value = SnakeGameState()
            }
            GameEvent.StartGame -> {
                _state.update { it.copy(gameState = GameState.STARTED) }
                viewModelScope.launch {
                    while (state.value.gameState == GameState.STARTED) {
                        val delayms = when (state.value.snake.size) {
                            in 1 .. 5 -> 250L
                            in 6 .. 10 -> 200L
                            else -> 150L
                        }
                        delay(200)
                        _state.value = updateGame(state.value)
                    }
                }

            }
            is GameEvent.UpdateDirection -> {
                updateDirection(event.offset, event.canvasWidth)

            }
        }
    }

    private fun updateDirection(offset: Offset, canvasWidth: Int) {
        if(!state.value.isGameOver) {
            val cellSize = canvasWidth / state.value.xGridSize
            val tapX = (offset.x / cellSize).toInt()
            val tapY = (offset.y / cellSize).toInt()
            val head = state.value.snake.first()

            _state.update {
                it.copy(
                    direction = when(state.value.direction) {
                        Direction.UP, Direction.DOWN -> {
                            if(tapX < head.x) Direction.LEFT else Direction.RIGHT

                        }
                        Direction.LEFT, Direction.RIGHT -> {
                            if (tapY < head.y) Direction.UP else Direction.DOWN
                        }
                    }
                )
            }
        }
    }
    private fun updateGame(currentGame: SnakeGameState): SnakeGameState {
        if (currentGame.isGameOver) {
            return currentGame
        }

        val head = currentGame.snake.first()
        val xGridSize = currentGame.xGridSize
        val yGridSize = currentGame.yGridSize

        //updating movement of snakes

        val newHead = when (currentGame.direction) {
            Direction.UP -> Coordinate(x = head.x, y = (head.y - 1))
            Direction.DOWN -> Coordinate(x = head.x, y = (head.y + 1))
            Direction.LEFT -> Coordinate(x = head.x - 1, y = head.y)
            Direction.RIGHT -> Coordinate(x = head.x + 1, y = head.y)

        }

        //saap khud se na takray or mobile k bahar na jaay
        if(
            currentGame.snake.contains(newHead) || ! isWithinBounds(newHead, xGridSize, yGridSize)
        ) {
            return currentGame.copy(isGameOver = true)
        }

        //mere saap ne thana tha liya
        var newSnake = mutableListOf(newHead) + currentGame.snake
        val newFood = if(newHead == currentGame.food) SnakeGameState.randFoodCoordinate()
        else currentGame.food

        //saap ko paal posh k bada bhi to krna hai
        if(newHead != currentGame.food) {
            newSnake = newSnake.toMutableList()
            newSnake.removeAt(newSnake.size - 1)

        }

        return currentGame.copy(snake = newSnake, food = newFood)

    }

    private fun isWithinBounds(
        coordinate: Coordinate,
        xGridSize: Int,
        yGridSize: Int
    ): Boolean {
        return coordinate.x in 1 until ((xGridSize) - 1)
                && coordinate.y in 1 until ((yGridSize) - 1)
    }

}