package com.dicoding.asclepius.adapter

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.dicoding.asclepius.data.entity.HistoryEntity
import com.dicoding.asclepius.databinding.ItemHistoryBinding
import java.io.File

class HistoryAdapter : RecyclerView.Adapter<HistoryAdapter.ViewHolder>() {

    private var listHistory: List<HistoryEntity> = emptyList()

    fun setHistory(history: List<HistoryEntity>) {
        this.listHistory = history
        notifyDataSetChanged() // Notify adapter of data change
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemHistoryBinding =
            ItemHistoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemHistoryBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(listHistory[position])
    }

    override fun getItemCount(): Int = listHistory.size

    inner class ViewHolder(private val binding: ItemHistoryBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(history: HistoryEntity) {
            with(binding) {
                if (history.image.isNotEmpty()) {
                    val file = File(history.image)

                    val bitmap = BitmapFactory.decodeFile(file.absolutePath)
                    historyIv.setImageBitmap(bitmap)
                }
                historyLabelTv.text = history.label
                historyDateTv.text = history.date
                historyScoreTv.text = String.format("%.2f", history.score * 100)

                // Set click listener
                itemView.setOnLongClickListener {
                    onItemHoldCallback.onItemHold(history)
                    true
                }
            }
        }
    }

    private lateinit var onItemHoldCallback: OnItemHoldCallback

    fun setOnItemHoldCallback(onItemHoldCallback: OnItemHoldCallback) {
        this.onItemHoldCallback = onItemHoldCallback
    }

    fun interface OnItemHoldCallback {
        fun onItemHold(history: HistoryEntity)
    }
}