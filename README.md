# PulseVault

Kişisel, tamamen offline çalışan kart/hesap kasası (Android).

## Güvenlik mimarisi

- **Android Keystore (AES-256-GCM)** — şifreleme anahtarı donanımda üretilir (TEE / varsa StrongBox), uygulama kodu anahtarın ham baytlarına asla erişemez.
- **Biyometrik kilit** — anahtar `setUserAuthenticationRequired(true)` ile üretilir; parmak izi/yüz veya cihaz PIN'i doğrulanmadan şifre çözme işlemi OS seviyesinde engellenir.
- **İnternet izni yok** — manifestte `INTERNET` izni hiç tanımlı değil, uygulama fiziksel olarak ağa veri gönderemez.
- **Yedekleme kapalı** — `allowBackup=false` + `dataExtractionRules` ile Google'ın otomatik bulut yedeğine hiçbir veri gitmez.
- **Hassas pano kopyalama** — kopyalanan kart bilgisi `EXTRA_IS_SENSITIVE` ile işaretlenir (pano geçmişinde görünmez) ve 30 saniye sonra otomatik silinir.

## Teknik yığın

- Kotlin + Jetpack Compose (Material 3)
- Room (yerel veritabanı, hassas alanlar şifreli byte array olarak saklanır)
- Android Keystore + BiometricPrompt
- WorkManager (pano otomatik temizleme)
- minSdk 23 (Android 6.0+) — cihazların ~%99'unu kapsar

## Durum

İlk test build'i. Tek kullanıcılı, tek cihazlı kullanım için tasarlandı; senkronizasyon veya bulut yedeği yok (bilinçli tercih).

## CI/CD

GitHub Actions her push'ta debug APK'yı build edip `dist/` altında artifact olarak yükler. Tutarlı imza için `debug.keystore` repoya bilerek commit edilmiştir (aksi halde her CI çalışmasında farklı imza üretilir ve APK'lar birbiriyle uyumsuz olur).
