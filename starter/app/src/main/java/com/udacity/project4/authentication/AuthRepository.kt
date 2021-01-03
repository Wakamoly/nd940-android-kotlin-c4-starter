package com.udacity.project4.authentication

import com.udacity.project4.authentication.datastore.UserPreferences
import com.udacity.project4.base.BaseRepository

class AuthRepository (
    private val userPreferences: UserPreferences
) : BaseRepository() {



}
