package com.udacity.project4.authentication

import com.udacity.project4.authentication.datastore.UserPreferences
import com.udacity.project4.base.BaseRepository

class AuthRepository (
    private val userPreferences: UserPreferences
) : BaseRepository() {

    suspend fun login(
        email: String, password: String, username: String
    ) {
        // Database operations or network calls
        saveCredentials(username, email)
    }

    private suspend fun saveCredentials(username: String, email: String){
        userPreferences.saveCredentials(username, email)
    }

}
