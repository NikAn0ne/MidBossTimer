package com.example.midbosstimer

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import androidx.compose.runtime.collectAsState



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TimerApp()
        }
    }
}


@Composable
fun TimerApp(viewModel: TimerViewModel = viewModel()) {
    val scope = rememberCoroutineScope()
    val timerValue by viewModel.timerValue.collectAsState()
    val isRunning by viewModel.isRunning.collectAsState()
    val selectedTimer by viewModel.selectedTimer.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.SpaceBetween,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TimerButton("7 мин", 7 * 60) { viewModel.selectTimer(it) }
            TimerButton("6 мин", 6 * 60) { viewModel.selectTimer(it) }
            TimerButton("5 мин", 5 * 60) { viewModel.selectTimer(it) }
        }

        Text(
            text = formatTime(timerValue),
            style = MaterialTheme.typography.displayLarge
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { viewModel.startTimer(scope) }, enabled = !isRunning) { Text("Старт") }
            Button(onClick = { viewModel.pauseTimer() }, enabled = isRunning) { Text("Пауза") }
            Button(onClick = { viewModel.stopTimer() }) { Text("Стоп") }
        }

        Button(onClick = { viewModel.resetTimers() }) {
            Text("Сбросить")
        }
    }
}

@Composable
fun TimerButton(label: String, seconds: Int, onClick: (Int) -> Unit) {
    Button(onClick = { onClick(seconds) }) {
        Text(label)
    }
}

fun formatTime(seconds: Int): String {
    val minutes = seconds / 60
    val secs = seconds % 60
    return String.format("%02d:%02d", minutes, secs)
}

class TimerViewModel : ViewModel() {
    private val _timerValue = MutableStateFlow(0)
    val timerValue: StateFlow<Int> get() = _timerValue

    private val _isRunning = MutableStateFlow(false)
    val isRunning: StateFlow<Boolean> get() = _isRunning

    private val _selectedTimer = MutableStateFlow(0)
    val selectedTimer: StateFlow<Int> get() = _selectedTimer

    private var mediaPlayer: MediaPlayer? = null
    private var countdownJob: Job? = null

    fun selectTimer(seconds: Int) {
        if (!_isRunning.value) {
            _selectedTimer.value = seconds
            _timerValue.value = seconds
        }
    }

    fun startTimer(scope: CoroutineScope) {
        if (_isRunning.value || _selectedTimer.value == 0) return

        _isRunning.value = true
        countdownJob = scope.launch {
            while (_timerValue.value > 0) {
                delay(1000L)
                _timerValue.value--
                if (_timerValue.value == 30) playSound()
            }
            _isRunning.value = false
        }
    }

    fun pauseTimer() {
        countdownJob?.cancel()
        _isRunning.value = false
    }

    fun stopTimer() {
        countdownJob?.cancel()
        _isRunning.value = false
        _timerValue.value = _selectedTimer.value
    }

    fun resetTimers() {
        countdownJob?.cancel()
        _isRunning.value = false
        _timerValue.value = 0
        _selectedTimer.value = 0
    }

    private fun playSound() {
        mediaPlayer?.release()
        //mediaPlayer = MediaPlayer.create(getApplication(), R.raw.alert_sound).apply { start() }
    }

    override fun onCleared() {
        super.onCleared()
        mediaPlayer?.release()
    }
}

