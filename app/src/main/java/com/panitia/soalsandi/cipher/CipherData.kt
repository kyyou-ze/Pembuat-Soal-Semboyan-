package com.panitia.soalsandi.cipher

/**
 * Cipher tables copied from "Juknis Persimanu 2 Cabang Jepara" (Lomba Semboyan G&T):
 * - Sandi Kimia: each letter A-Z maps to a chemical-formula-like code.
 * - Sandi Merah Putih: a 5x5 grid, rows keyed by "P U T I H", columns keyed by "M E R A H".
 *   The grid only has 25 cells for 26 letters, so Y and Z share the last cell,
 *   exactly as the reference table only shows through Y in that last cell.
 *
 * Only A-Z are needed: the juknis specifies every soal uses 30 huruf alfabet
 * (numbers/punctuation from the original chart are not required).
 */
object CipherData {

    val KIMIA: Map<Char, String> = mapOf(
        'A' to "OH", 'B' to "HO3", 'C' to "SOHA", 'D' to "SO2", 'E' to "O",
        'F' to "O2SA", 'G' to "H2O", 'H' to "O4", 'I' to "O2", 'J' to "OH3",
        'K' to "NOH", 'L' to "ASO2", 'M' to "H2", 'N' to "HO", 'O' to "S3",
        'P' to "OS2A", 'Q' to "H2OS", 'R' to "OHA", 'S' to "O3", 'T' to "H",
        'U' to "O2H", 'V' to "O3H", 'W' to "OH2", 'X' to "SO2H", 'Y' to "SOH2",
        'Z' to "H2O2"
    )

    /**
     * 5x5 grid, columns headed M E R A H (top), rows headed P U T I H (left).
     * A letter's code is COLUMN-header + ROW-header — e.g. A sits in column M,
     * row P, so its code is "MP" (not "PM"). Only 25 cells exist, so Z has no
     * Sandi Merah Putih code at all; soal generation must never pick Z for it.
     */
    val MERAH_PUTIH: Map<Char, String> by lazy {
        val rows = listOf('P', 'U', 'T', 'I', 'H')
        val cols = listOf('M', 'E', 'R', 'A', 'H')
        val gridLetters = listOf("ABCDE", "FGHIJ", "KLMNO", "PQRST", "UVWXY")

        val map = LinkedHashMap<Char, String>()
        gridLetters.forEachIndexed { r, rowLetters ->
            rowLetters.forEachIndexed { c, letter ->
                map[letter] = "${cols[c]}${rows[r]}"
            }
        }
        map // A..Y only — no entry for Z
    }

    fun randomLetter(): Char = ('A' + (0 until 26).random())

    /** For Sandi Merah Putih only: picks a random letter from A-Y (Z is excluded, it has no code). */
    fun randomLetterExcludingZ(): Char = ('A' + (0 until 25).random())
}
