package com.elementalist.bluetoothchat.Server

import android.Manifest
import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import com.elementalist.bluetoothchat.MY_TAG
import com.elementalist.bluetoothchat.ui.theme.BluetoothChatTheme


class ServerActivity : ComponentActivity() {
    //lateinit var pairedDevice: BluetoothDevice

    val viewModel by lazy { ServerViewModel() }

    //if the pairing is made from the other phone then we need to listen for pairing success
    private val receiver = object : BroadcastReceiver() {
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context?, intent: Intent?) {
            when (intent?.action) {
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val mDevice: BluetoothDevice? =
                        intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    if (mDevice?.bondState == BluetoothDevice.BOND_BONDED) {
                        //never gets bonded!!!!
                        Log.i(MY_TAG, "bonded")
                        viewModel.addToDisplayState("Device ${mDevice.name} bonded")
                    } else if (mDevice?.bondState == BluetoothDevice.BOND_BONDING) {
                        viewModel.addToDisplayState("Creating bonding with device: ${mDevice.name}")
                        Log.i(MY_TAG, "bonding")
                        //pairedDevice = mDevice
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        val filter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        registerReceiver(receiver, filter)

        askPermissions(multiplePermissionLauncher)
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

    override fun onDestroy() {
        super.onDestroy()
        unregisterReceiver(receiver)
    }

    private val requiredPermissions =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_ADVERTISE,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
        }

    private val multiplePermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { isGranted ->
            Log.i(MY_TAG, "Launcher result: $isGranted");
            if (isGranted.containsValue(false)) {
                Log.i(MY_TAG, "At least one of the permissions was not granted.")
                this.finish()
            } else {
                makeDiscoverable()
            }

        }

    private fun askPermissions(multiplePermissionLauncher: ActivityResultLauncher<Array<String>>) {
        if (!hasPermissions(requiredPermissions)) {
            Log.i(
                MY_TAG,
                "Launching multiple contract permission launcher for ALL required permissions"
            )
            multiplePermissionLauncher.launch(requiredPermissions)
        } else {
            Log.i(MY_TAG, "All permissions are already granted")
            makeDiscoverable()
        }
    }

    private fun hasPermissions(permissions: Array<String>?): Boolean {
        if (permissions != null) {
            for (permission in permissions) {
                if (ActivityCompat.checkSelfPermission(
                        this,
                        permission
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    //Permission is not granted
                    return false
                }
                //Permission already granted
            }
            return true
        }
        return false
    }

    private val makeDiscoverableResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == ComponentActivity.RESULT_OK || //result ok is not working
            result.resultCode == 300 //the result code is the number of seconds we defined!!!!!!!!!
        ) {
            Log.i(MY_TAG,result.toString())
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

    private fun serverSetUp(bluetoothAdapter: BluetoothAdapter) {
        Log.i(MY_TAG, "server set up")
        //initiate the RFCOMM server side
        AcceptThread(bluetoothAdapter = bluetoothAdapter).start()
    }

}


//    private fun initializeAndRequestPermissions() {
//        val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
//            arrayOf(
//                Manifest.permission.BLUETOOTH,
//                Manifest.permission.BLUETOOTH_CONNECT,
//                Manifest.permission.BLUETOOTH_SCAN,
//                Manifest.permission.BLUETOOTH_ADVERTISE,
//                Manifest.permission.ACCESS_FINE_LOCATION,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            )
//        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            Log.i(MY_TAG, "permissions for my android version")
//            arrayOf(
//                Manifest.permission.BLUETOOTH,
//                Manifest.permission.ACCESS_FINE_LOCATION,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            )
//        } else {
//            arrayOf(
//                Manifest.permission.BLUETOOTH,
//                Manifest.permission.ACCESS_FINE_LOCATION,
//                Manifest.permission.ACCESS_COARSE_LOCATION
//            )
//        }
//        val missingPermissions = requiredPermissions.filter { permission ->
//            Log.i(MY_TAG, "permission: $permission is ${checkSelfPermission(permission)}")
//            ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED
//            //checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED
//        }.toTypedArray()
//        if (missingPermissions.isEmpty()) {
//            Log.i(MY_TAG, "missingPermissions.isEmpty")
//            makeDiscoverable()
//        } else {
//
//            // requestPermissionsLauncher.launch(missingPermissions)
////            for (permission in missingPermissions) {
////                Log.i(MY_TAG, "missing: $permission")
////                ActivityCompat.requestPermissions(
////                    this,
////                    arrayOf(permission),
////                    BLUETOOTH_PERMISSION_REQUEST_CODE
////                )
////            }
//            ActivityCompat.requestPermissions(
//                this,
//                missingPermissions,
//                BLUETOOTH_PERMISSION_REQUEST_CODE
//            )
//        }
//    }