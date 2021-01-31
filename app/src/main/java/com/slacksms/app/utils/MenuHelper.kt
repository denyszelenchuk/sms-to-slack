package com.slacksms.app.utils

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.view.MenuItem
import com.slacksms.app.R
import com.slacksms.app.channels.ChannelsActivity
import com.slacksms.app.donate.DonateActivity
import com.slacksms.app.privacy.PrivacyWebViewActivity
import com.slacksms.app.rules.RulesActivity

object MenuHelper {

    fun selectMenuItem(item: MenuItem, activeMenuItem: Int, activity: Activity) {
        when (item.itemId) {
            R.id.nav_channels -> {
                if (activeMenuItem != R.id.nav_channels) {
                    val intent = Intent(activity, ChannelsActivity::class.java)
                    activity.startActivity(intent)
                    activity.finish()
                }
            }
            R.id.nav_rules -> {
                if (activeMenuItem != R.id.nav_rules) {
                    val intent = Intent(activity, RulesActivity::class.java)
                    activity.startActivity(intent)
                }
            }
            R.id.nav_donate -> {
                if (activeMenuItem != R.id.nav_donate) {
                    val intent = Intent(activity, DonateActivity::class.java)
                    activity.startActivity(intent)
                }
            }
            R.id.nav_privacy -> {
                if (activeMenuItem != R.id.nav_privacy) {
                    val intent = Intent(activity, PrivacyWebViewActivity::class.java)
                    activity.startActivity(intent)
                }
            }
            R.id.nav_share -> {
                val shareIntent = Intent(Intent.ACTION_SEND)
                shareIntent.type = "text/plain"
                shareIntent.putExtra(
                    Intent.EXTRA_TEXT,
                    activity.resources.getString(R.string.share_application_message)
                )
                activity.startActivity(shareIntent)
            }
        }
    }
}