package com.slacksms.app.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.ContactsContract
import android.provider.Telephony
import android.telephony.SmsMessage
import android.widget.Toast
import com.slacksms.app.App
import com.slacksms.app.R
import com.slacksms.app.data.AppDatabase
import com.slacksms.app.data.rules.Rule
import com.slacksms.app.data.rules.RulesRepository
import com.slacksms.app.executors.AppExecutors
import com.slacksms.app.rules.RulesPresenter
import com.slacksms.app.rules.RulesView
import net.gpedro.integrations.slack.SlackApi
import net.gpedro.integrations.slack.SlackMessage
import kotlin.concurrent.thread

class SMSReceiver : BroadcastReceiver(), RulesView {

    private lateinit var smsMessages: Array<SmsMessage>
    private lateinit var rulesRepository: RulesRepository
    private lateinit var rulesPresenter: RulesPresenter
    private lateinit var appDb: AppDatabase
    private var wasInitiatedBySms = false
    private val mAppExecutors: AppExecutors = AppExecutors()
    private val context: Context = App.getContext()

    override fun loadRules(loadedRulesList: List<Rule>) {
        // do nothing
    }

    override fun showRules(loadedRulesList: List<Rule>) {
        try {
            if (wasInitiatedBySms) {
                for (rule in loadedRulesList) {
                    val channel = rule.getChannelTitle()
                    val webhook = rule.getChannelWebhook()
                    for (smsMessage in smsMessages) {
                        if (rule.getSender() == smsMessage.displayOriginatingAddress
                            || rule.getPhoneNumber() == smsMessage.displayOriginatingAddress) {
                            thread {
                                try {
                                    val slackApi = SlackApi(webhook)
                                    slackApi.call(
                                        SlackMessage(
                                            channel,
                                            context.getString(R.string.app_name),
                                            "*${smsMessage.displayOriginatingAddress}:* ${smsMessage.messageBody}")
                                    )
                                } catch (e: Exception) {
                                    val message = context.getString(
                                        R.string.sms_redirection_error_toast,
                                        channel)
                                    mAppExecutors.mainThread().execute {
                                        Toast.makeText(
                                            context,
                                            message,
                                            Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } finally {
            wasInitiatedBySms = false
        }
    }

    override fun hideRules() {
        // do nothing
    }

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Telephony.Sms.Intents.SMS_RECEIVED_ACTION) {

            appDb = AppDatabase.getInstance(context)
            rulesRepository = RulesRepository(AppExecutors(), appDb)
            rulesPresenter = RulesPresenter(rulesRepository, this)
            smsMessages = Telephony.Sms.Intents.getMessagesFromIntent(intent)

            // This flag is needed in showRules() to identify that
            // rulesPresenter.start() was called from here (triggered by SMS)
            wasInitiatedBySms = true
            rulesPresenter.load()
        }
    }

    private fun getContactName(phoneNumber: String, context: Context): String {
        val uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber))

        val projection = arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME)

        var contactName = ""
        val cursor = context.contentResolver.query(uri, projection, null, null, null)

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                contactName = cursor.getString(0)
            }
            cursor.close()
        }

        return contactName
    }
}
