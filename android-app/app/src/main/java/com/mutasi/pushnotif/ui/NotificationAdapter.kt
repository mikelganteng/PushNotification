package com.mutasi.pushnotif.ui

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.mutasi.pushnotif.data.CapturedNotification
import com.mutasi.pushnotif.databinding.ItemNotificationBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotificationAdapter(
    private val onResend: (CapturedNotification) -> Unit
) : ListAdapter<CapturedNotification, NotificationAdapter.VH>(DIFF) {

    private val dateFmt = SimpleDateFormat("dd/MM/yy HH:mm:ss", Locale("id"))

    inner class VH(private val binding: ItemNotificationBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CapturedNotification) {
            binding.tvTitle.text = item.title.ifEmpty { "(Tanpa judul)" }
            binding.tvApp.text = item.appName
            binding.tvBody.text = item.bigText.ifEmpty { item.body }
            binding.tvTime.text = dateFmt.format(Date(item.postedAt))

            val (statusText, color) = when (item.status) {
                "sent" -> "Terkirim ✓" to Color.parseColor("#22c55e")
                "failed" -> "Gagal ✗" to Color.parseColor("#ef4444")
                "pending" -> "Pending..." to Color.parseColor("#f59e0b")
                else -> item.status to Color.GRAY
            }
            binding.tvStatus.text = statusText
            binding.tvStatus.setTextColor(color)

            binding.btnResend.setOnClickListener { onResend(item) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VH(binding)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(getItem(position))

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<CapturedNotification>() {
            override fun areItemsTheSame(a: CapturedNotification, b: CapturedNotification) = a.id == b.id
            override fun areContentsTheSame(a: CapturedNotification, b: CapturedNotification) = a == b
        }
    }
}
