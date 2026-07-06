<div align="center">

#  Notisave

**Never lose a notification again.**

A sleek, privacy-first Android app that silently captures and organizes every notification — so you can read them on your own time.

[![Build APK](https://github.com/Nanusharma/Notisave/actions/workflows/build.yml/badge.svg)](https://github.com/Nanusharma/Notisave/actions/workflows/build.yml)
![Android](https://img.shields.io/badge/Android-8.0%2B-3DDC84?style=flat&logo=android&logoColor=white)
![Kotlin](https://img.shields.io/badge/Kotlin-2.0-7F52FF?style=flat&logo=kotlin&logoColor=white)
![Jetpack Compose](https://img.shields.io/badge/Jetpack%20Compose-Material%203-4285F4?style=flat&logo=jetpackcompose&logoColor=white)
![License](https://img.shields.io/badge/License-Open%20Source-blue?style=flat)

</div>

---

## ✨ Features

| Feature | Description |
|---|---|
| 📥 **Auto-Capture** | Silently records every incoming notification using Android's `NotificationListenerService` |
| 🔍 **Search & Filter** | Instantly find past notifications by keyword or filter by app |
| 📋 **Detail View** | Tap any notification to see its full content in a clean bottom sheet |
| 🎨 **Theming** | System / Light / Dark theme modes with Material You dynamic colors |
| 🗓️ **Data Retention** | Configurable auto-cleanup — keep for 7, 14, 30, 90 days, or forever |
| 🔒 **100% Local** | All data stays on-device in a local Room database. No servers, no cloud, no tracking |
| 🩺 **Self-Healing** | Background health worker ensures the listener service stays alive |
| 🚀 **Boot Persistence** | Automatically restarts the listener after device reboot |

---

## 🏗️ Architecture

```
com.notisave.app
├── data/                  # Room database, DAO, entities, repository
├── service/               # NotificationListenerService & BootReceiver
├── ui/
│   ├── navigation/        # Compose NavGraph
│   ├── screens/           # NotificationList, Detail, Settings, Onboarding
│   └── theme/             # Material 3 theming (colors, typography)
├── viewmodel/             # ViewModels for state management
├── worker/                # WorkManager health check
├── MainActivity.kt        # Single Activity entry point
└── NotisaveApp.kt         # Application class + WorkManager init
```

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| **Language** | Kotlin |
| **UI** | Jetpack Compose + Material 3 |
| **Navigation** | Compose Navigation |
| **Database** | Room (KSP) |
| **Async** | Kotlin Coroutines + Flow |
| **Background** | WorkManager |
| **Preferences** | DataStore |
| **Image Loading** | Coil |
| **Min SDK** | 26 (Android 8.0 Oreo) |
| **Target SDK** | 35 |
| **Build** | Gradle 8.7 + Kotlin DSL |

---

## 🚀 Getting Started

### Prerequisites

- **Android Studio** Ladybug or newer
- **JDK 17**
- An Android device or emulator running **API 26+**

### Build & Run

```bash
# Clone the repo
git clone https://github.com/Nanusharma/Notisave.git
cd Notisave

# Build debug APK
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug
```

Or simply open the project in Android Studio and hit ▶️ **Run**.

### CI / CD

The project includes a GitHub Actions workflow that automatically builds debug & release APKs on every push to `main`. Download the latest artifacts from the [Actions tab](https://github.com/Nanusharma/Notisave/actions).

> **Note:** Pushes that only modify `.md` files will **not** trigger a build.

---

## 📱 Permissions

| Permission | Why it's needed |
|---|---|
| `BIND_NOTIFICATION_LISTENER_SERVICE` | Core functionality — captures notifications |
| `RECEIVE_BOOT_COMPLETED` | Restarts the listener after reboot |
| `REQUEST_IGNORE_BATTERY_OPTIMIZATIONS` | Keeps the service alive in the background |

---

## 🤝 Contributing

Contributions are welcome! Feel free to open an issue or submit a pull request.

1. Fork the repository
2. Create a feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

---

## 📄 License

This project is open-source and available for personal and educational use.

---

<div align="center">

**Made with ❤️ using Kotlin & Jetpack Compose**

</div>
