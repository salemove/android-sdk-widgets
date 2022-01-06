package com.glia.exampleapp.auth.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import java.util.List;

public class AuthorizationPagerAdapter<T extends Fragment> extends FragmentStateAdapter {
    private final List<T> authorizationScreens;

    public AuthorizationPagerAdapter(Fragment fragment, List<T> authorizationScreens) {
        super(fragment);
        this.authorizationScreens = authorizationScreens;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return authorizationScreens.get(position);
    }

    @Override
    public int getItemCount() {
        return authorizationScreens.size();
    }
}
