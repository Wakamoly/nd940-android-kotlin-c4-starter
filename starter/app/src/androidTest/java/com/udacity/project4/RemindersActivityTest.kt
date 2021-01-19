package com.udacity.project4

import android.app.Application
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.action.ViewActions.typeText
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.testUtils.ReminderGenerator
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.EspressoIdlingResource.countingIdlingResource
import com.udacity.project4.util.monitorActivity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get


@RunWith(AndroidJUnit4::class)
@LargeTest
//END TO END test to black box test the app
class RemindersActivityTest : AutoCloseKoinTest() {
// Extended Koin Test - embed autoclose @after method to close Koin after every test

    private lateinit var repository: RemindersLocalRepository
    private lateinit var appContext: Application
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    /**
     * As we use Koin as a Service Locator Library to develop our code, we'll also use Koin to test our code.
     * at this step we will initialize Koin related code to be able to use it in out testing.
     */
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
            register(countingIdlingResource)
            register(dataBindingIdlingResource)
        }
    }

    @After
    fun unregisterIdlingResources() {
        IdlingRegistry.getInstance().apply {
            unregister(countingIdlingResource)
            unregister(dataBindingIdlingResource)
        }
    }

    @Test
    fun launch_WithSingleReminder() {
        val reminder = ReminderGenerator.reminderDTO1()
        runBlocking {
            repository.saveReminder(reminder)
        }

        val scenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(scenario)

        Espresso.onView(withText(reminder.title))
                .check(matches(isDisplayed()))
        Espresso.onView(withText(reminder.description))
                .check(matches(isDisplayed()))
        Espresso.onView(withText(reminder.location))
                .check(matches(isDisplayed()))
    }

    @Test
    fun addReminder_NavigateBack() {
        val scenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(scenario)

        // ReminderListFragment -> SaveReminderFragment
        Espresso.onView(withId(R.id.noDataTextView))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        Espresso.onView(withId(R.id.addReminderFAB)).perform(click())

        // SaveReminderFragment -> SelectLocationFragment
        Espresso.onView(withId(R.id.selectLocation)).perform(click())

        // SelectLocationFragment -> Set a location
        Espresso.onView(withId(R.id.map)).perform(click())
        Espresso.onView(withId(R.id.btn_save)).perform(click())

        // SaveReminderFragment -> TypeText title and desc, close keyboard, save
        Espresso.onView(withId(R.id.reminderTitle))
                .perform(typeText("Title"))
        Espresso.onView(withId(R.id.reminderDescription))
                .perform(typeText("Description"))
        Espresso.closeSoftKeyboard()
        Espresso.onView(withId(R.id.saveReminder)).perform(click())

        // ReminderListFragment -> Assert new reminder is saved and displayed, and NoData is gone
        Espresso.onView(withId(R.id.noDataTextView))
                .check(matches(withEffectiveVisibility(Visibility.GONE)))
        Espresso.onView(withText("Title"))
                .check(matches(isDisplayed()))
        Espresso.onView(withText("Description"))
                .check(matches(isDisplayed()))
    }

    @Test
    fun launch_navigateToMap() {
        val scenario = ActivityScenario.launch(RemindersActivity::class.java)
        dataBindingIdlingResource.monitorActivity(scenario)

        val reminder = ReminderGenerator.reminderDTO2()
        runBlocking {
            repository.saveReminder(reminder)
        }

        // ReminderListFragment -> SelectLocationFragment
        Espresso.onView(withId(R.id.noDataTextView))
                .check(matches(withEffectiveVisibility(Visibility.VISIBLE)))
        Espresso.onView(withId(R.id.selectLocationFragment)).perform(click())
    }

}