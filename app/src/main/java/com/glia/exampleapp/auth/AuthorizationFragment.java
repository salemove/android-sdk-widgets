package com.glia.exampleapp.auth;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.widget.ViewPager2;

import com.glia.exampleapp.R;
import com.glia.exampleapp.auth.adapter.AuthorizationPagerAdapter;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.List;


public class AuthorizationFragment extends Fragment {
    private final List<AuthorizationPageFragment> authorizationPages = new ArrayList<AuthorizationPageFragment>() {{
        add(new AuthorizationPageFragment(
                AuthorizationType.APP_TOKEN,
                R.string.authorization_app_token,
                R.xml.auth_token
        ));
        add(new AuthorizationPageFragment(
                AuthorizationType.SITE_API_KEY,
                R.string.authorization_site_api_key,
                R.xml.auth_site_api_key
        ));
    }};

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    public AuthorizationFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.authorization_fragment, container, false);
        tabLayout = view.findViewById(R.id.tab_layout);
        viewPager = view.findViewById(R.id.view_pager);
        viewPager.setAdapter(new AuthorizationPagerAdapter<>(this, authorizationPages));
        setupMediator();
        return view;
    }

    private void setupMediator() {
        new TabLayoutMediator(tabLayout, viewPager,
                (tab, position) ->
                        tab.setText(authorizationPages.get(position).getTitleResource())
        ).attach();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        int item = sharedPreferences.getInt(getString(R.string.pref_authorization_type), AuthorizationType.DEFAULT);
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                sharedPreferences.edit()
                        .putInt(
                                getString(R.string.pref_authorization_type),
                                authorizationPages.get(position).getAuthType()
                        ).apply();
            }
        });
        viewPager.post(() -> viewPager.setCurrentItem(item, false));
    }
}

