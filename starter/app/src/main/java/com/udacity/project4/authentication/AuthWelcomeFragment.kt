package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.activityViewModels
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.databinding.AuthWelcomeFragmentBinding
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersDao
import com.udacity.project4.utils.ViewModelFactory

class AuthWelcomeFragment : BaseFragment<AuthViewModel, AuthWelcomeFragmentBinding, AuthRepository>() {

    companion object {
        const val TAG = "AuthWelcomeFrag"
        const val SIGN_IN_RESULT_CODE = 1001
    }

    // TODO: 1/3/21 Remove one of these view model declarations

    private val authViewModel : AuthViewModel by activityViewModels { ViewModelFactory(AuthRepository(userPreferences)) }

    private lateinit var remindersDao: RemindersDao

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mCtx = requireContext()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // TODO: Implement the create account and sign in using FirebaseUI, use sign in using email and sign in using Google
        binding.loginButton.setOnClickListener { launchSignInFlow() }

    }

    private fun launchSignInFlow() {
        // Give users the option to sign in / register with their email or Google account. If users
        // choose to register with their email, they will need to create a password as well.
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
        )

        // Create and launch sign-in intent. We listen to the response of this activity with the
        // SIGN_IN_RESULT_CODE code.
        startActivityForResult(
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(
                providers
            ).build(), SIGN_IN_RESULT_CODE
        )
    }

    override fun getViewModel() = AuthViewModel::class.java

    override fun getFragmentBinding(
        inflater: LayoutInflater,
        container: ViewGroup?
    ) = AuthWelcomeFragmentBinding.inflate(inflater, container, false)

    override fun getFragmentRepository() = AuthRepository(userPreferences)

}