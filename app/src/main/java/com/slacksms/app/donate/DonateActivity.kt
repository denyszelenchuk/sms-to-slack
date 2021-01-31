package com.slacksms.app.donate

import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.util.Log
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.NavUtils
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.slacksms.app.R
import com.slacksms.app.channels.ChannelsActivity
import com.slacksms.app.stripe.BackendApi
import com.slacksms.app.stripe.CardInputWidgetActivity
import com.slacksms.app.stripe.RetrofitFactory
import com.slacksms.app.stripe.Settings
import com.slacksms.app.utils.ConnectivityChecker
import com.slacksms.app.utils.MenuHelper.selectMenuItem
import com.stripe.android.PaymentConfiguration
import com.stripe.android.Stripe
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.json.JSONObject


class DonateActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private val backendApi: BackendApi = RetrofitFactory.instance.create(BackendApi::class.java)
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private lateinit var donateButton: Button
    private lateinit var donateAmountEditText: EditText
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: Toolbar
    private lateinit var navView: NavigationView
    private lateinit var stripe: Stripe


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donate)
        toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar!!.title = getString(R.string.title_donate)

        donateAmountEditText = findViewById(R.id.donate_amount_edit_text)
        donateAmountEditText.filters = arrayOf<InputFilter>(InputFilterMinMax("1", "10000"))
        drawerLayout = findViewById(R.id.drawer_layout)
        toolbar = findViewById(R.id.toolbar)
        navView = findViewById(R.id.nav_view)

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)
        donateButton = findViewById(R.id.donate_button)
        donateButton.setOnClickListener {
            if (donateAmountEditText.text != null && donateAmountEditText.text.isNotEmpty()) {
                donateButton.isEnabled = false
                // Stripe accepts amount in cents. Example: 5$ = 500 cents.
                val amount = donateAmountEditText.text.toString().toInt() * 100
                val params: HashMap<String, Any> = HashMap()
                params["amount"] = amount
                params["currency"] = "chf"
                params["setupFutureUsage"] = "off_session"
                if (ConnectivityChecker().isNetworkConnected(this)) {
                    compositeDisposable.add(
                        backendApi
                            .createPaymentIntent(params)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe { responseBody ->
                                try {
                                    val responseJSONObject = JSONObject(responseBody.string())
                                    val intent = Intent(this, CardInputWidgetActivity::class.java)
                                    intent.putExtra("scrt", responseJSONObject.getString("client_secret"))
                                    startActivity(intent)
                                    this.finish()
                                } catch (e: Exception) {
                                    Log.d("ERROR", e.message!!)
                                    donateButton.isEnabled = true
                                }
                            }
                    )
                } else {
                    donateButton.isEnabled = true
                    Snackbar.make(it, "Please check your internet connection!", Snackbar.LENGTH_LONG).show()
                }
            }
        }

        PaymentConfiguration.init(this, Settings.PUBLISHABLE_KEY)
        stripe = Stripe(this, PaymentConfiguration.getInstance(this).publishableKey)
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            val mainActivityIntent = Intent(this, ChannelsActivity::class.java)
            NavUtils.navigateUpTo(this, mainActivityIntent)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        selectMenuItem(item, R.id.nav_donate, this)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}
