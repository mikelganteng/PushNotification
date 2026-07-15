package com.mutasi.pushnotif

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
    
    private val serverOptions = listOf(
        "Localhost (192.168.1.100:3000)",
        "Localhost (192.168.1.1:3000)",
        "Localhost (10.0.2.2:3000) - Emulator",
        "Server Cloud",
        "Custom"
    )
    
    private val serverUrls = mapOf(
        "Localhost (192.168.1.100:3000)" to "http://192.168.1.100:3000",
        "Localhost (192.168.1.1:3000)" to "http://192.168.1.1:3000",
        "Localhost (10.0.2.2:3000) - Emulator" to "http://10.0.2.2:3000",
        "Server Cloud" to "https://your-server.com:3000",
        "Custom" to ""
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        requestNotificationPermission()
        setupRecyclerView()
        setupServerSpinner()
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
    
    private fun setupServerSpinner() {
        val spinnerAdapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_item,
            serverOptions
        )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerServerUrl.adapter = spinnerAdapter
        
        binding.spinnerServerUrl.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                try {
                    val selected = serverOptions[position]
                    if (selected == "Custom") {
                        binding.layoutCustomUrl.visibility = View.VISIBLE
                    } else {
                        binding.layoutCustomUrl.visibility = View.GONE
                        val url = serverUrls[selected] ?: ""
                        if (url.isNotEmpty()) {
                            binding.etServerUrl.setText(url)
                        }
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Do nothing
            }
        }
    }

    private fun loadSettings() {
        try {
            val savedUrl = repo.getServerUrl()
            
            // Cari apakah URL yang tersimpan ada di pilihan predefined
            var selectedPosition = 0
            serverUrls.entries.forEachIndexed { index, entry ->
                if (entry.value == savedUrl) {
                    selectedPosition = index
                    return@forEachIndexed
                }
            }
            
            // Jika tidak ketemu, pilih Custom
            if (selectedPosition == 0 && savedUrl.isNotEmpty() && !serverUrls.containsValue(savedUrl)) {
                selectedPosition = serverOptions.indexOf("Custom")
                binding.etServerUrl.setText(savedUrl)
                binding.layoutCustomUrl.visibility = View.VISIBLE
            }
            
            binding.spinnerServerUrl.setSelection(selectedPosition)
            binding.etApiKey.setText(repo.getApiKey())
            binding.etFilter.setText(repo.getFilterPackages().joinToString(", "))
            binding.switchEnabled.isChecked = repo.isEnabled()
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading settings: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupListeners() {
        binding.btnSave.setOnClickListener {
            try {
                val selectedOption = binding.spinnerServerUrl.selectedItem?.toString() ?: ""
                val serverUrl = if (selectedOption == "Custom") {
                    val customUrl = binding.etServerUrl.text?.toString()?.trim() ?: ""
                    if (customUrl.isEmpty()) {
                        Toast.makeText(this, "URL Custom tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                        return@setOnClickListener
                    }
                    customUrl
                } else {
                    serverUrls[selectedOption] ?: ""
                }
                
                if (serverUrl.isEmpty()) {
                    Toast.makeText(this, "URL Server tidak valid!", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                
                if (!serverUrl.startsWith("http://") && !serverUrl.startsWith("https://")) {
                    Toast.makeText(this, "URL harus dimulai dengan http:// atau https://", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                
                repo.setServerUrl(serverUrl)
                repo.setApiKey(binding.etApiKey.text?.toString()?.trim() ?: "")
                
                val filters = binding.etFilter.text?.toString()?.trim() ?: ""
                val filterSet = filters
                    .split(",")
                    .map { it.trim() }
                    .filter { it.isNotEmpty() }
                    .toSet()
                repo.setFilterPackages(filterSet)
                repo.setEnabled(binding.switchEnabled.isChecked)
                
                Toast.makeText(this, "Pengaturan disimpan: $serverUrl", Toast.LENGTH_SHORT).show()
                testConnection()
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this, "Error menyimpan: ${e.message}", Toast.LENGTH_LONG).show()
            }
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
        
        binding.btnSelectApps.setOnClickListener { showAppSelector() }
    }

    private fun testConnection() {
        lifecycleScope.launch {
            try {
                binding.tvServerStatus.text = "Mengecek server..."
                val serverUrl = repo.getServerUrl()
                
                if (serverUrl.isBlank()) {
                    binding.tvServerStatus.text = "Server: URL kosong"
                    binding.tvServerStatus.setTextColor(getColor(android.R.color.holo_red_dark))
                    return@launch
                }
                
                val ok = withContext(Dispatchers.IO) {
                    try {
                        ApiClient(serverUrl, repo.getApiKey(), repo.getDeviceId()).healthCheck()
                    } catch (e: Exception) {
                        e.printStackTrace()
                        false
                    }
                }
                binding.tvServerStatus.text = if (ok) "Server: Online ✓" else "Server: Offline ✗"
                binding.tvServerStatus.setTextColor(
                    getColor(if (ok) android.R.color.holo_green_dark else android.R.color.holo_red_dark)
                )
            } catch (e: Exception) {
                e.printStackTrace()
                binding.tvServerStatus.text = "Server: Error - ${e.message}"
                binding.tvServerStatus.setTextColor(getColor(android.R.color.holo_red_dark))
            }
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
    
    private fun showAppSelector() {
        lifecycleScope.launch {
            try {
                val installedApps = withContext(Dispatchers.IO) {
                    val pm = packageManager
                    pm.getInstalledApplications(PackageManager.GET_META_DATA)
                        .filter { pm.getLaunchIntentForPackage(it.packageName) != null }
                        .map { 
                            val appName = pm.getApplicationLabel(it).toString()
                            AppItem(it.packageName, appName)
                        }
                        .sortedBy { it.appName }
                }
                
                val currentFilters = repo.getFilterPackages()
                val selectedItems = BooleanArray(installedApps.size) { 
                    currentFilters.contains(installedApps[it].packageName) 
                }
                
                AlertDialog.Builder(this@MainActivity)
                    .setTitle("Pilih Aplikasi untuk Monitor")
                    .setMultiChoiceItems(
                        installedApps.map { it.appName }.toTypedArray(),
                        selectedItems
                    ) { _, which, isChecked ->
                        selectedItems[which] = isChecked
                    }
                    .setPositiveButton("Simpan") { _, _ ->
                        val selected = installedApps.filterIndexed { index, _ -> 
                            selectedItems[index] 
                        }.map { it.packageName }.toSet()
                        
                        repo.setFilterPackages(selected)
                        binding.etFilter.setText(selected.joinToString(", "))
                        Toast.makeText(
                            this@MainActivity, 
                            "Dipilih ${selected.size} aplikasi", 
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    .setNegativeButton("Batal", null)
                    .setNeutralButton("Hapus Semua") { _, _ ->
                        repo.setFilterPackages(emptySet())
                        binding.etFilter.setText("")
                        Toast.makeText(this@MainActivity, "Filter dihapus", Toast.LENGTH_SHORT).show()
                    }
                    .show()
            } catch (e: Exception) {
                Toast.makeText(this@MainActivity, "Error: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private data class AppItem(val packageName: String, val appName: String)
}
