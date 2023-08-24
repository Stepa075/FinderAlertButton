package com.stepa0751.finderalertbutton.fragments

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.stepa0751.finderalertbutton.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}