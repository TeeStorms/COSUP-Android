COSUP Android App
Kotlin + Jetpack Compose app for the COSUP NGO (Community Oriented Substance Use Programme).
It looks and behaves like the COSUP website and shares the same Azure database, through the website's `/api` endpoints.
Setup (each developer)
Clone the repo and open it in Android Studio (JDK 17).
Create `local.properties` (it is git-ignored) and add the website address:
`API_BASE_URL=https://<your-cosup-site>.azurewebsites.net/`
(emulator + local website: `https://10.0.2.2:<port>/`)
Firebase: in the Firebase console (project `cosup-semester2`) add an Android app with package `com.cosup.app`
and your debug SHA-1, download `google-services.json` and put it in `app/` (git-ignored, never commit it).
Run the `app` configuration.
Team workflow
`main` holds the CI workflow (`.github/workflows/android-ci.yml`). Create your branch from main:
`git checkout main && git pull && git checkout -b feature/<name>-<what>`
Commit after every change, e.g. `feat: add login screen`, `fix: crash on empty news list`.
Every push on any branch runs lint, unit tests and builds a debug APK (download it from the run's Artifacts).
Merge into `main` with a Pull Request.
CI secret (optional but recommended)
Repo -> Settings -> Secrets -> Actions -> `GOOGLE_SERVICES_JSON` = base64 of `google-services.json`
(`base64 -w0 app/google-services.json`). Without it CI uses a placeholder so the build still passes.
Never commit
Database passwords, connection strings, `local.properties`, `google-services.json`, keystores.
The app never talks to the database directly.
Folders
`app/` Android project
`website-api-additions/` C# files to add to the CosupWeb project (API + token handler)
`website-assets-fixed-icons/` service icons with the checkerboard background removed