package com.example.ble_scanner.presentation

import android.bluetooth.BluetoothDevice
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun BleScannerScreen(
    devices:List<BleDeviceUI>,
    isScanning: Boolean,
    onScanClick:() -> Unit,
    onStopScanClick:() ->Unit,
  //  onDisconnectAll:() -> Unit,
    onDeviceClick:(BleDeviceUI) -> Unit
){
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .navigationBarsPadding() // set ideal padding for phone built in nav bar
            .statusBarsPadding() // set ideal padding for phone built in phone status bar
    ) {
       Text(
           modifier = Modifier.padding(top = 16.dp),
           text ="BLE Scanner",
           fontSize = 30.sp,
           fontStyle = FontStyle.Italic,
           fontWeight = FontWeight.SemiBold
       )

        Button(
            onClick = onScanClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding( horizontal = 16.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text(if (isScanning) "Scanning..." else "Start Scan")
        }

        Button(
            onClick = onStopScanClick,
            modifier = Modifier
                .fillMaxWidth()
                .padding( horizontal = 16.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Text("Stop Scan")
        }

        Spacer(Modifier.height(16.dp))

        LazyColumn() {
            items(devices, key = {it.address}){ device ->
                DeviceRow(device, onClick = {onDeviceClick(device)})

            }
        }

    }

}

@Composable
fun DeviceRow(
    device: BleDeviceUI,
    onClick:() -> Unit
){

    val displayName =
        if (device.name.isBlank() || device.name == "Unknown")
            "Unnamed BLE Device"
        else
            device.name

    Card(
        modifier = Modifier
            .clickable{onClick()}
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = CardDefaults.cardElevation(4.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = displayName,
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = "Address: ${device.address}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "RSSI: ${device.rssi} dBm",
                style = MaterialTheme.typography.bodySmall
            )

            Text(
                text = if (device.isConnectable) "🔗 Connectable" else "📡 Advertiser only",
                style = MaterialTheme.typography.bodySmall
            )


            device.manufacturer?.let {
                Text(
                    text = "Manufacturer: $it",
                    style = MaterialTheme.typography.bodySmall
                )
            }
            if (device.serviceUuids.isNotEmpty()) {
                Text(
                    text = "Services: ${device.serviceUuids.joinToString(", ")}",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}


@Preview(showBackground = true)
@Composable
fun BleScannerScreenPreview() {
    // Create a list of sample devices
    val sampleDevices = listOf(
        BleDeviceUI(
            name = "Phone",
            address = "AA:BB:CC:DD:EE:01",
            rssi = -45,
            isConnectable = true,
            manufacturer = "Google",
            serviceUuids = listOf("Heart Rate", "Battery"),
            bluetoothDevice = null
        ),
        BleDeviceUI(
            name = "Earbuds",
            address = "AA:BB:CC:DD:EE:02",
            rssi = -70,
            isConnectable = true,
            manufacturer = "Apple",
            serviceUuids = listOf("Battery"),
            bluetoothDevice =  null
        ),
        BleDeviceUI(
            name = "TV",
            address = "AA:BB:CC:DD:EE:03",
            rssi = -80,
            isConnectable = false,
            manufacturer = "Samsung",
            serviceUuids = emptyList(),
            bluetoothDevice = null
        )
    )

    // Preview the screen
    BleScannerScreen(
        devices = sampleDevices,
        isScanning = false,
        onScanClick = {},
        onDeviceClick = {},
        onStopScanClick = {},
       // onDisconnectAll = {}
    )
}


data class BleDeviceUI(
    val name: String,
    val address: String,
    val rssi: Int,
    val isConnectable: Boolean,
    val manufacturer: String?,
    val serviceUuids: List<String>,
    val bluetoothDevice: BluetoothDevice?
)