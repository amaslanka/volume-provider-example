package com.example.volumeproviderapp

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.media.VolumeProviderCompat

class VolumeProvider(
    private val context: Context,
    maxVolume: Int,
    currentVolume: Int,
) : VolumeProviderCompat(VOLUME_CONTROL_RELATIVE, maxVolume, currentVolume) {

    override fun onAdjustVolume(direction: Int) {
        Log.d(TAG, "onAdjustVolume, direction: $direction")
        if (direction != 0) {
            Toast.makeText(context, "Received button click event: $direction", Toast.LENGTH_SHORT)
                .show()
        }
    }

    companion object {
        private const val TAG = "VolumeProvider"
    }
}
