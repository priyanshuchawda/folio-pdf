package com.pulse.pdf.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pulse.pdf.R
import com.pulse.pdf.databinding.ActivityLibraryBinding
import com.pulse.pdf.databinding.ItemRecentBinding
import com.pulse.pdf.util.RecentDoc
import com.pulse.pdf.util.RecentStore
import java.io.File

class LibraryActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLibraryBinding
    private lateinit var store: RecentStore

    private val openPdf = registerForActivityResult(
        ActivityResultContracts.OpenDocument(),
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION,
                )
            } catch (_: SecurityException) {
            }
            ReaderActivity.open(this, uri)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLibraryBinding.inflate(layoutInflater)
        setContentView(binding.root)

        store = RecentStore(this)
        binding.toolbar.title = getString(R.string.app_name)
        binding.btnOpen.setOnClickListener {
            openPdf.launch(arrayOf("application/pdf"))
        }
        binding.btnSample.setOnClickListener { openBundledSample() }
        binding.recycler.layoutManager = LinearLayoutManager(this)
        refresh()
    }

    private fun openBundledSample() {
        val out = File(cacheDir, "sample.pdf")
        if (!out.exists() || out.length() == 0L) {
            assets.open("sample.pdf").use { input ->
                out.outputStream().use { input.copyTo(it) }
            }
        }
        val uri = FileProvider.getUriForFile(this, "$packageName.files", out)
        ReaderActivity.open(this, uri)
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val items = store.list()
        binding.empty.visibility =
            if (items.isEmpty()) android.view.View.VISIBLE else android.view.View.GONE
        binding.recycler.adapter = RecentAdapter(items) { doc ->
            ReaderActivity.open(this, Uri.parse(doc.uri))
        }
    }

    private class RecentAdapter(
        private val items: List<RecentDoc>,
        private val onClick: (RecentDoc) -> Unit,
    ) : RecyclerView.Adapter<RecentAdapter.VH>() {

        override fun getItemCount() = items.size

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
            val b = ItemRecentBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return VH(b)
        }

        override fun onBindViewHolder(holder: VH, position: Int) {
            holder.bind(items[position])
        }

        inner class VH(private val b: ItemRecentBinding) : RecyclerView.ViewHolder(b.root) {
            fun bind(doc: RecentDoc) {
                b.title.text = doc.name
                b.subtitle.text = b.root.context.getString(R.string.page_resume, doc.lastPage + 1)
                b.root.setOnClickListener { onClick(doc) }
            }
        }
    }
}
