package com.udacity.project4.base

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.viewbinding.ViewBinding
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.authentication.datastore.UserPreferences
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.koin.android.ext.android.inject

/**
 * Base Fragment to observe on the common LiveData objects
 */
abstract class BaseFragmentReminderListVM<B: ViewBinding> : Fragment() {

    protected lateinit var userPreferences: UserPreferences
    protected lateinit var binding : B
    protected lateinit var mCtx: Context

    //Get the view model this time as a single to be shared with another fragment
    protected val _viewModel: RemindersListViewModel by inject()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        userPreferences = UserPreferences(mCtx)
        binding = getFragmentBinding(inflater, container)

        return binding.root
    }

    fun logout() = lifecycleScope.launch {
        withContext(Dispatchers.IO) {
            userPreferences.clear()
            LocalDB.clearAlltables(mCtx)
        }
        val intent = Intent(mCtx, AuthenticationActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        activity?.finish()
        startActivity(intent)
    }

    abstract fun getFragmentBinding(inflater: LayoutInflater, container: ViewGroup?) : B

    override fun onStart() {
        super.onStart()
        _viewModel.showErrorMessage.observe(this, {
            Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
        })
        _viewModel.showToast.observe(this, {
            Toast.makeText(activity, it, Toast.LENGTH_LONG).show()
        })
        _viewModel.showSnackBar.observe(this, {
            Snackbar.make(this.requireView(), it, Snackbar.LENGTH_LONG).show()
        })
        _viewModel.showSnackBarInt.observe(this, {
            Snackbar.make(this.requireView(), getString(it), Snackbar.LENGTH_LONG).show()
        })

        _viewModel.navigationCommand.observe(this, { command ->
            when (command) {
                is NavigationCommand.To -> this.findNavController().navigate(command.directions)
                is NavigationCommand.Back -> this.findNavController().popBackStack()
                is NavigationCommand.BackTo -> this.findNavController().popBackStack(
                        command.destinationId,
                        false
                )
            }
        })
    }

}