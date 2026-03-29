package com.example.pacecomplication.presentation.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.ButtonDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TimeText
import androidx.wear.tooling.preview.devices.WearDevices
import com.example.pacecomplication.R
import com.example.pacecomplication.presentation.PaceRepository
import com.example.pacecomplication.presentation.ui.theme.PaceComplicationTheme

@Composable
fun WearApp(onStopClick: () -> Unit = {}) {

    val pace by PaceRepository.currentPace.collectAsState()
    PaceComplicationTheme {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            contentAlignment = Alignment.Center
        ) {
            TimeText()
            Greeting(pace)
            Spacer(Modifier.height(2.dp))

        }
    }
}

@Composable
fun TecButton(onStopClick: () -> Unit = {}) {
    Button(
        onClick = onStopClick,
        modifier = Modifier
            .fillMaxWidth(0.8f) // Кнопка чуть уже экрана для красоты
            .height(50.dp),
        // Кнопка автоматически возьмет основной (primary) цвет темы
        colors = ButtonDefaults.primaryButtonColors()
    ) {
        Text(text = "СТАРТ")
    }
}

@Composable
fun Greeting(pace: String) {
    Text(
        modifier = Modifier.fillMaxWidth(),
        textAlign = TextAlign.Center,
        color = MaterialTheme.colors.primary,
        text = "${stringResource(R.string.pace_label)}\n$pace ${stringResource(R.string.pace_unit)}"
    )
}

@Preview(device = WearDevices.SMALL_ROUND, showSystemUi = true)
@Composable
fun DefaultPreview() {
    WearApp()
}