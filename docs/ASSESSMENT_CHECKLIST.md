# Assessment compliance checklist

Reference: Technical Assessment — Information System Researcher II (TASAT-RCEF M&E),
14 October 2026, Appendix A.

## Core requirements

| PDF item | Code / interaction | Status |
|---|---|---|
| Two accounts | `LocalAuthRepository` and `LoginScreen` | Implemented in source |
| Questionnaire block 1, B1Q1–B1Q6 | `FarmerInfoBlock` | Implemented in source |
| Conditional block 2, B2Q1–B2Q7 | `FarmingInfoBlock` | Implemented in source |
| Conditional seed variety | `B2Q5` visible and required if `B2Q4 = Yes` | Implemented in source |
| Conditional Other response | `B2Q7` visible and required when `Others` selected | Implemented in source |
| Save offline / survive restart | Room SQLite; manual save and debounced autosave | Implemented in source |
| Record list and save/resume | `InterviewListScreen`; reopen drafts | Implemented in source |
| Android 13 compatibility | `minSdk = 26`, `targetSdk = 35` | Configured; device test pending |
| APK checked into public repository | Build with Android SDK and commit APK | **Pending** |
| Public repo URL | Push local Git history to GitHub or GitLab | **Pending** |

## Manual acceptance test

1. Install app on Android 13+ emulator/phone.
2. Disable Wi-Fi and mobile data for the entire test.
3. Log in with `enumerator1` / `Field@123`.
4. Start a new interview. Enter B1Q1–B1Q6. Select rice farming = Yes.
5. Tap Next. Enter 2.5 ha, an ecosystem, a method, certified seeds = Yes,
   a seed variety, select Others among challenges, enter a description.
6. Save as draft. Kill and reopen the app. Resume draft and verify all values.
7. Complete; ensure status is Completed, view record, and confirm read-only.
8. Create a second interview with rice farming = No. Check block 2 is skipped.
9. Create a third interview with certified seeds = No. Check B2Q5 is hidden.
10. On an interview with Others unselected, confirm B2Q7 is hidden and not saved.
11. Enter missing required answers and confirm completion is rejected with errors.
12. Log out. Log in with `enumerator2` / `Survey@123`. Confirm account 1's
    interview list does not appear. Create a record and verify persistence.
13. Force-stop/reopen and confirm records and local session persist.

## Build/packaging

The authoring environment used for this project lacks Android SDK and Gradle.
The source-level Kotlin validator smoke test passed, but full Android compilation
and instrumentation testing must occur in Android Studio or GitHub Actions.
The APK has NOT been generated yet; do not claim otherwise in submission.
