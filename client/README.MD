# LibreTune Android Client

## Building and running

To build and run the LibreTune Android app, follow these steps:

1. Clone the repository:

   ```bash
   git clone
   ```

2. Navigate to the `client` directory:

   ```bash
   cd client
   ```

3. Configure the Android SDK path for Gradle:

   ```bash
   printf 'sdk.dir=%s\n' "$HOME/Android/Sdk" > local.properties
   ```

   Alternatively, export `ANDROID_HOME` or `ANDROID_SDK_ROOT` to a valid SDK path.

4. Build the app using Gradle:

   ```bash
   ./gradlew assembleDebug
   ```

5. Install the app on your Android device or emulator:

   ```bash
   adb install -r app/build/outputs/apk/debug/app-debug.apk
   ```
