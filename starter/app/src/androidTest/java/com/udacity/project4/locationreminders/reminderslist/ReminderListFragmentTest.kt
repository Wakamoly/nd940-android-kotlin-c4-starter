package com.udacity.project4.locationreminders.reminderslist

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.view.Gravity
import androidx.appcompat.widget.Toolbar
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.recyclerview.widget.RecyclerView
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.contrib.DrawerMatchers.isOpen
import androidx.test.espresso.contrib.RecyclerViewActions
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.testUtils.ReminderGenerator
import com.udacity.project4.testUtils.ReminderMapper.toDomainModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.EspressoIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.util.monitorFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest : AutoCloseKoinTest() {

    private lateinit var repository: RemindersLocalRepository
    private lateinit var appContext: Application
    // An Idling Resource that waits for Data Binding to have no pending bindings.
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @get:Rule
    val instantExecutorRule = InstantTaskExecutorRule()

    @Before
    fun init() {
        stopKoin() //stop the original app koin
        appContext = getApplicationContext()

        val myModule = module {

            single { LocalDB.createRemindersDao(appContext) }
            single { RemindersLocalRepository(get()) }

            //Declare singleton definitions to be later injected using by inject()
            single {
                RemindersListViewModel(
                    get() as RemindersLocalRepository
                )
            }

            single {
                //This view model is declared singleton to be used across multiple fragments
                SaveReminderViewModel(
                    get(),
                    get() as RemindersLocalRepository
                )
            }

        }

        //declare a new koin module
        startKoin {
            androidContext(appContext)
            modules(listOf(myModule))
        }

        //Get our real repository
        repository = get()

        //clear the data to start fresh
        runBlocking {
            repository.deleteAllReminders()
        }
    }

    @Before
    fun registerIdlingResources() {
        IdlingRegistry.getInstance().apply {
            register(EspressoIdlingResource.countingIdlingResource)
            register(dataBindingIdlingResource)
        }
    }

    @After
    fun unregisterIdlingResources() {
        IdlingRegistry.getInstance().apply {
            unregister(EspressoIdlingResource.countingIdlingResource)
            unregister(dataBindingIdlingResource)
        }
    }

    @Test
    fun tapFAB_navigateToSaveReminderFragment() {
        // GIVEN -> Reminders fragment with empty list
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)

        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN -> Tap on FAB
        onView(withId(R.id.addReminderFAB)).perform(click())

        // THEN -> Verify navigation to SaveReminderFragment
        verify(navController).navigate(
            ReminderListFragmentDirections.reminderListFragmentToSaveReminder()
        )
    }

    @Test
    fun tapItem_navigateToSaveReminderFragment() {
        // GIVEN -> Start reminders list with 2 reminders
        val reminder1 = ReminderGenerator.reminderDTO1()
        val reminder2 = ReminderGenerator.reminderDTO2()
        runBlocking {
            repository.saveReminder(reminder1)
            repository.saveReminder(reminder2)
        }

        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)

        val navController = mock(NavController::class.java)
        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        // WHEN -> Tap on reminder1's item
        onView(withId(R.id.reminders_recycler_view)).perform(
            RecyclerViewActions.actionOnItem<RecyclerView.ViewHolder>(
                hasDescendant(withText(reminder1.title)),
                click()
            )
        )

        // THEN -> Verify navigation to SaveReminderFragment with argument, converted to domain model from DTO
        verify(navController).navigate(
            ReminderListFragmentDirections.reminderListFragmentToSaveReminder(reminder1.toDomainModel())
        )
    }

    @Test
    fun remindersScreen_clickToMap_opensMapFragment() {
        // Start the Tasks screen.
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(activityScenario)

        // 1. Check that left drawer is closed at startup.
        onView(withId(R.id.selectLocationFragment)).perform(click())

        // 1.1. Navigation - back once
        onView(
            withContentDescription(
                activityScenario
                    .getToolbarNavigationContentDescription()
            )
        ).perform(click())

        // 2. Open drawer by clicking drawer icon.
        onView(
            withContentDescription(
                activityScenario
                    .getToolbarNavigationContentDescription()
            )
        ).perform(click())

        // 3. Check if drawer is open.
        onView(withId(R.id.drawer_layout))
            .check(matches(isOpen(Gravity.START))) // Left drawer is open.

        // When using ActivityScenario.launch(), always call close()
        activityScenario.close()
    }

    @Test
    fun loadReminders_withReminders_showItems() {
        // GIVEN -> Save two reminders
        val reminder1 = ReminderGenerator.reminderDTO1()
        val reminder2 = ReminderGenerator.reminderDTO2()
        runBlocking {
            repository.saveReminder(reminder1)
            repository.saveReminder(reminder2)
        }

        // WHEN -> Setup scenario
        val scenario = launchFragmentInContainer<ReminderListFragment>(
            Bundle(),
            R.style.AppTheme
        )
        dataBindingIdlingResource.monitorFragment(scenario)

        // THEN -> Check that both reminders AND their data is displayed successfully
        onView(withText(reminder1.title)).check(matches(isDisplayed()))
        onView(withText(reminder1.description)).check(matches(isDisplayed()))
        onView(withText(reminder1.location)).check(matches(isDisplayed()))

        onView(withText(reminder2.title)).check(matches(isDisplayed()))
        onView(withText(reminder2.description)).check(matches(isDisplayed()))
        onView(withText(reminder2.location)).check(matches(isDisplayed()))
    }

    @Test
    fun loadReminderList_withoutReminders_showsNoData() {
        val scenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(scenario)
        onView(withId(R.id.noDataTextView)).check(matches(isDisplayed()))
    }

}

fun <T : Activity> ActivityScenario<T>.getToolbarNavigationContentDescription()
        : String {
    var description = ""
    onActivity {
        description =
            it.findViewById<Toolbar>(R.id.toolbar).navigationContentDescription as String
    }
    return description
}