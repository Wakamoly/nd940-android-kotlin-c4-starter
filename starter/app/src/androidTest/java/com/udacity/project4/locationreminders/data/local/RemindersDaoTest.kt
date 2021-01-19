package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.SmallTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.testUtils.ReminderGenerator
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {

    private lateinit var database: RemindersDatabase
    private lateinit var remindersDao: RemindersDao
    private lateinit var reminder: ReminderDTO

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun initDb() {
        // Build our database and create a reminder object
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
        reminder = ReminderGenerator.reminderDTO1()
        remindersDao = database.reminderDao()
    }

    @After
    fun closeDb() {
        // close the database after each test
        database.close()
    }

    @Test
    fun insertReminder_GetById() = runBlockingTest {
        // GIVEN -> Insert reminder created in initDb()
        remindersDao.saveReminder(reminder)

        // WHEN -> Retrieve reminder by the ID inserted
        val reminderInserted = remindersDao.getReminderById(reminder.id)

        // THEN -> The inserted data has the expected outcomes
        assertThat(reminderInserted, notNullValue())
        assertThat(reminderInserted.id, `is`(reminder.id))
        assertThat(reminderInserted.title, `is`(reminder.title))
        assertThat(reminderInserted.description, `is`(reminder.description))
        assertThat(reminderInserted.location, `is`(reminder.location))
        assertThat(reminderInserted.latitude, `is`(reminder.latitude))
        assertThat(reminderInserted.longitude, `is`(reminder.longitude))
    }

    @Test
    fun deleteReminders_returnEmptyList() = runBlockingTest {
        // GIVEN -> Insert reminder created in initDb()
        remindersDao.saveReminder(reminder)

        // WHEN -> Our list of reminders now contains our reminder data item
        assertThat(remindersDao.getReminders(), hasItem(reminder))

        // THEN -> Remove all, ensure list is empty
        remindersDao.deleteAllReminders()
        assertThat(remindersDao.getReminders().isEmpty(), `is`(true))
    }

}