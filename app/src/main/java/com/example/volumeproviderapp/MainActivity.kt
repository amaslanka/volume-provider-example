package com.example.volumeproviderapp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.IllegalArgumentException


class MainActivity : AppCompatActivity() {

    private var isServiceRunning = MediaService.isRunning

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

    private fun refreshButton() {
        button.text = when (isServiceRunning) {
            true -> "Stop Service"
            false -> "Start Service"
        }
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
