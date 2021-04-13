package com.demo.hms.locationdemo.kotlin

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.demo.hms.locationdemo.R

class TrackingService : Service(),GPS.OnGPSEventListener {

    companion object{
        private const val SERVICE_NOTIFICATION_ID=1
        private const val LOCATION_NOTIFICATION_ID=2
        private const val CHANNEL_ID="Location Service"
        private const val ACTION_START="START"
        private const val ACTION_STOP="STOP"
        private const val TRACKING_INTERVAL="Interval"


        public fun startService(context: Context, trackingInterval:Long=1000){
            val intent=Intent(context,TrackingService::class.java).apply {
                action= ACTION_START
                putExtra(TRACKING_INTERVAL,trackingInterval)
            }
            context.startService(intent)
        }

        public fun stopService(context: Context){
            val intent=Intent(context,TrackingService::class.java).apply {
                action= ACTION_STOP
            }
            context.startService(intent)
        }
    }

    private var gps:GPS?=null
    private var isStarted=false

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        intent?.run {
            when(action){
                ACTION_START->{
                    if(!isStarted){
                        startForeground()
                        val interval=getLongExtra(TRACKING_INTERVAL,1000)
                        startLocationRequests(interval)
                        isStarted=true
                    }
                }

                ACTION_STOP ->{
                    if(isStarted){
                        gps?.removeLocationUpdates()
                        stopForeground(true)
                        isStarted=false
                    }
                }
            }
        }
        return START_STICKY
    }

    private fun startForeground(){
        createNotificationChannel()
        val builder=NotificationCompat.Builder(this,CHANNEL_ID)
        builder.setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.channel_name))
            .setContentText(getString(R.string.service_running))
            .setAutoCancel(false)
        startForeground(SERVICE_NOTIFICATION_ID,builder.build())
    }

    private fun startLocationRequests(interval:Long){
        gps=GPS(this).apply {
            startLocationRequest(interval)
            listener=this@TrackingService
        }
    }

    private fun publishLocation(lat: Double, lon: Double){
        val builder=NotificationCompat.Builder(this,CHANNEL_ID)
        builder.setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(getString(R.string.channel_name))
            .setContentText("Location Update Lat:$lat Lon:$lon")
        val notificationManager=getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(LOCATION_NOTIFICATION_ID,builder.build())
    }

    private fun createNotificationChannel(){
        // Create the NotificationChannel
        val name = getString(R.string.channel_name)
        val descriptionText = getString(R.string.channel_description)
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
        mChannel.description = descriptionText
        // Register the channel with the system; you can't change the importance
        // or other notification behaviors after this
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(mChannel)
    }

    override fun onResolutionRequired(e: Exception) {

    }

    override fun onLocationUpdate(lat: Double, lon: Double) {
        publishLocation(lat,lon)
    }


}