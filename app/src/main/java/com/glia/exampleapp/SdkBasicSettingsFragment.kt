package com.glia.exampleapp

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat

class SdkBasicSettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.sdk_basic_prefs, rootKey)
    }
}
