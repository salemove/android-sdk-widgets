package com.glia.exampleapp

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

class RemoteThemeSettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.remote_theme_prefs, rootKey)
    }
}
