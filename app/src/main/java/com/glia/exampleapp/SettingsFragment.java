package com.glia.exampleapp;

import android.os.Bundle;

import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.glia.widgets.GliaWidgets;

public class SettingsFragment extends PreferenceFragmentCompat {

    public SettingsFragment() {
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.all_prefs, rootKey);
        setPreferenceDescription(R.string.pref_glia_core_sdk_version, GliaWidgets.getWidgetsCoreSdkVersion());
        setPreferenceDescription(R.string.pref_glia_widgets_sdk_version, GliaWidgets.getWidgetsSdkVersion());
    }

    private void setPreferenceDescription(int preferenceId, String value) {
        Preference preference = findPreference(getString(preferenceId));
        if (preference != null) {
            preference.setSummary(value);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(Preference preference) {
        // Normally, preferences can point to sub preference screens using the `android:fragment` or `setFragment`.
        // However in our case we are using `NavHostFragment` for fragment management and navigation so
        // we need to use for all fragment inside same activity or we will get an error:
        //
        // IllegalStateException: Fragment 'MyFragment' declared target fragment 'AnotherMyFragment'
        // that does not belong to this FragmentManager!
        String baseSettingsKey = getString(R.string.pref_glia_basic_settings);
        String remoteSettingsKey = getString(R.string.pref_glia_remote_theme_settings);
        String currentKey = preference.getKey();
        NavController navController = NavHostFragment.findNavController(this);
        if (currentKey.equals(baseSettingsKey)) {
            navController.navigate(R.id.base_settings);
        } else if (currentKey.equals(remoteSettingsKey)) {
            navController.navigate(R.id.remote_settings);
        } else {
            return super.onPreferenceTreeClick(preference);
        }
        return true;
    }
}
