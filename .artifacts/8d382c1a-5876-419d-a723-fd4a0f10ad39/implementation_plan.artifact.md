# Fix CI Build and Resource Resolution Plan

Fix compile-time resource resolution issues (`Unresolved reference 'R'` and missing drawables like `banner`, `about`, `city_of_tshwane`, `university_of_pretoria`) and package mismatches in test files that caused the GitHub Actions CI workflow to fail.

## User Review Required

> [!IMPORTANT]
> - Test files (`ExampleInstrumentedTest.kt` and `ExampleUnitTest.kt`) will be updated from package `com.example.cosup` to `com.cosup.app`.
> - Drawable assets in `res/drawable-nodpi/` will also be copied/referenced correctly under `res/drawable/` to guarantee reliable AAPT resource generation for `R.drawable.*`.

## Open Questions

- None.

## Proposed Changes

### Tests & Package Alignment
#### [MODIFY] [ExampleInstrumentedTest.kt](file:///C:/Users/User/AndroidStudioProjects/COSUP/app/src/androidTest/java/com/example/cosup/ExampleInstrumentedTest.kt) -> Move/Update to `com/cosup/app/ExampleInstrumentedTest.kt`
- Update package to `com.cosup.app` and expected package name to `"com.cosup.app"`.

#### [MODIFY] [ExampleUnitTest.kt](file:///C:/Users/User/AndroidStudioProjects/COSUP/app/src/test/java/com/example/cosup/ExampleUnitTest.kt) -> Move/Update to `com/cosup/app/ExampleUnitTest.kt`
- Update package to `com.cosup.app`.

### Resources & Drawables
#### [MODIFY] [res/drawable/](file:///C:/Users/User/AndroidStudioProjects/COSUP/app/src/main/res/drawable/)
- Copy drawable images from `res/drawable-nodpi/` to `res/drawable/` to ensure AAPT compiles them into `R.drawable`.

## Verification Plan

### Automated Tests
- Run `./gradlew clean assembleDebug testDebugUnitTest` to verify successful compilation and test execution.

### Manual Verification
- Verify GitHub Actions CI build succeeds on push.
