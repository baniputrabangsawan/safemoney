# Repository Guidelines

## Project Overview

Jaga Uang is an offline Android income/expense tracker for Indonesian users aged 40+. Preserve Indonesian copy, whole-rupiah amounts, and separate income/expense totals. **No balance feature:** never add opening/running balances or income-minus-expense totals. Books scope transactions; payment methods are separate. Transfers stay within one book and are excluded from totals/category charts. No online accounts or sync.

## Architecture & Data Flow

- One Gradle module, `:app`; package `com.jagauang.app`. `JagaUangApplication` owns the manual application-scoped `AppContainer`; no Hilt/Koin or use-case layer.
- Reads: Room DAO flows → repository entity/domain mappers → eager `AppContainer` StateFlows → `collectAsStateWithLifecycle`. The active book selects the transaction query; shared period/category summaries run on `Dispatchers.Default`.
- Writes: form/detail ViewModels or management-screen coroutines → repository validation → suspending DAO operations. Repository interfaces, implementations, and `WriteResult` live in `data/repository/`.
- Four tabs share **one `main` NavHost destination**. `when(selectedTab)` swaps content; `rememberSaveable` and keyed `SaveableStateHolder` retain tab/scroll state. Preserve this to avoid overlapping tab frames. Form/detail/management destinations use `launchSingleTop`; all transitions are `None`. Back from a non-home main tab selects Beranda.

## Key Directories

Kotlin root: `app/src/main/java/com/jagauang/app/`.

- `domain/`: pure models, amount parsing, validation, periods, transfer-excluding summaries.
- `data/local/`: Room entities, DAOs, database, explicit migrations; `data/repository/`: mapping and write outcomes.
- `ui/{home,history,report,more,transaction}/`: feature screens; `ui/components/` and `ui/theme/`: shared controls and visual tokens.
- Root-relative: `app/schemas/` holds Room schema exports; `app/src/test/` holds JVM tests; `app/src/main/res/` holds DM Sans, launcher assets, and Android theme resources. `app/build/`, `.gradle/`, and `.kotlin/` are generated.

## Development Commands

Run from the project root:

```bash
export ANDROID_HOME=/home/bani-mint/Android/Sdk
export PATH="$ANDROID_HOME/platform-tools:$PATH"
./gradlew assembleDebug
./gradlew testDebugUnitTest
./gradlew lintDebug
adb devices -l
./gradlew installDebug
adb shell am start -W -n com.jagauang.app/.MainActivity
```

Installation requires an authorized device. APK: `app/build/outputs/apk/debug/app-debug.apk`; lint report: `app/build/reports/lint-results-debug.html`.

## Code Conventions & Common Patterns

- Four-space Kotlin indentation; PascalCase types/composables/files, camelCase properties/functions, uppercase persisted enum codes. Map domain camelCase to storage snake_case.
- Money: whole-rupiah `Long`, Rp1–Rp999,999,999,999, parsed with `AmountParser`; never floating-point currency. Dates: `LocalDate`/epoch days, 1900-01-01 through today; time: minutes since midnight; timestamps: epoch milliseconds. Weeks start Monday; ranges are inclusive. Material date-picker milliseconds represent UTC dates.
- Reuse `TransactionValidator`: distinct non-null transfer methods, trimmed notes capped at 200 characters, names capped at 40. Keep domain logic Android-free.
- Form/detail writes use `viewModelScope`, `mutableStateOf`, saving/dirty flags, and Indonesian failure feedback. Handle `WriteResult.Success`, `NotFound`, and `Invalid` explicitly; database exceptions can propagate. Creation currently generates a new UUID inside each `save()`, so retries after uncertain failures can duplicate records. Upsert alone does not make this retry-safe; a fix must retain the draft UUID.
- Reuse shared StateFlows, stable lazy-list keys, and saveable UI state. Keep aggregation off-main and receipt copying on `Dispatchers.IO`.
- UI work: search Mobbin first; follow `DESIGN.md`. Use pure-white backgrounds/system bars, navy identity, muted accents, white outlined Home cards, and neutral Material surfaces. Preserve DM Sans, readable labels, ≥48dp touch targets, and large-font layouts.

## Important Files

- Kotlin-root `AppNavHost.kt`, `AppContainer.kt`, `MainActivity.kt`, `JagaUangApplication.kt`: navigation, shared state, entrypoints.
- `data/local/JagaUangDatabase.kt` and `data/local/Migrations.kt`: `jaga_uang.db`, schema v2, migration 1→2. Preserve legacy IDs, category codes, amounts, dates, and assignment to `book-rumah`; never use destructive migration.
- Before product changes, consult `PRD.md`; before persistence/navigation changes, `ARCHITECURE.md` (**actual spelling**); before UI changes, `DESIGN.md`. Verify documentation claims against source.
- Build configuration: `app/build.gradle.kts`, `build.gradle.kts`, `settings.gradle.kts`, `gradle.properties`, `gradle/wrapper/gradle-wrapper.properties`. Root logo PNGs are source assets.

## Runtime/Tooling Preferences

Use JDK 17 and the checked-in Gradle 8.9 wrapper. Configured stack: AGP 8.7.3, Kotlin/Compose compiler 2.0.21, KSP, Room 2.6.1; compile/target SDK 35, min SDK 26. Dependencies are direct Gradle Kotlin DSL declarations, not a version catalog. Room exports schemas through KSP. Keep AndroidX and the existing 2GiB JVM heap in root `gradle.properties`, not wrapper properties. No Node/Bun workflow, custom scripts, CI, or formatter configuration is present.

## Testing & QA

- JUnit 4.13.2 JVM tests cover amount parsing, periods, and summaries. Focused example: `./gradlew testDebugUnitTest --tests 'com.jagauang.app.domain.AmountParserTest'`. No coverage threshold or instrumented/Compose/migration harness is configured; schema exports are not migration tests.
- For behavioral changes, run focused regression tests plus debug build/lint, then exercise the affected UI. Check rapid/repeated tabs, detail → Back, scroll/subtab restoration, and period/book changes; successful launch alone is insufficient.
- Use synthetic data on disposable test installations. Preserve user data: no uninstall, storage clearing, destructive migrations, or logging private transactions/receipts. Manifest disables backup and declares no permissions.
- Regression scenario: income Rp100,000 + expense Rp30,000 + transfer Rp20,000 → separate totals Rp100,000/Rp30,000. Empty books show zeros; records persist offline.
- Known limitations: fresh v2 installs have no seeded books/methods/categories, although `activeBookId` starts at `book-rumah`; seeding occurs only in migration 1→2. History rows ignore the period despite filtered totals; Report date-jump is a no-op. Recurrence stores/lists metadata without notification scheduling. Accessibility requirements are not certified compliance.
