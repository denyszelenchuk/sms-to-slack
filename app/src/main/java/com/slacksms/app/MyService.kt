package com.slacksms.app

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.SystemClock
import android.provider.Telephony
import androidx.core.app.JobIntentService
import androidx.core.app.NotificationCompat
import com.slacksms.app.receivers.SMSReceiver


class MyService : JobIntentService() {

    private var smsBroadcastReceiver: SMSReceiver? = null

    override fun onHandleWork(intent: Intent) {

        this.onCreate()

        smsBroadcastReceiver = SMSReceiver()
        registerReceiver(smsBroadcastReceiver, IntentFilter(Telephony.Sms.Intents.SMS_RECEIVED_ACTION))

        val serviceComponent = ComponentName(applicationContext, MyService::class.java)
        // create and schedule a jobinfo builder
        val builder = JobInfo.Builder(0, serviceComponent)
        builder.setMinimumLatency((1 * 1000).toLong()) // waiting time
        builder.setOverrideDeadline((20 * 1000).toLong()) // max delay
        // device idle true
        builder.setRequiresDeviceIdle(true)
        // works even on charging and discharging
        builder.setRequiresCharging(false)
        val jobScheduler = applicationContext.getSystemService(JobScheduler::class.java)
        jobScheduler.schedule(builder.build())
    }

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= 26) {
            val channelId = "my_channel_01"
            val channel = NotificationChannel(channelId,
                "Channel human readable title",
                NotificationManager.IMPORTANCE_DEFAULT)

            (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager).createNotificationChannel(channel)

            val notification = NotificationCompat.Builder(this, channelId)
                .setContentTitle("")
                .setContentText("").build()

            startForeground(1, notification)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (smsBroadcastReceiver != null) {
            unregisterReceiver(smsBroadcastReceiver)
        }
    }

    override fun onTaskRemoved(rootIntent: Intent) {
        val restartServiceIntent = Intent(applicationContext, this.javaClass)
        restartServiceIntent.setPackage(packageName)

        val restartServicePendingIntent = PendingIntent.getService(applicationContext, 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT)
        val alarmService = applicationContext.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        alarmService.set(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 1000,
            restartServicePendingIntent)

        super.onTaskRemoved(rootIntent)
    }

    companion object {

        private const val JOB_ID = 0x01

        fun enqueueWork(context: Context, work: Intent) {
            enqueueWork(context, MyService::class.java, JOB_ID, work)
        }
    }
}
