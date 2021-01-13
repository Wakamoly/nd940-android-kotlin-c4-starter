package com.udacity.project4.locationreminders.data.local

import com.udacity.project4.base.BaseRepository
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import kotlinx.coroutines.*

/**
 * Concrete implementation of a data source as a db.
 *
 * The repository is implemented so that you can focus on only testing it.
 *
 * @param remindersDao the dao that does the Room db operations
 * @param ioDispatcher has been moved to BaseRepository calls
 */
class RemindersLocalRepository(
    private val remindersDao: RemindersDao
) : ReminderDataSource, BaseRepository() {

    /**
     * Get the reminders list from the local db
     * @return Result the holds a Success with all the reminders or an Error object with the error message
     */
    override suspend fun getReminders(): Result<List<ReminderDTO>> = safeApiCall {
        remindersDao.getReminders()
    }

    /**
     * Insert a reminder in the db.
     * @param reminder the reminder to be inserted
     */
    override suspend fun saveReminder(reminder: ReminderDTO) {
        safeApiCall {
            remindersDao.saveReminder(reminder)
        }
    }

    /**
     * Get a reminder by its id
     * @param id to be used to get the reminder
     * @return Result the holds a Success object with the Reminder or an Error object with the error message
     */
    override suspend fun getReminder(id: String): Result<ReminderDTO> = safeApiCall {
        remindersDao.getReminderById(id)
    }

    /**
     * Deletes all the reminders in the db
     */
    override suspend fun deleteAllReminders() {
        safeApiCall {
            remindersDao.deleteAllReminders()
        }
    }
}
