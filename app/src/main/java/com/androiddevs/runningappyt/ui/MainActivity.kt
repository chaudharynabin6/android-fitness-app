package com.androiddevs.runningappyt.ui

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.androiddevs.runningappyt.R
import com.androiddevs.runningappyt.other.Constants
import com.androiddevs.runningappyt.ui.fragments.TrackingFragmentDirections
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var sharedPref : SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        navigateToTrackingFragmentIfNeeded(intent)
        setSupportActionBar(toolbar)
        bottomNavigationView.setupWithNavController(navController = navHostFragment.findNavController())

        val name: String = sharedPref.getString(Constants.KEY_NAME, "") ?: "Running App"
        val toolbarText = "Let's go $name"
        tvToolbarTitle.text = toolbarText

        navHostFragment.findNavController()
            .addOnDestinationChangedListener { controller, destination, arguments ->

                when (destination.id) {
                    R.id.settingFragment, R.id.runFragment, R.id.statisticFragment ->
                        bottomNavigationView.visibility = View.VISIBLE
                    else -> bottomNavigationView.visibility = View.GONE
                }
            }
    }

    private fun navigateToTrackingFragmentIfNeeded(intent: Intent?) {

        intent?.let {
            if (
                it.action == Constants.ACTION_SHOW_TRACKING_FRAGMENT
            ) {
                val action = TrackingFragmentDirections.actionGlobalTrackingFragment()
                navHostFragment.findNavController().navigate(action)
            }
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        navigateToTrackingFragmentIfNeeded(intent)
    }

}
