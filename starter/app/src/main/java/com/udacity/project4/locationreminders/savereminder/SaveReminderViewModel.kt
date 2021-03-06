package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.BaseRepository
import com.udacity.project4.base.BaseViewModel
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.launch

class SaveReminderViewModel(
    private val app: Application,
    private val reminderRepository: BaseRepository
) : BaseViewModel() {

    val reminderTitle = MutableLiveData<String>()
    val reminderDescription = MutableLiveData<String>()
    val reminderID = MutableLiveData<String>()
    val reminderSelectedLocationStr = MutableLiveData<String>()
    val selectedPOI = MutableLiveData<PointOfInterest>()
    val latitude = MutableLiveData<Double>()
    val longitude = MutableLiveData<Double>()

    /**
     * Clear the live data objects to start fresh next time the view model gets called
     */
    fun onClear() {
        reminderTitle.value = null
        reminderDescription.value = null
        reminderSelectedLocationStr.value = null
        selectedPOI.value = null
        latitude.value = null
        longitude.value = null
        reminderID.value = null
    }

    fun clearLoading() {
        showLoading.value = false
    }

    /**
     * Sets the live data objects to the data provided
     */
    fun setValues(data: ReminderDataItem) {
        reminderTitle.value = data.title
        reminderDescription.value = data.description
        reminderSelectedLocationStr.value = data.location
        latitude.value = data.latitude
        longitude.value = data.longitude
        reminderID.value = data.id
    }

    /**
     * Validate the entered data then saves the reminder data to the DataSource
     */
    fun validateAndSaveReminder(reminderData: ReminderDataItem) : Boolean {
        if (validateEnteredData(reminderData)) {
            saveReminder(reminderData)
            return true
        }
        return false
    }

    /**
     * Save the reminder to the data source
     */
    private fun saveReminder(reminderData: ReminderDataItem) {
        showLoading.value = true
        viewModelScope.launch {
            if (reminderRepository.saveReminder(
                            ReminderDTO(
                                    reminderData.title,
                                    reminderData.description,
                                    reminderData.location,
                                    reminderData.latitude,
                                    reminderData.longitude,
                                    reminderData.id
                            )
                    ) is Result.Success) {
                showLoading.value = false
                showToast.value = app.getString(R.string.reminder_saved)
                navigationCommand.value = NavigationCommand.Back
                Log.d("SaveReminderViewModel", "saveReminder: SUCCESS")
            } else {
                showLoading.value = false
                showToast.value = app.getString(R.string.reminder_failed)
                Log.d("SaveReminderViewModel", "saveReminder: FAILURE")
            }
            Log.d("SaveReminderViewModel", "saveReminder: END")
        }
    }

    /**
     * Validate the entered data and show error to the user if there's any invalid data
     */
    private fun validateEnteredData(reminderData: ReminderDataItem): Boolean {
        if (reminderData.title.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_enter_title
            return false
        }

        if (reminderData.location.isNullOrEmpty()) {
            showSnackBarInt.value = R.string.err_select_location
            return false
        }
        return true
    }

    fun updateSelectedLocation(
        latLng: LatLng,
        snippet: String,
        pointOfInterest: PointOfInterest? = null
    ) {
        Log.d("SaveReminderViewModel", "updateSelectedLocation: $snippet")
        selectedPOI.postValue(pointOfInterest)
        latitude.postValue(latLng.latitude)
        longitude.postValue(latLng.longitude)
        reminderSelectedLocationStr.postValue(snippet)
    }

}