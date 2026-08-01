package com.screenpulsedev.pulsevault

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.screenpulsedev.pulsevault.auth.BiometricAuthManager
import com.screenpulsedev.pulsevault.crypto.VaultCryptoManager
import com.screenpulsedev.pulsevault.data.VaultCategory
import com.screenpulsedev.pulsevault.data.VaultItem
import com.screenpulsedev.pulsevault.data.VaultItemPayload
import com.screenpulsedev.pulsevault.data.VaultRepository
import com.screenpulsedev.pulsevault.ui.screens.AddItemScreen
import com.screenpulsedev.pulsevault.ui.screens.ItemDetailScreen
import com.screenpulsedev.pulsevault.ui.screens.LockScreen
import com.screenpulsedev.pulsevault.ui.screens.VaultListScreen
import com.screenpulsedev.pulsevault.ui.theme.PulseVaultTheme
import com.screenpulsedev.pulsevault.util.copySensitiveText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Screen-level navigation kept intentionally simple for this first test build:
 * a small sealed state machine instead of full Navigation-Compose, since the
 * whole point of this pass is proving out the security flow end to end.
 */
sealed interface Screen {
    data object Locked : Screen
    data object List : Screen
    data object Add : Screen
    data class Detail(val item: VaultItem, val payload: VaultItemPayload) : Screen
}

class VaultViewModel(private val repo: VaultRepository) : ViewModel() {
    private val _screen = MutableStateFlow<Screen>(Screen.Locked)
    val screen: StateFlow<Screen> = _screen.asStateFlow()

    private val _items = MutableStateFlow<kotlin.collections.List<VaultItem>>(emptyList())
    val items: StateFlow<kotlin.collections.List<VaultItem>> = _items.asStateFlow()

    private val _errorEvent = MutableStateFlow<String?>(null)
    val errorEvent: StateFlow<String?> = _errorEvent.asStateFlow()

    fun consumeError() { _errorEvent.value = null }

    init {
        viewModelScope.launch {
            repo.observeAll().collect { _items.value = it }
        }
    }

    fun goTo(screen: Screen) { _screen.value = screen }

    fun addItem(cipher: javax.crypto.Cipher, label: String, category: VaultCategory, payload: VaultItemPayload) {
        viewModelScope.launch {
            try {
                repo.addItem(cipher, label, category, payload)
                _screen.value = Screen.List
            } catch (e: Exception) {
                _errorEvent.value = "Kaydedilemedi: ${e.message}"
            }
        }
    }

    fun decryptAndShow(cipher: javax.crypto.Cipher, item: VaultItem) {
        viewModelScope.launch {
            try {
                val payload = repo.decryptItem(cipher, item)
                _screen.value = Screen.Detail(item, payload)
            } catch (e: Exception) {
                // Most common cause: this entry was encrypted under an older
                // Keystore key (e.g. before a security-config change invalidated
                // the previous key). It can't be recovered — only deleted.
                _errorEvent.value = "Bu kayıt açılamadı ve kurtarılamaz (eski anahtarla şifrelenmiş olabilir). Kaydı silip yeniden eklemen gerekiyor."
            }
        }
    }

    fun delete(item: VaultItem) {
        viewModelScope.launch {
            repo.deleteItem(item)
            _screen.value = Screen.List
        }
    }
}

class MainActivity : FragmentActivity() {

    private val repo by lazy { VaultRepository(applicationContext) }
    private val viewModel by viewModels<VaultViewModel>(
        factoryProducer = {
            object : androidx.lifecycle.ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T =
                    VaultViewModel(repo) as T
            }
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        VaultCryptoManager.ensureKeyExists()

        setContent {
            PulseVaultTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    VaultApp(viewModel = viewModel, activity = this)
                }
            }
        }
    }
}

@Composable
fun VaultApp(viewModel: VaultViewModel, activity: FragmentActivity) {
    val screen by viewModel.screen.collectAsState()
    val items by viewModel.items.collectAsState()
    val errorEvent by viewModel.errorEvent.collectAsState()
    val executor = ContextCompat.getMainExecutor(activity)

    androidx.compose.runtime.LaunchedEffect(errorEvent) {
        errorEvent?.let {
            Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
            viewModel.consumeError()
        }
    }

    when (val current = screen) {
        is Screen.Locked -> {
            val crashLog = remember {
                val f = java.io.File(activity.filesDir, "crash_log.txt")
                if (f.exists()) f.readText().takeLast(4000) else null
            }
            LockScreen(
                crashLogText = crashLog,
                onUnlockClick = {
                    if (!BiometricAuthManager.canAuthenticate(activity)) {
                        Toast.makeText(
                            activity,
                            "Cihazda kilit ekranı ayarlı değil. Ayarlar > Güvenlik'ten parmak izi veya PIN ekle.",
                            Toast.LENGTH_LONG
                        ).show()
                        return@LockScreen
                    }
                    try {
                        BiometricAuthManager.authenticate(
                            activity = activity,
                            title = "PulseVault'u Aç",
                            subtitle = "Devam etmek için kimliğini doğrula",
                            executor = executor,
                            onSuccess = { viewModel.goTo(Screen.List) },
                            onError = { msg -> Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show() }
                        )
                    } catch (e: Exception) {
                        Toast.makeText(activity, "Hata: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            )
        }

        is Screen.List -> VaultListScreen(
            items = items,
            onAddClick = { viewModel.goTo(Screen.Add) },
            onItemClick = { item ->
                BiometricAuthManager.authenticate(
                    activity = activity,
                    title = "Kaydı Görüntüle",
                    subtitle = item.label,
                    executor = executor,
                    onSuccess = {
                        try {
                            val cipher = VaultCryptoManager.getDecryptCipher(item.iv)
                            viewModel.decryptAndShow(cipher, item)
                        } catch (e: Exception) {
                            Toast.makeText(activity, "Hata: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    },
                    onError = { msg -> Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show() }
                )
            }
        )

        is Screen.Add -> AddItemScreen(
            onSave = { label, category, payload ->
                BiometricAuthManager.authenticate(
                    activity = activity,
                    title = "Kaydı Şifrele",
                    subtitle = "Kaydetmek için kimliğini doğrula",
                    executor = executor,
                    onSuccess = {
                        try {
                            val cipher = VaultCryptoManager.getEncryptCipher()
                            viewModel.addItem(cipher, label, category, payload)
                        } catch (e: Exception) {
                            Toast.makeText(activity, "Hata: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    },
                    onError = { msg -> Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show() }
                )
            },
            onCancel = { viewModel.goTo(Screen.List) }
        )

        is Screen.Detail -> ItemDetailScreen(
            label = current.item.label,
            payload = current.payload,
            onCopy = { fieldLabel, value ->
                copySensitiveText(activity, fieldLabel, value)
                Toast.makeText(activity, "$fieldLabel kopyalandı (30sn sonra silinir)", Toast.LENGTH_SHORT).show()
            },
            onDelete = { viewModel.delete(current.item) },
            onBack = { viewModel.goTo(Screen.List) }
        )
    }
}
