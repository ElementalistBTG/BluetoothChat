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
import com.elementalist.bluetoothchat.*
import com.elementalist.bluetoothchat.ui.theme.BluetoothChatTheme


class ServerActivity : ComponentActivity() {

    private val viewModel by lazy { ServerViewModel() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val bluetoothManager = getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        val bluetoothAdapter = bluetoothManager.adapter

        //We ask permissions and after being granted these permissions we make the device discoverable.
        askPermissions(multiplePermissionLauncher, requiredPermissionsInitialServer, this) {
            makeDiscoverable()
        }
        //After thorough experimentation i concluded that for 2 different Xiaomi phones used for testing
        //we need gps services to be enabled for bluetooth
        //to work as intended so we ask the user to enable GPS also
        if (!isLocationEnabled(this) && Build.VERSION.SDK_INT <= 30) {
            enableLocation(this)
        }

        viewModel.bluetoothAdapter = bluetoothAdapter
        //We set the initial displayed items on screen
        viewModel.changeStateOfServer(StatesOfServer.APP_STARTED)

        setContent {
            BluetoothChatTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colors.background
                ) {
                    ServerScreen(viewModel)
                }
            }
        }

    }

    private val multiplePermissionLauncher =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                if (permissions.containsValue(false)) {
                    //At least one of the permissions was not granted.
                    Toast.makeText(
                        applicationContext,
                        "At least one permission was denied. Please allow all permissions manually and relaunch this app",
                        Toast.LENGTH_LONG
                    ).show()
                    this.finish()
                } else {
                    //All permissions are granted
                    makeDiscoverable()
                }
            }
        } else {
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                Log.i(MY_TAG, "Launcher result: $permissions")
                if (permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)) {
                    //permission for location was granted.
                    //we direct the user to select "Allow all the time option" for devices with SDK 29 or 30
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
        if (result.resultCode == RESULT_OK || //result ok is not working
            result.resultCode == 300 //the result code is the number of seconds we defined!!!!!!!!! (bug probably on specific devices)
        ) {
            Toast.makeText(
                this,
                "Bluetooth Enabled and device made discoverable!",
                Toast.LENGTH_SHORT
            ).show()
        } else {
            Toast.makeText(this, "Bluetooth is required for this app to run", Toast.LENGTH_SHORT)
                .show()
        }
    }

    /**
     * Pop-up activation for enable bluetooth and make device discoverable
     *
     */
    private fun makeDiscoverable() {
        val discoverableIntent: Intent =
            Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE).apply {
                putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300)
            }
        makeDiscoverableResultLauncher.launch(discoverableIntent)
    }


}