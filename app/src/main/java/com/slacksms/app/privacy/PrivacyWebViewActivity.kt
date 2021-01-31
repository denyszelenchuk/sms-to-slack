package com.slacksms.app.privacy

import android.content.Intent
import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import com.slacksms.app.R
import com.slacksms.app.channels.ChannelsActivity

class PrivacyWebViewActivity : AppCompatActivity() {

    private lateinit var webView: WebView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_privacy_web_view)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        webView = findViewById(R.id.web_view)
        webView.loadUrl(PRIVACY_FORM_URL)
    }

    override fun onBackPressed() {
        val mainActivityIntent = Intent(this, ChannelsActivity::class.java)
        NavUtils.navigateUpTo(this, mainActivityIntent)
    }

    companion object {
        const val PRIVACY_FORM_URL = "file:///android_asset/privacy_policy.html"
    }
}
