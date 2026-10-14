The installable debug APK goes in this folder as:

    app/debug-apk/rice-field-survey-debug.apk

It must be built using Android Studio or the GitHub Actions workflow before submission.
No APK is included in the initial source archive because the authoring environment
has no Android SDK / Gradle runtime and cannot compile Android applications.

Once you have an APK, add it to Git (the .gitignore intentionally permits this path).
