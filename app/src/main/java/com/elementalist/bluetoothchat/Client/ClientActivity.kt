package com.elementalist.bluetoothchat.Client

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import com.elementalist.bluetoothchat.MY_TAG
import com.elementalist.bluetoothchat.ui.theme.BluetoothChatTheme


class ClientActivity : ComponentActivity() {

    lateinit var bluetoothAdapter: BluetoothAdapter
    lateinit var viewModel: ClientViewModel
    //val viewModel by lazy { ClientViewModel(bluetoothAdapter) }

    //    private val receiver = object : BroadcastReceiver() {
//        @SuppressLint("MissingPermission")
//        override fun onReceive(context: Context?, intent: Intent?) {
//            when (intent?.action) {
//                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
//                    val mDevice: BluetoothDevice? =
//                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                    if (mDevice?.bondState == BluetoothDevice.BOND_BONDED) {
//                        Log.i(MY_TAG, "bonded")
//                        viewModel.addToDisplayState("Device ${mDevice.name} bonded")
//                        clientSetUp(mDevice)
//                    } else if (mDevice?.bondState == BluetoothDevice.BOND_BONDING) {
//                        viewModel.addToDisplayState("Creating bonding with device: ${mDevice.name}")
//                        Log.i(MY_TAG, "bonding")
//                        //pairedDevice = mDevice
//                    }
//                }
//            }
//        }
//    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothAdapter = bluetoothManager.adapter

//        val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
//        registerReceiver(receiver, filter)

        if (!bluetoothAdapter.isEnabled) {
            enableBluetooth()
        }
        //This specific action is required since my personal mobile needs GPS enabled to discover devices (not written in any official documentation)
        if (!isLocationEnabled(this) && Build.VERSION.SDK_INT <= 30) {
            enableLocation()
        }

        viewModel = ClientViewModel(bluetoothAdapter)

        // Register for broadcasts when a device is discovered
        val filter = IntentFilter()
        //register a broadcast receiver to check if the user disables his Bluetooth (or it has it already disabled)
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED)
        //receivers for device discovering
        filter.addAction(BluetoothDevice.ACTION_FOUND)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        registerReceiver(mReceiver, filter)

//        companionModeOn()

        setContent {
            BluetoothChatTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    ClientScreen(viewModel = viewModel)
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    override fun onDestroy() {
        super.onDestroy()

        // Make sure we're not doing discovery anymore
        if (bluetoothAdapter.isDiscovering) {
            bluetoothAdapter.cancelDiscovery()
        }

        // Unregister broadcast listeners
        this.unregisterReceiver(mReceiver)
    }

    private val mReceiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                //when discovery finds a device
                BluetoothDevice.ACTION_FOUND -> {
                    Log.i(MY_TAG, "device found")
                    val device =
                        intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    //if it's already paired skip it
                    if (device != null && device.name != null && device.bondState != BluetoothDevice.BOND_BONDED) {
                        viewModel.addDiscoveredDevice(device)
                    }
                }
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    Log.i(MY_TAG, "ACTION_DISCOVERY_STARTED")
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.i(MY_TAG, "ACTION_DISCOVERY_FINISHED")
                    //if there are no device show proper message
                    if (viewModel.discoveredDevices.isEmpty() && viewModel.pairedDevices.isEmpty()) {
                        Toast.makeText(
                            applicationContext,
                            "Unfortunately no devices were found in your vicinity",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(applicationContext, "Scan finished", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
                BluetoothAdapter.ACTION_STATE_CHANGED -> {
                    if (bluetoothAdapter.state == BluetoothAdapter.STATE_OFF
                    //  || bluetoothAdapter.state == BluetoothAdapter.STATE_TURNING_OFF
                    ) {
                        enableBluetooth()
                    }
                }
            }
        }
    }

    private val enableBluetoothResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Toast.makeText(this, "Bluetooth Enabled!", Toast.LENGTH_SHORT).show()
            //companionModeOn()
        } else {
            Log.i(MY_TAG, result.resultCode.toString())
            Log.i(MY_TAG, result.data.toString())
            Toast.makeText(this, "Bluetooth is required for this app to run", Toast.LENGTH_SHORT)
                .show()
            this.finish()
        }
    }

    private fun enableBluetooth() {
        Toast.makeText(this, "Bluetooth should be enabled for this app to run", Toast.LENGTH_SHORT)
            .show()
        val enableBtIntent = Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE)
        enableBluetoothResultLauncher.launch(enableBtIntent)
    }

    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager =
            context.getSystemService(LOCATION_SERVICE) as LocationManager
        return locationManager.isLocationEnabled
    }

    private fun enableLocation() {
        Toast.makeText(
            this,
            "Location should be enabled for MY XIAOMI PHONE!?!?!?",
            Toast.LENGTH_SHORT
        ).show()
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
        startActivity(intent)
    }

//    private val companionDeviceManager: CompanionDeviceManager by lazy {
//        getSystemService(Context.COMPANION_DEVICE_SERVICE) as CompanionDeviceManager
//    }

//    @SuppressLint("MissingPermission")
//    private val pairBluetoothDeviceResultLauncher = registerForActivityResult(
//        ActivityResultContracts.StartIntentSenderForResult()
//    ) { activityResult ->
//        if (activityResult.resultCode == ComponentActivity.RESULT_OK) {
//            Log.i(MY_TAG, "result code OK")
//            viewModel.addToDisplayState("Device selected. Pairing process begins")
//            val data = activityResult.data
//            //the user chose to pair the app with a bluetooth device
//            val deviceToPair: BluetoothDevice? =
//                data?.getParcelableExtra(CompanionDeviceManager.EXTRA_DEVICE)
//            deviceToPair?.let { device ->
//                device.createBond()
//                viewModel.addToDisplayState("Pairing with device: ${device.name}")
//                //maintain continuous interaction with a paired device
//            }
//        } else {
//            Toast.makeText(this, "Device not paired", Toast.LENGTH_SHORT)
//                .show()
//            Log.i(MY_TAG, "Device not paired")
//        }
//    }


//    private fun companionModeOn() {
//        val deviceFilter: BluetoothDeviceFilter = BluetoothDeviceFilter.Builder()
//            //.addServiceUuid(ParcelUuid(UUID(0x123abcL,-1L)),null)
//            //.setNamePattern(Pattern.compile("raspberry"))
//            .build()
//
//        val pairingRequest: AssociationRequest = AssociationRequest.Builder()
//            .addDeviceFilter(deviceFilter)
//            //stop scanning as soon as one device matching the filter is found
//            //.setSingleDevice(true)
//            .build()
//
//        //when the user tries to pair with a Bluetooth device we show a dialog
//        companionDeviceManager.associate(
//            pairingRequest,
//            object : CompanionDeviceManager.Callback() {
//                override fun onDeviceFound(chooserLauncher: IntentSender?) {
//                    Log.i(MY_TAG, "Device found")
//                    val intentSenderRequest =
//                        IntentSenderRequest.Builder(
//                            chooserLauncher!!
//                        ).build()
//
//                    pairBluetoothDeviceResultLauncher.launch(intentSenderRequest)
//                }
//
//                override fun onFailure(error: CharSequence?) {
//                    Toast.makeText(
//                        applicationContext,
//                        "Failure on companion with error: $error",
//                        Toast.LENGTH_SHORT
//                    ).show()
//                    Log.i(MY_TAG, "Companion Error: $error")
//                }
//            }, null
//        )
//    }


}