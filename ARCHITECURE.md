# Jaga Uang — Arsitektur Android

**Status** Draft MVP · **Pilihan** Kotlin, Jetpack Compose, Room · **minSdk** 26 · **DB version** 2

## Struktur aplikasi

Aplikasi Android native modul tunggal `:app`. Material 3, Navigation Compose, ViewModel, Coroutine, StateFlow, Room + KSP, Gradle Kotlin DSL, satu kontainer dependensi manual di `AppContainer` (scope Application).

Empat tab dirender dengan `when(selectedTab)` dalam satu tujuan utama `NavHost`, bukan empat tujuan yang menjalankan transisi konten. `rememberSaveable` menyimpan tab aktif; `SaveableStateHolder` memisahkan state gulir/subtab tiap tab. Form, detail, dan layar kelola tetap tujuan Navigation Compose dengan transisi `None`. Mengetuk tab aktif tidak mengubah back stack; Kembali dari tab non-Beranda menuju Beranda.

```text
app/src/main/java/<package>/
  app/              Activity, AppContainer, Application, navigasi (4 tab)
  data/local/       Room: entitas, DAO, database v2, migrasi 1→2
  data/repository/  Repository + pemetaan entity↔domain
  domain/           Model, validasi, periode, kalkulator ringkasan
  ui/home/ history/ report/ more/ transaction/
  ui/theme/ components/
```

| Bagian | Tanggung jawab |
| --- | --- |
| Compose | Menampilkan state, event, navigasi sesuai hasil operasi |
| ViewModel | State layar + validasi input, panggil repositori |
| Repositori | Validasi aturan tulis, terjemah DAO → model domain |
| Room | Sumber kebenaran; query reaktif, tulis atomik |

## Skema Room v2

Tabel: `books`, `payment_methods`, `categories`, `transactions`.

| Tabel | Kolom penting |
| --- | --- |
| `books` | `id` PK, `name`, `created_at_ms` |
| `payment_methods` | `id` PK, `name`, `created_at_ms` |
| `categories` | `code` PK, `type` (INCOME/EXPENSE), `name`, `created_at_ms` |
| `transactions` | `id` PK, `book_id`, `type` (INCOME/EXPENSE/TRANSFER), `amount_idr`, `transaction_day` (epochDay), `time_minutes`, `category_code`, `payment_method_id`, `transfer_source_id`, `transfer_destination_id`, `note`, `attachment_path`, `recurring_code`, `created_at_ms`, `updated_at_ms` |

Indeks `transactions(book_id, transaction_day, created_at_ms, id)`. Urutan daftar: `transaction_day DESC, time_minutes DESC, created_at_ms DESC, id DESC`.

## Migrasi 1 → 2

`MIGRATION_1_2` membuat tabel buku/metode/kategori, menyemai buku bawaan (Rumah, Pribadi, Usaha), metode (Tunai, Bank, Dompet Digital), kategori lama, lalu membangun ulang `transactions` dengan kolom baru. Transaksi lama dipetakan ke buku **Rumah**; `category_code`, `amount_idr`, `transaction_day` dipertahankan; `time_minutes` diturunkan dari `created_at_ms` (zona lokal). Skema diekspor ke `app/schemas`.

## Operasi inti

- **Ringkasan.** `SummaryCalculator` menghitung total pemasukan/pengeluaran dan agregasi per kategori untuk rentang tanggal (inklusi). Transfer dikecualikan.
- **Periode.** `Period(type, anchor)` dengan rentang Harian/Mingguan/Bulanan/Tahunan/Semua dan `shift` untuk navigasi. Dibagikan antar Beranda/Riwayat/Laporan lewat `AppContainer.period`.
- **Tambah/Ubah/Hapus.** UUID tetap untuk percobaan ulang; `@Upsert` idempoten; hapus idempoten. Validasi nominal, tanggal, jam, catatan, dan asal≠tujuan untuk transfer.
- **Nominal.** Parser `AmountParser` menerima `125000` dan `125.000`, menolak tanda/pecahan/pengelompokan salah; tanpa `Float`/`Double`.

## Persistensi dan privasi

Database di penyimpanan internal, tanpa izin internet, `allowBackup=false`. Nominal/catatan tidak dicatat ke log.

## Pengujian

`./gradlew assembleDebug`, `./gradlew testDebugUnitTest`, `./gradlew lintDebug`, `./gradlew installDebug`. Unit test: parser nominal, periode, kalkulator ringkasan (transfer dikecualikan, rentang).

Dokumen terkait · [PRD.md](PRD.md) · [DESIGN.md](DESIGN.md)
