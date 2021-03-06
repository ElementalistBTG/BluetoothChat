package com.elementalist.bluetoothchat.Client

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Button
import androidx.compose.material.Divider
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.elementalist.bluetoothchat.MY_TAG
import com.elementalist.bluetoothchat.askPermissions
import com.elementalist.bluetoothchat.askSinglePermission
import com.elementalist.bluetoothchat.requiredPermissionsInitialClient


@SuppressLint("MissingPermission")
@Composable
fun ClientScreen(viewModel: ClientViewModel) {

    val context = LocalContext.current

    val locationPermissionLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                // Permission Accepted: Do something
                viewModel.scanForDevices()
            } else {
                // Permission Denied: Do something
                Log.i(MY_TAG, "Permission Denied")
                Toast.makeText(
                    context,
                    "You must manually select the option 'Allow all the time' for location in order for this app to work!",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    /**
     * Requesting the Manifest.permission.ACCESS_BACKGROUND_LOCATION permission after
     * the Manifest.permission.ACCESS_COARSE_LOCATION has been granted
     *
     */
    fun extraLocationPermissionRequest() {
        askSinglePermission(
            locationPermissionLauncher,
            Manifest.permission.ACCESS_BACKGROUND_LOCATION,
            context
        ) {
            viewModel.scanForDevices()
        }
    }

    val multiplePermissionLauncher =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                Log.i(MY_TAG, "Launcher result: $permissions")
                if (permissions.containsValue(false)) {
                    Log.i(MY_TAG, "At least one of the permissions was not granted.")
                    Toast.makeText(
                        context,
                        "At least one of the permissions was not granted. Go to app settings and give permissions manually",
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    //do something
                    viewModel.scanForDevices()
                }
            }
        } else {
            rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
                Log.i(MY_TAG, "Launcher result: $permissions")
                if (permissions.getOrDefault(Manifest.permission.ACCESS_COARSE_LOCATION, false)) {
                    //permission for location was granted.
                    //we direct the user to select "Allow all the time option"
                    Toast.makeText(
                        context,
                        "You must select the option 'Allow all the time' for the app to work",
                        Toast.LENGTH_SHORT
                    ).show()
                    extraLocationPermissionRequest()
                } else {
                    Toast.makeText(
                        context,
                        "Location permission was not granted. Please do so manually",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

    val pairedDevices = viewModel.pairedDevices
    val discoveredDevices = viewModel.discoveredDevices
    val selectedDevice = viewModel.selectedDevice

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {

        Button(onClick = {
            //check for permissions and launch scanning after they are granted
            askPermissions(
                multiplePermissionLauncher,
                requiredPermissionsInitialClient,
                context
            ) { viewModel.scanForDevices() }
        }) {
            Text(text = "Scan for devices")
        }

        Spacer(modifier = Modifier.padding(5.dp))

        if(pairedDevices.isNotEmpty()){
            Text(
                text = "Paired Devices",
                modifier = Modifier.fillMaxWidth().padding(2.dp).border(3.dp, MaterialTheme.colors.primaryVariant),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.padding(3.dp))

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(pairedDevices) { device ->
                ListedDeviceItem(
                    deviceName = device.name
                ) {
                    viewModel.selectDevice(device)
                    Log.i(MY_TAG, viewModel.selectedDevice?.name.toString())
                }
                Divider(Modifier.padding(3.dp), color = Color.Green)
            }
        }

        Spacer(modifier = Modifier.padding(5.dp))

        if(discoveredDevices.isNotEmpty()){
            Text(
                text = "Discovered Devices",
                modifier = Modifier.fillMaxWidth().padding(2.dp).border(3.dp, MaterialTheme.colors.primaryVariant),
                textAlign = TextAlign.Center
            )
        }

        Spacer(modifier = Modifier.padding(3.dp))

        LazyColumn(modifier = Modifier.fillMaxWidth()) {
            items(discoveredDevices) { device ->
                ListedDeviceItem(
                    deviceName = device.name
                ) {
                    viewModel.selectDevice(device)
                    Log.i(MY_TAG, viewModel.selectedDevice?.name.toString())
                }
                Divider(Modifier.padding(3.dp), color = Color.Green)
            }
        }

        if (selectedDevice != null) {
            Button(onClick = { viewModel.sendDataToDevice() }) {
                Text(text = "Send Data to ${selectedDevice.name}")
            }
        }
    }
}






