package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.authentication.datastore.UserPreferences
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.utils.ViewModelFactory
import kotlinx.coroutines.runBlocking

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    companion object {
        const val TAG = "AuthActivity"
        const val SIGN_IN_RESULT_CODE = 1001
    }

    private lateinit var viewModel : AuthViewModel
    private lateinit var userPreferences : UserPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        FirebaseApp.initializeApp(this)

        userPreferences = UserPreferences(this)
        val userLoggedIn = runBlocking { UserPreferences(this@AuthenticationActivity).isUserLoggedIn() }
        if (userLoggedIn){
            navigateToRemindersActivity()
        } else {
            setContentView(R.layout.activity_authentication)

            val factory = ViewModelFactory(AuthRepository(userPreferences))
            viewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)

            viewModel.authenticationState.observe(this, Observer {
                when(it){
                    AuthViewModel.AuthenticationState.AUTHENTICATED -> {
                        navigateToRemindersActivity()
                    }
                    else -> { }
                }
            })

//          TODO: a bonus is to customize the sign in flow to look nice using :
            //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout

        }

    }

    private fun navigateToRemindersActivity() {
        val intent = Intent(this, RemindersActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        finish()
        startActivity(intent)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_RESULT_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // User successfully signed in
                val user = FirebaseAuth.getInstance().currentUser
                Log.i(AuthWelcomeFragment.TAG, "Successfully signed in user ${user?.displayName}!")
                viewModel.login(user?.email ?: "", "", user?.displayName ?: "")
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button. Otherwise check
                // response.getError().getErrorCode() and handle the error.
                Log.i(AuthWelcomeFragment.TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
            }
        }
    }

}
