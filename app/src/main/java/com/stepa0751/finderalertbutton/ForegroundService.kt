package com.stepa0751.finderalertbutton

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import kotlinx.coroutines.*


class ForegroundService : Service() {
    private var startTime: Long = 0L
    private var isServiceStarted = false
    private var notificationManager: NotificationManager? = null
    private var job: Job? = null

    private val builder by lazy {
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Simple Timer")
            .setGroup("Timer")
            .setGroupSummary(false)
            .setDefaults(NotificationCompat.DEFAULT_ALL)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(getPendingIntent())
            .setSilent(true)
            .setSmallIcon(R.drawable.ic_play)
    }

    override fun onCreate() {
        super.onCreate()
        notificationManager =
            applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        processCommand(intent)
        return START_REDELIVER_INTENT
    }

    override fun onBind(intent: Intent): IBinder? {
        return null
    }

    private fun processCommand(intent: Intent?) {
        when (intent?.extras?.getString(COMMAND_ID) ?: INVALID) {
            COMMAND_START -> {

                commandStart()
            }
            COMMAND_STOP -> commandStop()
            INVALID -> return
        }
    }

    private fun commandStart() {
        if (isServiceStarted) {
            return
        }
        Log.i("MyLog", "commandStart()")
        try {
            moveToStartedState()
            startForegroundAndShowNotification()
            continueTimer()
        } finally {
            isServiceStarted = true
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    private fun continueTimer() {
        job = GlobalScope.launch(Dispatchers.Main) {
            while (true) {
                val timeNow = System.currentTimeMillis() - startTime
                notificationManager?.notify(
                    NOTIFICATION_ID,
                    getNotification(
                        (System.currentTimeMillis() - startTime).displayTime().dropLast(3)
                    )
                )
                delay(INTERVAL)
                if (timeNow > 10000) {
                    startTime = System.currentTimeMillis()
                }
            }
        }
    }

    private fun commandStop() {
        if (!isServiceStarted) {
            return
        }
        Log.i("MyLog", "commandStop()")
        try {
            job?.cancel()
            stopForeground(true)
            stopSelf()
        } finally {
            isServiceStarted = false
        }
    }

    private fun moveToStartedState() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.d("MyLog", "moveToStartedState(): Running on Android O or higher")
            startForegroundService(Intent(this, ForegroundService::class.java))
        } else {
            Log.d("MyLog", "moveToStartedState(): Running on Android N or lower")
            startService(Intent(this, ForegroundService::class.java))
        }
    }

    private fun startForegroundAndShowNotification() {
        createChannel()
        val notification = getNotification("content")
        startForeground(NOTIFICATION_ID, notification)
    }

    private fun getNotification(content: String) = builder.setContentText(content).build()


    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelName = "pomodoro"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val notificationChannel = NotificationChannel(
                CHANNEL_ID, channelName, importance
            )
            notificationManager?.createNotificationChannel(notificationChannel)
        }
    }

    private fun getPendingIntent(): PendingIntent? {
        val resultIntent = Intent(this, MainActivity::class.java)
        resultIntent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
        return PendingIntent.getActivity(this, 0, resultIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)
    }

    private companion object {

        private const val CHANNEL_ID = "Channel_ID"
        private const val NOTIFICATION_ID = 777
        private const val INTERVAL = 1000L
    }
}