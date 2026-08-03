# Generator Soal Sandi

Aplikasi Android (Kotlin + Jetpack Compose, Material Design 3) untuk panitia
lomba Pramuka membuat soal Sandi Kimia & Sandi Merah Putih secara otomatis,
sepenuhnya offline.

## Build otomatis lewat GitHub (tanpa Android Studio)

Project ini sudah menyertakan `.github/workflows/build.yml`. Caranya:

1. Push/upload seluruh isi folder ini ke sebuah repo GitHub (bisa lewat
   `git push`, atau upload manual lewat web GitHub kalau tidak familiar
   dengan git).
2. Buka tab **Actions** di repo tsb → workflow **"Build APK"** akan otomatis
   jalan tiap kali push ke branch `main` (atau jalankan manual lewat tombol
   **Run workflow**).
3. Setelah selesai (beberapa menit), buka run tsb → bagian **Artifacts** di
   bawah → unduh **GeneratorSoalSandi-debug-apk** → isinya file `.apk` yang
   sudah bisa langsung di-install ke HP Android (aktifkan "Install dari
   sumber tidak dikenal" di HP untuk APK debug ini karena tidak ditandatangani
   Play Store).

Workflow ini memakai `gradle/actions/setup-gradle` untuk menyediakan Gradle
langsung di server GitHub (project ini sengaja tidak menyertakan
`gradlew`/`gradle-wrapper.jar` biner), jadi tidak perlu Android Studio atau
Gradle terpasang di komputer sama sekali — cukup GitHub.

## Cara membuka project di Android Studio (opsional, kalau mau develop lokal)

1. Buka **Android Studio** (disarankan versi terbaru, minimal Iguana/2023.2+).
2. Pilih **Open**, arahkan ke folder `GeneratorSoalSandi` ini.
3. Saat pertama dibuka, Android Studio akan menawarkan membuat **Gradle
   Wrapper** secara otomatis — klik **OK/Sync**, itu satu-satunya langkah yang
   perlu internet (mengunduh Gradle & dependency AndroidX/Compose seperti
   biasa saat development Android). Setelah build pertama, aplikasi berjalan
   100% offline di perangkat.
4. Tunggu proses **Gradle Sync**, lalu jalankan (Run ▶) ke emulator atau HP.

## Struktur kode

```
app/src/main/java/com/panitia/soalsandi/
├── cipher/CipherData.kt        Peta Sandi Kimia (A-Z) & Sandi Merah Putih (kode=kolom+baris, A→MP; Z tanpa kode)
├── model/Models.kt             SoalItem, SoalPackage, HistoryEntry
├── generator/SoalGenerator.kt  Logika generate 30 soal/paket, anti-duplikat
├── data/HistoryRepository.kt   Penyimpanan lokal (JSON) riwayat & kombinasi terpakai
├── export/
│   ├── SoalRenderer.kt         Gambar kotak soal + jawaban (dipakai PDF & gambar)
│   ├── PdfExporter.kt          Export PDF (android.graphics.pdf.PdfDocument)
│   ├── ImageExporter.kt        Export PNG/JPG (Bitmap + Canvas)
│   ├── ZipExporter.kt          Gabungkan banyak file jadi .zip (java.util.zip)
│   └── FileSaver.kt            Simpan ke folder Downloads perangkat + share
├── ui/
│   ├── GeneratorScreen.kt      Layar utama: tombol, grid soal, dialog
│   ├── HistoryScreen.kt        Layar riwayat
│   ├── components/SoalViews.kt Kotak soal & daftar jawaban (Compose)
│   └── theme/                  Warna, tipografi, tema Material 3
└── MainActivity.kt             Menyatukan semua layar & state
```

## Aturan yang sudah hardcoded (tidak bisa diubah pengguna)

- Selalu 30 soal per paket.
- Nomor 1–15: Sandi Kimia.
- Nomor 16–30: Sandi Merah Putih.
- Kombinasi 30-huruf yang sama tidak pernah dihasilkan dua kali (dicek
  terhadap seluruh riwayat yang tersimpan, bukan cuma sesi saat ini).

## Catatan tentang dependency

Tidak ada library pihak ketiga untuk PDF/gambar/ZIP — semuanya memakai API
bawaan Android (`PdfDocument`, `Bitmap`/`Canvas`, `java.util.zip`), supaya
aplikasi benar-benar tidak butuh internet saat dipakai, dan tidak ada iklan,
login, atau akun.

## Yang belum sempat diuji

Kode ini ditulis di lingkungan tanpa Android SDK/emulator, jadi belum pernah
di-build/di-run langsung. Kemungkinan penyesuaian kecil yang mungkin perlu
dilakukan di Android Studio:
- Versi Gradle Plugin / Kotlin / Compose BOM di `app/build.gradle.kts`
  mungkin perlu di-update ke versi stabil terbaru jika Android Studio
  meminta.
- Ikon aplikasi (`mipmap/ic_launcher`) masih placeholder sederhana — ganti
  sesuai selera lewat Image Asset Studio (klik kanan `res` → New → Image Asset).
