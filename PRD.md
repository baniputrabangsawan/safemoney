# Jaga Uang — Product Requirements Document

**Status** Draft MVP · **Platform** Android · **Bahasa** Indonesia · **Mata uang** IDR

## Tujuan

Jaga Uang membantu pengguna mencatat pemasukan, pengeluaran, dan transfer antarmetode pembayaran, lalu melihat ringkasan per periode. Sasaran utama pengguna 40 tahun ke atas: alur singkat, angka mudah dibaca, bekerja offline tanpa akun.

## Konsep inti

- **Tidak ada saldo.** Aplikasi tidak menghitung saldo awal, saldo berjalan, atau selisih pemasukan dikurangi pengeluaran. Ringkasan hanya menampilkan **Total Pemasukan** dan **Total Pengeluaran** untuk periode aktif.
- **Buku catatan.** Transaksi dikelompokkan per buku. Bawaan: **Rumah**, **Pribadi**, **Usaha**. Pengguna bisa menambah, mengganti nama, dan berpindah buku. Beranda, Riwayat, dan Laporan mengikuti buku aktif.
- **Metode pembayaran.** Dikelola terpisah, bawaan: **Tunai**, **Bank**, **Dompet Digital**.
- **Kategori.** Dikelola per jenis; bawaan pemasukan (Gaji, Usaha, Lainnya) dan pengeluaran (Makan, Transportasi, Tagihan, Belanja, Lainnya). Kedua jenis mendukung **Tanpa kategori**.

## Cakupan MVP

| ID | Fitur | Kriteria penerimaan |
| --- | --- | --- |
| P01 | Beranda | Pemilih buku di bilah atas, empat tombol besar 2×2 (Catat Pemasukan, Catat Pengeluaran, Transfer, Lihat Transaksi), ringkasan periode (dua total), dan transaksi terbaru. |
| P02 | Catat pemasukan/pengeluaran | Nominal (kalkulator), kategori, metode pembayaran, catatan opsional + lampiran foto struk, tanggal, jam, pengingat berulang. Input suara bila tersedia. Simpan mudah dijangkau. |
| P03 | Transfer | Asal dan tujuan metode pembayaran, nominal, tanggal, jam sebagai satu transaksi. Asal ≠ tujuan wajib. Dikecualikan dari total dan diagram kategori. |
| P04 | Riwayat | Filter Semua/Harian/Mingguan/Bulanan/Tahunan, navigasi periode (sebelumnya/berikutnya + pemilih tanggal), daftar per hari, akses cepat pencatatan, dan dua total periode. |
| P05 | Laporan | Subtab Ringkasan, Pengeluaran, Pemasukan. Diagram donat + daftar kategori (nama, nominal, persentase). Pesan kosong jelas saat tidak ada data. |
| P06 | Buku & metode | Kelola buku catatan (tambah, ganti nama, pindah) dan metode pembayaran (tambah, ganti nama). |
| P07 | Detail transaksi | Ubah dan Hapus dengan konfirmasi. |

## Aturan transaksi

- Nominal rupiah bulat Rp1–Rp999.999.999.999. Jenis menentukan tanda: pemasukan `+`, pengeluaran `−`, transfer netral.
- Tanggal kalender perangkat, lampau sampai 1 Januari 1900, mendatang ditolak. Jam 00:00–23:59.
- Catatan opsional maksimal 200 karakter setelah trim. Transfer tidak memakai kategori maupun metode tunggal.
- Total pemasukan/pengeluaran hanya menghitung transaksi jenis tersebut pada buku dan periode aktif. Transfer tidak dihitung.

## Contoh uji (buku Rumah, periode sama)

Simpan pemasukan Rp100.000, pengeluaran Rp30.000, transfer Rp20.000 Tunai → Bank. Hasil: **Pemasukan Rp100.000**, **Pengeluaran Rp30.000**, transfer tampil di Riwayat. Pindah ke buku **Usaha** yang kosong → kedua total **Rp0**. Mode pesawat → catatan tetap tersedia.

## Batas MVP

Akun daring, sinkronisasi, bank link, anggaran, pemindaian struk otomatis (OCR), dan penjadwalan notifikasi pengingat berada di luar MVP. Data tersimpan di perangkat.

Dokumen terkait · [ARCHITECURE.md](ARCHITECURE.md) · [DESIGN.md](DESIGN.md)
