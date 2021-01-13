package com.udacity.project4.locationreminders.reminderslist

import android.os.Bundle
import android.view.*
import com.firebase.ui.auth.AuthUI
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentRemindersBinding
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersDao
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.utils.setTitle
import com.udacity.project4.utils.setup


class ReminderListFragment : BaseFragment<RemindersListViewModel, FragmentRemindersBinding, RemindersLocalRepository>() {
    //use Koin to retrieve the ViewModel instance
    //override val _viewModel: RemindersListViewModel by viewModel()
    //private lateinit var binding: FragmentRemindersBinding

    private lateinit var remindersDao: RemindersDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mCtx = requireContext()
        remindersDao = LocalDB.createRemindersDao(mCtx)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.viewModel = viewModel

        setHasOptionsMenu(true)
        setTitle(getString(R.string.app_name))

        binding.refreshLayout.setOnRefreshListener { viewModel.loadReminders() }

        binding.lifecycleOwner = this
        setupRecyclerView()
        binding.addReminderFAB.setOnClickListener {
            navigateToAddReminder()
        }
    }

    override fun onResume() {
        super.onResume()
        //load the reminders list on the ui
        viewModel.loadReminders()
    }

    private fun navigateToAddReminder(reminder: ReminderDataItem? = null) {
        //use the navigationCommand live data to navigate between the fragments
        viewModel.navigationCommand.postValue(
            NavigationCommand.To(
                ReminderListFragmentDirections.toSaveReminder(reminder)
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

    override fun getViewModel() = RemindersListViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentRemindersBinding.inflate(inflater, container, false)

    override fun getFragmentRepository() = RemindersLocalRepository(remindersDao)

}
