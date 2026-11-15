package com.example.data.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.data.BrowserRepository
import com.example.data.blocker.AppBlockerEngine
import com.example.data.local.BrowserDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

/**
 * Background and Foreground Service that continuously tracks active usage times
 * for blacklisted apps and website packages, updating Room database metrics in real-time.
 */
class FocusUsageMonitorService : Service() {

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)
    private lateinit var repository: BrowserRepository
    private lateinit var wakeLock: PowerManager.WakeLock

    override fun onCreate() {
        super.onCreate()
        val database = BrowserDatabase.getInstance(applicationContext)
        repository = BrowserRepository(database.browserDao())
        createNotificationChannel()
        acquireWakeLock()
    }

    private fun acquireWakeLock() {
        val pm = getSystemService(Context.POWER_SERVICE) as PowerManager
        wakeLock = pm.newWakeLock(
            PowerManager.PARTIAL_WAKE_LOCK,
            "SwordfishFocusMonitor:WakeLock"
        )
        wakeLock.acquire(10 * 60 * 1000) // hold for up to 10 minutes; re-acquired each loop iteration
    }

    private fun releaseWakeLock() {
        wakeLock.takeIf { it.isHeld }?.release()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val action = intent?.action

        if (action == ACTION_STOP) {
            stopMonitoring()
            stopSelf()
            return START_NOT_STICKY
        }

        val focusMinutes = intent?.getIntExtra(EXTRA_FOCUS_MINUTES, 25) ?: 25
        startForeground(NOTIFICATION_ID, buildNotification(focusMinutes * 60))
        _isServiceRunning.value = true

        startMonitoringLoop(focusMinutes)

        return START_STICKY
    }

    private fun startMonitoringLoop(focusMinutes: Int) {
        serviceScope.launch {
            var remainingSeconds = focusMinutes * 60
            while (isActive && _isServiceRunning.value) {
                // Keep the CPU awake while monitoring so background tracking doesn't pause
                if (!wakeLock.isHeld) acquireWakeLock()
                delay(1000)

                // Update session time
                _currentSessionActiveSeconds.value += 1
                if (remainingSeconds > 0) {
                    remainingSeconds--
                }

                // If a blacklisted target is currently being used, record active usage in Room (batched every 5s)
                val currentTarget = _currentActiveTarget.value
                if (!currentTarget.isNullOrBlank() && _currentSessionActiveSeconds.value % 5 == 0L) {
                    repository.recordActiveUsage(currentTarget, 5)
                }

                // Update notification text periodically
                if (_currentSessionActiveSeconds.value % 5 == 0L) {
                    val notification = buildNotification(remainingSeconds)
                    val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                    manager.notify(NOTIFICATION_ID, notification)
                }
            }
        }
    }

    private fun stopMonitoring() {
        _isServiceRunning.value = false
        _currentActiveTarget.value = null
        serviceJob.cancel()
        releaseWakeLock()
    }

    override fun onDestroy() {
        stopMonitoring()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun buildNotification(remainingSeconds: Int): Notification {
        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingOpen = PendingIntent.getActivity(
            this,
            0,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val stopIntent = Intent(this, FocusUsageMonitorService::class.java).apply {
            action = ACTION_STOP
        }
        val pendingStop = PendingIntent.getService(
            this,
            1,
            stopIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val timeString = AppBlockerEngine.formatSecondsToTime(remainingSeconds)

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Swordfish Focus Shield Active")
            .setContentText("Monitoring usage times • $timeString remaining")
            .setSmallIcon(android.R.drawable.ic_lock_idle_lock)
            .setOngoing(true)
            .setContentIntent(pendingOpen)
            .addAction(android.R.drawable.ic_media_pause, "Stop Shield", pendingStop)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Swordfish Distraction Monitor",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Monitors active usage times and enforces focus shielding"
                setShowBadge(false)
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    companion object {
        const val CHANNEL_ID = "swordfish_focus_channel"
        const val NOTIFICATION_ID = 2001

        const val ACTION_START = "com.example.action.START_MONITOR"
        const val ACTION_STOP = "com.example.action.STOP_MONITOR"
        const val EXTRA_FOCUS_MINUTES = "extra_focus_minutes"

        private val _isServiceRunning = MutableStateFlow(false)
        val isServiceRunning: StateFlow<Boolean> = _isServiceRunning.asStateFlow()

        private val _currentSessionActiveSeconds = MutableStateFlow(0L)
        val currentSessionActiveSeconds: StateFlow<Long> = _currentSessionActiveSeconds.asStateFlow()

        private val _currentActiveTarget = MutableStateFlow<String?>(null)
        val currentActiveTarget: StateFlow<String?> = _currentActiveTarget.asStateFlow()

        fun start(context: Context, minutes: Int = 25) {
            val intent = Intent(context, FocusUsageMonitorService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_FOCUS_MINUTES, minutes)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, FocusUsageMonitorService::class.java).apply {
                action = ACTION_STOP
            }
            context.startService(intent)
        }

        fun setActiveTarget(target: String?) {
            _currentActiveTarget.value = target
        }
    }

    /**
     * Restarts focus monitoring after the device reboots so background tracking is continuous.
     */
    class FocusBootReceiver : android.content.BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
                FocusUsageMonitorService.start(context, 25)
            }
        }
    }
}
