package com.udacity.project4.authentication

import com.udacity.project4.authentication.datastore.UserPreferences
import com.udacity.project4.base.BaseRepository
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

class AuthRepository (
    private val userPreferences: UserPreferences,
    dispatcher: CoroutineDispatcher = Dispatchers.IO
) : BaseRepository (dispatcher) {

    suspend fun login(
        email: String, username: String
    ) {
        // Database operations or network calls
        saveCredentials(username, email)
    }

    private suspend fun saveCredentials(username: String, email: String){
        userPreferences.saveCredentials(username, email)
    }

    // TODO: 1/18/2021 Cleanup unused abstractions
    override suspend fun getReminders(): Result<List<ReminderDTO>> { TODO("Not yet implemented") }
    override suspend fun saveReminder(reminder: ReminderDTO): Result<Any> { TODO("Not yet implemented") }
    override suspend fun getReminder(id: String): Result<ReminderDTO> { TODO("Not yet implemented") }
    override suspend fun deleteAllReminders() { TODO("Not yet implemented") }

}
