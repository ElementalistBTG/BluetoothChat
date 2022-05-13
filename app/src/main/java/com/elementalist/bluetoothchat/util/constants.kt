package com.elementalist.bluetoothchat

import android.Manifest
import android.os.Build
import java.util.*

//For a RFComm connection to exist both devices must use a name and a uuid to communicate. We hardcode these into our app
const val connectionName = "con"
val myUuid: UUID = UUID.fromString("12345678-abcd-abcd-abcd-1234567890ab")
//tag used for logging purposes
const val MY_TAG = "mytag"
//Since the permissions needed for this app are fixed we define them here
val requiredPermissionsInitialClient =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            Manifest.permission.BLUETOOTH_SCAN,
            //Manifest.permission.BLUETOOTH_ADVERTISE
        )
    } else {
        arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

val requiredPermissionsInitialServer =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        arrayOf(
            Manifest.permission.BLUETOOTH_CONNECT,
            //Manifest.permission.BLUETOOTH_SCAN,
            Manifest.permission.BLUETOOTH_ADVERTISE
        )
    } else {
        arrayOf(
            Manifest.permission.BLUETOOTH,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }