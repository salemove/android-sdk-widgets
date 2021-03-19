package com.glia.exampleapp;

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

import com.glia.widgets.GliaWidgets;
import com.glia.widgets.UiTheme;
import com.glia.widgets.chat.ChatView;

public class ChatFragment extends Fragment {

    private NavController navController;
    private ChatView chatView;
    private ChatView.OnBackClickedListener onBackClickedListener = () -> {
        chatView.backPressed();
        navController.popBackStack();
    };
    private ChatView.OnEndListener onEndListener = () -> navController.popBackStack();
    private NavController.OnDestinationChangedListener onDestinationChangedListener =
            (controller, destination, arguments) -> {
                if (destination.getId() == R.id.main_fragment && chatView != null) {
                    chatView.backPressed();
                }
            };

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
        navController.addOnDestinationChangedListener(onDestinationChangedListener);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        chatView = view.findViewById(R.id.chat_view);
        UiTheme theme = Utils.getUiThemeByPrefs(sharedPreferences, getResources());
        chatView.setTheme(theme);
        chatView.setOnEndListener(onEndListener);
        chatView.setOnBackClickedListener(onBackClickedListener);
        chatView.startEmbeddedChat(
                Utils.getStringFromPrefs(R.string.pref_company_name, getString(R.string.settings_value_default_company_name), sharedPreferences, getResources()),
                Utils.getStringFromPrefs(R.string.pref_queue_id, getString(R.string.queue_id), sharedPreferences, getResources()),
                Utils.getStringFromPrefs(R.string.pref_context_url, getString(R.string.queue_id), sharedPreferences, getResources()),
                savedInstanceState);
    }

    @Override
    public void onResume() {
        chatView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroyView() {
        onBackClickedListener = null;
        onEndListener = null;
        navController.removeOnDestinationChangedListener(onDestinationChangedListener);
        onBackClickedListener = null;
        chatView.onDestroyView();
        super.onDestroyView();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        GliaWidgets.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
}
