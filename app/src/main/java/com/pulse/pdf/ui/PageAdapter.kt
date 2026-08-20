package com.pulse.pdf.ui

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.pulse.pdf.databinding.ItemPageBinding
import com.pulse.pdf.pdf.PdfDocumentSession

class PageAdapter(
    private val session: PdfDocumentSession,
    private val listWidthPx: Int,
    private val onInteract: () -> Unit,
    private val onToggleChrome: () -> Unit,
) : RecyclerView.Adapter<PageAdapter.PageVH>() {

    /** Uniform height from page-0 seed — keeps 1000-page lists scrollable instantly. */
    private val pageHeightPx: Int = session.pageHeightForWidth(listWidthPx)

    init {
        setHasStableIds(true)
    }

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getItemCount(): Int = session.pageCount

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PageVH {
        val binding = ItemPageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PageVH(binding)
    }

    override fun onBindViewHolder(holder: PageVH, position: Int) {
        holder.bind(position)
    }

    override fun onViewRecycled(holder: PageVH) {
        holder.unbind()
        super.onViewRecycled(holder)
    }

    override fun onViewAttachedToWindow(holder: PageVH) {
        super.onViewAttachedToWindow(holder)
        // Re-request if recycled before render finished
        holder.ensureLoaded()
    }

    inner class PageVH(private val binding: ItemPageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var boundPage = -1

        fun bind(page: Int) {
            boundPage = page
            binding.pageImage.layoutParams = binding.pageImage.layoutParams.apply {
                width = ViewGroup.LayoutParams.MATCH_PARENT
                height = pageHeightPx
            }
            binding.root.layoutParams = binding.root.layoutParams.apply {
                height = pageHeightPx + binding.root.paddingBottom
            }
            binding.pageImage.scaleType = ImageView.ScaleType.FIT_CENTER

            binding.root.setOnClickListener {
                onInteract()
                onToggleChrome()
            }
            binding.pageImage.setOnClickListener {
                onInteract()
                onToggleChrome()
            }

            val cached = session.getCached(page)
            if (cached != null) {
                show(page, cached)
            } else {
                binding.pageImage.setImageBitmap(null)
                binding.pageProgress.visibility = android.view.View.VISIBLE
                session.requestPage(page) { idx, bmp ->
                    if (idx == boundPage) show(idx, bmp)
                }
            }
        }

        fun ensureLoaded() {
            val page = boundPage
            if (page < 0) return
            if (session.getCached(page) != null) return
            session.requestPage(page) { idx, bmp ->
                if (idx == boundPage) show(idx, bmp)
            }
        }

        private fun show(page: Int, bmp: Bitmap) {
            if (page != boundPage) return
            if (bmp.isRecycled) {
                binding.pageProgress.visibility = android.view.View.VISIBLE
                session.requestPage(page) { idx, again ->
                    if (idx == boundPage && !again.isRecycled) show(idx, again)
                }
                return
            }
            binding.pageProgress.visibility = android.view.View.GONE
            binding.pageImage.setImageBitmap(bmp)
        }

        fun unbind() {
            boundPage = -1
            binding.pageImage.setImageBitmap(null)
            binding.root.setOnClickListener(null)
            binding.pageImage.setOnClickListener(null)
        }
    }
}
