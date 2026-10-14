package com.spy.app

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat

class SpyService : Service() {

    private var camera: CameraController? = null
    private var server: HttpServer? = null

    override fun onCreate() {
        super.onCreate()
        startForeground(1, notificacion())
        camera = CameraController(this).also { it.open() }
        server = HttpServer(8080, this, camera!!).also { it.start() }
    }

    private fun notificacion(): Notification {
        val canalId = "spy"
        if (Build.VERSION.SDK_INT >= 26) {
            val canal = NotificationChannel(canalId, "Spy", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(canal)
        }
        return NotificationCompat.Builder(this, canalId)
            .setContentTitle("SpyApp activo")
            .setSmallIcon(android.R.drawable.ic_menu_camera)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
