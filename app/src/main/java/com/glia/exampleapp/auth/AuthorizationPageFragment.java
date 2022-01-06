package com.glia.exampleapp.auth;

import android.os.Bundle;

import androidx.preference.PreferenceFragmentCompat;

public class AuthorizationPageFragment extends PreferenceFragmentCompat {
    private final int titleRes;
    private final int authType;
    private final int prefRes;

    public AuthorizationPageFragment(int authType, int titleRes, int prefRes) {
        this.titleRes = titleRes;
        this.authType = authType;
        this.prefRes = prefRes;
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(prefRes, rootKey);
    }

    public int getTitleResource() {
        return titleRes;
    }

    public int getAuthType() {
        return authType;
    }
}
