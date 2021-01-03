package com.udacity.project4.utils

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.annotation.IdRes
import androidx.navigation.*
import androidx.navigation.NavController.OnDestinationChangedListener
import com.google.android.material.bottomnavigation.BottomNavigationView
import java.lang.ref.WeakReference


object BottomNavigationUI {

    fun onNavDestinationSelected(
        item: MenuItem,
        navController: NavController
    ): Boolean {
        val resId = item.itemId
        var args: Bundle? = null
        val options: NavOptions
        val optionsBuilder = NavOptions.Builder()
            .setLaunchSingleTop(true)
            .setEnterAnim(android.R.anim.fade_in)
            .setExitAnim(android.R.anim.fade_out)
            .setPopEnterAnim(android.R.anim.slide_in_left)
            .setPopExitAnim(android.R.anim.slide_out_right)
        if (item.order and Menu.CATEGORY_SECONDARY == 0) {
            optionsBuilder.setPopUpTo(findStartDestination(navController.graph).id, false)
        }
        val navAction = navController.currentDestination?.getAction(resId)
        if (navAction != null) {
            val navOptions = navAction.navOptions

            // Note : You can Add *setLaunchSingleTop* and *setPopUpTo* from *navOptions* to *builder*
            if (navOptions!!.enterAnim != -1) {
                optionsBuilder.setEnterAnim(navOptions.enterAnim)
            }
            if (navOptions.exitAnim != -1) {
                optionsBuilder.setExitAnim(navOptions.exitAnim)
            }
            if (navOptions.popEnterAnim != -1) {
                optionsBuilder.setPopEnterAnim(navOptions.popEnterAnim)
            }
            if (navOptions.popExitAnim != -1) {
                optionsBuilder.setPopExitAnim(navOptions.popExitAnim)
            }
            val navActionArgs = navAction.defaultArguments
            if (navActionArgs != null) {
                args = Bundle()
                args.putAll(navActionArgs)
            }
        }
        options = optionsBuilder.build()
        return try {
            //TODO provide proper API instead of using Exceptions as Control-Flow.
            navController.navigate(resId, args, options)
            true
        } catch (e: IllegalArgumentException) {
            false
        }
    }

    fun setupWithNavController(
        bottomNavigationView: BottomNavigationView,
        navController: NavController
    ) {
        bottomNavigationView.setOnNavigationItemSelectedListener { item ->
            onNavDestinationSelected(
                item,
                navController
            )
        }
        val weakReference = WeakReference(bottomNavigationView)
        navController.addOnDestinationChangedListener(
            object : OnDestinationChangedListener {
                override fun onDestinationChanged(
                    controller: NavController,
                    destination: NavDestination, arguments: Bundle?
                ) {
                    val view = weakReference.get()
                    if (view == null) {
                        navController.removeOnDestinationChangedListener(this)
                        return
                    }
                    val menu = view.menu
                    var h = 0
                    val size = menu.size()
                    while (h < size) {
                        val item = menu.getItem(h)
                        if (matchDestination(destination, item.itemId)) {
                            item.isChecked = true
                        }
                        h++
                    }
                }
            })
    }

    fun matchDestination(
        destination: NavDestination,
        @IdRes destId: Int
    ): Boolean {
        var currentDestination: NavDestination? = destination
        while (currentDestination!!.id != destId && currentDestination.parent != null) {
            currentDestination = currentDestination.parent
        }
        return currentDestination.id == destId
    }

    fun matchDestinations(
        destination: NavDestination,
        destinationIds: Set<Int?>
    ): Boolean {
        var currentDestination: NavDestination? = destination
        do {
            if (destinationIds.contains(currentDestination!!.id)) {
                return true
            }
            currentDestination = currentDestination.parent
        } while (currentDestination != null)
        return false
    }

    fun findStartDestination(graph: NavGraph): NavDestination {
        var startDestination: NavDestination = graph
        while (startDestination is NavGraph) {
            val parent = startDestination
            startDestination = parent.findNode(parent.startDestination)!!
        }
        return startDestination
    }

}