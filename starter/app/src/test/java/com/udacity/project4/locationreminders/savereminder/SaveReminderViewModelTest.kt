package com.udacity.project4.locationreminders.savereminder

import android.app.Application
import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.testUtils.MainCoroutineRule
import com.udacity.project4.testUtils.ReminderGenerator
import com.udacity.project4.testUtils.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert
import org.junit.Assert
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@ExperimentalCoroutinesApi

// Avoid error : [[Robolectric] WARN: Android SDK 10000 requires Java 9 (have Java 8). Tests won't be run on SDK 10000 unless explicitly requested.]
@Config(sdk = [Build.VERSION_CODES.O_MR1])

@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var saveReminderViewModel: SaveReminderViewModel
    private lateinit var fakeDataSource: FakeDataSource

    private lateinit var app: Application

    @Before
    fun setupViewModel() = mainCoroutineRule.runBlockingTest {
        // Avoid error : [org.koin.core.error.KoinAppAlreadyStartedException: A Koin Application has already been started]
        stopKoin()
        app = ApplicationProvider.getApplicationContext()
        fakeDataSource = FakeDataSource()

        saveReminderViewModel = SaveReminderViewModel(app, fakeDataSource)
    }

    @Test
    fun saveReminder_checkLoading() = mainCoroutineRule.runBlockingTest {
        mainCoroutineRule.pauseDispatcher()
        saveReminderViewModel.validateAndSaveReminder(ReminderGenerator.reminderValidDI())
        MatcherAssert.assertThat(
            saveReminderViewModel.showLoading.getOrAwaitValue(),
            `is`(true)
        )

        mainCoroutineRule.resumeDispatcher()
        MatcherAssert.assertThat(
            saveReminderViewModel.showLoading.getOrAwaitValue(),
            `is`(false)
        )
    }

    @Test
    fun saveReminder_success() = mainCoroutineRule.runBlockingTest {
        saveReminderViewModel.validateAndSaveReminder(ReminderGenerator.reminderValidDI())

        Assert.assertEquals(
            saveReminderViewModel.showToast.getOrAwaitValue(),
            app.getString(R.string.reminder_saved)
        )

        Assert.assertEquals(
            saveReminderViewModel.navigationCommand.getOrAwaitValue(),
            NavigationCommand.Back
        )
    }

    @Test
    fun validateReminderDataItems() = mainCoroutineRule.runBlockingTest {
        // Data with null title
        MatcherAssert.assertThat(
            saveReminderViewModel.validateAndSaveReminder(ReminderGenerator.reminderNullTitleDI()),
            `is`(false)
        )
        MatcherAssert.assertThat(
            saveReminderViewModel.showSnackBarInt.getOrAwaitValue(),
            `is`(R.string.err_enter_title)
        )

        // Data with null location
        MatcherAssert.assertThat(
            saveReminderViewModel.validateAndSaveReminder(ReminderGenerator.reminderNullLocationDI()),
            `is`(false)
        )
        MatcherAssert.assertThat(
            saveReminderViewModel.showSnackBarInt.getOrAwaitValue(),
            `is`(R.string.err_select_location)
        )

        // Valid data
        MatcherAssert.assertThat(
            saveReminderViewModel.validateAndSaveReminder(ReminderGenerator.reminderValidDI()),
            `is`(true)
        )
    }

}
