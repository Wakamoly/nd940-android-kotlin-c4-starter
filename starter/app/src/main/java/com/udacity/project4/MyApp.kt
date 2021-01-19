package com.udacity.project4

import android.app.Application
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.dsl.module

class MyApp : Application() {

    override fun onCreate() {
        super.onCreate()

        /**
         * use Koin Library as a service locator
         */
        val myModule = module {

            single { LocalDB.createRemindersDao(this@MyApp) }
            single { RemindersLocalRepository( get() ) }

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

        startKoin {
            androidContext(this@MyApp)
            modules(listOf(myModule))
        }

    }
}