# Taskora

**Taskora** is an offline-first productivity application designed to help users organize tasks, build consistent habits, and improve their daily productivity through a gamified experience.

## ✨ Features

### 📋 Task Management
- Create, edit, and organize daily tasks.
- Set task descriptions, deadlines, and difficulty levels.
- Track task completion and productivity.
- Organize tasks based on their status and priority.

### 📅 Calendar
- Monthly calendar view.
- View tasks based on their scheduled dates.
- Easily track upcoming activities and deadlines.

### 🎯 Focus Mode
- Dedicated focus timer.
- Multiple focus sessions.
- Pause, resume, and track focus activities.
- Associate focus sessions with specific tasks.

### 🔄 Weekly Routines
- Create reusable weekly task templates.
- Configure recurring days and task difficulty.
- Generate tasks for upcoming weeks.

### 🏆 Gamification
- Earn experience points (XP) by completing tasks.
- Level progression system.
- Maintain productivity streaks.
- Unlock achievements.
- Complete hidden quests and challenges.

### 📊 Productivity Analytics
- Track productivity trends.
- Review completed tasks and earned XP.
- Analyze task distribution and consistency.
- Monitor focus activity.

### 🎨 Personalization
- Light, dark, and system themes.
- Custom accent colors.
- Personalized profile and profile picture.
- Customizable dashboard sections.

### 🔔 Notifications
- Local task reminders.
- Configurable reminder intervals.
- Quiet hours settings.

### 💾 Local Data & Backup
- Offline data storage.
- Local database persistence.
- Backup and restore functionality.
- Password-protected encrypted backups.

### 🌐 Language Support
- English.
- Indonesia.

## 🛠️ Technology Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose
- **Design System:** Material 3
- **Database:** Room / SQLite
- **Architecture:** MVVM
- **Asynchronous Programming:** Kotlin Coroutines and Flow
- **Build System:** Gradle
- **IDE:** Android Studio

## 🔒 Privacy & Offline-First

Taskora is designed to work offline.

- No account registration required.
- No backend server required.
- No cloud synchronization required.
- Application data is stored locally on the device.
- Backup files are managed by the user.

Your productivity data stays on your device.

## 📱 Requirements

- Android 8.0 (API 26) or later.
- Android device with sufficient storage.
- No internet connection required for core application functionality.

## 🚀 Getting Started

### Prerequisites

- Android Studio.
- JDK 17.
- Android SDK.
- Git.

### Installation

1. Clone the repository:

   ```bash
   git clone https://github.com/sayyidazriel-art/taskora.git
   ```

2. Open the project in Android Studio.

3. Wait for Gradle Sync to finish.

4. Connect an Android device with USB debugging enabled.

5. Select your device and click **Run**.

Alternatively, build the application using:

```bash
./gradlew assembleDebug
```

On Windows:

```powershell
.\gradlew.bat assembleDebug
```

## 📂 Project Structure

```text
Taskora/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/
│   │       │   └── com/taskora/app/
│   │       ├── res/
│   │       └── AndroidManifest.xml
│   └── build.gradle.kts
├── gradle/
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew
├── gradlew.bat
├── README.md
└── .gitignore
```

## 🗺️ Project Goals

Taskora aims to provide a comfortable and practical productivity experience by combining task management, habit building, focus tracking, and gamification in one application.

The primary goals are:

- Keep productivity simple and accessible.
- Encourage consistent daily habits.
- Help users stay focused.
- Provide meaningful productivity insights.
- Prioritize user privacy and offline functionality.

## 📄 License

No open-source license has been assigned to this project yet. All rights are reserved by the project owner unless otherwise stated.