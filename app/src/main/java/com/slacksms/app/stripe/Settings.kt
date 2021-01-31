package com.slacksms.app.stripe

import com.slacksms.app.BuildConfig

/**
 * See [Configuring the example app](https://github.com/stripe/stripe-android#building-the-example-project)
 * for instructions on how to configure the app before running it.
 */
internal object Settings {
    /**
     * Set to the base URL of your test backend. If you are using
     * [example-ios-backend](https://github.com/stripe/example-ios-backend),
     * the URL will be something like `https://hidden-beach-12345.herokuapp.com/`.
     */
    const val BASE_URL = BuildConfig.BASE_URL

    /**
     * Set to publishable key from https://dashboard.stripe.com/test/apikeys
     */
    const val PUBLISHABLE_KEY = BuildConfig.PUBLISHABLE_KEY

    /**
     * Optionally, set to a Connect Account id to use for API requests to test Connect
     *
     * See https://dashboard.stripe.com/test/connect/accounts/overview
     */
    val STRIPE_ACCOUNT_ID: String = BuildConfig.STRIPE_ACCOUNT_ID
}
