package com.glia.exampleapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.preference.PreferenceManager;

import com.glia.widgets.GliaWidgets;
import com.glia.widgets.UiTheme;
import com.glia.widgets.chat.ChatActivity;

public class MainFragment extends Fragment {

    public MainFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FragmentActivity activity = requireActivity();
        view.findViewById(R.id.settings_button).setOnClickListener(view1 -> {
            NavController navController =
                    Navigation.findNavController(activity, R.id.nav_host_fragment);
            navController.navigate(R.id.settings);
        });
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        view.findViewById(R.id.integrator_chat).setOnClickListener(view1 -> {
            NavController navController =
                    Navigation.findNavController(activity, R.id.nav_host_fragment);
            navController.navigate(R.id.chat);
        });
        view.findViewById(R.id.activity_button).setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ChatActivity.class);
            intent.putExtra(GliaWidgets.COMPANY_NAME,
                    Utils.getStringFromPrefs(R.string.pref_company_name, getString(R.string.settings_value_default_company_name), sharedPreferences, getResources()));
            intent.putExtra(GliaWidgets.QUEUE_ID,
                    Utils.getStringFromPrefs(R.string.pref_queue_id, getString(R.string.queue_id), sharedPreferences, getResources()));
            intent.putExtra(GliaWidgets.CONTEXT_URL,
                    Utils.getStringFromPrefs(R.string.pref_context_url, getString(R.string.queue_id), sharedPreferences, getResources()));
            UiTheme uiTheme = Utils.getUiThemeByPrefs(sharedPreferences, getResources());
            intent.putExtra(GliaWidgets.UI_THEME, uiTheme);
            startActivity(intent);
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        GliaWidgets.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
