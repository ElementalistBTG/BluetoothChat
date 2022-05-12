package com.elementalist.bluetoothchat.Server

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.Context
import android.content.Intent
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
import com.elementalist.bluetoothchat.askPermissions
import com.elementalist.bluetoothchat.askSinglePermission
import com.elementalist.bluetoothchat.requiredPermissionsInitialServer
import com.elementalist.bluetoothchat.ui.theme.BluetoothChatTheme


class ServerActivity : ComponentActivity() {

    private val viewModel by lazy { ServerViewModel() }

//    //if the pairing is made from the other phone then we need to listen for pairing success
//    private val receiver = object : BroadcastReceiver() {
//        @SuppressLint("MissingPermission")
//        override fun onReceive(context: Context?, intent: Intent?) {
//            when (intent?.action) {
//                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
//                    val mDevice: BluetoothDevice? =
//                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                    if (mDevice?.bondState == BluetoothDevice.BOND_BONDED) {
//                        //never gets bonded!!!!
//                        Log.i(MY_TAG, "bonded")
//                        viewModel.addToDisplayState("Device ${mDevice.name} bonded")
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
        val bluetoothAdapter = bluetoothManager.adapter

//        val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
//        registerReceiver(receiver, filter)

        askPermissions(multiplePermissionLauncher, requiredPermissionsInitialServer, this) {
            makeDiscoverable()
        }

        if (!isLocationEnabled(this) && Build.VERSION.SDK_INT <= 30) {
            enableLocation()
        }

        serverSetUp(bluetoothAdapter)

        setContent {
            BluetoothChatTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    ServerScreen(viewModel)
                }
            }
        }

    }

//    override fun onDestroy() {
//        super.onDestroy()
//        unregisterReceiver(receiver)
//    }


    private val multiplePermissionLauncher =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                Log.i(MY_TAG, "Launcher result: $permissions")
                if (permissions.containsValue(false)) {
                    Log.i(MY_TAG, "At least one of the permissions was not granted.")
                    this.finish()
                } else {
                    makeDiscoverable()
                }
            }
        } else {
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                Log.i(MY_TAG, "Launcher result: $permissions")
                if (permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)) {
                    //permission for location was granted.
                    //we direct the user to select "Allow all the time option
                    allowLocationAllTheTime()
                } else {
                    Toast.makeText(
                        this,
                        "Location permission was not granted. Please do so manually",
                        Toast.LENGTH_SHORT
                    ).show()
                    this.finish()
                }
            }
        }

    private val locationPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission Accepted: Do something
                makeDiscoverable()
            } else {
                // Permission Denied: Do something
                Log.i(MY_TAG, "Permission Denied")
                Toast.makeText(
                    this,
                    "You should really select the option 'Allow all the time' for location in order for this app to work!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    private fun allowLocationAllTheTime() {
        askSinglePermission(
            locationPermissionLauncher,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            this
        ) {
            makeDiscoverable()
        }
    }

    private val makeDiscoverableResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == ComponentActivity.RESULT_OK || //result ok is not working
            result.resultCode == 300 //the result code is the number of seconds we defined!!!!!!!!!
        ) {
            Log.i(MY_TAG, result.toString())
            Toast.makeText(this, "Bluetooth Enabled and visible!", Toast.LENGTH_SHORT).show()
            viewModel.addToDisplayState("Device made discoverable to other devices.")
            //serverSetUp(bluetoothAdapter)
        } else {
            Toast.makeText(this, "Bluetooth is required for this app to run", Toast.LENGTH_SHORT)
                .show()
        }
    }

    private fun makeDiscoverable() {
        val discoverableIntent: Intent =
            Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
            }
        Log.i(MY_TAG, "enableBluetoothAndMakeDiscoverable")
        makeDiscoverableResultLauncher.launch(discoverableIntent)
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

    private fun serverSetUp(bluetoothAdapter: BluetoothAdapter) {
        viewModel.addToDisplayState("Server set up")
        Log.i(MY_TAG, "server set up")
        //initiate the RFCOMM server side
        AcceptThread(bluetoothAdapter, viewModel).start()
    }

}