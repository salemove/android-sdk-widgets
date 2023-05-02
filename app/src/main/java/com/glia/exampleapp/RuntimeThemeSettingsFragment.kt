package com.glia.exampleapp

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

class RuntimeThemeSettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.runtime_theme_prefs, rootKey)
    }
}