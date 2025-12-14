package com.example.ble_scanner.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.ble_scanner.ble.BLEScanner
import com.example.ble_scanner.ble.BleGattClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch


class BleViewModel (
    // dependent on BLE scanner since its necessary
    // to hold data and prepare it to be passed to Screen
    private val scanner: BLEScanner,
    private val gattClient: BleGattClient
): ViewModel(){

    private val _devices = MutableStateFlow<List<BleDeviceUI>>(emptyList())
    val devices: StateFlow<List<BleDeviceUI>> = _devices

    private val _isScanning = MutableStateFlow(false)
    val isScanning: StateFlow<Boolean> = _isScanning

    fun updateDeviceName(address: String, name: String) {
        _devices.value = _devices.value.map { device ->
            if (device.address == address) device.copy(name = name) else device
        }
    }

    fun connect(device: BleDeviceUI){
        device.bluetoothDevice?.let {
            gattClient.connect(it)
        }
    }

    fun disconnectAll(){
        gattClient.disconnect()
    }



    fun startScan(){
        viewModelScope.launch {
            _isScanning.value = true
            scanner.startScan { result ->
                _devices.value = result
            }
        }
    }

    fun stopScan(){
        _isScanning.value = false
        scanner.stopScan()
    }

    fun hasPermissions() = scanner.hasPermissions()

    override fun onCleared() {
        super.onCleared()
        scanner.stopScan()
    }

}