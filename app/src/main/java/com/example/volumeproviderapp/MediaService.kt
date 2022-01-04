package com.example.volumeproviderapp

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat


class MediaService : Service() {

    private val binder = Binder()
    private var mediaSession: MediaSessionCompat? = null
    private var volumeProvider: VolumeProvider? = null

    override fun onBind(p0: Intent?): IBinder =
        binder

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate")
        startForeground()
        isRunning = true
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand")
        startForeground()
        createMediaSession()
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        destroyMediaSession()
        isRunning = false
    }

    private fun startForeground() {
        startForeground(
            NOTIFICATION_ID,
            createForegroundNotification(),
        )
    }

    private fun createForegroundNotification(): Notification {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel()
        }
        return NotificationCompat.Builder(applicationContext, NOTIFICATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentText("Volume Provider Test App")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setShowWhen(false)
            .build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            NOTIFICATION_CHANNEL_ID,
            NOTIFICATION_CHANNEL_NAME,
            NotificationManager.IMPORTANCE_NONE,
        )
        (getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager)?.apply {
            createNotificationChannel(channel)
        }
    }

    private fun createMediaSession() {
        destroyMediaSession()
        mediaSession = MediaSessionCompat(applicationContext, MEDIA_SESSION_TAG)
            .apply {
                setPlaybackState(
                    PlaybackStateCompat.Builder()
                        .setState(
                            PlaybackStateCompat.STATE_PLAYING,
                            PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                            1f,
                        )
                        .build()
                )

                volumeProvider = VolumeProvider(
                    currentVolume = getStreamCurrentVolume(),
                    maxVolume = getStreamMaxVolume(),
                )
                setPlaybackToRemote(volumeProvider)
                isActive = true
            }

        Log.d(TAG, "Created new media session")
    }

    private fun destroyMediaSession() {
        volumeProvider = null
        mediaSession?.apply {
            release()
            Log.d(TAG, "Destroyed media session")
        }
        mediaSession = null
    }

    private fun getStreamCurrentVolume(): Int =
        (getSystemService(Context.AUDIO_SERVICE) as AudioManager)
            .getStreamVolume(AudioManager.STREAM_MUSIC)

    private fun getStreamMaxVolume(): Int =
        (getSystemService(Context.AUDIO_SERVICE) as AudioManager)
            .getStreamMaxVolume(AudioManager.STREAM_MUSIC)

    companion object {
        private const val TAG = "MediaService"
        private const val NOTIFICATION_ID = 999
        private const val NOTIFICATION_CHANNEL_ID = "channel"
        private const val NOTIFICATION_CHANNEL_NAME = "Notifications"
        private const val MEDIA_SESSION_TAG = "VolumeProviderExampleApp"

        var isRunning: Boolean = false
            private set
    }
}