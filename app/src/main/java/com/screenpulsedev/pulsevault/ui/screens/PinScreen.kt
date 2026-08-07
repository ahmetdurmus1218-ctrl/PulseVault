package com.screenpulsedev.pulsevault.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Backspace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.screenpulsedev.pulsevault.auth.PinManager
import kotlinx.coroutines.delay

private const val PIN_LENGTH = PinManager.PIN_LENGTH

/**
 * mode = ENTER: asks for the existing PIN, calls onSuccess(pin) when digits are entered.
 * mode = SET: asks twice (create, then confirm) and calls onPinCreated(pin) on match.
 */
enum class PinScreenMode { ENTER, SET }

@Composable
fun PinScreen(
    mode: PinScreenMode,
    title: String,
    errorMessage: String? = null,
    lockoutSecondsRemaining: Long = 0,
    onPinEntered: (String) -> Unit,
    onPinCreated: ((String) -> Unit)? = null,
    onCancel: (() -> Unit)? = null
) {
    onCancel?.let { BackHandler(onBack = it) }

    var firstPin by remember { mutableStateOf<String?>(null) }
    var currentInput by remember { mutableStateOf("") }
    var localError by remember { mutableStateOf<String?>(null) }

    // Live countdown so the person can see the lockout ticking down instead of
    // just a static message — re-checks the actual stored value every second.
    var remainingSeconds by remember(lockoutSecondsRemaining) { mutableStateOf(lockoutSecondsRemaining) }
    LaunchedEffect(lockoutSecondsRemaining) {
        remainingSeconds = lockoutSecondsRemaining
        while (remainingSeconds > 0) {
            delay(1000)
            remainingSeconds = (remainingSeconds - 1).coerceAtLeast(0)
        }
    }
    val isLockedOut = remainingSeconds > 0

    val displayTitle = when {
        mode == PinScreenMode.SET && firstPin == null -> title
        mode == PinScreenMode.SET && firstPin != null -> "PIN'i Onayla"
        else -> title
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        onCancel?.let {
            Box(modifier = Modifier.fillMaxWidth()) {
                IconButton(onClick = it, modifier = Modifier.align(Alignment.CenterStart)) {
                    Icon(Icons.Filled.ArrowBack, contentDescription = "Geri")
                }
            }
            Spacer(Modifier.height(8.dp))
        }

        Text(displayTitle, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))

        if (isLockedOut) {
            Icon(Icons.Filled.Lock, contentDescription = null, tint = MaterialTheme.colorScheme.error, modifier = Modifier.size(40.dp))
            Spacer(Modifier.height(12.dp))
            Text(
                "Çok fazla hatalı deneme.\n${formatDuration(remainingSeconds)} sonra tekrar dene.",
                color = MaterialTheme.colorScheme.error,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                fontWeight = FontWeight.SemiBold
            )
            return@Column
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            repeat(PIN_LENGTH) { index ->
                val filled = index < currentInput.length
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .background(
                            color = if (filled) MaterialTheme.colorScheme.primary
                            else MaterialTheme.colorScheme.surfaceVariant,
                            shape = CircleShape
                        )
                )
            }
        }

        val shownError = localError ?: errorMessage
        if (shownError != null) {
            Spacer(Modifier.height(16.dp))
            Text(shownError, color = MaterialTheme.colorScheme.error)
        }

        Spacer(Modifier.height(32.dp))

        val rows = listOf(
            listOf("1", "2", "3"),
            listOf("4", "5", "6"),
            listOf("7", "8", "9"),
            listOf("", "0", "⌫")
        )
        rows.forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                row.forEach { digit ->
                    Box(
                        modifier = Modifier
                            .size(60.dp)
                            .then(
                                if (digit.isNotEmpty()) Modifier.clickable {
                                    localError = null
                                    when (digit) {
                                        "⌫" -> if (currentInput.isNotEmpty()) currentInput = currentInput.dropLast(1)
                                        else -> if (currentInput.length < PIN_LENGTH) currentInput += digit
                                    }
                                    if (currentInput.length == PIN_LENGTH) {
                                        val pin = currentInput
                                        currentInput = ""
                                        if (mode == PinScreenMode.ENTER) {
                                            onPinEntered(pin)
                                        } else {
                                            if (firstPin == null) {
                                                firstPin = pin
                                            } else if (firstPin == pin) {
                                                onPinCreated?.invoke(pin)
                                            } else {
                                                localError = "PIN'ler eşleşmedi, tekrar dene"
                                                firstPin = null
                                            }
                                        }
                                    }
                                } else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        if (digit == "⌫") {
                            Icon(Icons.Filled.Backspace, contentDescription = "Sil")
                        } else if (digit.isNotEmpty()) {
                            Text(digit, style = MaterialTheme.typography.headlineMedium)
                        }
                    }
                }
            }
            Spacer(Modifier.height(6.dp))
        }
    }
}

private fun formatDuration(totalSeconds: Long): String {
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return if (minutes > 0) "$minutes dk $seconds sn" else "$seconds sn"
}
