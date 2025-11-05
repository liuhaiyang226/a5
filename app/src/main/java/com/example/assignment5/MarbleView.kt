package com.example.assignment5

import android.annotation.SuppressLint
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun MarbleView(viewModel: MarbleViewModel = viewModel()) {
    val context = LocalContext.current

    // setup sensor when composable is first created
    LaunchedEffect(Unit) {
        viewModel.setupSensor(context)
    }

    // register/unregister sensor based on lifecycle
    DisposableEffect(Unit) {
        viewModel.registerSensor()
        onDispose {
            viewModel.unregisterSensor()
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White)
    ) {
        val widthPx = with(LocalDensity.current) { maxWidth.toPx() }
        val heightPx = with(LocalDensity.current) { maxHeight.toPx() }

        // update screen bounds in viewmodel
        LaunchedEffect(widthPx, heightPx) {
            viewModel.updateBounds(widthPx, heightPx)
        }

        val state = viewModel.marbleState

        // the marble - a simple blue circle
        Box(
            modifier = Modifier
                .offset(
                    x = with(LocalDensity.current) { state.x.toDp() },
                    y = with(LocalDensity.current) { state.y.toDp() }
                )
                .size(80.dp)
                .background(Color.Blue, CircleShape)
        )
    }
}