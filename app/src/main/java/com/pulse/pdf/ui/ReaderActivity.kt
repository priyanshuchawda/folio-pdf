package com.pulse.pdf.ui

import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewpager2.widget.ViewPager2
import com.pulse.pdf.PdfSession
import com.pulse.pdf.R
import com.pulse.pdf.databinding.ActivityReaderBinding
import com.pulse.pdf.pdf.PdfDocumentSession
import com.pulse.pdf.util.RecentStore
import com.pulse.pdf.util.ScreenWakeGuard

class ReaderActivity : AppCompatActivity() {

    private lateinit var binding: ActivityReaderBinding
    private lateinit var wakeGuard: ScreenWakeGuard
    private lateinit var recents: RecentStore
    private var session: PdfDocumentSession? = null
    private var docUri: Uri? = null
    private var chromeVisible = true

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

        // Dim reading brightness — big LCD battery win on Fire HD-class devices
        window.attributes = window.attributes.apply {
            screenBrightness = 0.42f
        }

        binding.toolbar.setNavigationOnClickListener { finish() }
        binding.btnPrev.setOnClickListener {
            wakeGuard.onUserInteraction()
            binding.pager.currentItem = (binding.pager.currentItem - 1).coerceAtLeast(0)
        }
        binding.btnNext.setOnClickListener {
            wakeGuard.onUserInteraction()
            val last = (session?.pageCount ?: 1) - 1
            binding.pager.currentItem = (binding.pager.currentItem + 1).coerceAtMost(last)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (!chromeVisible) {
                    setChromeVisible(true)
                } else {
                    finish()
                }
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
            // Intent VIEW grants may be temporary — still readable for this session
        }

        val pfd = contentResolver.openFileDescriptor(uri, "r")
            ?: run {
                Toast.makeText(this, R.string.open_failed, Toast.LENGTH_LONG).show()
                finish()
                return
            }

        val dm = resources.displayMetrics
        val sess = PdfDocumentSession(
            pfd = pfd,
            displayWidthPx = dm.widthPixels,
            displayHeightPx = dm.heightPixels,
        )
        session = sess
        PdfSession.attach(sess)

        val name = queryName(uri) ?: getString(R.string.app_name)
        binding.toolbar.title = name
        recents.touch(uri, name)

        val startPage = recents.list().firstOrNull { it.uri == uri.toString() }?.lastPage ?: 0

        val adapter = PageAdapter(
            session = sess,
            onInteract = { wakeGuard.onUserInteraction() },
            onToggleChrome = { setChromeVisible(!chromeVisible) },
        )
        binding.pager.adapter = adapter
        // One page ahead max — ViewPager2 defaults waste RAM on low-memory tablets
        binding.pager.offscreenPageLimit = 1
        (binding.pager.getChildAt(0) as? androidx.recyclerview.widget.RecyclerView)?.apply {
            setItemViewCacheSize(0)
            recycledViewPool.setMaxRecycledViews(0, 1)
        }
        binding.pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                wakeGuard.onUserInteraction()
                updatePageLabel(position)
                sess.prefetchAround(position)
                docUri?.let { recents.updatePage(it, position) }
            }
        })

        val page = startPage.coerceIn(0, (sess.pageCount - 1).coerceAtLeast(0))
        binding.pager.setCurrentItem(page, false)
        updatePageLabel(page)
        sess.prefetchAround(page)
        wakeGuard.onUserInteraction()
        enterImmersive()
    }

    private fun updatePageLabel(position: Int) {
        val total = session?.pageCount ?: 0
        binding.pageLabel.text = getString(R.string.page_of, position + 1, total)
        binding.btnPrev.isEnabled = position > 0
        binding.btnNext.isEnabled = position < total - 1
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
        // Persist page; drop cached bitmaps while backgrounded
        session?.trimMemory()
        docUri?.let { recents.updatePage(it, binding.pager.currentItem) }
    }

    override fun onResume() {
        super.onResume()
        wakeGuard.onUserInteraction()
        session?.let {
            val p = binding.pager.currentItem
            it.requestPage(p) { _, _ -> binding.pager.adapter?.notifyItemChanged(p) }
            it.prefetchAround(p)
        }
    }

    override fun onTrimMemory(level: Int) {
        super.onTrimMemory(level)
        if (level >= android.content.ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN) {
            session?.trimMemory()
        } else if (level >= android.content.ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE) {
            session?.prefetchAround(binding.pager.currentItem)
        }
    }

    override fun onDestroy() {
        wakeGuard.dispose()
        binding.pager.adapter = null
        session?.let { PdfSession.detach(it) }
        session = null
        super.onDestroy()
    }

    companion object {
        const val EXTRA_URI = "uri"

        fun open(context: android.content.Context, uri: Uri) {
            context.startActivity(
                Intent(context, ReaderActivity::class.java).apply {
                    data = uri
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    putExtra(EXTRA_URI, uri)
                },
            )
        }
    }
}
