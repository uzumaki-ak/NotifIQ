# Intelligent Notification Manager

**Production-ready Android app for smart notification management**

## 🎯 Features

- ✅ **Smart Prioritization** - Automatically categorizes notifications (Critical, Important, Normal, Silent)
- ✅ **Behavior Learning** - Learns from user interactions to improve accuracy
- ✅ **Spam Detection** - Identifies and silences spammy apps
- ✅ **100% Private** - All processing happens locally, no data leaves device
- ✅ **Dark/Light Mode** - Beautiful Material Design 3 UI
- ✅ **Background Cleanup** - Automatic old notification cleanup

## 🏗️ Architecture

- **MVVM** - Clean architecture with ViewModel + Repository pattern
- **Jetpack Compose** - Modern declarative UI
- **Room Database** - Local SQLite database for all data
- **Hilt** - Dependency injection
- **Coroutines + Flow** - Asynchronous programming
- **WorkManager** - Background tasks

## 📱 Requirements

- **Min SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)
- **Kotlin**: 1.9.20
- **Gradle**: 8.2.0

## 🚀 Setup & Build

### 1. Clone the repository
```bash
git clone <your-repo-url>
cd IntelligentNotificationManager
```

### 2. Open in Android Studio
- Use Android Studio Hedgehog or newer
- Let Gradle sync automatically

### 3. Build the project
```bash
./gradlew assembleDebug
```

### 4. Install on device
```bash
./gradlew installDebug
```

### 5. Grant notification access
- Open app
- Follow onboarding instructions
- Grant notification listener permission in Android settings

## 📊 Database Schema

### NotificationEntity
Stores all received notifications with:
- Basic info (app, title, text)
- Scoring data (base, content, frequency, behavior, final scores)
- User interaction tracking (opened, dismissed, time to action)

### AppBehaviorEntity
Tracks learning data per app:
- Usage statistics (total received, opened, dismissed, ignored)
- Calculated rates (open rate, dismiss rate, ignore rate)
- Behavior adjustment score (-20 to +20)
- Frequency metrics

### KeywordEntity
User-defined custom keywords:
- Keyword text
- Type (CRITICAL, IMPORTANT, SPAM)
- Score modifier

## 🧠 Scoring Algorithm

**Formula**: `finalScore = (baseScore + contentScore + behaviorAdjustment) * frequencyMultiplier`

### 1. Base Score (0-100)
- Banking apps: 80
- Messaging: 60
- Email: 50
- Social: 35
- Entertainment: 15
- Games: 10

### 2. Content Score (-30 to +40)
- Critical keywords (+20 each): "otp", "failed", "urgent", etc.
- Important keywords (+10 each): "message", "delivery", etc.
- Spam keywords (-8 each): "sale", "new video", etc.
- Financial keywords (+15 each): "bank", "payment", etc.

### 3. Behavior Adjustment (-20 to +20)
- High open rate (>80%): +15
- Medium open rate (>60%): +10
- High dismiss rate (>60%): -12
- High ignore rate (>70%): -15

### 4. Frequency Multiplier
- 0-2 notifications/hour: 1.0 (no penalty)
- 3-5/hour: 0.9
- 6-10/hour: 0.7
- 11-20/hour: 0.5
- 20+/hour: 0.3 (severe spam)

### Final Categories
- **Critical**: Score 70-100 (Red)
- **Important**: Score 40-69 (Orange)
- **Normal**: Score 15-39 (Blue)
- **Silent**: Score 0-14 (Gray)

## 🔧 Customization

### Add more app categories
Edit `Constants.kt` → `AppCategories`

### Add more keywords
Edit `Constants.kt` → `Keywords`

### Adjust scoring weights
Edit `Constants.kt` → base weights and multipliers

### Modify cleanup schedule
Edit `MainActivity.kt` → `setupBackgroundWorkers()`

## 📦 Project Structure
```
app/
├── data/
│   ├── database/        # Room database, entities, DAOs
│   └── repository/      # Data access layer
├── domain/
│   ├── scoring/         # Importance scoring engine
│   ├── learning/        # Behavior learning logic
│   └── usecase/         # Business logic use cases
├── presentation/
│   ├── ui/
│   │   ├── screens/     # Compose screens
│   │   ├── components/  # Reusable UI components
│   │   └── theme/       # App theme
│   └── viewmodel/       # ViewModels
├── service/             # NotificationListenerService
├── worker/              # Background workers
├── di/                  # Dependency injection
└── utils/               # Utilities and constants
```

## 🐛 Troubleshooting

### Notifications not appearing?
1. Check if notification access is granted
2. Restart the app
3. Check if NotificationListenerService is running

### App using too much battery?
- Background workers run every 6 hours (behavior) and 24 hours (cleanup)
- Adjust frequency in MainActivity if needed

### Database getting too large?
- Cleanup runs daily
- Adjust retention periods in Constants.kt

## 📝 License

MIT License - See LICENSE file

## 👨‍💻 Author

Built with 🔥 by Ak

---

**Ready to build? Let's fucking go!** 🚀