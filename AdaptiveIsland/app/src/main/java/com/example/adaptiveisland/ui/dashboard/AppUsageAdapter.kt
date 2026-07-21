package com.example.adaptiveisland.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.adaptiveisland.R
import com.example.adaptiveisland.data.history.AppUsageEntity
import com.example.adaptiveisland.util.TimeFormatter

/**
 * ListAdapter populating per-application metrics using stable primary keys hashes.
 */
class AppUsageAdapter : ListAdapter<AppUsageEntity, AppUsageAdapter.AppViewHolder>(AppDiffCallback) {

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AppViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_app_usage, parent, false)
        return AppViewHolder(view)
    }

    override fun onBindViewHolder(holder: AppViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun getItemId(position: Int): Long {
        val item = getItem(position)
        return "${item.date}_${item.packageName}".hashCode().toLong()
    }

    class AppViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvItemAppName: TextView = itemView.findViewById(R.id.tvItemAppName)
        private val tvItemAppTime: TextView = itemView.findViewById(R.id.tvItemAppTime)

        fun bind(entity: AppUsageEntity) {
            tvItemAppName.text = entity.appName
            tvItemAppTime.text = TimeFormatter.formatElapsedTime(entity.totalTimeMs)
        }
    }

    private object AppDiffCallback : DiffUtil.ItemCallback<AppUsageEntity>() {
        override fun areItemsTheSame(oldItem: AppUsageEntity, newItem: AppUsageEntity): Boolean {
            return oldItem.date == newItem.date && oldItem.packageName == newItem.packageName
        }

        override fun areContentsTheSame(oldItem: AppUsageEntity, newItem: AppUsageEntity): Boolean {
            return oldItem == newItem
        }
    }
}