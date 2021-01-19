package com.udacity.project4.locationreminders.data

import com.udacity.project4.base.BaseRepository
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.Dispatchers

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource (
        private var reminders: MutableList<ReminderDTO>? = mutableListOf()
) : BaseRepository(Dispatchers.Main) {

    fun setReturnError(shouldReturnError: Boolean) {
        when (shouldReturnError){
            true -> { this.reminders = null }
        }
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return reminders.getReminders()
    }

    override suspend fun saveReminder(reminder: ReminderDTO) : Result<Any> = safeApiCall {
        when (val result = reminders.addReminder(reminder)) {
            true -> { Result.Success(result) }
            false -> { Result.Error("Couldn't add reminder!") }
            else -> { Result.Error("Unknown error") }
        }
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        val result = safeApiCall {
            reminders.getReminderByID(id)
        }
        return when(result){
            is Result.Success -> result.data
            is Result.Error -> Result.Error(result.message.toString())
            Result.Loading -> Result.Error("Crazy wacky error that happened for some ridiculous reason")
        }
    }

    override suspend fun deleteAllReminders() {
        safeApiCall {
            reminders.clearAll()
        }
    }


    /**
     * Extension functions to simulate DAO
     */

    private fun MutableList<ReminderDTO>?.addReminder(reminderDTO: ReminderDTO) : Boolean {
        this?.let {
            add(reminderDTO)
            return true
        }
        return false
    }

    private fun MutableList<ReminderDTO>?.clearAll() : Boolean {
        this?.let {
            clear()
            return true
        }
        return false
    }

    private fun MutableList<ReminderDTO>?.getReminderByID(id: String) : Result<ReminderDTO> {
        this?.firstOrNull {
            it.id == id
        }?.let {
            return Result.Success(it)
        }
        return Result.Error("Couldn't find reminder by ID!")
    }

    private fun MutableList<ReminderDTO>?.getReminders() : Result<List<ReminderDTO>> {
        this?.let {
            return Result.Success(it)
        }
        return Result.Error("Reminders not available!")
    }

}