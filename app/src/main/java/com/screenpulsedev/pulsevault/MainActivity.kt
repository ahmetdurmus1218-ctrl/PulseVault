package com.screenpulsedev.pulsevault

import android.os.Bundle
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.screenpulsedev.pulsevault.auth.BiometricAuthManager
import com.screenpulsedev.pulsevault.auth.PinManager
import com.screenpulsedev.pulsevault.crypto.VaultCryptoManager
import com.screenpulsedev.pulsevault.data.CardNetwork
import com.screenpulsedev.pulsevault.data.VaultCategory
import com.screenpulsedev.pulsevault.data.VaultItem
import com.screenpulsedev.pulsevault.data.VaultItemPayload
import com.screenpulsedev.pulsevault.data.VaultRepository
import com.screenpulsedev.pulsevault.ui.screens.AddItemScreen
import com.screenpulsedev.pulsevault.ui.screens.ItemDetailScreen
import com.screenpulsedev.pulsevault.ui.screens.LockScreen
import com.screenpulsedev.pulsevault.ui.screens.PinScreen
import com.screenpulsedev.pulsevault.ui.screens.PinScreenMode
import com.screenpulsedev.pulsevault.ui.screens.SettingsScreen
import com.screenpulsedev.pulsevault.ui.screens.SimpleDetailScreen
import com.screenpulsedev.pulsevault.ui.screens.VaultListScreen
import com.screenpulsedev.pulsevault.ui.theme.PulseVaultTheme
import com.screenpulsedev.pulsevault.util.copySensitiveText
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.crypto.Cipher

sealed interface Screen {
    data object Locked : Screen
    data object PinEntry : Screen
    data object List : Screen
    data object Add : Screen
    data object Settings : Screen
    data object SetPin : Screen
    data object AccessibilityWarning : Screen
    data class Detail(val item: VaultItem, val payload: VaultItemPayload) : Screen
    data class Edit(val item: VaultItem, val payload: VaultItemPayload) : Screen
    // Held while LOCKED, in place of Detail/Edit — deliberately carries NO
    // decrypted payload, so nothing plaintext sits in memory during a lock.
    // On unlock, the real screen is rebuilt via a fresh decrypt (see VaultApp).
    data class PendingDetail(val item: VaultItem) : Screen
    data class PendingEdit(val item: VaultItem) : Screen
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

    private var savedScreen: Screen = Screen.List

    /** Called whenever the app leaves the foreground — remembers where we were,
     *  but NEVER keeps a decrypted payload alive across a lock. */
    fun lock() {
        val current = _screen.value
        savedScreen = when (current) {
            is Screen.Detail -> Screen.PendingDetail(current.item)
            is Screen.Edit -> Screen.PendingEdit(current.item)
            is Screen.Locked, is Screen.PinEntry, is Screen.AccessibilityWarning -> savedScreen
            else -> current
        }
        _screen.value = Screen.Locked
    }

    /** Called after a successful re-auth — returns to whatever screen we were on. */
    fun restoreAfterUnlock() { _screen.value = savedScreen }

    fun toggleFavorite(item: VaultItem) {
        viewModelScope.launch { repo.toggleFavorite(item) }
    }

    fun addItem(
        cipher: Cipher, label: String, category: VaultCategory, payload: VaultItemPayload,
        network: CardNetwork, bank: String, isVirtual: Boolean
    ) {
        viewModelScope.launch {
            try {
                repo.addItem(cipher, label, category, payload, network, bank, isVirtual)
                _screen.value = Screen.List
            } catch (e: Exception) {
                _errorEvent.value = "Kaydedilemedi: ${e.message}"
            }
        }
    }

    fun updateItem(
        cipher: Cipher, id: Long, label: String, category: VaultCategory, payload: VaultItemPayload,
        network: CardNetwork, bank: String, isVirtual: Boolean
    ) {
        viewModelScope.launch {
            try {
                repo.updateItem(cipher, id, label, category, payload, network, bank, isVirtual)
                _screen.value = Screen.List
            } catch (e: Exception) {
                _errorEvent.value = "Güncellenemedi: ${e.message}"
            }
        }
    }

    fun decryptAndShow(cipher: Cipher, item: VaultItem) {
        viewModelScope.launch {
            try {
                val payload = repo.decryptItem(cipher, item)
                _screen.value = Screen.Detail(item, payload)
            } catch (e: Exception) {
                _errorEvent.value = "Bu kayıt açılamadı ve kurtarılamaz. Kaydı silip yeniden eklemen gerekiyor."
            }
        }
    }

    fun decryptForEdit(cipher: Cipher, item: VaultItem) {
        viewModelScope.launch {
            try {
                val payload = repo.decryptItem(cipher, item)
                _screen.value = Screen.Edit(item, payload)
            } catch (e: Exception) {
                _errorEvent.value = "Bu kayıt açılamadı ve kurtarılamaz. Kaydı silip yeniden eklemen gerekiyor."
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
                override fun <T : ViewModel> create(modelClass: Class<T>): T = VaultViewModel(repo) as T
            }
        }
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        VaultCryptoManager.ensureKeyExists()

        window.setFlags(
            android.view.WindowManager.LayoutParams.FLAG_SECURE,
            android.view.WindowManager.LayoutParams.FLAG_SECURE
        )

        setContent {
            PulseVaultTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    com.screenpulsedev.pulsevault.ui.theme.GlowBackdrop {
                        VaultApp(viewModel = viewModel, activity = this)
                    }
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        // Leaving the foreground (home button, app switcher, another app) always
        // re-locks — but NOT on a rotation/config-change recreate, which also
        // triggers onStop and would otherwise be a very annoying false lock.
        //
        // NOTE: we deliberately do NOT wipe the clipboard here anymore. Pasting
        // a copied value into another app requires switching away from PulseVault
        // first — which is exactly when onStop fires. Clearing immediately here
        // wiped it before the paste could ever happen. The 30s delayed auto-clear
        // (scheduled at copy time) is the real safety net.
        if (!isChangingConfigurations) {
            viewModel.lock()
        }
    }
}

@Composable
fun VaultApp(viewModel: VaultViewModel, activity: FragmentActivity) {
    val screen by viewModel.screen.collectAsState()
    val items by viewModel.items.collectAsState()
    val errorEvent by viewModel.errorEvent.collectAsState()
    val executor = ContextCompat.getMainExecutor(activity)
    var appPinEnabled by remember { mutableStateOf(PinManager.isPinSet(activity)) }
    var clipboardDelaySeconds by remember {
        mutableStateOf(com.screenpulsedev.pulsevault.util.SecureClipboardManager.getDelaySeconds(activity))
    }
    var pinError by remember { mutableStateOf<String?>(null) }

    androidx.compose.runtime.LaunchedEffect(errorEvent) {
        errorEvent?.let {
            Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
            viewModel.consumeError()
        }
    }

    fun proceedAfterDeviceAuth() {
        val risky = com.screenpulsedev.pulsevault.auth.hasActiveAccessibilityServices(activity)
        when {
            risky -> viewModel.goTo(Screen.AccessibilityWarning)
            PinManager.isPinSet(activity) -> viewModel.goTo(Screen.PinEntry)
            else -> viewModel.restoreAfterUnlock()
        }
    }

    when (val current = screen) {
        is Screen.Locked -> LockScreen(
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
                        onSuccess = { proceedAfterDeviceAuth() },
                        onError = { msg -> Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show() }
                    )
                } catch (e: Exception) {
                    Toast.makeText(activity, "Hata: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        )

        is Screen.PinEntry -> PinScreen(
            mode = PinScreenMode.ENTER,
            title = "PIN Gir",
            errorMessage = pinError,
            lockoutSecondsRemaining = PinManager.lockoutSecondsRemaining(activity),
            onPinEntered = { pin ->
                if (PinManager.verifyPin(activity, pin)) {
                    pinError = null
                    viewModel.restoreAfterUnlock()
                } else {
                    pinError = "Yanlış PIN, tekrar dene"
                }
            },
            onCancel = { viewModel.lock() }
        )

        is Screen.AccessibilityWarning -> com.screenpulsedev.pulsevault.ui.screens.AccessibilityWarningScreen(
            onOpenSettings = {
                try {
                    activity.startActivity(android.content.Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS))
                } catch (e: Exception) {
                    Toast.makeText(activity, "Ayarlar açılamadı", Toast.LENGTH_SHORT).show()
                }
            },
            onProceedAnyway = {
                if (PinManager.isPinSet(activity)) {
                    viewModel.goTo(Screen.PinEntry)
                } else {
                    viewModel.restoreAfterUnlock()
                }
            },
            onLock = { viewModel.lock() }
        )

        is Screen.List -> VaultListScreen(
            items = items,
            onAddClick = { viewModel.goTo(Screen.Add) },
            onSettingsClick = { viewModel.goTo(Screen.Settings) },
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

        is Screen.Settings -> SettingsScreen(
            isAppPinEnabled = appPinEnabled,
            onToggleAppPin = { enable ->
                if (enable) {
                    viewModel.goTo(Screen.SetPin)
                } else {
                    PinManager.clearPin(activity)
                    appPinEnabled = false
                }
            },
            clipboardDelaySeconds = clipboardDelaySeconds,
            onClipboardDelayChange = { seconds ->
                com.screenpulsedev.pulsevault.util.SecureClipboardManager.setDelaySeconds(activity, seconds)
                clipboardDelaySeconds = seconds
            },
            onRequestBatteryExemption = {
                try {
                    val intent = android.content.Intent(
                        android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS,
                        android.net.Uri.parse("package:${activity.packageName}")
                    )
                    activity.startActivity(intent)
                } catch (e: Exception) {
                    Toast.makeText(activity, "Bu ayar cihazında bulunamadı", Toast.LENGTH_SHORT).show()
                }
            },
            onBack = { viewModel.goTo(Screen.List) }
        )

        is Screen.SetPin -> PinScreen(
            mode = PinScreenMode.SET,
            title = "Yeni PIN Oluştur",
            onPinEntered = {},
            onPinCreated = { pin ->
                PinManager.setPin(activity, pin)
                appPinEnabled = true
                Toast.makeText(activity, "PIN oluşturuldu", Toast.LENGTH_SHORT).show()
                viewModel.goTo(Screen.Settings)
            },
            onCancel = { viewModel.goTo(Screen.Settings) }
        )

        is Screen.Add -> AddItemScreen(
            onSave = { label, category, payload, network, bank, isVirtual ->
                BiometricAuthManager.authenticate(
                    activity = activity,
                    title = "Kaydı Şifrele",
                    subtitle = "Kaydetmek için kimliğini doğrula",
                    executor = executor,
                    onSuccess = {
                        try {
                            val cipher = VaultCryptoManager.getEncryptCipher()
                            viewModel.addItem(cipher, label, category, payload, network, bank, isVirtual)
                        } catch (e: Exception) {
                            Toast.makeText(activity, "Hata: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    },
                    onError = { msg -> Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show() }
                )
            },
            onCancel = { viewModel.goTo(Screen.List) }
        )

        is Screen.PendingDetail -> {
            // Re-authenticate and re-decrypt from scratch — nothing plaintext
            // survived the lock, so this is a fresh request, not a cached restore.
            androidx.compose.runtime.LaunchedEffect(current.item.id) {
                BiometricAuthManager.authenticate(
                    activity = activity,
                    title = "Kaydı Görüntüle",
                    subtitle = current.item.label,
                    executor = executor,
                    onSuccess = {
                        try {
                            val cipher = VaultCryptoManager.getDecryptCipher(current.item.iv)
                            viewModel.decryptAndShow(cipher, current.item)
                        } catch (e: Exception) {
                            Toast.makeText(activity, "Hata: ${e.message}", Toast.LENGTH_LONG).show()
                            viewModel.goTo(Screen.List)
                        }
                    },
                    onError = {
                        viewModel.goTo(Screen.List)
                    }
                )
            }
            LockScreen(onUnlockClick = {})
        }

        is Screen.PendingEdit -> {
            androidx.compose.runtime.LaunchedEffect(current.item.id) {
                BiometricAuthManager.authenticate(
                    activity = activity,
                    title = "Düzenlemeye Devam Et",
                    subtitle = current.item.label,
                    executor = executor,
                    onSuccess = {
                        try {
                            val cipher = VaultCryptoManager.getDecryptCipher(current.item.iv)
                            viewModel.decryptForEdit(cipher, current.item)
                        } catch (e: Exception) {
                            Toast.makeText(activity, "Hata: ${e.message}", Toast.LENGTH_LONG).show()
                            viewModel.goTo(Screen.List)
                        }
                    },
                    onError = {
                        viewModel.goTo(Screen.List)
                    }
                )
            }
            LockScreen(onUnlockClick = {})
        }

        is Screen.Detail -> {
            val isCardLike = current.item.category == VaultCategory.CREDIT_CARD ||
                current.item.category == VaultCategory.BANK_ACCOUNT
            if (isCardLike) {
                ItemDetailScreen(
                    label = current.item.label,
                    category = current.item.category,
                    network = current.item.network,
                    lastFourDigits = current.item.lastFourDigits,
                    bank = current.item.bank,
                    isVirtual = current.item.isVirtual,
                    isFavorite = current.item.isFavorite,
                    payload = current.payload,
                    onCopy = { fieldLabel, value ->
                        copySensitiveText(activity, fieldLabel, value)
                        Toast.makeText(activity, "$fieldLabel kopyalandı (${clipboardDelaySeconds}sn sonra silinir)", Toast.LENGTH_SHORT).show()
                    },
                    onRequestAuth = { onGranted ->
                        BiometricAuthManager.authenticate(
                            activity = activity,
                            title = "Bu Alanı Göster",
                            subtitle = "Kimliğini doğrula",
                            executor = executor,
                            onSuccess = onGranted,
                            onError = { msg -> Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show() }
                        )
                    },
                    onDelete = { viewModel.delete(current.item) },
                    onEdit = { viewModel.goTo(Screen.Edit(current.item, current.payload)) },
                    onToggleFavorite = { viewModel.toggleFavorite(current.item) },
                    onBack = { viewModel.goTo(Screen.List) }
                )
            } else {
                SimpleDetailScreen(
                    label = current.item.label,
                    category = current.item.category,
                    isFavorite = current.item.isFavorite,
                    payload = current.payload,
                    onCopy = { fieldLabel, value ->
                        copySensitiveText(activity, fieldLabel, value)
                        Toast.makeText(activity, "$fieldLabel kopyalandı (${clipboardDelaySeconds}sn sonra silinir)", Toast.LENGTH_SHORT).show()
                    },
                    onDelete = { viewModel.delete(current.item) },
                    onEdit = { viewModel.goTo(Screen.Edit(current.item, current.payload)) },
                    onToggleFavorite = { viewModel.toggleFavorite(current.item) },
                    onBack = { viewModel.goTo(Screen.List) }
                )
            }
        }

        is Screen.Edit -> AddItemScreen(
            screenTitle = "Düzenle",
            initialLabel = current.item.label,
            initialCategory = current.item.category,
            initialBank = current.item.bank,
            initialNetwork = current.item.network,
            initialIsVirtual = current.item.isVirtual,
            initialPayload = current.payload,
            lockCategory = true,
            onSave = { label, category, payload, network, bank, isVirtual ->
                BiometricAuthManager.authenticate(
                    activity = activity,
                    title = "Değişiklikleri Kaydet",
                    subtitle = "Kaydetmek için kimliğini doğrula",
                    executor = executor,
                    onSuccess = {
                        try {
                            val cipher = VaultCryptoManager.getEncryptCipher()
                            viewModel.updateItem(cipher, current.item.id, label, category, payload, network, bank, isVirtual)
                        } catch (e: Exception) {
                            Toast.makeText(activity, "Hata: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                    },
                    onError = { msg -> Toast.makeText(activity, msg, Toast.LENGTH_SHORT).show() }
                )
            },
            onCancel = { viewModel.goTo(Screen.List) }
        )
    }
}
