# Migration from Jetpack Compose to XML + Fragments Plan

Migrate the COSUP Android app UI from Jetpack Compose to traditional Android XML Views and Fragments while preserving the data layer, Firebase, API, ViewModel, tests, and CI workflow.

## User Review Required

> [!IMPORTANT]
> - All Jetpack Compose UI files will be replaced by XML layout files, Fragments, Adapters (`SimpleListAdapter`), ViewBinding helpers (`Binders.kt`, `Ext.kt`), Navigation component (`nav_graph.xml`), and Menus.
> - Commits will be made incrementally per screen/feature to keep the CI pipeline green.

## Open Questions

- None.

## Proposed Changes

### Step 1: Branch & Dependencies
- Create/switch to `feature/xml-ui` (or `Ndabezinhle_Zothando_Branch`).
- Update `app/build.gradle.kts` to enable `viewBinding = true`, add Navigation, Material Components, and RecyclerView/ConstraintLayout dependencies.

### Step 2: Theme & Resources
- Add `values/colors.xml`, `themes.xml`, `styles.xml`, `strings.xml`, and `values-night/colors.xml`.
- Add vector icons (`ic_home`, `ic_services`, `ic_place`, `ic_news`, `ic_mail`, `ic_call`, `ic_person`, `ic_theme`) and shape drawables.
- Add `menu/bottom_nav.xml`, `menu/top_menu.xml`, and `navigation/nav_graph.xml`.

### Step 3: Shared Components & Helpers
- Add shared layouts (`view_badge`, `view_stat`, `view_hopeline`, `view_partners`, `item_service`, `item_news`, `item_news_highlight`, `item_site`, `item_referral_row`).
- Add helpers: `ui/common/Ext.kt`, `Binders.kt`, `SimpleListAdapter.kt`, and `SiteRow.kt`.

### Step 4: Screens & Fragments (One commit per screen)
- **Home:** `fragment_home.xml` + `HomeFragment.kt`
- **Services:** `services.xml` + `ServicesFragment.kt`
- **Sites:** `sites.xml` + `SitesFragment.kt`
- **News:** `news.xml` + `NewsFragment.kt`
- **Contact:** `contact.xml` + `ContactFragment.kt`
- **About:** `about.xml` + `AboutFragment.kt`
- **Auth:** `auth.xml` + `dialog_forgot.xml` + `dialog_guest.xml` + `AuthFragment.kt`
- **Profile:** `profile.xml` + `ProfileFragment.kt`
- **Admin:** `admin.xml` + admin row layouts (`item_admin_header`, `line`, `referral`, `news_form`, `news`, `site`, `activity`) + `AdminFragment.kt`

### Step 5: Shell & Navigation
- Add `activity_main.xml` and update `MainActivity.kt` and `CosupApp.kt`.

### Step 6: Cleanup Compose
- Delete all Jetpack Compose files (`ui/CosupRoot.kt`, `ui/theme/Theme.kt`, `ui/components/Common.kt`, `Cards.kt`, and `ui/screens/*`).
- Remove Compose compiler and dependencies from Gradle build files.

### Step 7: Documentation
- Add `MIGRATION_TO_XML.md` and update `README.md`.

## Verification Plan

### Automated Tests
- Run `./gradlew clean assembleDebug testDebugUnitTest` after each commit step to ensure compilation and tests pass.
