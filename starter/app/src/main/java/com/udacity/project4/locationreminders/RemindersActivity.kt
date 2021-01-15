package com.udacity.project4.locationreminders

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.*
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.navigation.NavigationView
import com.udacity.project4.R
import com.udacity.project4.databinding.ActivityRemindersBinding
import kotlinx.android.synthetic.main.activity_reminders.*

/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRemindersBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var appBarConfiguration: AppBarConfiguration

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = DataBindingUtil.setContentView(this, R.layout.activity_reminders)

        setupNavigationDrawer()
        setSupportActionBar(binding.toolbar)

        val navController: NavController = Navigation.findNavController(this, R.id.nav_host_fragment)
        val bottomNavigationView = binding.bottomNavigation
        val navView = binding.navView

        NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout)
        appBarConfiguration = AppBarConfiguration(navController.graph, drawerLayout)
        navView.setupWithNavController(navController)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
        bottomNavigationView.setupWithNavController(navController)

        bottomNavigationView.setOnNavigationItemSelectedListener {
            navController.navigate(it.itemId)
            true
        }

    }

    override fun onSupportNavigateUp(): Boolean {
        return findNavController(R.id.nav_host_fragment).navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                (nav_host_fragment as NavHostFragment).navController.popBackStack()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    private fun setupNavigationDrawer() {
        drawerLayout = binding.drawerLayout
            .apply {
                setStatusBarBackground(R.color.colorPrimaryDark)
            }
    }

}
