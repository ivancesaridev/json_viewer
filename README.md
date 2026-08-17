# JSON Viewer

A Kotlin Multiplatform app for reading and editing JSON files on Android and iOS, with a shared
Compose Multiplatform UI.

## Features

- **Open files** from device storage through the system document picker, or straight from another
  app by tapping a `.json` file.
- **Browse as a tree** with syntax highlighting, collapsible branches, and lazy rendering that keeps
  large documents responsive.
- **Search** keys and values, jumping between matches with the branches expanded automatically.
- **Edit** with line numbers, automatic indentation, bracket and quote pairing, and a quick-key row
  for the characters a phone keyboard buries.
- **Validation before saving**, so a syntax error is reported instead of silently overwriting the
  file.
- **Recent files**, persisted with Jetpack DataStore and removable individually.
- **Share** the current file through the system share sheet.
- English and Italian, light and dark themes.

## Requirements

- JDK 17 (the build declares a toolchain, so Gradle will provision one if needed)
- Android SDK with API 36
- Xcode 16 or newer, for the iOS app

## Building

### Android

```bash
./gradlew :composeApp:installDebug          # debug build on a connected device
./gradlew :composeApp:testDebugUnitTest     # shared unit tests
./gradlew :composeApp:bundleRelease         # release App Bundle
```

Release builds are minified and shrunk with R8. Signing is read from `keystore.properties` in the
repository root, falling back to the `ANDROID_KEYSTORE_FILE`, `ANDROID_KEYSTORE_PASSWORD`,
`ANDROID_KEY_ALIAS` and `ANDROID_KEY_PASSWORD` environment variables. Copy
`keystore.properties.example` to get started; neither the properties file nor any keystore is
committed. Run `./gradlew :composeApp:printReleaseSigningStatus` to check what the build picked up.

### iOS

```bash
open iosApp/iosApp.xcodeproj
```

The app target builds the shared framework through a `Compile Kotlin Framework` build phase, so no
separate Gradle step is needed. Set your team identifier in `iosApp/Configuration/Config.xcconfig`
or in Xcode's Signing & Capabilities pane — it is deliberately left empty in version control.

To run the shared tests against the iOS target:

```bash
./gradlew :composeApp:iosSimulatorArm64Test
```

## Project structure

```
composeApp/
  src/commonMain/     shared UI, view models, JSON tree model, navigation
  src/commonTest/     shared unit tests
  src/androidMain/    Android file access, manifest, resources
  src/iosMain/        iOS file access
iosApp/               Xcode project for the iOS app
```

The platform boundary is narrow: `commonMain/util` declares `expect` functions for picking,
reading, writing and sharing files, and each platform provides the `actual`.

### Platform differences worth knowing

On Android, files are edited in place through the URI the document picker returns. iOS does not
grant apps lasting access to files outside their own container, so the picker imports a copy into
the app's Documents folder and edits apply there; the copy is visible in the Files app and can be
sent anywhere else from the share sheet.

## Privacy

JSON Viewer collects nothing and makes no network requests. See [PRIVACY.md](PRIVACY.md).

## License

MIT — see [LICENSE](LICENSE).
