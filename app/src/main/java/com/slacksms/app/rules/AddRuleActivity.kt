package com.slacksms.app.rules

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemSelectedListener
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import com.slacksms.app.MyService
import com.slacksms.app.R
import com.slacksms.app.channels.ChannelPresenter
import com.slacksms.app.channels.ChannelView
import com.slacksms.app.channels.ChannelsPresenter
import com.slacksms.app.channels.ChannelsView
import com.slacksms.app.data.AppDatabase
import com.slacksms.app.data.channels.Channel
import com.slacksms.app.data.channels.ChannelsRepository
import com.slacksms.app.data.rules.Rule
import com.slacksms.app.data.rules.RulesRepository
import com.slacksms.app.executors.AppExecutors
import com.slacksms.app.utils.NotificationsHelper.sendDonateNotification
import com.slacksms.app.utils.SharedPreferencesHelper


class AddRuleActivity : AppCompatActivity(), View.OnClickListener, ChannelsView, ChannelView, RulesView {

    private lateinit var appDb: AppDatabase
    private lateinit var rulesRepository: RulesRepository
    private lateinit var channelsRepository: ChannelsRepository
    private lateinit var mRule: Rule
    private lateinit var mChannel: Channel
    private lateinit var rulesPresenter: RulesPresenter
    private lateinit var channelsPresenter: ChannelsPresenter
    private lateinit var channelPresenter: ChannelPresenter
    private lateinit var ruleNameEditText: EditText
    private lateinit var ruleChannelsSpinner: Spinner
    private lateinit var rulesSenderEditText: EditText
    private lateinit var rulesPhoneNumber: EditText
    private lateinit var saveRuleButton: Button
    private lateinit var arrayAdapter: ArrayAdapter<Channel?>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_rule)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        ruleNameEditText = findViewById(R.id.rule_name)
        ruleChannelsSpinner = findViewById(R.id.rule_channels_spinner)
        rulesSenderEditText = findViewById(R.id.rule_sender_name)
        rulesPhoneNumber = findViewById(R.id.rule_phone_number)
        saveRuleButton = findViewById(R.id.rule_save_button)

        rulesPhoneNumber.setHint(R.string.rule_phone_number_hint)
        rulesSenderEditText.setHint(R.string.rule_sender_name_hint)

        appDb = AppDatabase.getInstance(this)
        rulesRepository = RulesRepository(AppExecutors(), appDb)
        channelsRepository = ChannelsRepository(AppExecutors(), appDb)
        rulesPresenter = RulesPresenter(rulesRepository, this)
        channelsPresenter = ChannelsPresenter(channelsRepository, this)
        channelPresenter = ChannelPresenter(channelsRepository, this)

        channelsRepository.getChannels(channelsPresenter.createChannelCallback())

        saveRuleButton.setOnClickListener(this)

        ruleNameEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                enableSaveIfReady()
            }
        })

        rulesSenderEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                enableSaveIfReady()
            }
        })

        rulesPhoneNumber.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                enableSaveIfReady()
            }
        })

        ruleChannelsSpinner.onItemSelectedListener = object : OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {
                // do nothing
            }

            override fun onItemSelected(parentView: AdapterView<*>, selectedItemView: View?, position: Int, id: Long) {
                channelPresenter.start(arrayAdapter.getItem(position)!!.getId())
            }
        }
    }

    fun enableSaveIfReady() {
        val wasNameProvided = ruleNameEditText.text.toString().length > 1
        val wasSenderProvided = rulesSenderEditText.text.toString().length > 2
        val wasPhoneProvided = rulesPhoneNumber.text.toString().length > 1
        val wasChannelSelected = (ruleChannelsSpinner.selectedItem != null)

        val nameAndChannelProvided = wasNameProvided && wasChannelSelected

        when {
            wasSenderProvided -> {
                rulesPhoneNumber.hint =
                    "${getString(R.string.rule_phone_number_hint)} ${getString(R.string.rule_optional)}"

            }
            wasPhoneProvided -> {
                rulesSenderEditText.hint =
                    "${getString(R.string.rule_sender_name_hint)} ${getString(R.string.rule_optional)}"
            }
            else -> {
                rulesPhoneNumber.hint = getString(R.string.rule_phone_number_hint)
                rulesSenderEditText.hint = getString(R.string.rule_sender_name_hint)
            }
        }

        saveRuleButton.isEnabled = nameAndChannelProvided && (wasSenderProvided || wasPhoneProvided)
    }

    override fun onStart() {
        super.onStart()
        channelsPresenter.start()
    }

    override fun onStop() {
        super.onStop()
        channelsPresenter.stop()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                NavUtils.navigateUpFromSameTask(this)
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.rule_save_button -> {
                var sender = rulesSenderEditText.text.toString()

                if (sender.isEmpty()) {
                    sender = " - "
                }
                var phoneNumber = rulesPhoneNumber.text.toString()

                if (phoneNumber.isEmpty()) {
                    phoneNumber = " - "
                }

                mRule = Rule(ruleNameEditText.text.toString(),
                    sender,
                    phoneNumber,
                    mChannel.getTitle(),
                    mChannel.getId(),
                    mChannel.getWebhook())
                rulesRepository.saveRule(mRule, rulesPresenter.createRulesCallback())

                // Start service since we have rule to check on SMS received event
                val intent = Intent(this, MyService::class.java)
                startForegroundService(intent)
                if (!SharedPreferencesHelper(this).getBoolean("wasDonated")) {
                    sendDonateNotification(this)
                }
                NavUtils.navigateUpFromSameTask(this)
                this.finish()
            }
        }
    }

    override fun hideChannels() {
        // do nothing
    }

    override fun showChannels(loadedChannelsList: List<Channel>) {
        arrayAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, loadedChannelsList)
        ruleChannelsSpinner.adapter = arrayAdapter
    }

    override fun showChannel(channel: Channel) {
        mChannel = channel
    }

    override fun hideChannel() {
        // do nothing
    }

    override fun showRules(loadedRulesList: List<Rule>) {
        // do nothing
    }

    override fun loadRules(loadedRulesList: List<Rule>) {
        // do nothing
    }

    override fun hideRules() {
        // do nothing
    }
}
