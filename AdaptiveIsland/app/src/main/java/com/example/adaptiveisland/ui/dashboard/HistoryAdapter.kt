package com.example.adaptiveisland.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.adaptiveisland.R
import com.example.adaptiveisland.data.history.DailyUsageEntity
import com.example.adaptiveisland.util.TimeFormatter

/**
 * ListAdapter displaying comprehensive historical aggregated device tracking logs.
 */
class HistoryAdapter : ListAdapter<DailyUsageEntity, HistoryAdapter.HistoryViewHolder>(HistoryDiffCallback) {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_daily_history, parent, false)
        return HistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemId(position: Int): Long {
        return getItem(position).date.hashCode().toLong()
    }

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvItemDate: TextView = itemView.findViewById(R.id.tvItemDate)
        private val tvItemTotalTime: TextView = itemView.findViewById(R.id.tvItemTotalTime)

        fun bind(entity: DailyUsageEntity) {
            tvItemDate.text = entity.date
            tvItemTotalTime.text = TimeFormatter.formatElapsedTime(entity.totalScreenTimeMs)
        }
    }

    private object HistoryDiffCallback : DiffUtil.ItemCallback<DailyUsageEntity>() {
        override fun areItemsTheSame(
            oldItem: DailyUsageEntity,
            newItem: DailyUsageEntity
        ): Boolean {
            return oldItem.date == newItem.date
        }

        override fun areContentsTheSame(oldItem: DailyUsageEntity, newItem: DailyUsageEntity): Boolean {
            return oldItem == newItem
        }
    }
}