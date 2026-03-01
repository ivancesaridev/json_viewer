# JsonReader

JsonReader is a Kotlin Multiplatform application built with Compose Multiplatform for reading and editing JSON files. It provides a simple and intuitive interface to explore and modify JSON data on both Android and iOS platforms.

## Features

- **File Picking**: Select JSON files from your device's storage.
- **JSON Details**: View file metadata such as path, name, and size.
- **JSON Editing**: Modify the content of your JSON files directly within the app.
- **Recent Files**: Keep track of recently opened files for quick access, persisted via Jetpack DataStore.
- **Deep Linking**: Open JSON files directly from other apps or file managers.

## Tech Stack

- **Kotlin**: The primary programming language.
- **Compose Multiplatform**: For building the shared UI across Android and iOS.
- **KotlinX Serialization**: For parsing and serializing JSON data.
- **Jetpack DataStore**: For persistent storage of recent files.
- **Jetpack Navigation**: For handling app navigation and routing.

## Project Structure

- `composeApp/`: Contains the shared UI and logic.
    - `commonMain/`: Shared code for all platforms.
    - `androidMain/`: Android-specific implementations.
    - `iosMain/`: iOS-specific implementations.

## Getting Started

### Prerequisites

- Android Studio or IntelliJ IDEA with the KMP plugin installed.
- Xcode (for iOS development).
- Kotlin 2.1.10+
- Gradle 8.x

### Running the App

#### Android
To run the Android application, use the following Gradle command:
```bash
./gradlew :composeApp:installDebug
```

#### iOS
To run the iOS application, you can use Xcode to open the project or use the following Gradle command (if configured):
```bash
./gradlew :composeApp:iosDeploy
```

## License

This project is licensed under the MIT License - see the LICENSE file for details.
