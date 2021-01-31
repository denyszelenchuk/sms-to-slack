package com.slacksms.app.stripe

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NavUtils
import androidx.core.view.GravityCompat
import com.google.android.material.snackbar.Snackbar
import com.slacksms.app.R
import com.slacksms.app.channels.ChannelsActivity
import com.slacksms.app.donate.DonateActivity
import com.slacksms.app.utils.ConnectivityChecker
import com.slacksms.app.utils.SharedPreferencesHelper
import com.stripe.android.ApiResultCallback
import com.stripe.android.PaymentConfiguration
import com.stripe.android.PaymentIntentResult
import com.stripe.android.Stripe
import com.stripe.android.model.*
import com.stripe.android.view.CardInputWidget


class CardInputWidgetActivity : AppCompatActivity() {

    private lateinit var cardWidget: CardInputWidget
    private lateinit var stripe: Stripe
    private lateinit var scrt: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_card_input_widget)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        cardWidget = findViewById(R.id.card_input_widget)
        scrt = intent.getStringExtra("scrt")!!
        stripe = Stripe(this,
            PaymentConfiguration.getInstance(this).publishableKey)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.card_widget_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_done -> {
                if (ConnectivityChecker().isNetworkConnected(this)) {
                    val card = cardWidget.card
                    if (card != null) {
                        val paymentMethodParamsCard = card.toPaymentMethodParamsCard()

                        val billingDetails = PaymentMethod.BillingDetails.Builder()
                            .setAddress(Address.Builder()
                                .setCountry(card.addressCountry)
                                .setCity(card.addressCity)
                                .setPostalCode(card.addressZip)
                                .setState(card.addressState)
                                .setLine1(card.addressLine1)
                                .setLine2(card.addressLine2)
                                .build())
                            .setName(card.name)
                            .build()

                        val paymentMethodCreateParams =
                            PaymentMethodCreateParams.create(paymentMethodParamsCard,
                                billingDetails)
                        val confirmPaymentIntentParams =
                            ConfirmPaymentIntentParams.createWithPaymentMethodCreateParams(
                                paymentMethodCreateParams,
                                scrt
                            )
                        confirmPayment(confirmPaymentIntentParams)
                    }
                    SharedPreferencesHelper(this).save("wasDonated", true)
                    true
                } else {
                    Snackbar.make(cardWidget, "Please check your internet connection!", Snackbar.LENGTH_LONG).show()
                    false
                }
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun confirmPayment(params: ConfirmPaymentIntentParams) {
        stripe.confirmPayment(this, params)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        stripe.onPaymentResult(requestCode, data,
            object : ApiResultCallback<PaymentIntentResult> {

                override fun onSuccess(result: PaymentIntentResult) {
                    // If authentication succeeded, the PaymentIntent will have
                    // user actions resolved; otherwise, handle the PaymentIntent
                    // status as appropriate (e.g. the customer may need to choose
                    // a new payment method)

                    val paymentIntent = result.intent
                    val status = paymentIntent.status
                    if (status == StripeIntent.Status.Succeeded) {
                        setResult(Activity.RESULT_OK, Intent())
                        finish()
                    } else if (status == StripeIntent.Status.RequiresPaymentMethod) {
                        // attempt authentication again or
                        // ask for a new Payment Method
                    }
                }

                override fun onError(e: Exception) {
                    val dialogBuilder = AlertDialog.Builder(this@CardInputWidgetActivity)
                    dialogBuilder.setTitle("Payment failed")
                    dialogBuilder.setMessage(e.message)
                    dialogBuilder.setPositiveButton("OK") { _, _ -> }
                    val alertDialog: AlertDialog = dialogBuilder.create()
                    alertDialog.setCancelable(true)
                    alertDialog.show()
                }
            })
    }

    override fun onBackPressed() {
        val donateActivityIntent = Intent(this, DonateActivity::class.java)
        NavUtils.navigateUpTo(this, donateActivityIntent)
    }
}
