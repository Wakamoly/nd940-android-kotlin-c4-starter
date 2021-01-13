package com.udacity.project4.utils

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.udacity.project4.authentication.AuthRepository
import com.udacity.project4.authentication.AuthViewModel
import com.udacity.project4.base.BaseRepository
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel

@Suppress("UNCHECKED_CAST")
class ViewModelFactory(
    private val repository: BaseRepository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when{
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> AuthViewModel(repository as AuthRepository) as T
            modelClass.isAssignableFrom(RemindersListViewModel::class.java) -> RemindersListViewModel(repository as RemindersLocalRepository) as T
            modelClass.isAssignableFrom(SaveReminderViewModel::class.java) -> SaveReminderViewModel(
                Application(), repository as RemindersLocalRepository
            ) as T
            else -> throw IllegalArgumentException("ViewModel Class Not Found")
        }
    }
}