# Jaga Uang — Desain Antarmuka Android

**Status** Draft MVP · **Tema** Terang · **Sasaran** 40 tahun ke atas

## Identitas

Simbol buku-perisai huruf J; ikon launcher = bidang biru penuh + simbol krem (adaptive icon, sudah dibuat dari aset `jaga-uang-ikon-android-biru.png`). Logo horizontal `jaga-uang-logo-biru.png` untuk materi pengenalan.

## Warna & tipografi

| Peran | Warna |
| --- | --- |
| Primer | `#17365D` |
| Aksen utama | `#4F46E5` |
| Latar | `#FFFFFF` |
| Permukaan | `#FFFFFF` |
| Teks utama | `#182334` |
| Teks sekunder | `#475569` |
| Pemasukan | `#0F7B4F` (tint `#E6F6EF`) |
| Pengeluaran/kesalahan | `#B42318` (tint `#FDECEE`) |
| Biru / Ungu | `#2563EB` (`#EAF1FE`) / `#7C3AED` (`#F1ECFE`) |

Font **DM Sans** (dibundel, fallback sistem). Skala: nominal besar 26–32sp semibold, judul layar 24sp, nominal kartu 18–20sp, isi 16sp, metadata 13–14sp. Margin 16–20dp, target sentuh ≥48dp, radius kartu 14–20dp.

Latar dan bilah sistem putih bersih, namun warna dipakai dengan percaya diri: kartu ringkasan Beranda bergradasi indigo→navy, empat tombol aksi memakai tile tint lembut (hijau, merah, biru, ungu) dengan ikon berwarna, kartu total Riwayat/Laporan memakai tint yang sama, dan ikon menu Lainnya memakai kotak tint. Teks nominal memakai warna aksen gelap agar kontras ≥4,5:1. Referensi arah: [Buddy](https://mobbin.com/screens/416998c7-9be2-4177-9038-1c464938c3a2), [Copilot](https://mobbin.com/screens/39e851c1-7c5d-4f1e-bf81-1c3be05e5566), dan [Vivid](https://mobbin.com/screens/81d47f2c-da59-4bd8-b238-01eda717addf) di Mobbin.

## Navigasi (4 tab bawah)

| Tab | Isi |
| --- | --- |
| **Beranda** | Pemilih buku di bilah atas, grid 2×2 (Catat Pemasukan, Catat Pengeluaran, Transfer, Lihat Transaksi), ringkasan periode (dua total), transaksi terbaru. |
| **Riwayat** | Filter Semua/Harian/Mingguan/Bulanan/Tahunan, navigasi periode (sebelumnya/berikutnya + pemilih tanggal), daftar per hari, FAB "Catat". |
| **Laporan** | Subtab Ringkasan/Pengeluaran/Pemasukan; donat + daftar kategori (nama, nominal, persentase). |
| **Lainnya** | Kelola Buku Catatan, Metode Pembayaran, Kategori, Pengingat. |

Form tambah/edit/transfer, detail, dan layar kelola memakai tombol kembali.

Perpindahan halaman langsung tanpa animasi masuk/keluar, fade, atau transisi saat kembali. Pengaturan animasi sistem HP tidak diubah.
Hanya satu konten tab ditampilkan per frame, tanpa menumpuk halaman lama dan baru. Posisi gulir dan subtab dipulihkan saat kembali; mengetuk tab aktif tidak membuka halaman tambahan.

## Grafik laporan

Donat ditempatkan di kartu putih, dengan total rupiah lengkap di atas dan jumlah kategori di tengah. Jalur lingkaran dipusatkan pada sisi terpendek Canvas, diberi inset setengah ketebalan garis ditambah ruang tepi 2dp agar seluruh garis tetap terlihat. Segmen dipisahkan celah kecil; satu kategori tetap berupa lingkaran penuh. Palet donat adalah satu ramp slate-biru berurutan (gelap→terang), bukan warna-warni; kategori dibedakan lewat kecerahan agar grafik tenang dan tidak mencolok.

Rincian di bawah grafik memakai warna yang sama, nama kategori, nominal, dan persentase. Titik legenda diberi bingkai tipis agar segmen paling terang tetap terlihat di atas putih. Nama dan nominal disusun vertikal agar tidak berebut ruang; porsi positif di bawah 1% ditulis `<1%`, bukan `0%`. Periode kosong menampilkan pesan tanpa grafik.

## Form

Urutan bidang: Nominal (kalkulator besar), Kategori, Metode Pembayaran, Catatan (opsional + lampiran foto struk + input suara), Tanggal, Jam, Pengingat Berulang. Form transfer: Asal, Tujuan, Nominal, Tanggal, Jam, Catatan. Tombol **Simpan** menempel di bawah, tetap terjangkau.

## Status dan teks

| Keadaan | Teks |
| --- | --- |
| Tidak ada transaksi | **Belum ada transaksi pada periode ini.** |
| Simpan | **Menyimpan…** lalu **Transaksi tersimpan.** / **Transaksi diperbarui.** |
| Hapus | **Hapus transaksi ini?** (tanggal + nominal) → **Batal** / **Hapus** |
| Gagal | **Transaksi belum bisa disimpan/dihapus.** |
| Transfer asal=tujuan | **Metode asal dan tujuan harus berbeda.** |

## Aksesibilitas

Font hingga 200% tanpa terpotong, format rupiah lengkap, label TalkBack pada kontrol ikon, kontras ≥4.5:1, fokus urut, target sentuh ≥48dp.

Dokumen terkait · [PRD.md](PRD.md) · [ARCHITECURE.md](ARCHITECURE.md)
