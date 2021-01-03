package com.udacity.project4.utils

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.udacity.project4.authentication.AuthRepository
import com.udacity.project4.authentication.AuthViewModel
import com.udacity.project4.base.BaseRepository

class ViewModelFactory(
    private val repository: BaseRepository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return when{
            modelClass.isAssignableFrom(AuthViewModel::class.java) -> AuthViewModel(repository as AuthRepository) as T
            //modelClass.isAssignableFrom(ShoeListViewModel::class.java) -> ShoeListViewModel(repository as ShoeListRepository) as T
            else -> throw IllegalArgumentException("ViewModel Class Not Found")
        }
    }
}