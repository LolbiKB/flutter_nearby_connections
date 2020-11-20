package com.nankai.flutter_nearby_connections

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.nearby.Nearby
import com.google.android.gms.nearby.connection.*

const val NOTIFICATION_ID = 101
const val CHANNEL_ID = "channel"

class NearbyService : Service() {
    private val binder: IBinder = LocalBinder(this)
    private lateinit var callbackUtils: CallbackUtils
    private lateinit var connectionsClient: ConnectionsClient

    override fun onCreate() {
        super.onCreate()
        startForeground(NOTIFICATION_ID, getNotification())
    }

    fun initService(callbackUtils : CallbackUtils){
        connectionsClient =  Nearby.getConnectionsClient(this)
        this@NearbyService.callbackUtils = callbackUtils
    }

    override fun onBind(intent: Intent?): IBinder? {

        return binder
    }

    fun sendStringPayload(endpointId: String ,str: String) {
         connectionsClient.sendPayload(endpointId, Payload.fromBytes(str.toByteArray()))
    }

    fun startAdvertising(strategy: Strategy, deviceName: String) {
        Log.d(TAG, "startAdvertising()")
         connectionsClient.startAdvertising(
                 deviceName, SERVICE_ID, callbackUtils.connectionLifecycleCallback,
                AdvertisingOptions.Builder().setStrategy(strategy).build())
    }

    fun startDiscovery(strategy: Strategy) {
        Log.d(TAG, "startDiscovery()")
         connectionsClient.startDiscovery(
                SERVICE_ID, callbackUtils.endpointDiscoveryCallback,
                DiscoveryOptions.Builder().setStrategy(strategy).build())
    }

    fun stopDiscovery() {
         connectionsClient.stopDiscovery()
    }

    fun stopAdvertising() {
         connectionsClient.stopAdvertising()
    }

    fun disconnect(endpointId: String) {
         connectionsClient.disconnectFromEndpoint(endpointId)
    }

    fun connect(endpointId: String, displayName: String) {
         connectionsClient.requestConnection(displayName, endpointId, callbackUtils.connectionLifecycleCallback)
    }

    override fun onDestroy() {
        super.onDestroy()
        stopAdvertising()
        stopDiscovery()
         connectionsClient.stopAllEndpoints()
    }

    private fun getNotification(): Notification? {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val serviceChannel = NotificationChannel(
                    CHANNEL_ID, "Foreground Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            )
            val manager = getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(serviceChannel)
        }
        return NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Nearby Service")
                .setContentText("Wi-Fi Direct")
                .setSmallIcon(android.R.drawable.stat_notify_sync)
                .build()
    }
}

internal class LocalBinder(private val nearbyService: NearbyService) : Binder() {
    val service: NearbyService
        get() = nearbyService
}