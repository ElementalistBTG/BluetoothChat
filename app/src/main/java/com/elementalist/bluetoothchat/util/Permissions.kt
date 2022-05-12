package com.elementalist.bluetoothchat

import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.core.app.ActivityCompat

fun askPermissions(
    multiplePermissionLauncher: ActivityResultLauncher<Array<String>>,
    requiredPermissions: Array<String>,
    context: Context,
    actionIfAlreadyGranted: () -> Unit
) {
    if (!hasPermissions(requiredPermissions, context)) {
        Log.i(
            MY_TAG,
            "Launching multiple contract permission launcher for ALL required permissions"
        )
        multiplePermissionLauncher.launch(requiredPermissions)
    } else {
        Log.i(MY_TAG, "All permissions are already granted")
        actionIfAlreadyGranted()
    }
}

fun askSinglePermission(
    singlePermissionLauncher: ActivityResultLauncher<String>,
    permission: String,
    context: Context,
    actionIfAlreadyGranted: () -> Unit
) {
    if (!hasSinglePermission(permission, context)) {
        Log.i(
            MY_TAG,
            "Launching contract permission launcher for the required permissions"
        )
        singlePermissionLauncher.launch(permission)
    } else {
        Log.i(MY_TAG, "Permission: $permission is already granted")
        actionIfAlreadyGranted()
    }
}

fun hasPermissions(permissions: Array<String>?, context: Context): Boolean {
    if (permissions != null) {
        for (permission in permissions) {
            if (ActivityCompat.checkSelfPermission(
                    context,
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

fun hasSinglePermission(permission: String, context: Context): Boolean {
    return ActivityCompat.checkSelfPermission(
        context,
        permission
    ) == PackageManager.PERMISSION_GRANTED
}

