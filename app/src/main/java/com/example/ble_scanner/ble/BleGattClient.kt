package com.example.ble_scanner.ble

import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothGatt
import android.bluetooth.BluetoothGattCallback
import android.bluetooth.BluetoothGattCharacteristic
import android.bluetooth.BluetoothProfile
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import java.util.UUID

class BleGattClient(
    private val context: Context,
    var onDeviceNameRead: (address: String, name: String) -> Unit
) {

    private val DEVICE_NAME_UUID =
        UUID.fromString("00002A00-0000-1000-8000-00805F9B34FB")

    private val GENERIC_ACCESS_SERVICE_UUID =
        UUID.fromString("00001800-0000-1000-8000-00805f9b34fb")


    private var gatt: BluetoothGatt? = null

    fun connect(device: BluetoothDevice){
        disconnect() // disconnect previous connection before connecting to anothe device
        gatt = device.connectGatt(context, false, gattCallback)
    }

    fun disconnect(){
        gatt?.disconnect()
        gatt?.close()
        gatt = null
    }

    private val gattCallback = object: BluetoothGattCallback(){
        override fun onConnectionStateChange(
            gatt: BluetoothGatt,
            status:Int,
            newState:Int
        ){
            if(newState == BluetoothProfile.STATE_CONNECTED){
                gatt.discoverServices()
            }
        }

        override fun onServicesDiscovered(
            gat: BluetoothGatt,
            status:Int
        ){
            val genericAccess = gatt?.getService(GENERIC_ACCESS_SERVICE_UUID)
            val nameCharacteristic = genericAccess?.getCharacteristic(DEVICE_NAME_UUID)
            nameCharacteristic?.let { gatt?.readCharacteristic(it) }
        }

        override fun onCharacteristicRead(
            gatt: BluetoothGatt,
            characteristic: BluetoothGattCharacteristic,
            status: Int
        ) {
            if (status == BluetoothGatt.GATT_SUCCESS && characteristic.uuid == DEVICE_NAME_UUID) {
                val name = characteristic.value?.toString(Charsets.UTF_8) ?: "Unknown"
                Log.d("BLE_GATT", "Device name = $name")
                Handler(Looper.getMainLooper()).post {
                    onDeviceNameRead(gatt.device.address, name)
                }
            }
        }
    }
}