package com.panitia.soalsandi.data

import android.content.Context
import com.panitia.soalsandi.model.HistoryEntry
import com.panitia.soalsandi.model.SoalItem
import com.panitia.soalsandi.model.SoalPackage
import com.panitia.soalsandi.model.SoalType
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.UUID

/**
 * Everything here is 100% local (internal app storage). No network, no login,
 * no account — matches the "offline, no internet needed" requirement.
 */
class HistoryRepository(context: Context) {

    private val historyFile = File(context.filesDir, "sandi_history.json")
    private val usedCombosFile = File(context.filesDir, "sandi_used_combos.json")

    // ---------- Used combo tracking (never repeat an identical 30-letter set) ----------

    fun loadUsedCombos(): MutableSet<String> {
        if (!usedCombosFile.exists()) return mutableSetOf()
        return try {
            val arr = JSONArray(usedCombosFile.readText())
            (0 until arr.length()).mapTo(mutableSetOf()) { arr.getString(it) }
        } catch (e: Exception) {
            mutableSetOf()
        }
    }

    fun saveUsedCombos(combos: Set<String>) {
        // Cap growth so the file doesn't grow forever after thousands of generations.
        val capped = combos.toList().takeLast(20_000)
        val arr = JSONArray()
        capped.forEach { arr.put(it) }
        usedCombosFile.writeText(arr.toString())
    }

    // ---------- History ----------

    fun loadHistory(): List<HistoryEntry> {
        if (!historyFile.exists()) return emptyList()
        return try {
            val arr = JSONArray(historyFile.readText())
            (0 until arr.length()).map { entryFromJson(arr.getJSONObject(it)) }
                .sortedByDescending { it.timestampMillis }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun addHistoryEntry(entry: HistoryEntry) {
        val current = loadHistory().toMutableList()
        current.add(0, entry)
        writeHistory(current)
    }

    fun deleteHistoryEntry(id: String) {
        val current = loadHistory().filterNot { it.id == id }
        writeHistory(current)
    }

    fun clearHistory() {
        writeHistory(emptyList())
    }

    private fun writeHistory(entries: List<HistoryEntry>) {
        val arr = JSONArray()
        entries.forEach { arr.put(entryToJson(it)) }
        historyFile.writeText(arr.toString())
    }

    // ---------- JSON (de)serialization ----------

    private fun entryToJson(entry: HistoryEntry): JSONObject = JSONObject().apply {
        put("id", entry.id)
        put("timestampMillis", entry.timestampMillis)
        put("jumlahPaket", entry.jumlahPaket)
        val pkgArr = JSONArray()
        entry.packages.forEach { pkgArr.put(packageToJson(it)) }
        put("packages", pkgArr)
    }

    private fun entryFromJson(json: JSONObject): HistoryEntry {
        val pkgArr = json.getJSONArray("packages")
        val packages = (0 until pkgArr.length()).map { packageFromJson(pkgArr.getJSONObject(it)) }
        return HistoryEntry(
            id = json.getString("id"),
            timestampMillis = json.getLong("timestampMillis"),
            jumlahPaket = json.getInt("jumlahPaket"),
            packages = packages
        )
    }

    private fun packageToJson(pkg: SoalPackage): JSONObject = JSONObject().apply {
        put("name", pkg.name)
        val itemsArr = JSONArray()
        pkg.items.forEach { item ->
            itemsArr.put(JSONObject().apply {
                put("number", item.number)
                put("letter", item.letter.toString())
                put("code", item.code)
                put("type", item.type.name)
            })
        }
        put("items", itemsArr)
    }

    private fun packageFromJson(json: JSONObject): SoalPackage {
        val itemsArr = json.getJSONArray("items")
        val items = (0 until itemsArr.length()).map { i ->
            val o = itemsArr.getJSONObject(i)
            SoalItem(
                number = o.getInt("number"),
                letter = o.getString("letter")[0],
                code = o.getString("code"),
                type = SoalType.valueOf(o.getString("type"))
            )
        }
        return SoalPackage(name = json.getString("name"), items = items)
    }

    companion object {
        fun newId(): String = UUID.randomUUID().toString()
    }
}
