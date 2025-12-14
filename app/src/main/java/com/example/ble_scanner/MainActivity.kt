package com.example.ble_scanner

import android.Manifest
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContract
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.ble_scanner.ble.BLEScanner
import com.example.ble_scanner.ble.BleGattClient
import com.example.ble_scanner.presentation.BleScannerScreen
import com.example.ble_scanner.presentation.BleViewModel
import com.example.ble_scanner.ui.theme.BLEScannerTheme

class MainActivity : ComponentActivity() {

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //  Create BLEScanner
        val bleScanner = BLEScanner(this)

        // Create GATT client (NO ViewModel yet)
        val gattClient = BleGattClient(
            context = this,
            onDeviceNameRead = { _, _ -> } // placeholder
        )

        // Create ViewModel (THIS is where viewModel comes from)
        val viewModel = BleViewModel(
            scanner = bleScanner,
            gattClient = gattClient
        )

        // Wire GATT → ViewModel callback
        gattClient.onDeviceNameRead = { address, name ->
            viewModel.updateDeviceName(address, name)
        }

        //Permissions launcher
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { }

        // Compose UI
        setContent {
            BLEScannerTheme {

                val devices by viewModel.devices.collectAsState()
                val isScanning by viewModel.isScanning.collectAsState()

                BleScannerScreen(
                    devices = devices,
                    isScanning = isScanning,
                    onScanClick = {
                        if (viewModel.hasPermissions()) {
                            viewModel.startScan()
                        } else {
                            permissionLauncher.launch(
                                arrayOf(
                                    Manifest.permission.ACCESS_FINE_LOCATION,
                                    Manifest.permission.BLUETOOTH_SCAN,
                                    Manifest.permission.BLUETOOTH_CONNECT
                                )
                            )
                        }
                    },
                    onStopScanClick = {
                        viewModel.stopScan()
                    },
                    onDeviceClick = { device ->
                        viewModel.connect(device)
                    },
                )
            }
        }
    }
}
