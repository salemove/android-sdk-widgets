package com.glia.exampleapp;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import com.glia.exampleapp.auth.AuthorizationType;

public class SettingsFragment extends PreferenceFragmentCompat {

    private SharedPreferences sharedPreferences;

    public SettingsFragment() {
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
        sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        initAuthorizationPreferenceClickListener();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateAuthorizationDescription();
    }

    private void initAuthorizationPreferenceClickListener() {
        NavController navController = NavHostFragment.findNavController(this);
        Preference authorizationButtonPreference = findPreference(getString(R.string.pref_app_authorization));
        if (authorizationButtonPreference != null)
            authorizationButtonPreference.setOnPreferenceClickListener(preference -> {
                navController.navigate(R.id.authorization);
                return true;
            });
    }

    private void updateAuthorizationDescription() {
        int authorizationType = sharedPreferences.getInt(getString(R.string.pref_authorization_type), AuthorizationType.DEFAULT);
        Preference preference = findPreference(getString(R.string.pref_app_authorization));
        if (preference != null) setAuthorizationSummary(preference, authorizationType);
    }

    private void setAuthorizationSummary(Preference preference, int authorizationType) {
        if (authorizationType == AuthorizationType.APP_TOKEN)
            setAppTokenSummary(preference);
        else if (authorizationType == AuthorizationType.SITE_API_KEY)
            setApiKeySummary(preference);
    }

    private void setAppTokenSummary(Preference preference) {
        preference.setSummary(R.string.authorization_app_token);
    }

    private void setApiKeySummary(Preference preference) {
        preference.setSummary(R.string.authorization_site_api_key);
    }
}
