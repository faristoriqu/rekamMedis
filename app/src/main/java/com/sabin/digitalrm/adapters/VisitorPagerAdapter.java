package com.sabin.digitalrm.adapters;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by xkill on 25/10/18.
 */

public class VisitorPagerAdapter extends FragmentPagerAdapter {
    private List<String> fragmentTitle = new ArrayList<>();
    private List<Fragment> visitorFragment = new ArrayList<>();

    public VisitorPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public int getCount() {
        return visitorFragment.size();
    }

    @Override
    public Fragment getItem(int i) {
        return visitorFragment.get(i);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return fragmentTitle.get(position);
    }

    public void addFragment(Fragment fragment, String title){
        visitorFragment.add(fragment);
        fragmentTitle.add(title);
    }

    public void removeFragment(int pos){
        fragmentTitle.remove(pos);
        visitorFragment.remove(pos);
    }
}
