package com.pulse.pdf.ui

import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.text.InputType
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.github.barteksc.pdfviewer.listener.OnErrorListener
import com.github.barteksc.pdfviewer.listener.OnLoadCompleteListener
import com.github.barteksc.pdfviewer.listener.OnPageChangeListener
import com.github.barteksc.pdfviewer.listener.OnPageErrorListener
import com.github.barteksc.pdfviewer.util.FitPolicy
import com.pulse.pdf.R
import com.pulse.pdf.databinding.ActivityReaderBinding
import com.pulse.pdf.util.RecentStore
import com.pulse.pdf.util.ScreenWakeGuard
import java.io.File

/**
 * Pdfium-backed reader (same native engine family as Chrome / Drive).
 * Vertical continuous scroll, lazy page decode — fast on 1000+ page docs.
 */
class ReaderActivity : AppCompatActivity(),
    OnPageChangeListener,
    OnLoadCompleteListener,
    OnErrorListener,
    OnPageErrorListener {

    private lateinit var binding: ActivityReaderBinding
    private lateinit var wakeGuard: ScreenWakeGuard
    private lateinit var recents: RecentStore
    private var docUri: Uri? = null
    private var chromeVisible = true
    private var pageCount = 0
    private var currentPage = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReaderBinding.inflate(layoutInflater)
        setContentView(binding.root)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(binding.topBar) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, bars.top, 0, 0)
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomBar) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(v.paddingLeft, 0, v.paddingRight, bars.bottom)
            insets
        }

        wakeGuard = ScreenWakeGuard(this)
        recents = RecentStore(this)
        window.attributes = window.attributes.apply { screenBrightness = 0.42f }

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.btnPrev.setOnClickListener {
            wakeGuard.onUserInteraction()
            if (currentPage > 0) binding.pdfView.jumpTo(currentPage - 1, true)
        }
        binding.btnNext.setOnClickListener {
            wakeGuard.onUserInteraction()
            if (currentPage < pageCount - 1) binding.pdfView.jumpTo(currentPage + 1, true)
        }
        binding.pageLabel.setOnClickListener {
            wakeGuard.onUserInteraction()
            showGoToPageDialog()
        }
        binding.pdfView.setOnClickListener {
            wakeGuard.onUserInteraction()
            setChromeVisible(!chromeVisible)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!chromeVisible) setChromeVisible(true) else finish()
            }
        })

        val uri = intent?.data
            ?: intent?.getParcelableExtra<Uri>(EXTRA_URI)
            ?: run {
                Toast.makeText(this, R.string.no_document, Toast.LENGTH_SHORT).show()
                finish()
                return
            }
        openDocument(uri)
    }

    private fun openDocument(uri: Uri) {
        docUri = uri
        try {
            contentResolver.takePersistableUriPermission(
                uri,
                Intent.FLAG_GRANT_READ_URI_PERMISSION,
            )
        } catch (_: SecurityException) {
        }

        val name = queryName(uri) ?: getString(R.string.app_name)
        binding.toolbar.title = name
        recents.touch(uri, name)
        val startPage = recents.list().firstOrNull { it.uri == uri.toString() }?.lastPage ?: 0

        binding.loading.visibility = View.VISIBLE
        // Prefer File for file:// (ContentResolver often hits EACCES on scoped storage).
        val loader = if (uri.scheme == "file") {
            val path = uri.path
            if (path.isNullOrBlank()) {
                onError(IllegalArgumentException("empty file path"))
                return
            }
            binding.pdfView.fromFile(File(path))
        } else {
            binding.pdfView.fromUri(uri)
        }
        loader
            .defaultPage(startPage.coerceAtLeast(0))
            .enableSwipe(true)
            .swipeHorizontal(false) // vertical: scroll down through pages
            .enableDoubletap(true)
            .enableAnnotationRendering(false)
            .password(null)
            .scrollHandle(FolioScrollHandle(this))
            .spacing(8)
            .autoSpacing(false)
            .pageFitPolicy(FitPolicy.WIDTH)
            .fitEachPage(true)
            .pageSnap(false)
            .pageFling(false)
            .nightMode(false)
            .onLoad(this)
            .onPageChange(this)
            .onError(this)
            .onPageError(this)
            .onTap {
                wakeGuard.onUserInteraction()
                setChromeVisible(!chromeVisible)
                true
            }
            .onPageScroll { _, _ -> wakeGuard.onUserInteraction() }
            .load()
    }

    override fun loadComplete(nbPages: Int) {
        pageCount = nbPages
        binding.loading.visibility = View.GONE
        updatePageLabel(binding.pdfView.currentPage)
        wakeGuard.onUserInteraction()
        // Nudge scrollbar so users see the Drive-style scrubber immediately
        binding.pdfView.post {
            binding.pdfView.setPositionOffset(binding.pdfView.positionOffset, true)
        }
        enterImmersive()
    }

    override fun onPageChanged(page: Int, pageCount: Int) {
        this.pageCount = pageCount
        currentPage = page
        updatePageLabel(page)
        wakeGuard.onUserInteraction()
        docUri?.let { recents.updatePage(it, page) }
    }

    override fun onError(t: Throwable?) {
        binding.loading.visibility = View.GONE
        Toast.makeText(this, R.string.open_failed, Toast.LENGTH_LONG).show()
        android.util.Log.e("Folio", "pdf open failed", t)
    }

    override fun onPageError(page: Int, t: Throwable?) {
        android.util.Log.w("Folio", "page error $page", t)
    }

    private fun updatePageLabel(position: Int) {
        binding.pageLabel.text = getString(R.string.page_of_hint, position + 1, pageCount)
        binding.btnPrev.isEnabled = position > 0
        binding.btnNext.isEnabled = position < pageCount - 1
    }

    /** Tap page number → type destination and jump (Drive-style). */
    private fun showGoToPageDialog() {
        if (pageCount <= 0) return
        setChromeVisible(true)
        exitImmersive()

        val pad = (16 * resources.displayMetrics.density).toInt()
        val input = EditText(this).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            imeOptions = EditorInfo.IME_ACTION_GO
            hint = getString(R.string.go_to_page_hint, pageCount)
            setText((currentPage + 1).toString())
            setSelectAllOnFocus(true)
            setPadding(pad, pad, pad, pad)
        }
        val container = FrameLayout(this).apply {
            addView(
                input,
                FrameLayout.LayoutParams(
                    FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT,
                ).apply { setMargins(pad, pad / 2, pad, 0) },
            )
        }

        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.go_to_page_title)
            .setView(container)
            .setPositiveButton(R.string.go_to_page_action) { _, _ ->
                jumpToTypedPage(input.text?.toString())
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()

        input.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_GO) {
                jumpToTypedPage(input.text?.toString())
                dialog.dismiss()
                true
            } else {
                false
            }
        }

        dialog.window?.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        dialog.setOnShowListener { input.requestFocus() }
        dialog.show()
    }

    private fun jumpToTypedPage(raw: String?) {
        val target = raw?.trim()?.toIntOrNull()
        if (target == null || target < 1 || target > pageCount) {
            Toast.makeText(
                this,
                getString(R.string.go_to_page_invalid, pageCount),
                Toast.LENGTH_SHORT,
            ).show()
            return
        }
        val index = target - 1
        binding.pdfView.jumpTo(index, true)
        currentPage = index
        updatePageLabel(index)
        docUri?.let { recents.updatePage(it, index) }
        wakeGuard.onUserInteraction()
    }

    private fun setChromeVisible(visible: Boolean) {
        chromeVisible = visible
        val v = if (visible) View.VISIBLE else View.GONE
        binding.topBar.visibility = v
        binding.bottomBar.visibility = v
        if (visible) exitImmersive() else enterImmersive()
    }

    private fun enterImmersive() {
        WindowInsetsControllerCompat(window, binding.root).let { c ->
            c.hide(WindowInsetsCompat.Type.systemBars())
            c.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }

    private fun exitImmersive() {
        WindowInsetsControllerCompat(window, binding.root)
            .show(WindowInsetsCompat.Type.systemBars())
    }

    private fun queryName(uri: Uri): String? {
        var name: String? = null
        if (uri.scheme == "content") {
            val c: Cursor? = contentResolver.query(uri, null, null, null, null)
            c?.use {
                if (it.moveToFirst()) {
                    val idx = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (idx >= 0) name = it.getString(idx)
                }
            }
        }
        return name ?: uri.lastPathSegment
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        wakeGuard.onUserInteraction()
    }

    override fun onPause() {
        super.onPause()
        wakeGuard.releaseWake()
        docUri?.let { recents.updatePage(it, currentPage) }
    }

    override fun onResume() {
        super.onResume()
        wakeGuard.onUserInteraction()
    }

    override fun onDestroy() {
        wakeGuard.dispose()
        super.onDestroy()
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        val uri = intent.data ?: intent.getParcelableExtra<Uri>(EXTRA_URI) ?: return
        openDocument(uri)
    }

    companion object {
        const val EXTRA_URI = "uri"

        fun open(context: android.content.Context, uri: Uri) {
            context.startActivity(
                Intent(context, ReaderActivity::class.java).apply {
                    data = uri
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                    putExtra(EXTRA_URI, uri)
                },
            )
        }
    }
}
