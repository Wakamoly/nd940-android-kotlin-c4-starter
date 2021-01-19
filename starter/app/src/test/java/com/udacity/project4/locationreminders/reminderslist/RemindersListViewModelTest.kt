package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.testUtils.MainCoroutineRule
import com.udacity.project4.testUtils.ReminderGenerator
import com.udacity.project4.testUtils.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert
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
class RemindersListViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    var mainCoroutineRule = MainCoroutineRule()

    private lateinit var remindersListViewModel: RemindersListViewModel
    private lateinit var fakeDataSource: FakeDataSource

    @Before
    fun setupViewModel() = mainCoroutineRule.runBlockingTest {
        // Avoid error : [org.koin.core.error.KoinAppAlreadyStartedException: A Koin Application has already been started]
        stopKoin()

        fakeDataSource = FakeDataSource().apply {
            saveReminder(ReminderGenerator.reminderDTO1())
            saveReminder(ReminderGenerator.reminderDTO2())
        }

        remindersListViewModel = RemindersListViewModel(
            fakeDataSource
        )
    }

    @Test
    fun loadReminders_checkLoading() = mainCoroutineRule.runBlockingTest {
        mainCoroutineRule.pauseDispatcher()
        remindersListViewModel.loadReminders()
        MatcherAssert.assertThat(
            remindersListViewModel.showLoading.getOrAwaitValue(),
            `is`(true)
        )

        mainCoroutineRule.resumeDispatcher()
        MatcherAssert.assertThat(
            remindersListViewModel.showLoading.getOrAwaitValue(),
            `is`(false)
        )
    }

    @Test
    fun loadReminders_withReminders_resultNotEmpty() = mainCoroutineRule.runBlockingTest {
        remindersListViewModel.loadReminders()

        MatcherAssert.assertThat(
            remindersListViewModel.remindersList.getOrAwaitValue().isEmpty(),
            `is`(false)
        )
        MatcherAssert.assertThat(
            remindersListViewModel.showNoData.getOrAwaitValue(),
            `is`(false)
        )
    }

    @Test
    fun loadReminders_remindersUnavailable_showError() = mainCoroutineRule.runBlockingTest {
        fakeDataSource.setReturnError(true)
        remindersListViewModel.loadReminders()

        MatcherAssert.assertThat(
            remindersListViewModel.showSnackBar.getOrAwaitValue(),
            `is`("Reminders not available!")
        )
    }

}
