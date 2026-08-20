package com.pulse.pdf.ui

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pulse.pdf.databinding.ItemPageBinding
import com.pulse.pdf.pdf.PdfDocumentSession

class PageAdapter(
    private val session: PdfDocumentSession,
    private val onInteract: () -> Unit,
    private val onToggleChrome: () -> Unit,
) : RecyclerView.Adapter<PageAdapter.PageVH>() {

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

    inner class PageVH(private val binding: ItemPageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        private var boundPage = -1

        fun bind(page: Int) {
            boundPage = page
            binding.pageImage.setImageBitmap(null)
            binding.pageImage.onInteract = onInteract
            binding.pageImage.onSingleTap = onToggleChrome
            binding.pageProgress.visibility = android.view.View.VISIBLE

            val cached = session.getCached(page)
            if (cached != null) {
                show(page, cached)
            } else {
                session.requestPage(page) { idx, bmp ->
                    if (idx == boundPage) show(idx, bmp)
                }
            }
        }

        private fun show(page: Int, bmp: Bitmap) {
            if (page != boundPage) return
            binding.pageProgress.visibility = android.view.View.GONE
            binding.pageImage.setImageBitmap(bmp)
        }

        fun unbind() {
            boundPage = -1
            binding.pageImage.setImageBitmap(null)
        }
    }
}
