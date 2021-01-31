package com.slacksms.app.channels

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import com.slacksms.app.R
import com.slacksms.app.data.AppDatabase
import com.slacksms.app.data.channels.Channel
import com.slacksms.app.data.channels.ChannelsRepository
import com.slacksms.app.executors.AppExecutors


class AddChannelActivity : AppCompatActivity(), View.OnClickListener, ChannelView {


    private lateinit var channelNameEditText: EditText
    private lateinit var channelWebhookEditText: EditText
    private lateinit var saveChannelButton: Button
    private lateinit var appDb: AppDatabase
    private lateinit var repository: ChannelsRepository
    private lateinit var channel: Channel
    private lateinit var channelPresenter: ChannelPresenter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_channel)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        channelNameEditText = findViewById(R.id.channel_name)
        channelWebhookEditText = findViewById(R.id.channel_webhook)
        saveChannelButton = findViewById(R.id.save_channel_button)

        appDb = AppDatabase.getInstance(this)
        repository = ChannelsRepository(AppExecutors(), appDb)
        channelPresenter = ChannelPresenter(repository, this)

        channelNameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                enableSaveIfReady()
            }
        })

        channelWebhookEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                enableSaveIfReady()
            }
        })

        saveChannelButton.setOnClickListener(this)
    }

    fun enableSaveIfReady() {
        val wasNameProvided = channelNameEditText.text.toString().length > 2
        val wasWebhookProvided = channelWebhookEditText.text.toString().length > 2
        saveChannelButton.isEnabled = wasNameProvided && wasWebhookProvided
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.save_channel_button -> {
                channel = Channel(channelNameEditText.text.toString(),
                    channelWebhookEditText.text.toString())
                repository.saveChannel(channel, channelPresenter.createChannelCallback())
                val mainActivityIntent = Intent(this, ChannelsActivity::class.java)
                NavUtils.navigateUpTo(this, mainActivityIntent)
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {

                val mainActivityIntent = Intent(this, ChannelsActivity::class.java)

                // Respond to the action bar's Up/Home button
                NavUtils.navigateUpTo(this, mainActivityIntent)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun showChannel(channel: Channel) {
        //do nothing
    }

    override fun hideChannel() {
        //do nothing
    }
}
