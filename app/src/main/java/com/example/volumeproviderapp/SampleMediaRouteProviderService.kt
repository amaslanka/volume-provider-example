package com.example.volumeproviderapp

import android.util.Log
import androidx.mediarouter.media.MediaRouteProvider

import androidx.mediarouter.media.MediaRouteProviderService


class SampleMediaRouteProviderService : MediaRouteProviderService() {

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
    }

    override fun onCreateMediaRouteProvider(): MediaRouteProvider {
        Log.d(TAG, "onCreateMediaRouteProvider")
        return SampleMediaRouteProvider(this)
    }

    companion object {
        private const val TAG = "SampleMediaRouteProviderService"
    }
}
