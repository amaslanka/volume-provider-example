package com.example.volumeproviderapp

import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.util.Log
import androidx.mediarouter.media.MediaControlIntent
import androidx.mediarouter.media.MediaRouteDescriptor
import androidx.mediarouter.media.MediaRouteProvider
import androidx.mediarouter.media.MediaRouteProviderDescriptor
import androidx.mediarouter.media.MediaRouter
import androidx.mediarouter.media.MediaRouter.ControlRequestCallback


class SampleMediaRouteProvider(context: Context) : MediaRouteProvider(context) {

    private var volume: Int = 5

    init {
        publishRoutes()
    }

    private fun publishRoutes() {
        // Create a route descriptor using previously created IntentFilters
        val routeDescriptor: MediaRouteDescriptor =
            MediaRouteDescriptor.Builder(VARIABLE_VOLUME_BASIC_ROUTE_ID, EXAMPLE_ROUTE_NAME)
                .setDescription(EXAMPLE_ROUTE_DESCRIPTION)
                .addControlFilters(CONTROL_FILTERS_BASIC)
                .setPlaybackStream(AudioManager.STREAM_MUSIC)
                .setPlaybackType(MediaRouter.RouteInfo.PLAYBACK_TYPE_REMOTE)
                .setVolumeHandling(MediaRouter.RouteInfo.PLAYBACK_VOLUME_VARIABLE)
                .setVolumeMax(VOLUME_MAX)
                .setVolume(volume)
                .build()

        // Add the route descriptor to the provider descriptor
        val providerDescriptor: MediaRouteProviderDescriptor =
            MediaRouteProviderDescriptor.Builder()
                .addRoute(routeDescriptor)
                .build()

        // Publish the descriptor to the framework
        descriptor = providerDescriptor
    }

    override fun onCreateRouteController(routeId: String): RouteController {
        return SampleRouteController(routeId)
    }

    private class SampleRouteController(routeId: String) : MediaRouteProvider.RouteController() {

        init {
            Log.d(TAG, "Controller created $routeId")
        }

        override fun onRelease() {
            super.onRelease()
            Log.d(TAG, "Controller released")
        }

        override fun onSelect() {
            super.onSelect()
            Log.d(TAG, "Controller selected")
        }

        override fun onUnselect(reason: Int) {
            super.onUnselect(reason)
            Log.d(TAG, "Controller unselected: $reason")
        }

        override fun onSetVolume(volume: Int) {
            super.onSetVolume(volume)
            Log.d(TAG, "Controller set volume: $volume")
        }

        override fun onUpdateVolume(delta: Int) {
            super.onUpdateVolume(delta)
            Log.d(TAG, "Controller update volume: $delta")
        }

        override fun onControlRequest(
            intent: Intent,
            callback: ControlRequestCallback?
        ): Boolean {
            Log.d(TAG, "Controller onControlRequest: $intent")
            return true
        }
    }

    companion object {
        private const val TAG = "SampleMediaRouteProvider"

        private const val VARIABLE_VOLUME_BASIC_ROUTE_ID = "demo_cast"
        private const val EXAMPLE_ROUTE_NAME = "Demo Cast"
        private const val EXAMPLE_ROUTE_DESCRIPTION = "Example Route Description"
        private const val VOLUME_MAX = 10

        private const val CATEGORY_SAMPLE_ROUTE =
            "com.example.volumeproviderapp.CATEGORY_SAMPLE_ROUTE"

        private const val ACTION_GET_STATISTICS =
            "com.example.volumeproviderapp.ACTION_GET_STATISTICS"

        private fun IntentFilter.addDataTypeUnchecked(type: String) {
            try {
                addDataType(type)
            } catch (ex: IntentFilter.MalformedMimeTypeException) {
                throw RuntimeException(ex)
            }
        }

        private val CONTROL_FILTERS_BASIC: ArrayList<IntentFilter> = run {
            val getStats = IntentFilter().apply {
                addCategory(CATEGORY_SAMPLE_ROUTE);
                addAction(ACTION_GET_STATISTICS);
            }
            val videoPlayback: IntentFilter = IntentFilter().apply {
                addCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)
                addAction(MediaControlIntent.ACTION_PLAY)
                addDataScheme("http")
                addDataScheme("https")
                addDataScheme("rtsp")
                addDataTypeUnchecked("video/*")
                arrayListOf(this)
            }

            val playControls = IntentFilter().apply {
                addCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)
                addAction(MediaControlIntent.ACTION_SEEK)
                addAction(MediaControlIntent.ACTION_GET_STATUS)
                addAction(MediaControlIntent.ACTION_PAUSE)
                addAction(MediaControlIntent.ACTION_RESUME)
                addAction(MediaControlIntent.ACTION_STOP)
            }

            val f4 = IntentFilter().apply {
                addCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)
                addAction(MediaControlIntent.ACTION_ENQUEUE)
                addDataScheme("http")
                addDataScheme("https")
                addDataScheme("rtsp")
                addDataScheme("file")
                addDataTypeUnchecked("video/*")
            }

            val f5 = IntentFilter()
            f5.addCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)
            f5.addAction(MediaControlIntent.ACTION_REMOVE)

            val f6 = IntentFilter()
            f6.addCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)
            f6.addAction(MediaControlIntent.ACTION_START_SESSION)
            f6.addAction(MediaControlIntent.ACTION_GET_SESSION_STATUS)
            f6.addAction(MediaControlIntent.ACTION_END_SESSION)

            arrayListOf(
                getStats,
                videoPlayback,
                playControls,
                f4,
                f5,
                f6,
            )
        }
    }
}
