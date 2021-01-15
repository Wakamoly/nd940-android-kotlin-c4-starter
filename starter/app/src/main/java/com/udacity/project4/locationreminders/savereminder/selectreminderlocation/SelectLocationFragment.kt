package com.udacity.project4.locationreminders.savereminder.selectreminderlocation


import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.content.res.Resources
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.BaseFragmentSaveVM
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersDao
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.ViewModelFactory
import com.udacity.project4.utils.setDisplayHomeAsUpEnabled
import com.udacity.project4.utils.truncateLatLng
import org.koin.android.ext.android.inject
import java.util.*

class SelectLocationFragment : BaseFragmentSaveVM<FragmentSelectLocationBinding>() {

    private var locationPermissionGranted = false
    private var lastKnownLocation : Location? = null
    private var cameraPosition : CameraPosition? = null
    private val defaultLocation : LatLng = LatLng(-34.0, 151.0)
    private val defaultZoom = 15f
    private val args: SelectLocationFragmentArgs by navArgs()
    private var marker: Marker? = null
    private lateinit var map: GoogleMap
    private lateinit var remindersDao: RemindersDao

    private val remindersViewModel by activityViewModels<RemindersListViewModel> { ViewModelFactory(RemindersLocalRepository(remindersDao)) }

    private val callback = OnMapReadyCallback { googleMap ->
        map = googleMap

        setMapStyle(map)
        initObservers()

        // Turn on the My Location layer and the related control on the map.
        updateLocationUI()
        if (cameraPosition != null) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                cameraPosition?.target, cameraPosition?.zoom!!
            ))
        } else {
            // Get the current location of the device and set the position of the map.
            getDeviceLocation()
        }

    }

    companion object {
        private const val KEY_CAMERA_POSITION = "camera_position"
        private const val KEY_LOCATION = "location"
        private const val PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1
        private const val TAG = "SelectLocationFrag"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mCtx = requireContext()
        remindersDao = LocalDB.createRemindersDao(mCtx)
        if (savedInstanceState != null) {
            lastKnownLocation = savedInstanceState.getParcelable(KEY_LOCATION)
            cameraPosition = savedInstanceState.getParcelable(KEY_CAMERA_POSITION)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        map.let { map ->
            outState.putParcelable(KEY_CAMERA_POSITION, map.cameraPosition)
            outState.putParcelable(KEY_LOCATION, lastKnownLocation)
        }
        super.onSaveInstanceState(outState)
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(callback)

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        return binding.root
    }

    private fun initObservers() {
        // TODO: 1/9/21 Navigate accordingly
        if (args.reminder != null){
            addMarker(args.reminder!!, true)
            initSaveLocationClickListeners()
        } else {
            remindersViewModel.loadReminders()
            remindersViewModel.remindersList.observe(viewLifecycleOwner, Observer {
                for (i in it){
                    addMarker(i, false)
                }
            })
        }
    }

    private fun initSaveLocationClickListeners() {
        binding.btnSave.visibility = View.VISIBLE

        binding.btnSave.setOnClickListener {
            if (marker == null) {
                _viewModel.showErrorMessage.value = (getString(R.string.select_poi))
            } else {
                _viewModel.navigationCommand.postValue(NavigationCommand.Back)
            }
        }

        map.setOnPoiClickListener {
            marker?.remove()

            val snippet = it.name
            _viewModel.updateSelectedLocation(it.latLng, snippet, it)

            marker = map.addMarker(
                MarkerOptions().position(it.latLng)
                    .title("Selected Location")
                    .snippet(snippet)
            )
        }

        map.setOnMapClickListener {
            marker?.remove()

            val snippet = "${it.latitude.truncateLatLng(5)}, ${it.longitude.truncateLatLng(5)}"
            _viewModel.updateSelectedLocation(it, snippet)

            marker = map.addMarker(
                MarkerOptions().position(it)
                    .title(getString(R.string.selected_location))
                    .snippet(snippet)
            )
        }
    }

    private fun addMarker(data: ReminderDataItem, single: Boolean) {
        if (data.latitude != null && data.longitude != null){
            val latLng = LatLng(data.latitude!!, data.longitude!!)
            if (single){
                marker = map.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title(data.title)
                        .snippet(data.description)
                )
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    latLng, defaultZoom
                ))
            } else {
                map.addMarker(
                    MarkerOptions()
                        .position(latLng)
                        .title(data.title)
                        .snippet(data.description)
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                )
            }

        }
    }

    // TODO: 1/13/21 Use this for adding a new reminder from the general map view?
    private fun onLocationSelected() {
        //        TODO: When the user confirms on the selected location,
        //         send back the selected location details to the view model
        //         and navigate back to the previous fragment to save the reminder and add the geofence
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(::map.isInitialized){
            when (item.itemId) {
                // Change the map type based on the user's selection.
                R.id.normal_map -> { map.mapType = GoogleMap.MAP_TYPE_NORMAL; return true }
                R.id.hybrid_map -> { map.mapType = GoogleMap.MAP_TYPE_HYBRID; return true }
                R.id.satellite_map -> { map.mapType = GoogleMap.MAP_TYPE_SATELLITE; return true }
                R.id.terrain_map -> { map.mapType = GoogleMap.MAP_TYPE_TERRAIN; return true }
                else -> super.onOptionsItemSelected(item)
            }
        }
        return false
    }

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = FragmentSelectLocationBinding.inflate(inflater, container, false)

    private fun getLocationPermission() {
        /*
         * Request location permission, so that we can get the location of the
         * device. The result of the permission request is handled by a callback,
         * onRequestPermissionsResult.
         */
        if (ContextCompat.checkSelfPermission(mCtx.applicationContext,
                Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
            updateLocationUI()
        } else {
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray) {
        locationPermissionGranted = false
        when (requestCode) {
            PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() &&
                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    locationPermissionGranted = true
                }
            }
        }
        updateLocationUI()
    }

    private fun updateLocationUI() {
        try {
            if (locationPermissionGranted) {
                map.isMyLocationEnabled = true
                map.uiSettings?.isMyLocationButtonEnabled = true
            } else {
                map.isMyLocationEnabled = false
                map.uiSettings?.isMyLocationButtonEnabled = false
                lastKnownLocation = null
                getLocationPermission()
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
        }
    }

    private fun getDeviceLocation() {
        /*
         * Get the best and most recent location of the device, which may be null in rare
         * cases when a location is not available.
         */
        try {
            if (locationPermissionGranted) {
                val locationResult = FusedLocationProviderClient(mCtx).lastLocation
                locationResult.addOnCompleteListener(requireActivity()) { task ->
                    if (task.isSuccessful) {
                        // Set the map's camera position to the current location of the device.
                        lastKnownLocation = task.result
                        if (lastKnownLocation != null) {
                            map.moveCamera(CameraUpdateFactory.newLatLngZoom(
                                LatLng(lastKnownLocation?.latitude ?: defaultLocation.latitude,
                                    lastKnownLocation?.longitude ?: defaultLocation.longitude), defaultZoom
                            ))
                        }
                    } else {
                        Log.d(TAG, "Current location is null. Using defaults.")
                        Log.e(TAG, "Exception: %s", task.exception)
                        map.moveCamera(CameraUpdateFactory
                            .newLatLngZoom(defaultLocation, defaultZoom))
                        map.uiSettings?.isMyLocationButtonEnabled = false
                    }
                }
            }
        } catch (e: SecurityException) {
            Log.e("Exception: %s", e.message, e)
            map.moveCamera(CameraUpdateFactory
                .newLatLngZoom(defaultLocation, defaultZoom))
            map.uiSettings?.isMyLocationButtonEnabled = false
        }
    }

    /**
     *
     * Navigate to https://mapstyle.withgoogle.com/ in your browser to style the map.
     *
     */
    private fun setMapStyle(map: GoogleMap) {
        try {
            // Customize the styling of the base map using a JSON object defined
            // in a raw resource file.
            val success = map.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    mCtx,
                    R.raw.map_style
                )
            )

            if (!success) {
                Log.e(TAG, "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e(TAG, "Can't find style. Error: ", e)
        }
    }

}
