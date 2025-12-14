package com.example.ble_scanner.ble

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.le.BluetoothLeScanner
import android.bluetooth.le.ScanCallback
import android.bluetooth.le.ScanRecord
import android.bluetooth.le.ScanResult
import android.content.Context
import android.content.pm.PackageManager
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import com.example.ble_scanner.presentation.BleDeviceUI


class BLEScanner(
    private val context: Context
) {

    // Android Bluetooth system services
    private val bluetoothManager =
        context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager

    private val bluetoothAdapter: BluetoothAdapter = bluetoothManager.adapter
    private val bleScanner: BluetoothLeScanner = bluetoothAdapter.bluetoothLeScanner

    // Used to stop scan after timeout
    private val handler = Handler(Looper.getMainLooper())
    private val scanPeriod = 10_000L

    // Holds devices found during THIS scan
    // Key = MAC address (unique per device)
    private val foundDevices = mutableMapOf<String, BleDeviceUI>()
    private var scanning = false

    // Permissions required for BLE scanning
    private val requiredPermissions = arrayOf(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.BLUETOOTH_SCAN,
        Manifest.permission.BLUETOOTH_CONNECT
    )

    // Callback used to push results to ViewModel
    private var onResultListener: ((List<BleDeviceUI>) -> Unit)? = null

    fun hasPermissions(): Boolean =
        requiredPermissions.all {
            ActivityCompat.checkSelfPermission(context, it) ==
                    PackageManager.PERMISSION_GRANTED
        }

    // SINGLE ScanCallback instance (VERY IMPORTANT)
    private val scanCallback = object : ScanCallback() {

        override fun onScanResult(callbackType: Int, result: ScanResult) {
            val device = result.device
            val record = result.scanRecord
            val address = device.address   // never null

            // Create UI model from raw ScanResult
            val uiDevice = BleDeviceUI(
                name = device.name ?:record?.deviceName?: "BLE Device Unknown",
                address = address,
                rssi = result.rssi,
                isConnectable = result.isConnectable,
                manufacturer = parseManufacturer(record),
                serviceUuids = parseServiceUuids(record),
                bluetoothDevice = device,
            )

            // Store/update device (RSSI updates over time)
            foundDevices[address] = uiDevice

            //  Push snapshot to UI
            onResultListener?.invoke(foundDevices.values.toList())

            Log.d(
                "BLE_SCAN",
                "Found ${uiDevice.name} (${uiDevice.address}) RSSI=${uiDevice.rssi}"
            )
        }
    }

    // Public API: start scanning
    fun startScan(onResult: (List<BleDeviceUI>) -> Unit) {
        if (scanning) return
        if (!hasPermissions()) return

        foundDevices.clear()
        onResultListener = onResult
        scanning = true

        Log.d("BLE_SCAN", "Starting BLE scan")

        // ACTUALLY start scanning
        bleScanner.startScan(scanCallback)

        // Stop scan after timeout
        handler.postDelayed({
            stopScan()
        }, scanPeriod)
    }

    // Stop scan safely
    fun stopScan() {
        if (!scanning) return

        scanning = false
        bleScanner.stopScan(scanCallback)

        Log.d("BLE_SCAN", "Scan stopped")
    }

    // Manufacturer parsing helper
    private fun parseManufacturer(record: ScanRecord?): String? {
        val data = record?.manufacturerSpecificData ?: return null
        if (data.size() == 0) return null

        return when (data.keyAt(0)) {
            0x004C -> "Apple"
            0x0131 -> "Google"
            0x0059 -> "Nordic Semiconductor"
            0x038F -> "Xiaomi"
            0x0CC2 -> "Anker Innovations Limited"
            else -> "Unknown (0x${data.keyAt(0).toString(16)})"
        }
    }
}

private fun parseServiceUuids(record: ScanRecord?): List<String> {
    return record?.serviceUuids
        ?.map { uuid ->
            when (uuid.uuid.toString().lowercase()) {
                "0000180d-0000-1000-8000-00805f9b34fb" -> "Heart Rate"
                "0000180f-0000-1000-8000-00805f9b34fb" -> "Battery"
                "00001809-0000-1000-8000-00805f9b34fb" -> "Temperature"
                else -> uuid.uuid.toString()
            }
        }
        ?: emptyList()
}
