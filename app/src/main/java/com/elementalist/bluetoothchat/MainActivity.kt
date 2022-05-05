package com.elementalist.bluetoothchat

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.PendingIntent
import android.bluetooth.*
import android.companion.AssociationRequest
import android.companion.BluetoothDeviceFilter
import android.companion.CompanionDeviceManager
import android.content.*
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Log.INFO
import android.widget.Space
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import com.elementalist.bluetoothchat.ui.theme.BluetoothChatTheme
import java.io.IOException
import java.util.*
import java.util.regex.Pattern

private const val SELECT_DEVICE_REQUEST_CODE = 0
private const val REQUEST_BT_CONNECT = 1


//https://towardsdatascience.com/sending-data-from-a-raspberry-pi-sensor-unit-over-serial-bluetooth-f9063f3447af
//https://github.com/ikolomiyets/bluetooth-test/blob/master/app/src/main/java/io/iktech/demo/bttest/MainActivity.kt
//https://github.com/android/connectivity-samples/tree/master/BluetoothChat

val viewModel by lazy {
    MainViewModel()
}

class MainActivity : ComponentActivity() {

    private val deviceManager: CompanionDeviceManager by lazy {
        getSystemService(Context.COMPANION_DEVICE_SERVICE) as CompanionDeviceManager
    }
    private lateinit var pairedDevice: BluetoothDevice

    //lateinit var bluetoothAdapter: BluetoothAdapter

    //    private val receiver = object: BroadcastReceiver() {
//        @SuppressLint("MissingPermission")
//        override fun onReceive(context: Context?, intent: Intent?) {
//            when(intent?.action){
//                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
//                    val mDevice : BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                    if (mDevice?.bondState == BluetoothDevice.BOND_BONDED) {
//                        //means device paired
//                        Log.i(MY_TAG, "bonded")
//                        viewModel.setPairedDevice(mDevice.name)
//                    }
//                    else if(mDevice?.bondState == BluetoothDevice.BOND_BONDING) {
//                        Log.i(MY_TAG, "bonding")
//                    }
//                }
//            }
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter
        if (!bluetoothAdapter.isEnabled) {
            enableBluetooth()
        }

        val deviceFilter: BluetoothDeviceFilter = BluetoothDeviceFilter.Builder()
            //.addServiceUuid(ParcelUuid(UUID(0x123abcL,-1L)),null)
            //.setNamePattern(Pattern.compile("raspberry"))
            .build()

        val pairingRequest: AssociationRequest = AssociationRequest.Builder()
            .addDeviceFilter(deviceFilter)
            //stop scanning as soon as one device matching the filter is found
            //.setSingleDevice(true)
            .build()

        //when the user tries to pair with a Bluetooth device we show a dialog
        deviceManager.associate(
            pairingRequest,
            object : CompanionDeviceManager.Callback() {
                override fun onDeviceFound(chooserLauncher: IntentSender?) {
                    Log.i(MY_TAG, "Device found")
                    val intentSenderRequest =
                        IntentSenderRequest.Builder(
                            chooserLauncher!!
                        ).build()

                    pairBluetoothDeviceResultLauncher.launch(intentSenderRequest)
                }

                override fun onFailure(error: CharSequence?) {
                    Toast.makeText(
                        applicationContext,
                        "Failure on companion with error: $error",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.i(MY_TAG, "Companion Error: $error")
                }
            }, null
        )

        //AcceptThread().start()

//        val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
//        registerReceiver(receiver,filter)

        setContent {
            BluetoothChatTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    MainScreen(
                        bluetoothAdapter = bluetoothAdapter,
                        pairedDevice = pairedDevice
                    )
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //unregisterReceiver(receiver)
    }

    private fun enableBluetooth() {
        val enableBluetoothIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        enableBluetoothResultLauncher.launch(enableBluetoothIntent)
    }

    @SuppressLint("MissingPermission")
    private val pairBluetoothDeviceResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartIntentSenderForResult()
    ) { activityResult ->
        if (activityResult.resultCode == RESULT_OK) {
            Log.i(MY_TAG, "result code OK")
            val data = activityResult.data
            //the user chose to pair the app with a bluetooth device
            val deviceToPair: BluetoothDevice? =
                data?.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE)
            deviceToPair?.let { device ->
                Log.i(MY_TAG, "Device paired, creating bond ${device.name}")
                device.createBond()
                viewModel.setPairedDevice(device)
                pairedDevice = deviceToPair
                //maintain continuous interaction with a paired device
            }

            Toast.makeText(this, "Device Paired", Toast.LENGTH_SHORT).show()
            Log.i(MY_TAG, "Device paired")
        } else {
            Toast.makeText(this, "Device not paired", Toast.LENGTH_SHORT)
                .show()
            Log.i(MY_TAG, "Device not paired")
        }
    }

    private val enableBluetoothResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Toast.makeText(this, "Bluetooth Enabled!", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "Bluetooth is required for this app to run", Toast.LENGTH_SHORT)
                .show()
            this.finish()
        }
    }


}


@SuppressLint("MissingPermission")
@Composable
fun MainScreen(
    bluetoothAdapter: BluetoothAdapter,
    pairedDevice: BluetoothDevice
) {

    val state = viewModel.state

    Box(modifier = Modifier.fillMaxSize()) {
        if (state.isLoading) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center)
            ) {
                Text(text = "Looking for Bluetooth devices")
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.SpaceEvenly
            ) {
                Button(modifier = Modifier.fillMaxWidth(),
                    onClick = { clientSetUp(pairedDevice) }) {
                    Text(text = "raspberry")
                }
                Button(modifier = Modifier.fillMaxWidth(),
                    onClick = { serverSetUp(bluetoothAdapter) }) {
                    Text(text = "android phone")
                }
                Text(text = "Paired with device: ${state.device?.name}")
                Log.i(MY_TAG, "Device name: ${state.device?.name}")
                Log.i(MY_TAG, "Device uuids: ${state.device?.uuids}")
                Log.i(MY_TAG, "Device address: ${state.device?.address}")
            }
        }

    }
}


fun serverSetUp(bluetoothAdapter: BluetoothAdapter) {
    //initiate the RFCOMM server side
    AcceptThread(bluetoothAdapter = bluetoothAdapter).start()
}

fun clientSetUp(device: BluetoothDevice) {
    ConnectThread(device = device)
}




