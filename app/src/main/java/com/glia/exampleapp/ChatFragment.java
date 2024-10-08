package com.glia.exampleapp;

import static java.util.Collections.singletonList;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.preference.PreferenceManager;

import com.glia.widgets.UiTheme;
import com.glia.widgets.chat.ChatView;

import java.util.Collections;

public class ChatFragment extends Fragment {

    private NavController navController;
    private ChatView chatView;
    private ChatView.OnEndListener onEndListener = () -> navController.popBackStack();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.chat_fragment, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        navController = NavHostFragment.findNavController(this);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        chatView = view.findViewById(R.id.chat_view);
        UiTheme theme = Utils.getRunTimeThemeByPrefs(sharedPreferences, getResources());
        chatView.setUiTheme(theme);
        chatView.setOnEndListener(onEndListener);
        chatView.startChat(
                Utils.getStringFromPrefs(R.string.pref_company_name, getString(R.string.settings_value_default_company_name), sharedPreferences, getResources()),
                singletonList(Utils.getStringFromPrefs(R.string.pref_queue_id, getString(R.string.glia_queue_id), sharedPreferences, getResources())),
                Utils.getStringFromPrefs(R.string.pref_context_asset_id, null, sharedPreferences, getResources()));
    }

    @Override
    public void onResume() {
        chatView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        onEndListener = null;
        chatView.onDestroyView();
        super.onDestroyView();
    }
}
