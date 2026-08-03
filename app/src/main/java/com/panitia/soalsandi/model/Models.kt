package com.panitia.soalsandi.model

enum class SoalType { KIMIA, MERAH_PUTIH }

data class SoalItem(
    val number: Int,      // 1..30
    val letter: Char,      // the actual answer
    val code: String,      // the encoded sandi shown in the box
    val type: SoalType
)

/** One paket = 30 soal (1-15 Sandi Kimia, 16-30 Sandi Merah Putih). */
data class SoalPackage(
    val name: String,       // "Paket A", "Paket B", ...
    val items: List<SoalItem>
) {
    /** Identity string used to detect duplicate packages (never repeat a past combination). */
    val comboKey: String
        get() = items.joinToString(separator = "") { it.letter.toString() }
}

data class HistoryEntry(
    val id: String,
    val name: String = "",   // optional custom label given by the user; blank = show date/time instead
    val timestampMillis: Long,
    val jumlahPaket: Int,
    val packages: List<SoalPackage>
)
