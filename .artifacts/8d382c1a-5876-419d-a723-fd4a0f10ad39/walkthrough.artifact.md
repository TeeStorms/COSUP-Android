# Walkthrough - CI Build Fixes

Fixed compile-time resource resolution errors (`Unresolved reference 'R'` and missing drawable resources in CI) and package mismatches.

## Changes

### Tests & Package Alignment
- Updated `ExampleInstrumentedTest.kt` and `ExampleUnitTest.kt` package from `com.example.cosup` to `com.cosup.app` to match application namespace and applicationId (`com.cosup.app`).

### Resources & Drawables
- Copied all core drawable assets (`about.jpg`, `banner.jpg`, `city_of_tshwane.jpg`, `hopeline.png`, `logo.png`, `university_of_pretoria.png`) from `res/drawable-nodpi/` to `res/drawable/` so AAPT2 correctly generates `R.drawable.*` references for Compose screens (`HomeScreen.kt`, `SimpleScreens.kt`).

## Verification Results

### Automated Tests
- Ran `./gradlew clean assembleDebug testDebugUnitTest`:
  - **Status:** `BUILD SUCCESSFUL`
  - **Unit Tests:** `1 passed, 0 skipped, 0 failed`
