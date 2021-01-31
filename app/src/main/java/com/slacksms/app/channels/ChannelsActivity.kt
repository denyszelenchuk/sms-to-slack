package com.slacksms.app.channels

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.provider.Telephony
import android.text.method.LinkMovementMethod
import android.view.Menu
import android.view.MenuItem
import android.view.View.GONE
import android.view.View.VISIBLE
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.slacksms.app.App
import com.slacksms.app.R
import com.slacksms.app.data.AppDatabase
import com.slacksms.app.data.channels.Channel
import com.slacksms.app.data.channels.ChannelsRepository
import com.slacksms.app.executors.AppExecutors
import com.slacksms.app.utils.MenuHelper.selectMenuItem
import com.slacksms.app.utils.NotificationsHelper.createNotificationChannel
import com.slacksms.app.utils.SharedPreferencesHelper


class ChannelsActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, ChannelsView, ChannelView {

    private lateinit var appDb: AppDatabase
    private lateinit var channelsRepository: ChannelsRepository
    private lateinit var channelsPresenter: ChannelsPresenter
    private lateinit var channelPresenter: ChannelPresenter
    private var channelsList: List<Channel> = ArrayList<Channel>()

    private lateinit var channelsRecyclerView: RecyclerView

    private lateinit var fabButton: FloatingActionButton
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: Toolbar
    private lateinit var navView: NavigationView

    private lateinit var viewAdapter: ChannelsAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    private lateinit var channelsDescriptionTextView: TextView
    private lateinit var channelMainLayout: ConstraintLayout

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(this.findViewById(R.id.toolbar))
        supportActionBar!!.title = getString(R.string.title_activity_main)
        channelMainLayout = findViewById(R.id.channel_main_layout)
        channelsRecyclerView = findViewById(R.id.channels_list)

        fabButton = findViewById(R.id.fab)
        drawerLayout = findViewById(R.id.drawer_layout)
        toolbar = findViewById(R.id.toolbar)
        navView = findViewById(R.id.nav_view)

        channelsDescriptionTextView = findViewById(R.id.channel_description_text_view)
        channelsDescriptionTextView.movementMethod = LinkMovementMethod.getInstance()

        viewManager = LinearLayoutManager(this)

        appDb = AppDatabase.getInstance(this)
        channelsRepository = ChannelsRepository(AppExecutors(), appDb)
        channelsPresenter = ChannelsPresenter(channelsRepository, this)
        channelPresenter = ChannelPresenter(channelsRepository, this)

        fabButton.setOnClickListener {
            if (isSmsPermissionGranted()) {
                val addChannelIntent = Intent(this, AddChannelActivity::class.java)
                startActivity(addChannelIntent)
            } else {
                requestReadSmsPermission()
            }
        }

        val toggle = ActionBarDrawerToggle(
            this,
            drawerLayout,
            toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close)

        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener(this)

        if (Telephony.Sms.getDefaultSmsPackage(this) != this.packageName) {
            SharedPreferencesHelper(this)
                .save(App.defaultSmsAppKey, Telephony.Sms.getDefaultSmsPackage(this))
        }
        createNotificationChannel(this)
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_delete_all_channels -> {
                channelsRepository.deleteChannels(channelsPresenter.createChannelCallback())
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        selectMenuItem(item, R.id.nav_channels, this)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun isSmsPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_SMS) == PackageManager.PERMISSION_GRANTED
    }

    /**
     * Request runtime SMS permission
     */
    private fun requestReadSmsPermission() {
        if (!(shouldShowRequestPermissionRationale(Manifest.permission.RECEIVE_SMS)
                && shouldShowRequestPermissionRationale(Manifest.permission.READ_SMS))) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.READ_SMS,
                    Manifest.permission.RECEIVE_SMS
                ),
                SMS_PERMISSION_CODE
            )
        } else {
            showSmsSnackbar()
        }

    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == SMS_PERMISSION_CODE) {
            // If request is cancelled, the result arrays are empty.
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                val intent = Intent(this, AddChannelActivity::class.java)
                startActivity(intent)
            } else {
                showSmsSnackbar()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        channelsPresenter.start()
    }

    override fun onPause() {
        super.onPause()
        channelsPresenter.stop()
    }

    override fun showChannels(loadedChannelsList: List<Channel>) {
        channelsList = loadedChannelsList
        viewAdapter = ChannelsAdapter(ArrayList(channelsList), this)
        channelsRecyclerView.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }

        val callback = DragManageAdapter(
            viewAdapter,
            ItemTouchHelper.UP.or(ItemTouchHelper.DOWN),
            ItemTouchHelper.LEFT.or(ItemTouchHelper.RIGHT)
        )
        val helper = ItemTouchHelper(callback)
        helper.attachToRecyclerView(channelsRecyclerView)
        channelsDescriptionTextView.visibility = GONE
    }

    override fun hideChannels() {
        (channelsList as ArrayList).clear()
        viewAdapter = ChannelsAdapter(ArrayList(channelsList), this)
        channelsRecyclerView.apply {
            adapter = viewAdapter
        }
        channelsDescriptionTextView.visibility = VISIBLE
    }

    override fun showChannel(channel: Channel) {
        // do nothing
    }

    override fun hideChannel() {
        channelsPresenter.start()
    }

    private fun showSmsSnackbar() {
        Snackbar.make(channelMainLayout, R.string.channel_grant_snackbar_text, Snackbar.LENGTH_LONG)
            .setAction(R.string.channel_grant_action) {
                val permissionIntent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package", packageName, null)
                permissionIntent.data = uri
                startActivityForResult(permissionIntent, 123)
            }.show()
    }

    inner class DragManageAdapter(adapter: ChannelsAdapter, dragDirs: Int, swipeDirs: Int) : ItemTouchHelper.SimpleCallback(dragDirs, swipeDirs) {

        override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
            return false
        }

        private var channelsAdapter = adapter

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            channelsAdapter.deleteItem(position)
            channelsRepository.deleteChannelById(channelsList[position].getId()!!, channelPresenter.createChannelCallback())
        }
    }

    companion object {
        const val SMS_PERMISSION_CODE = 999
    }
}
