package com.udacity.project4.locationreminders.savereminder

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.navArgs
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragmentSaveVM
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.GeofencingConstants
import com.udacity.project4.utils.hideKeyboard
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import java.util.*

class SaveReminderFragment : BaseFragmentSaveVM<FragmentSaveReminderBinding>() {

    private lateinit var reminderData: ReminderDataItem
    private val args: SaveReminderFragmentArgs by navArgs()

    private lateinit var geofencingClient: GeofencingClient
    private val geofencePendingIntent: PendingIntent by lazy {
        val intent = Intent(requireContext(), GeofenceBroadcastReceiver::class.java)
        intent.action = GeofencingConstants.ACTION_GEOFENCE_EVENT
        PendingIntent.getBroadcast(requireContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mCtx = requireContext()
        initView()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        setDisplayHomeAsUpEnabled(true)

        binding.lifecycleOwner = this
        binding.viewModel = _viewModel
        geofencingClient = LocationServices.getGeofencingClient(mCtx)

        return binding.root
    }

    private fun initView(){
        args.reminder?.let { reminderDataItem ->
            reminderData = reminderDataItem
            _viewModel.apply {
                setValues(reminderData)
            }
        }
        _viewModel.clearLoading()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            initReminderData()
            hideKeyboard()
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment(reminderData))
        }

        binding.saveReminder.setOnClickListener {
            initReminderData()
            if (_viewModel.validateAndSaveReminder(reminderData)) {
                addReminderGeofence(
                    reminderData.latitude!!,
                    reminderData.longitude!!,
                    reminderData.id
                )
                hideKeyboard()
                _viewModel.onClear()
            }
        }
    }

    private fun initReminderData() {
        reminderData = ReminderDataItem(
                _viewModel.reminderTitle.value,
                _viewModel.reminderDescription.value,
                _viewModel.reminderSelectedLocationStr.value,
                _viewModel.latitude.value,
                _viewModel.longitude.value,
                _viewModel.reminderID.value ?: UUID.randomUUID().toString()
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSaveReminderBinding.inflate(inflater, container, false)

    @SuppressLint("MissingPermission")
    private fun addReminderGeofence(latitude: Double, longitude: Double, reminderId: String) {
        val geofence = Geofence.Builder()
            .setRequestId(reminderId)
            .setCircularRegion(
                latitude,
                longitude,
                // TODO: 1/9/21 Possibility of setting custom radius?
                GeofencingConstants.GEOFENCE_RADIUS_METERS
            )
            .setExpirationDuration(Geofence.NEVER_EXPIRE)
            .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
            .build()

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofence(geofence)
            .build()

        geofencingClient.removeGeofences(geofencePendingIntent)?.run {
            addOnCompleteListener {
                geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)?.run {
                    addOnSuccessListener {
                        Log.d(TAG, "Added geofence for reminder id -> $reminderId successfully.")
                    }
                    addOnFailureListener {
                        _viewModel.showErrorMessage.postValue(getString(R.string.error_adding_geofence))
                        it.message?.let { message ->
                            Log.w(TAG, message)
                        }
                    }
                }
            }
        }
    }

    companion object {
        private val TAG = SaveReminderFragment::class.java.simpleName
    }


}
