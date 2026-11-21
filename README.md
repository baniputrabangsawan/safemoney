# Jaga Uang

Aplikasi Android pencatat pemasukan, pengeluaran, dan transfer untuk pengguna Indonesia usia 40+. Sepenuhnya offline, tanpa akun, tanpa sinkronisasi. Dibangun dengan Kotlin, Jetpack Compose, dan Room.

> Repositori: `baniputrabangsawan/safemoney` · `applicationId`: `com.jagauang.app`

## Konsep inti

**Tidak ada saldo.** Aplikasi ini tidak menghitung saldo awal, saldo berjalan, maupun selisih pemasukan dikurangi pengeluaran. Ringkasan hanya menampilkan **Total Pemasukan** dan **Total Pengeluaran** untuk periode aktif. Jangan menambahkan fitur saldo — ini keputusan produk, bukan kekurangan.

- **Buku catatan** memisahkan data (bawaan: Rumah, Pribadi, Usaha). Beranda, Riwayat, dan Laporan mengikuti buku aktif.
- **Metode pembayaran** dikelola terpisah (bawaan: Tunai, Bank, Dompet Digital).
- **Transfer** mencatat perpindahan dana antarmetode dalam satu buku. Transfer **dikecualikan** dari total dan diagram kategori.

## Fitur

| Tab | Isi |
| --- | --- |
| Beranda | Pemilih buku, grid 2×2 (Catat Pemasukan, Catat Pengeluaran, Transfer, Lihat Transaksi), ringkasan periode, transaksi terbaru |
| Riwayat | Filter Semua/Harian/Mingguan/Bulanan/Tahunan, navigasi periode + pemilih tanggal, daftar per hari, FAB pencatatan |
| Laporan | Subtab Ringkasan/Pengeluaran/Pemasukan, diagram donat + rincian kategori (nama, nominal, persentase) |
| Lainnya | Kelola Buku Catatan, Metode Pembayaran, Kategori, Pengingat |

Form pemasukan/pengeluaran: nominal (kalkulator), kategori, metode pembayaran, catatan + lampiran foto struk + input suara sistem, tanggal, jam, pengingat berulang. Detail transaksi menyediakan Ubah dan Hapus dengan konfirmasi.

## Prasyarat

| Kebutuhan | Versi |
| --- | --- |
| JDK | 17 |
| Gradle | 8.9 (wrapper sudah disertakan, tidak perlu install) |
| Android SDK | Platform 35, Build Tools 34.0.0+ |
| Perangkat | Android 8.0+ (minSdk 26), atau emulator |

`ANDROID_HOME` harus menunjuk ke SDK. Tidak ada version catalog atau skrip kustom; semua konfigurasi ada di Gradle Kotlin DSL.

## Cara menjalankan

Dari root proyek, gunakan wrapper yang sudah ada:

```bash
export ANDROID_HOME="$HOME/Android/Sdk"
export PATH="$ANDROID_HOME/platform-tools:$PATH"

./gradlew assembleDebug      # build APK debug
./gradlew testDebugUnitTest  # unit test JVM
./gradlew lintDebug          # Android Lint
```

Pasang ke perangkat yang terhubung lewat USB debugging:

```bash
adb devices -l               # pastikan status "device"
./gradlew installDebug
adb shell am start -W -n com.jagauang.app/.MainActivity
```

APK: `app/build/outputs/apk/debug/app-debug.apk`
Laporan lint: `app/build/reports/lint-results-debug.html`

Alternatif tanpa `adb`: pasang lewat Android Studio (**Run ▶**), atau `./gradlew installDebug` dengan emulator aktif.

### Menjalankan satu kelas test

```bash
./gradlew testDebugUnitTest --tests 'com.jagauang.app.domain.AmountParserTest'
```

## Arsitektur

Satu modul Gradle (`:app`), tanpa Hilt/Koin. `JagaUangApplication` membuat satu `AppContainer` berskala aplikasi yang menyimpan database, repository, dan StateFlow bersama.

```
app/src/main/java/com/jagauang/app/
├── AppContainer.kt           DI manual + StateFlow bersama (buku, periode, cache)
├── AppNavHost.kt             Satu destinasi "main" untuk 4 tab + rute form/detail
├── MainActivity.kt           Entry point Compose
├── JagaUangApplication.kt    Membuat AppContainer
├── domain/                   Model murni + aturan tanpa dependensi Android
│   ├── AmountParser.kt       Parse "125000" / "125.000", tolak tanda & pecahan
│   ├── TransactionValidator.kt
│   ├── Period.kt             Rentang harian/mingguan/bulanan/tahunan
│   ├── SummaryCalculator.kt  Total & agregasi kategori (transfer dikecualikan)
│   └── Transaction, Book, Category, PaymentMethod, Recurring, TransactionType
├── data/
│   ├── local/                Room: entity, DAO, database v2, migrasi 1→2
│   └── repository/           Repository + pemetaan entity↔domain, WriteResult
└── ui/
    ├── home/ history/ report/ more/ transaction/
    └── components/ theme/    Komponen bersama, token warna & DM Sans
```

### Alur data

```
Room DAO (Flow) → Repository (mapper) → AppContainer StateFlow (eager, Dispatchers.Default)
                                      → Composable (collectAsStateWithLifecycle)

Form/VM → Repository (validasi) → DAO (suspend, @Upsert) → Room
```

Penulisan mengembalikan `WriteResult.Success | NotFound | Invalid`; exception database dibiarkan naik dan ditangani ViewModel.

### Navigasi

Empat tab berbagi **satu destinasi NavHost** (`when(selectedTab)`), dengan `rememberSaveable` + `SaveableStateHolder` agar posisi gulir dan subtab bertahan. Form, detail, dan layar kelola adalah destinasi terpisah dengan `launchSingleTop` dan transisi `None`. Back dari tab non-Beranda kembali ke Beranda.

### Basis data (Room v2)

| Tabel | Isi |
| --- | --- |
| `transactions` | id, book_id, type (INCOME/EXPENSE/TRANSFER), amount_idr, transaction_day, time_minutes, category_code, payment_method_id, transfer_source_id, transfer_destination_id, note, attachment_path, recurring_code, timestamps |
| `books` | Buku catatan |
| `payment_methods` | Metode pembayaran |
| `categories` | Kategori per jenis |

Berkas: `jaga_uang.db` di penyimpanan internal. Skema diekspor ke `app/schemas/`. Migrasi `1→2` memetakan transaksi lama ke buku `book-rumah` tanpa mengubah kategori, nominal, atau tanggal. **Jangan pernah** memakai migrasi destruktif.

### Konvensi penting

- Uang selalu `Long` rupiah bulat (Rp1–Rp999.999.999.999); tidak pernah floating-point.
- Tanggal `LocalDate`/epoch day, jam = menit sejak tengah malam, timestamp = epoch milidetik. Minggu mulai Senin; rentang inklusif.
- Catatan dipangkas dan dibatasi 200 karakter; nama dibatasi 40.
- Transfer wajib punya asal dan tujuan berbeda dan tidak memakai kategori.

## Testing

JUnit 4 JVM test (28 metode) menutup logika murni:

| Berkas | Cakupan |
| --- | --- |
| `AmountParserTest` (18) | Format & batas nominal |
| `PeriodTest` (6) | Rentang periode |
| `SummaryCalculatorTest` (4) | Total, pengecualian transfer, agregasi kategori |

Belum ada test instrumentasi, Compose UI, atau migrasi Room; berkas skema JSON bukan test migrasi. Untuk perubahan perilaku, jalankan test + `lintDebug` + `assembleDebug`, lalu uji manual di perangkat (tab cepat, detail → Back, pemulihan gulir, ganti periode/buku).

> Gunakan data sintetis pada instalasi uji. Jangan uninstall atau hapus data instalasi pengguna, dan jangan mencatat nominal/catatan/struk ke log.

## Batasan yang diketahui

- Instalasi v2 baru **belum** menyemai buku/metode/kategori bawaan; penyemaian hanya terjadi pada migrasi 1→2, meski `activeBookId` sudah menunjuk `book-rumah`.
- Baris Riwayat belum mengikuti filter periode (totalnya sudah).
- Tombol loncat tanggal di Laporan belum berfungsi.
- Pengingat berulang hanya menyimpan metadata; belum ada penjadwalan notifikasi.
- Membuat transaksi menghasilkan UUID baru tiap `save()`, sehingga percobaan ulang setelah kegagalan tak pasti bisa menggandakan data.

## Dokumen terkait

- [`PRD.md`](PRD.md) — kebutuhan produk, aturan transaksi, skenario uji
- [`ARCHITECURE.md`](ARCHITECURE.md) — arsitektur, skema, migrasi (perhatikan ejaan nama berkas)
- [`DESIGN.md`](DESIGN.md) — warna, tipografi, aksesibilitas, alur layar
- [`AGENTS.md`](AGENTS.md) — panduan kerja untuk asisten AI di repo ini
