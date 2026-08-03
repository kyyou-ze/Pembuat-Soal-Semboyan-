package com.panitia.soalsandi.generator

import com.panitia.soalsandi.cipher.CipherData
import com.panitia.soalsandi.model.SoalItem
import com.panitia.soalsandi.model.SoalPackage
import com.panitia.soalsandi.model.SoalType

object SoalGenerator {

    /**
     * Hardcoded rule (not user-editable): 30 soal per paket.
     * 1-15  -> Sandi Kimia
     * 16-30 -> Sandi Merah Putih
     */
    private fun generateOnePackage(name: String): SoalPackage {
        val items = (1..30).map { number ->
            if (number <= 15) {
                val letter = CipherData.randomLetter()
                SoalItem(number, letter, CipherData.KIMIA.getValue(letter), SoalType.KIMIA)
            } else {
                val letter = CipherData.randomLetterExcludingZ()
                SoalItem(number, letter, CipherData.MERAH_PUTIH.getValue(letter), SoalType.MERAH_PUTIH)
            }
        }
        return SoalPackage(name, items)
    }

    /**
     * Generates [count] distinct packages. "Distinct" means the exact 30-letter
     * combination has never appeared before (checked against [usedCombos]) and is
     * unique within this same batch. If a collision happens by chance, it silently
     * regenerates until a fresh combination is found.
     *
     * [usedCombos] is mutated in place (new combos are added) so the caller can persist it.
     */
    fun generatePackages(count: Int, usedCombos: MutableSet<String>): List<SoalPackage> {
        val result = mutableListOf<SoalPackage>()
        repeat(count) { index ->
            val name = packageName(index, count)
            var pkg = generateOnePackage(name)
            var attempts = 0
            while (usedCombos.contains(pkg.comboKey) && attempts < 10_000) {
                pkg = generateOnePackage(name)
                attempts++
            }
            usedCombos.add(pkg.comboKey)
            result.add(pkg)
        }
        return result
    }

    /** Paket A, B, C... Z, AA, AB... for very large counts (e.g. 100 paket). */
    private fun packageName(index: Int, total: Int): String {
        if (total <= 26) {
            return "Paket ${('A' + index)}"
        }
        // Spreadsheet-style naming: A..Z, AA..AZ, BA.. for counts above 26.
        var n = index
        val sb = StringBuilder()
        do {
            sb.insert(0, ('A' + (n % 26)))
            n = n / 26 - 1
        } while (n >= 0)
        return "Paket $sb"
    }
}
