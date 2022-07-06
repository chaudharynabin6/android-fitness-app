package com.androiddevs.runningappyt.ui.fragments

import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.fragment.findNavController
import com.androiddevs.runningappyt.R
import com.androiddevs.runningappyt.other.Constants.KEY_FIRST_TIME_TOGGLE
import com.androiddevs.runningappyt.other.Constants.KEY_NAME
import com.androiddevs.runningappyt.other.Constants.KEY_WEIGHT
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_setup.*
import javax.inject.Inject
import kotlin.properties.Delegates

@AndroidEntryPoint
class SetupFragment : Fragment(R.layout.fragment_setup) {

    @Inject
    lateinit var sharedPref: SharedPreferences


    //    @set:Named(KEY_FIRST_TIME_TOGGLE)
//    @set:Inject()
//    var isFirstAppOpen by Delegates.notNull<Boolean>()

    //    @set:Inject
    var name: String = ""

    //    @set:Inject
    var weight: Float = 20f



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val isFirstAppOpen: Boolean = sharedPref.getBoolean(KEY_FIRST_TIME_TOGGLE, false)

        if (isFirstAppOpen) {
            val action = SetupFragmentDirections.actionSetupFragmentToRunFragment()
            findNavController().navigate(
                action
            )
        }
        tvContinue.setOnClickListener {
            val success = writePersonalDataToSharedPref()
            if (success) {
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.setupFragment, true)
                    .build()
                val action = SetupFragmentDirections.actionSetupFragmentToRunFragment()
                findNavController().navigate(
                    action.actionId,
                    savedInstanceState,
                    navOptions
                )
            } else {
                Snackbar.make(
                    requireActivity().findViewById(R.id.rootView),
                    "Please enter all the fields",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun writePersonalDataToSharedPref(): Boolean {
        val name = etName.text.toString()
        val weight = etWeight.text.toString()

        if (name.isEmpty() || weight.isEmpty()) {
            return false
        }
        sharedPref.edit()
            .putString(KEY_NAME, name)
            .putFloat(KEY_WEIGHT, weight.toFloat())
            .putBoolean(KEY_FIRST_TIME_TOGGLE, true)
            .commit()
        val toolbarText = "Let's go $name"
        requireActivity().tvToolbarTitle.text = toolbarText
        return true
    }
}