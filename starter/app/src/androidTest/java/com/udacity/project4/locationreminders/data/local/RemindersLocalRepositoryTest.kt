package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.testUtils.ReminderGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Medium Test to test the repository
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var database: RemindersDatabase
    private lateinit var remindersRepository: RemindersLocalRepository
    private lateinit var reminder: ReminderDTO

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun setup() {
        // Build our database, create our repository with Main thread, and create a reminder object
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
        remindersRepository = RemindersLocalRepository(database.reminderDao(), Dispatchers.Main)
        reminder = ReminderGenerator.reminderDTO1()
    }

    @After
    fun closeDb() {
        // close the database after each test
        database.close()
    }

    @Test
    fun saveReminder_retrieveReminderByID() = runBlocking {
        // GIVEN -> Save the reminder created in setup()
        remindersRepository.saveReminder(reminder)

        // WHEN -> Retrieve reminder inserted by ID
        val result = remindersRepository.getReminder(reminder.id) as Result.Success
        val data = result.data

        // THEN -> Expected outcomes
        assertThat(data, notNullValue())
        assertThat(data.id, `is`(reminder.id))
        assertThat(data.title, `is`(reminder.title))
        assertThat(data.description, `is`(reminder.description))
        assertThat(data.location, `is`(reminder.location))
        assertThat(data.latitude, `is`(reminder.latitude))
        assertThat(data.longitude, `is`(reminder.longitude))
    }

    @Test
    fun deleteReminders_returnsEmptyList() = runBlocking {
        // GIVEN -> Save the reminder created in setup()
        remindersRepository.saveReminder(reminder)

        // WHEN -> Get all reminders, assert the list contains our reminder data
        var reminders = remindersRepository.getReminders() as Result.Success
        assertThat(reminders.data, hasItem(reminder))

        // THEN -> Remove all reminders, assert the list of saved reminders is empty
        remindersRepository.deleteAllReminders()
        reminders = remindersRepository.getReminders() as Result.Success
        assertThat(reminders.data.isEmpty(), `is`(true))
    }

    @Test
    fun retrieveNonexistantData_returnsError() = runBlocking {
        val nilReminder = remindersRepository.getReminder(reminder.id) as Result.Error
        assertThat(nilReminder.message, `is`("Reminder does not exist!"))
    }

}