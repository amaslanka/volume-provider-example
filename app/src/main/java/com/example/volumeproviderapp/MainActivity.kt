package com.example.volumeproviderapp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.Menu
import androidx.core.view.MenuItemCompat
import androidx.mediarouter.app.MediaRouteActionProvider
import androidx.mediarouter.media.MediaControlIntent
import androidx.mediarouter.media.MediaRouteSelector
import androidx.mediarouter.media.MediaRouter
import androidx.mediarouter.media.RemotePlaybackClient
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.IllegalArgumentException


class MainActivity : AppCompatActivity() {

    private var isServiceRunning = MediaService.isRunning

    private var mediaRouter: MediaRouter? = null
    private var routeSelector: MediaRouteSelector? = null

    // Variables to hold the currently selected route and its playback client
    private var currentRoute: MediaRouter.RouteInfo? = null
    private var remotePlaybackClient: RemotePlaybackClient? = null

    // Define the Callback object and its methods, save the object in a class variable
    private val mediaRouterCallback = object : MediaRouter.Callback() {

        override fun onRouteSelected(router: MediaRouter, route: MediaRouter.RouteInfo) {
            Log.d(TAG, "onRouteSelected: route=$route")
            if (route.supportsControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)) {
                // Stop local playback (if necessary)
                // ...

                // Save the new route
                currentRoute = route

                // Attach a new playback client
                remotePlaybackClient = RemotePlaybackClient(this@MainActivity, route)

                // Start remote playback (if necessary)
                // ...
            }
            refreshCurrentRouteTextView()
        }

        override fun onRouteUnselected(
            router: MediaRouter,
            route: MediaRouter.RouteInfo,
            reason: Int
        ) {
            Log.d(TAG, "onRouteUnselected: route=$route")
            if (route.supportsControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)) {

                // Changed route: tear down previous client
                currentRoute?.also {
                    remotePlaybackClient?.release()
                    remotePlaybackClient = null
                }

                // Clean the current route
                currentRoute = null

                when (reason) {
                    MediaRouter.UNSELECT_REASON_ROUTE_CHANGED -> {
                        // Resume local playback (if necessary)
                        // ...
                    }
                }
            }
            refreshCurrentRouteTextView()
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            isServiceRunning = true
            refreshButton()
        }

        override fun onServiceDisconnected(p0: ComponentName?) {
            isServiceRunning = false
            refreshButton()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mediaRouter = MediaRouter.getInstance(this)

        // Create a route selector for the type of routes your app supports.
        routeSelector = MediaRouteSelector.Builder()
            // These are the framework-supported intents
            .addControlCategory(MediaControlIntent.CATEGORY_REMOTE_PLAYBACK)
            .addControlCategory(MediaControlIntent.CATEGORY_LIVE_AUDIO)
            .addControlCategory(MediaControlIntent.CATEGORY_LIVE_VIDEO)
            .build()

        refreshButton()
        button.setOnClickListener {
            if (isServiceRunning) {
                destroyMediaService()
            } else {
                startMediaService()
            }
            refreshButton()
        }
    }

    // Use this callback to run your MediaRouteSelector to generate the
    // list of available media routes
    override fun onStart() {
        routeSelector?.also { selector ->
            mediaRouter?.addCallback(selector, mediaRouterCallback,
                MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY)
        }
        refreshCurrentRouteTextView()
        super.onStart()
    }

    // Remove the selector on stop to tell the media router that it no longer
    // needs to discover routes for your app.
    override fun onStop() {
        mediaRouter?.removeCallback(mediaRouterCallback)
        super.onStop()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)

        // Inflate the menu and configure the media router action provider.
        menuInflater.inflate(R.menu.cast_menu, menu)

        // Attach the MediaRouteSelector to the menu item
        val mediaRouteMenuItem = menu.findItem(R.id.media_route_menu_item)
        val mediaRouteActionProvider =
            MenuItemCompat.getActionProvider(mediaRouteMenuItem) as MediaRouteActionProvider
        mediaRouteActionProvider.setAlwaysVisible(true)

        // Attach the MediaRouteSelector that you built in onCreate()
        routeSelector?.also(mediaRouteActionProvider::setRouteSelector)

        // Return true to show the menu.
        return true
    }

    private fun refreshButton() {
        button.text = when (isServiceRunning) {
            true -> "Stop Service"
            false -> "Start Service"
        }
    }

    private fun refreshCurrentRouteTextView() {
        currentRouteTextView.text = currentRoute?.name ?: "No route selected"
    }

    private fun startMediaService() {
        val mediaService = Intent(this, MediaService::class.java)
        bindService(mediaService, serviceConnection, Context.BIND_AUTO_CREATE)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(mediaService)
        } else {
            startService(mediaService)
        }
    }

    private fun destroyMediaService() {
        try {
            unbindService(serviceConnection)
        } catch (ex: IllegalArgumentException) {
            // Ignored
        }
        val mediaService = Intent(this, MediaService::class.java)
        stopService(mediaService)
        isServiceRunning = false
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}
