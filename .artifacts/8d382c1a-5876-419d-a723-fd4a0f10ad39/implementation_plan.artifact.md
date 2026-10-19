# Foundational Setup Implementation Plan

Set up foundational libraries, Compose UI configuration, design system colors (`Color.kt`), typography with Plus Jakarta Sans font, Material 3 themes (`Theme.kt`), shapes (pill-shaped buttons, 16dp card radius), and Firebase Auth / Retrofit / Gson / Coil / Room dependencies for the COSUP Android app.

## User Review Required

> [!IMPORTANT]
> - Dependencies for Jetpack Compose, Firebase BOM, Retrofit, Gson, Coil, and Room will be added to `app/build.gradle.kts` and `libs.versions.toml`.
> - Google Font "Plus Jakarta Sans" font file(s) will be downloaded and placed in `app/src/main/res/font/`.

## Open Questions

- None.

## Proposed Changes

### Build Configuration & Dependencies
#### [MODIFY] [libs.versions.toml](file:///C:/Users/User/AndroidStudioProjects/COSUP/gradle/libs.versions.toml)
- Add versions and library aliases for Compose BOM, Material3, Navigation Compose, Firebase BOM & Auth, Retrofit, Gson, Coil, and Room.

#### [MODIFY] [build.gradle.kts](file:///C:/Users/User/AndroidStudioProjects/COSUP/app/build.gradle.kts)
- Enable buildFeatures { compose = true } and add dependencies for Compose, Firebase, Retrofit, Gson, Coil, and Room.

### Design System & Theme
#### [NEW] [Color.kt](file:///C:/Users/User/AndroidStudioProjects/COSUP/app/src/main/java/com/example/cosup/ui/theme/Color.kt)
- Define Compose Colors mapped from COSUP website CSS variables:
  - Primary (Green): `#3BA53E`
  - Primary Dark: `#2d8a30`
  - Background Light: `#f7faf8`
  - Background Dark: `#080f0b`
  - Card Light: `#ffffff`
  - Card Dark: `#101c15`
  - Text Light: `#0d1f15`
  - Text Dark: `#eaf3ed`
  - Muted Light: `#6b7c72`
  - Muted Dark: `#93a79b`
  - Accent (Hopeline Red): `#e05a3a`
  - Accent Yellow: `#f0c93a`

#### [NEW] [Plus Jakarta Sans Font Files](file:///C:/Users/User/AndroidStudioProjects/COSUP/app/src/main/res/font/)
- Include Google Font "Plus Jakarta Sans" TTF files in `app/src/main/res/font/`.

#### [NEW] [Type.kt](file:///C:/Users/User/AndroidStudioProjects/COSUP/app/src/main/java/com/example/cosup/ui/theme/Type.kt)
- Define Material 3 Typography using Plus Jakarta Sans font family.

#### [NEW] [Theme.kt](file:///C:/Users/User/AndroidStudioProjects/COSUP/app/src/main/java/com/example/cosup/ui/theme/Theme.kt)
- Define Light and Dark ColorSchemes, shapes (pill-shaped buttons `CircleShape` or `RoundedCornerShape(50)`, cards `RoundedCornerShape(16.dp)`), and `CosupTheme` composable.

## Verification Plan

### Automated Tests
- Run `./gradlew assembleDebug` to verify compilation and dependency resolution.
- Run unit tests if applicable.

### Manual Verification
- Verify build succeeds without errors.
