package com.example.volumeproviderapp

import android.util.Log
import androidx.media.VolumeProviderCompat

class VolumeProvider(
    maxVolume: Int,
    currentVolume: Int,
) : VolumeProviderCompat(VOLUME_CONTROL_RELATIVE, maxVolume, currentVolume) {

    override fun onAdjustVolume(direction: Int) {
        Log.d(TAG, "onAdjustVolume, direction: $direction")
    }

    companion object {
        private const val TAG = "VolumeProvider"
    }
}
