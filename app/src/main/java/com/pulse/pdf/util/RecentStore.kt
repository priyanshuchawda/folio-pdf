package com.pulse.pdf.util

import android.content.Context
import android.content.SharedPreferences
import android.net.Uri

data class RecentDoc(
    val uri: String,
    val name: String,
    val lastPage: Int,
    val openedAt: Long,
)

class RecentStore(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("pulse_recents", Context.MODE_PRIVATE)

    fun list(): List<RecentDoc> {
        val raw = prefs.getString(KEY, "") ?: ""
        if (raw.isBlank()) return emptyList()
        return raw.split('\n').mapNotNull { line ->
            val p = line.split('\t')
            if (p.size < 4) return@mapNotNull null
            RecentDoc(p[0], p[1], p[2].toIntOrNull() ?: 0, p[3].toLongOrNull() ?: 0L)
        }.sortedByDescending { it.openedAt }.take(MAX)
    }

    fun touch(uri: Uri, name: String, lastPage: Int = 0) {
        val now = System.currentTimeMillis()
        val updated = list()
            .filterNot { it.uri == uri.toString() }
            .toMutableList()
        updated.add(0, RecentDoc(uri.toString(), name, lastPage, now))
        save(updated.take(MAX))
    }

    fun updatePage(uri: Uri, page: Int) {
        val updated = list().map {
            if (it.uri == uri.toString()) it.copy(lastPage = page, openedAt = System.currentTimeMillis())
            else it
        }
        save(updated)
    }

    private fun save(items: List<RecentDoc>) {
        val raw = items.joinToString("\n") {
            "${it.uri}\t${it.name}\t${it.lastPage}\t${it.openedAt}"
        }
        prefs.edit().putString(KEY, raw).apply()
    }

    companion object {
        private const val KEY = "items"
        private const val MAX = 12
    }
}
