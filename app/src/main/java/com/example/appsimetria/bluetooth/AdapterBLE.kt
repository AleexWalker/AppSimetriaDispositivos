package com.example.appsimetria.bluetooth

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.appsimetria.databinding.ActivityAdapterBleBinding
import timber.log.Timber
import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanResult
import android.bluetooth.le.ScanSettings
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SimpleItemAnimator
import com.example.appsimetria.BuildConfig
import com.example.appsimetria.ServicesMenu
import com.example.appsimetria.bluetooth.adapter.CharacteristicAdapter
import com.example.appsimetria.bluetooth.adapter.ScanResultAdapter
import com.example.appsimetria.bluetooth.connections.ConnectionEventListener
import com.example.appsimetria.bluetooth.connections.ConnectionManager
import org.jetbrains.anko.alert

private const val ENABLE_BLUETOOTH_REQUEST_CODE = 1
private const val LOCATION_PERMISSION_REQUEST_CODE = 2

class AdapterBLE : AppCompatActivity() {

    companion object {
        val instance = AdapterBLE()
    }

    private lateinit var binding: ActivityAdapterBleBinding

    /*******************************************
     * Properties
     *******************************************/
    private val bluetoothAdapter: BluetoothAdapter by lazy {
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    private val bleScanner by lazy {
        bluetoothAdapter.bluetoothLeScanner
    }

    private val scanSettings = ScanSettings.Builder()
        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
        .build()

    private var isScanning = false
        set(value) {
            field = value
            runOnUiThread { binding.scanButton.text = if (value) "Stop Scan" else "Start Scan" }
        }

    private val scanResults = mutableListOf<ScanResult>()
    private val scanResultAdapter: ScanResultAdapter by lazy {
        ScanResultAdapter(scanResults) { result ->

            binding.progressBarBLE.visibility = View.VISIBLE
            //binding.recyclerListBLE.isClickable = false
            //binding.recyclerListBLE.isFocusable = false

            if (isScanning) {
                stopBleScan()
            }
            with(result.device) {
                Timber.w("Connecting to $address")
                Toast.makeText(this@AdapterBLE, "Conectando a dispositivo ${result.device.address}", Toast.LENGTH_LONG).show()
                ConnectionManager.connect(this, this@AdapterBLE, binding.progressBarBLE)
            }
        }
    }

    private val isLocationPermissionGranted
        get() = hasPermission(Manifest.permission.ACCESS_FINE_LOCATION)

    /*******************************************
     * Activity function overrides
     *******************************************/

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdapterBleBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        binding.scanButton.setOnClickListener {
            if (isScanning)
                stopBleScan()
            else
                startBleScan()
        }
        setupRecyclerView()

        binding.imagenAtras.setOnClickListener {
            startActivity(Intent(this, ServicesMenu::class.java))
        }

    }

    override fun onResume() {
        super.onResume()
        ConnectionManager.registerListener(connectionEventListener)
        if (!bluetoothAdapter.isEnabled) {
            promptEnableBluetooth()
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            ENABLE_BLUETOOTH_REQUEST_CODE -> {
                if (resultCode != Activity.RESULT_OK) {
                    promptEnableBluetooth()
                }
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            LOCATION_PERMISSION_REQUEST_CODE -> {
                if (grantResults.firstOrNull() == PackageManager.PERMISSION_DENIED) {
                    requestLocationPermission()
                } else {
                    startBleScan()
                }
            }
        }
    }

    /*******************************************
     * Private functions
     *******************************************/

    @SuppressLint("MissingPermission")
    private fun promptEnableBluetooth() {
        if (!bluetoothAdapter.isEnabled) {
            val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
            startActivityForResult(enableBtIntent, ENABLE_BLUETOOTH_REQUEST_CODE)
        }
    }

    @SuppressLint("MissingPermission", "NotifyDataSetChanged")
    private fun startBleScan() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !isLocationPermissionGranted) {
            requestLocationPermission()
        } else {
            scanResults.clear()
            scanResultAdapter.notifyDataSetChanged()
            bleScanner.startScan(null, scanSettings, scanCallback)
            isScanning = true
        }
    }

    @SuppressLint("MissingPermission")
    private fun stopBleScan() {
        bleScanner.stopScan(scanCallback)
        isScanning = false
    }

    private fun requestLocationPermission() {
        if (isLocationPermissionGranted) {
            return
        }
        runOnUiThread {
            alert {
                title = "Location permission required"
                message = "Starting from Android M (6.0), the system requires apps to be granted " +
                        "location access in order to scan for BLE devices."
                isCancelable = false
                positiveButton(android.R.string.ok) {
                    requestPermission(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        LOCATION_PERMISSION_REQUEST_CODE
                    )
                }
            }.show()
        }
    }

    private fun setupRecyclerView() {
        binding.recyclerListBLE.apply {
            adapter = scanResultAdapter
            layoutManager = LinearLayoutManager(
                this@AdapterBLE,
                RecyclerView.VERTICAL,
                false
            )
            isNestedScrollingEnabled = false
        }

        val animator = binding.recyclerListBLE.itemAnimator
        if (animator is SimpleItemAnimator) {
            animator.supportsChangeAnimations = false
        }
    }

    /*******************************************
     * Callback bodies
     *******************************************/

    private val scanCallback = object : ScanCallback() {
        @SuppressLint("MissingPermission")
        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val indexQuery = scanResults.indexOfFirst { it.device.address == result.device.address }
            if (indexQuery != -1) { // A scan result already exists with the same address
                scanResults[indexQuery] = result
                scanResultAdapter.notifyItemChanged(indexQuery)
            } else {
                with(result.device) {
                    Timber.i("Found BLE device! Name: ${name ?: "Unnamed"}, address: $address")
                }
                scanResults.add(result)
                scanResultAdapter.notifyItemInserted(scanResults.size - 1)
            }
        }

        override fun onScanFailed(errorCode: Int) {
            Timber.e("onScanFailed: code $errorCode")
        }
    }

    private val connectionEventListener by lazy {
        ConnectionEventListener().apply {
            onConnectionSetupComplete = { gatt ->
                Intent(this@AdapterBLE, OperationsBLE::class.java).also {
                    it.putExtra(BluetoothDevice.EXTRA_DEVICE, gatt.device)
                    startActivity(it)
                }
                ConnectionManager.unregisterListener(this)

                /**
                val characteristics by lazy {
                    ConnectionManager.servicesOnDevice(gatt.device)?.flatMap { service ->
                        service.characteristics ?: listOf()
                    } ?: listOf()
                }
                CharacteristicAdapter(characteristics) { characteristic ->
                    ConnectionManager.writeCharacteristic(gatt.device, characteristic, "123456".toByteArray())
                }
                */
            }
            onDisconnect = {
                runOnUiThread {
                    alert {
                        title = "Disconnected"
                        message = "Disconnected or unable to connect to device."
                        positiveButton("OK") {}
                    }.show()
                }
            }
        }
    }

    /*******************************************
     * Extension functions
     *******************************************/

    private fun Context.hasPermission(permissionType: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permissionType) ==
                PackageManager.PERMISSION_GRANTED
    }

    private fun Activity.requestPermission(permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
    }
}