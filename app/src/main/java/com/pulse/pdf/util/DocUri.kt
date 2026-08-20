package com.pulse.pdf.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest

/**
 * Resolves VIEW / SEND URIs from Telegram, Drive, Downloads, etc.
 * Prefers the granted content Uri (no full copy). Falls back to a cache
 * file only when the provider FD cannot be opened (common Telegram edge case).
 */
object DocUri {

    fun fromIntent(intent: Intent?): Uri? {
        if (intent == null) return null
        intent.data?.let { return it }
        @Suppress("DEPRECATION")
        intent.getParcelableExtra<Uri>("uri")?.let { return it }
        if (Intent.ACTION_SEND == intent.action) {
            @Suppress("DEPRECATION")
            intent.getParcelableExtra<Uri>(Intent.EXTRA_STREAM)?.let { return it }
            intent.clipData?.takeIf { it.itemCount > 0 }?.getItemAt(0)?.uri?.let { return it }
        }
        return null
    }

    fun displayName(context: Context, uri: Uri): String? {
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { c ->
                if (c.moveToFirst()) {
                    val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0) return c.getString(idx)
                }
            }
        }
        return uri.lastPathSegment
    }

    /** True when [uri] can be opened as a seekable FD (Pdfium requirement). */
    fun canOpenFd(context: Context, uri: Uri): Boolean {
        return try {
            context.contentResolver.openFileDescriptor(uri, "r")?.use { true } ?: false
        } catch (_: Exception) {
            false
        }
    }

    /**
     * Stream [uri] into app cache for local fromFile() open.
     * Skips re-copy when the same source was cached earlier.
     */
    fun cacheCopy(context: Context, uri: Uri, displayName: String?): File {
        val key = MessageDigest.getInstance("SHA-1")
            .digest(uri.toString().toByteArray())
            .joinToString("") { "%02x".format(it) }
            .take(16)
        val safeName = (displayName ?: "doc.pdf")
            .replace(Regex("[^A-Za-z0-9._-]"), "_")
            .take(48)
        val out = File(context.cacheDir, "pdf_$key-$safeName")
        if (out.exists() && out.length() > 0L) return out

        val tmp = File(context.cacheDir, "pdf_$key.tmp")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(tmp).use { output -> input.copyTo(output, 256 * 1024) }
        } ?: throw IllegalStateException("cannot read $uri")
        if (!tmp.renameTo(out)) {
            tmp.copyTo(out, overwrite = true)
            tmp.delete()
        }
        return out
    }

    /** Drop old cached PDFs if cache grows too large (keep newest). */
    fun trimCache(context: Context, maxBytes: Long = 400L * 1024L * 1024L) {
        val files = context.cacheDir.listFiles { f -> f.isFile && f.name.startsWith("pdf_") }
            ?.sortedByDescending { it.lastModified() }
            ?: return
        var total = files.sumOf { it.length() }
        for (f in files.drop(2)) { // keep at least 2 recent
            if (total <= maxBytes) break
            total -= f.length()
            f.delete()
        }
    }
}
