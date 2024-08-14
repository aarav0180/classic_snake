package com.example.classic_snake

import android.content.res.Resources.Theme
import android.media.MediaPlayer
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.absolutePadding
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.ripple.LocalRippleTheme
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.nestedscroll.nestedScrollModifierNode
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.modifier.modifierLocalMapOf
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.imageResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.example.classic_snake.ui.theme.Peach
import com.example.classic_snake.ui.theme.Purple40
import com.example.classic_snake.ui.theme.Purple80


@Composable
fun SnakeGameScreen(
    state: SnakeGameState,
    onEvent: (GameEvent) -> Unit
) {

    val foodImageBitmap = ImageBitmap.imageResource(id = R.drawable.apal)
    val snakeHeadBitmap = when(state.direction) {

        Direction.RIGHT -> ImageBitmap.imageResource(id = R.drawable.snak1)
        Direction.LEFT -> ImageBitmap.imageResource(id = R.drawable.snak3)
        Direction.UP -> ImageBitmap.imageResource(id = R.drawable.snak4)
        Direction.DOWN -> ImageBitmap.imageResource(id = R.drawable.snak2)

    }

    val context = LocalContext.current
    val foodSound = remember { MediaPlayer.create(context, R.raw.eating_sound) }
    val overSound = remember { MediaPlayer.create(context, R.raw.game_over_sound) }

    LaunchedEffect(key1 = state.snake.size) {
        if(state.snake.size != 1) {
            foodSound?.start()
        }
    }

    LaunchedEffect(key1 = state.isGameOver) {
        if(state.isGameOver) {
            overSound?.start()
        }
    }


    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
        ) {
        Column (
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceAround
        ) {
            Card(
               modifier = Modifier
                   .padding(5.dp)
                   .fillMaxWidth()

            ) {
                Text(
                    modifier = Modifier.padding(14.dp),
                    text = "Score:${state.snake.size - 1}",
                    style = MaterialTheme.typography.headlineMedium
                )

            }
            Canvas(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(ratio = 2 / 3f)
                    .pointerInput(state.gameState) {
                        if (state.gameState != GameState.STARTED) {
                            return@pointerInput
                        }
                        detectTapGestures { offset ->
                            onEvent(GameEvent.UpdateDirection(offset, size.width))
                        }
                    }
            ) {
                val cellSize = size.width / 20
                drawGameBoard(
                    cellSize = cellSize,
                    cellColor = Color.LightGray,
                    borderCellColor = Color.DarkGray,
                    gridHeight = state.yGridSize,
                    gridWidth = state.xGridSize
                )
                drawFood(
                    foodImage = foodImageBitmap,
                    cellSize = cellSize.toInt(),
                    coordinate = state.food
                )
                drawSnake(
                    snakeHead = snakeHeadBitmap,
                    cellSize = cellSize,
                    snake = state.snake
                )


            }
            Row (
                modifier = Modifier
                    .padding(7.dp)
                    .fillMaxWidth()
            ){
                Button(
                    modifier = Modifier
                        .weight(1f),
                    onClick = { onEvent(GameEvent.ResetGame) },
                    enabled = state.gameState == GameState.PAUSED || state.isGameOver
                ) {
                    Text(text = if(state.isGameOver) "Restart" else "New Game")

                }
                Spacer(modifier = Modifier.width(10.dp))
                Button(
                    modifier = Modifier.weight(1f),
                    onClick = {
                        when(state.gameState) {
                            GameState.IDLE, GameState.PAUSED -> onEvent(GameEvent.StartGame)
                            GameState.STARTED -> onEvent(GameEvent.PauseGame)
                        }
                    },
                    enabled = !(state.isGameOver)

                ) {
                    Text(
                        text = when(state.gameState) {
                            GameState.IDLE -> "Start"
                            GameState.PAUSED -> "Resume"
                            GameState.STARTED -> "Pause"
                        }
                    )

                }


            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(4.dp)
            ) {
                Text(
                    modifier = Modifier
                        .padding(6.dp)
                        .align(Alignment.CenterHorizontally),
                    text = "Made By Aarav",
                    style = MaterialTheme.typography.bodyMedium

                )
            }


        }
        AnimatedVisibility(visible = state.isGameOver) {
            Text(
                modifier = Modifier.padding(16.dp),
                text = "Game Over",
                style = MaterialTheme.typography.displayLarge,
                color = Color.Black

            )
        }

    }

    

}

private  fun DrawScope.drawGameBoard(
    cellSize: Float,
    cellColor: Color,
    borderCellColor: Color,
    gridWidth: Int,
    gridHeight: Int
) {
    for(i in 0 until gridWidth ) {
        for(j in 0 until gridHeight) {
            val isBorderCell = i == 0 || j == 0 || i == gridWidth -1 || j == gridHeight - 1
            drawRect(
                color = if (isBorderCell) borderCellColor
                else if ((i + j) % 2 == 0) cellColor
                else cellColor.copy(0.5f)
                ,
                topLeft = Offset(x = i * cellSize, y = j * cellSize),
                size = Size(cellSize, cellSize)

            )
        }

    }
}

private fun DrawScope.drawFood(
    foodImage: ImageBitmap,
    cellSize: Int,
    coordinate: Coordinate

){
    drawImage(
        image = foodImage,
        dstOffset = IntOffset(
            x = (coordinate.x * cellSize),
            y = (coordinate.y * cellSize)
        ),
        dstSize = IntSize(cellSize, cellSize)
    )

}

private fun DrawScope.drawSnake(
    snakeHead: ImageBitmap,
    cellSize: Float,
    snake: List<Coordinate>
){
    val cellSizeInt = cellSize.toInt()
    snake.forEachIndexed{ index, coordinate ->
        val radius = if (index == snake.lastIndex) { cellSize / 2.5f} else {cellSize / 2}
        if(index == 0){
            drawImage(
                image = snakeHead,
                dstOffset = IntOffset(
                    x = (coordinate.x * cellSizeInt),
                    y = (coordinate.y * cellSizeInt)
                ),
                dstSize = IntSize(cellSizeInt, cellSizeInt)
            )
        }
        else {
            drawCircle(
                color = Peach,
                center = Offset(
                        x = (coordinate.x * cellSize) + radius,
                        y = (coordinate.y * cellSize) + radius
                        ),
                radius = radius
            )
        }
}
}