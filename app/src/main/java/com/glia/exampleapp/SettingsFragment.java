package com.glia.exampleapp;

import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.glia.widgets.GliaWidgets;

public class SettingsFragment extends PreferenceFragmentCompat {

    public SettingsFragment() {
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        setPreferenceDescription(R.string.pref_glia_core_sdk_version, GliaWidgets.getWidgetsCoreSdkVersion());
        setPreferenceDescription(R.string.pref_glia_widgets_sdk_version, GliaWidgets.getWidgetsSdkVersion());
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    private void setPreferenceDescription(int preferenceId, String value) {
        Preference preference = findPreference(getString(preferenceId));
        if (preference != null) {
            preference.setSummary(value);
        }
    }

}
