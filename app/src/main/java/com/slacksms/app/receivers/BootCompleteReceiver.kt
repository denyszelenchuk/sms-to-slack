package com.slacksms.app.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.slacksms.app.MyService

class BootCompleteReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_BOOT_COMPLETED == intent.action) {
            MyService.enqueueWork(context, Intent())
        }
    }
}
