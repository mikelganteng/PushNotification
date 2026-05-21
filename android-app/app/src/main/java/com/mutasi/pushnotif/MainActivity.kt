package com.mutasi.pushnotif

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.mutasi.pushnotif.databinding.ActivityMainBinding
import com.mutasi.pushnotif.network.ApiClient
import com.mutasi.pushnotif.service.ForwardService
import com.mutasi.pushnotif.service.NotificationCaptureService
import com.mutasi.pushnotif.ui.NotificationAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var adapter: NotificationAdapter
    private val repo by lazy { (application as MutasiApp).repository }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestNotificationPermission()
        setupRecyclerView()
        loadSettings()
        setupListeners()
    }

    override fun onResume() {
        super.onResume()
        updateStatus()
        refreshList()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1)
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = NotificationAdapter(
            onResend = { notif ->
                ForwardService.resend(this, notif.id)
                Toast.makeText(this, "Mengirim ulang...", Toast.LENGTH_SHORT).show()
                binding.root.postDelayed({ refreshList() }, 1500)
            }
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter
    }

    private fun loadSettings() {
        binding.etServerUrl.setText(repo.getServerUrl())
        binding.etApiKey.setText(repo.getApiKey())
        binding.etFilter.setText(repo.getFilterPackages().joinToString(", "))
        binding.switchEnabled.isChecked = repo.isEnabled()
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            repo.setServerUrl(binding.etServerUrl.text.toString())
            repo.setApiKey(binding.etApiKey.text.toString())
            val filters = binding.etFilter.text.toString()
                .split(",")
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .toSet()
            repo.setFilterPackages(filters)
            repo.setEnabled(binding.switchEnabled.isChecked)
            Toast.makeText(this, "Pengaturan disimpan", Toast.LENGTH_SHORT).show()
            testConnection()
        }

        binding.btnEnableAccess.setOnClickListener {
            NotificationCaptureService.openSettings(this)
        }

        binding.btnRetryAll.setOnClickListener {
            ForwardService.retryAllPending(this)
            Toast.makeText(this, "Mengirim ulang semua yang gagal...", Toast.LENGTH_SHORT).show()
            binding.root.postDelayed({ refreshList() }, 2000)
        }

        binding.btnRefresh.setOnClickListener { refreshList() }
    }

    private fun testConnection() {
        lifecycleScope.launch {
            binding.tvServerStatus.text = "Mengecek server..."
            val ok = withContext(Dispatchers.IO) {
                ApiClient(repo.getServerUrl(), repo.getApiKey(), repo.getDeviceId()).healthCheck()
            }
            binding.tvServerStatus.text = if (ok) "Server: Online ✓" else "Server: Offline ✗"
            binding.tvServerStatus.setTextColor(
                getColor(if (ok) android.R.color.holo_green_dark else android.R.color.holo_red_dark)
            )
        }
    }

    private fun updateStatus() {
        val accessEnabled = NotificationCaptureService.isEnabled(this)
        binding.tvAccessStatus.text = if (accessEnabled) {
            "Akses Notifikasi: Aktif ✓"
        } else {
            "Akses Notifikasi: Nonaktif ✗ (tap tombol di bawah)"
        }
        binding.tvAccessStatus.setTextColor(
            getColor(if (accessEnabled) android.R.color.holo_green_dark else android.R.color.holo_red_dark)
        )
        binding.btnEnableAccess.visibility = if (accessEnabled) View.GONE else View.VISIBLE

        if (accessEnabled) ForwardService.start(this)
        testConnection()
    }

    private fun refreshList() {
        val list = repo.getAllNotifications()
        adapter.submitList(list)
        binding.tvEmpty.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE

        val pending = list.count { it.status == "pending" || it.status == "failed" }
        binding.tvStats.text = "Total: ${list.size} | Pending/Gagal: $pending"
    }
}
