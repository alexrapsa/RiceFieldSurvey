# 🌾 Rice Field Survey — Offline Android Technical Assessment

Native Android farmer questionnaire based on **Appendix A of the Technical Assessment for
Information System Researcher II (TASAT-RCEF M&E), 14 October 2026**.

Designed as a small, readable **offline-first** application that helps enumerators collect farmer
survey answers when there is no mobile data or Wi-Fi.

> **Build status:** Complete Android source project with locally verified pure-Kotlin domain rules.
> The APK still needs to be compiled, tested on Android 13+, and committed. This authoring
> environment has neither the Android SDK nor Gradle, so **no APK is included yet**.

## Features

- Two distinct demo enumerator accounts, with persistent local sign-in session.
- Local-only record list scoped to each enumerator, with All / Drafts / Completed filters and search.
- Appendix A: all thirteen questions (B1Q1–B1Q6, B2Q1–B2Q7).
- Conditional Block 2 only if the farmer is currently engaged in rice farming.
- Conditional certified seed variety (B2Q5) and Other challenge details (B2Q7).
- Offline province picker with built-in searchable Philippine province list.
- Drafts are stored using Room/SQLite, with a 550 ms debounced autosave and an explicit Save draft.
- Resume drafts after restarting the app; validate required answers on completion.
- Completed interviews are preserved and open read-only.
- No `INTERNET` Android permission and no server dependency.

## Test accounts

| Username | Password |
|---|---|
| `enumerator1` | `Field@123` |
| `enumerator2` | `Survey@123` |

**Assessment/demo use only.** These credentials are embedded in source code. Local sign-in is
not secure authentication and the unencrypted SQLite database contains personal data.

## Tech stack

| Technology | Purpose |
|---|---|
| Kotlin | Native Android implementation |
| Jetpack Compose + Material 3 | Accessible declarative field forms |
| ViewModel + StateFlow | Screen state and lifecycle survival |
| Kotlin Coroutines / Flow | Asynchronous writes and reactive record list |
| Room / SQLite | Durable offline persistence |
| Clean Architecture + MVVM | Domain, data, presentation separation |
| Manual constructor injection | Lightweight DI without additional framework |
| JUnit | Domain validation tests |
| Gradle/AGP | Android build configuration |

**Android requirements:** Android 8.0/API 26 minimum, Android 13/API 33 compatible;
compile SDK 35, target SDK 35. Built with JDK 17 and Gradle 8.11.1.

## Clean Architecture diagram

```text
presentation/ (Jetpack Compose + ViewModels)
       │ calls use cases
       ▼
domain/usecase/ (Login, Observe, Get, SaveDraft, Submit)
       │ depends on interfaces
       ▼
domain/repository/ + domain/model/ (contracts, rules, validation)
       ▲ implemented by
       │
data/repository/ (RoomInterviewRepository, LocalAuthRepository)
       │ uses
data/local/ (Room DAO, entity, database) + SharedPreferences

MainActivity -> SurveyApplication -> SurveyContainer (composition root)
```

The domain model and questionnaire validation know **nothing about Android or the database**.
Room mapping is kept in `data/local/`, while composables call ViewModels (not DAOs).

## Project layout

```text
app/src/main/java/com/example/ricefieldsurvey/
  MainActivity.kt                   # simple navigation / composition root wiring
  SurveyApplication.kt
  di/SurveyContainer.kt
  domain/model/                     # Interview, SurveyOptions, SurveyValidator
  domain/repository/                # repository interfaces
  domain/usecase/                   # business operations
  data/local/                       # Room Entity, DAO, database, mapper
  data/repository/                  # local repository implementations
  presentation/auth/                # login screen and ViewModel
  presentation/interviews/          # record list and ViewModel
  presentation/editor/              # form screen and ViewModel
  presentation/components/          # reusable question UI
  presentation/theme/
app/src/test/                        # JUnit tests for validation and branching
.github/workflows/android-ci.yml     # automated debug APK and unit tests
```

## Installation using Android Studio (recommended)

1. Install **Android Studio**, Android SDK Platform **35** and Android SDK Build-Tools **35.0.0**.
   Configure **Gradle JDK 17** and an Android 13+ emulator or physical device.
2. Open the extracted **FieldSurveyAndroid** folder as a project.
3. Allow Gradle to sync (Google/Maven repositories require internet **during build only**).
   This archive includes `gradle-wrapper.properties` but cannot bundle the Gradle wrapper
   JAR. If Android Studio requests it, run `gradle wrapper --gradle-version 8.11.1`
   once using a locally installed Gradle; alternatively use the provided GitHub Actions build.
4. Select `app` and choose **Run** to install the app on an Android 13+ device/emulator.
5. Open **Build → Build Bundle(s) / APK(s) → Build APK(s)** to generate a debug-signed APK.
6. The generated APK is usually `app/build/outputs/apk/debug/app-debug.apk`.
   Copy it to `app/debug-apk/rice-field-survey-debug.apk` and commit the copied APK.

On a machine with Gradle 8.11.1 installed:

```bash
gradle :app:testDebugUnitTest :app:assembleDebug
```

On Windows (PowerShell), after building:

```powershell
New-Item -ItemType Directory -Force app/debug-apk
Copy-Item app/build/outputs/apk/debug/app-debug.apk app/debug-apk/rice-field-survey-debug.apk
```

Install the built APK directly onto an Android phone by copying
`app/debug-apk/rice-field-survey-debug.apk` to the device, opening it in the phone's
file manager, and allowing installation from that source when prompted. Alternatively,
connect a phone with USB debugging enabled and run:

```bash
adb install -r app/debug-apk/rice-field-survey-debug.apk
```

For assessment submission, a debug-signed APK is explicitly accepted.
The app itself never needs network connectivity once installed.

### Build without local Android tooling (GitHub Actions)

The included `.github/workflows/android-ci.yml` installs Java, Android SDK, and Gradle,
executes unit tests, builds the debug APK and uploads it as a workflow artifact.

1. Push the project to a **public** GitHub repository.
2. Open **Actions → Build and test Android application → Run workflow**.
3. Download `rice-field-survey-debug-apk` from the successful workflow run.
4. Rename `app-debug.apk` to `rice-field-survey-debug.apk` and place it in `app/debug-apk/`.
5. Commit and push the APK into the same repository (the artifact by itself does not satisfy
   the assessment's **APK included in repo** requirement).

## Data collection workflow

```text
Login
  └─ Saved interviews (Drafts / Completed)
       ├─ + New interview -> Farmer information (B1Q1–B1Q6)
       │    ├─ B1Q6 = No -> skip block 2 -> validate -> Complete
       │    └─ B1Q6 = Yes -> Rice farming information (B2Q1–B2Q7)
       │          ├─ B2Q4 = Yes -> require seed variety (B2Q5)
       │          └─ B2Q6 includes Others -> require detail (B2Q7)
       │                └─ validate -> Complete
       └─ Tap a saved interview
            ├─ Draft -> resume and edit
            └─ Completed -> read-only view
```

Changing a conditional answer clears now-hidden responses before saving. **Save draft**
permits incomplete answers. **Complete interview** checks all required fields and highlights
missing or invalid responses; the status changes only after Room successfully writes it.

## Tests

Run unit tests on a build-capable machine:

```bash
gradle :app:testDebugUnitTest
```

The 6 JUnit tests cover valid interviews, conditional hiding, conditional seed variety,
Others detail, invalid numeric/required values and no initial rice farming selection.

A standalone test of the domain rules (no Android SDK needed) can be run on a machine with
`kotlinc` and Java:

```bash
bash tools/check-domain.sh
```

See `docs/ASSESSMENT_CHECKLIST.md` for manual offline and restart acceptance testing.

## Handling personal data in a production version

Assessment scope explicitly allows omission of security, but real farmer information is sensitive.
For production I would add:

1. **Authentication:** server-verified enumerator identities, short sessions, role-based access,
   and per-user authorization on all records. Never embed actual passwords.
2. **At-rest encryption:** encrypt local databases (e.g. SQLCipher), use Android Keystore-managed
   keys, prevent backups/export of plaintext, and encrypt any sync payloads in transit.
3. **Secure syncing:** authenticated HTTPS API, device-side outbox, idempotency keys,
   per-record server acknowledgments, replay/retry and conflict resolution.
4. **Consent and governance:** informed consent collection, purpose limitation, retention and
   deletion policies, minimization, data access logs, breach procedures and review against the
   Philippine Data Privacy Act and relevant DA/PhilRice policy.
5. **Device controls:** lock-screen requirement, inactivity timeout, no sensitive debug logs,
   and remote revocation/wipe for lost devices.

No data leaves the device in this assessment version. Android automatic backup is disabled.

## Known limitations / next improvements

- **APK pending build and on-device verification.** This environment cannot execute Android
  Gradle builds; run the GitHub Actions workflow or Android Studio before submitting.
- Hardcoded demo credentials and SharedPreferences session are **not** secure.
- Room database stores farmer responses in plaintext. Disable backups is only a mitigation.
- No server synchronization, export, GPS/geotagging, photos, interview signature or consent flow.
- Province options are static/offline; maintain them from an official updated geography catalog.
- No database migrations beyond v1; add schema export and tested migrations for production.
- Autosave is delayed by 550ms; power loss during that very short window can lose the last edit.
- Completed records are intentionally read-only; formal correction workflow is a future feature.
- No instrumented Android UI tests yet; Android 13 device smoke test is still required.
- No paging of large local lists; add Paging 3 for large data volumes.
- Data entry text remains single-line as specified in Appendix A; large-font accessibility,
  localization and TalkBack/contrast testing should be expanded.

## Assessment-specific deliverables and publication

For submission you must create your **own public GitHub/GitLab repository with the real commit
history**, include the **built .apk in the repository**, and email the repository URL as instructed
in the PDF. This source archive contains an initial local Git history but has not been published.

```bash
# In the source folder; inspect git log before pushing:
git log --oneline
# Create a new public repository on GitHub, then:
git remote add origin https://github.com/YOUR_USERNAME/rice-field-survey.git
git branch -M main
git push -u origin main
```

Only after copying the built APK:

```bash
git add app/debug-apk/rice-field-survey-debug.apk
git commit -m "Include installable debug assessment APK"
git push
```

## AI tool disclosure

**AI-assisted development:** OpenAI ChatGPT (GPT-6) was used to interpret the assessment PDF,
plan Clean Architecture, draft Kotlin/Compose/Room source files, design UI validation and
branching, create tests and author this README. The pure-Kotlin domain rules were smoke-tested
with `kotlinc`. ChatGPT did **not** compile or run the Android APK and did **not** test on an
Android device. The human submitter should review, build, test, and take responsibility for the
application and this disclosure before submission.
