package com.slacksms.app.rules

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.NavUtils
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.slacksms.app.R
import com.slacksms.app.channels.ChannelsActivity
import com.slacksms.app.channels.ChannelsPresenter
import com.slacksms.app.channels.ChannelsView
import com.slacksms.app.data.AppDatabase
import com.slacksms.app.data.channels.Channel
import com.slacksms.app.data.channels.ChannelsRepository
import com.slacksms.app.data.rules.Rule
import com.slacksms.app.data.rules.RulesRepository
import com.slacksms.app.executors.AppExecutors
import com.slacksms.app.utils.MenuHelper.selectMenuItem


class RulesActivity : AppCompatActivity(),
                      NavigationView.OnNavigationItemSelectedListener, RulesView, ChannelsView {

    private lateinit var rulesRecyclerView: RecyclerView
    private lateinit var viewAdapter: RulesAdapter
    private lateinit var viewManager: RecyclerView.LayoutManager
    private var rulesList: List<Rule> = ArrayList()
    private lateinit var channelsPresenter: ChannelsPresenter
    private lateinit var channelsRepository: ChannelsRepository

    private lateinit var fabButton: FloatingActionButton
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var toolbar: Toolbar
    private lateinit var navView: NavigationView

    private lateinit var rulesRepository: RulesRepository
    private lateinit var rulesPresenter: RulesPresenter
    private lateinit var appDb: AppDatabase
    private lateinit var ruleDescriptionTextView: TextView
    private var wasChannelAdded = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rules)

        appDb = AppDatabase.getInstance(this)
        rulesRepository = RulesRepository(AppExecutors(), appDb)
        rulesPresenter = RulesPresenter(rulesRepository, this)
        rulesRecyclerView = findViewById(R.id.rules_list)
        ruleDescriptionTextView = findViewById(R.id.rules_description_text_view)
        viewManager = LinearLayoutManager(this)

        fabButton = findViewById(R.id.fab)
        drawerLayout = findViewById(R.id.drawer_layout)
        toolbar = findViewById(R.id.toolbar)
        navView = findViewById(R.id.nav_view)

        channelsRepository = ChannelsRepository(AppExecutors(), appDb)
        channelsPresenter = ChannelsPresenter(channelsRepository, this)

        setSupportActionBar(toolbar)
        val layout = findViewById<CoordinatorLayout>(R.id.rules_layout)
        val progressBar = ProgressBar(this, null, android.R.attr.progressBarStyleLarge)
        val params = RelativeLayout.LayoutParams(100, 100)
        params.addRule(RelativeLayout.CENTER_IN_PARENT)
        layout.addView(progressBar, params)

        fabButton.setOnClickListener { view ->
            if (wasChannelAdded) {
                val intent = Intent(this, AddRuleActivity::class.java)
                startActivity(intent)
            } else {
                Snackbar.make(view, "Add at least one channel", Snackbar.LENGTH_LONG)
                    .setAction("Add") {
                        val addChannelIntent =
                            Intent(this, ChannelsActivity::class.java)
                        startActivity(addChannelIntent)
                        this.finish()
                    }.show()
            }
        }

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
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            val mainActivityIntent = Intent(this, ChannelsActivity::class.java)
            NavUtils.navigateUpTo(this, mainActivityIntent)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.rules, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_delete_all_rules -> {

                val dialogBuilder = AlertDialog.Builder(this)
                dialogBuilder.setTitle("Delete all Rules")
                dialogBuilder.setMessage("Do you really want to delete all purchased Redirection Rules?")
                dialogBuilder.setPositiveButton("Yes") { _, _ ->
                    rulesRepository.deleteRules(rulesPresenter.createRulesCallback(), this)
                }
                dialogBuilder.setNegativeButton("No") { _, _ -> }
                val alertDialog: AlertDialog = dialogBuilder.create()
                alertDialog.setCancelable(true)
                alertDialog.show()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        selectMenuItem(item, R.id.nav_rules, this)
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    override fun onResume() {
        super.onResume()
        rulesPresenter.start()
        channelsPresenter.start()
    }

    override fun onPause() {
        super.onPause()
        rulesPresenter.stop()
    }

    override fun showRules(loadedRulesList: List<Rule>) {
        rulesList = loadedRulesList
        viewAdapter = RulesAdapter(ArrayList(rulesList), this)
        rulesRecyclerView.apply {
            // use this setting to improve performance if you know that changes
            // in content do not change the layout size of the RecyclerView
            setHasFixedSize(true)
            // use a linear layout manager
            layoutManager = viewManager
            // specify an viewAdapter (see also next example)
            adapter = viewAdapter
        }

        val callback = DragManageAdapter(
            viewAdapter,
            ItemTouchHelper.UP.or(ItemTouchHelper.DOWN),
            ItemTouchHelper.LEFT.or(ItemTouchHelper.RIGHT)
        )
        val helper = ItemTouchHelper(callback)
        helper.attachToRecyclerView(rulesRecyclerView)
        ruleDescriptionTextView.visibility = View.GONE
    }

    override fun loadRules(loadedRulesList: List<Rule>) {
        //do nothing
    }

    override fun hideRules() {
        (rulesList as ArrayList<Rule>).clear()
        viewAdapter = RulesAdapter(ArrayList(rulesList), this)
        rulesRecyclerView.apply {
            adapter = viewAdapter
        }
        ruleDescriptionTextView.visibility = View.VISIBLE
    }

    override fun showChannels(loadedChannelsList: List<Channel>) {
        wasChannelAdded = true
    }

    override fun hideChannels() {
        wasChannelAdded = false
    }

    inner class DragManageAdapter(
        adapter: RulesAdapter,
        dragDirs: Int,
        swipeDirs: Int) : ItemTouchHelper.SimpleCallback(dragDirs, swipeDirs) {

        override fun onMove(
            recyclerView: RecyclerView,
            viewHolder: RecyclerView.ViewHolder,
            target: RecyclerView.ViewHolder): Boolean {
            return false
        }

        private var rulesAdapter = adapter

        override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
            val position = viewHolder.adapterPosition
            rulesAdapter.deleteItem(position)
            rulesRepository.deleteRule(rulesList[position], rulesPresenter.createRulesCallback())
        }
    }
}
