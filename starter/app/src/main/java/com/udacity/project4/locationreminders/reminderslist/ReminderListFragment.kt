package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import android.view.*
import com.firebase.ui.auth.AuthUI
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragmentReminderListVM
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentRemindersBinding
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersDao
import com.udacity.project4.utils.setTitle
import com.udacity.project4.utils.setup


class ReminderListFragment : BaseFragmentReminderListVM<FragmentRemindersBinding>() {

    private lateinit var remindersDao: RemindersDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mCtx = requireContext()
        remindersDao = LocalDB.createRemindersDao(mCtx)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = _viewModel

        setHasOptionsMenu(true)
        setTitle(getString(R.string.app_name))

        binding.refreshLayout.setOnRefreshListener { _viewModel.loadReminders() }

        binding.lifecycleOwner = this
        setupRecyclerView()
        binding.addReminderFAB.setOnClickListener {
            navigateToAddReminder()
        }
    }

    override fun onResume() {
        super.onResume()
        //load the reminders list on the ui
        _viewModel.loadReminders()
    }

    private fun navigateToAddReminder(reminder: ReminderDataItem? = null) {
        //use the navigationCommand live data to navigate between the fragments
        _viewModel.navigationCommand.postValue(
            NavigationCommand.To(
                ReminderListFragmentDirections.reminderListFragmentToSaveReminder(reminder)
            )
        )
    }

    private fun setupRecyclerView() {
        val adapter = RemindersListAdapter {
            navigateToAddReminder(it)
        }

//        setup the recycler view using the extension function
        binding.remindersRecyclerView.setup(adapter)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout -> {
                AuthUI.getInstance()
                    .signOut(mCtx)
                    .addOnCompleteListener { // user is now signed out
                        logout()
                    }
            }
        }
        return super.onOptionsItemSelected(item)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
//        display logout as menu item
        inflater.inflate(R.menu.main_menu, menu)
    }

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentRemindersBinding.inflate(inflater, container, false)

}
