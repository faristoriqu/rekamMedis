package com.sabin.digitalrm.fragments.prm.pasien;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.sabin.digitalrm.R;
import com.sabin.digitalrm.adapters.AktivasiRMEPagerAdapter;
import com.sabin.digitalrm.fragments.BaseFragment;
import com.sabin.digitalrm.models.DetailVisitor;

public class ContainerPasienFragment extends BaseFragment {

    protected AktivasiRMEPagerAdapter mPagerAdapter;
    private ViewPager mViewPager;

    public ContainerPasienFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_container_pasien, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mPagerAdapter = new AktivasiRMEPagerAdapter(getChildFragmentManager());

        initTab();

        mViewPager = view.findViewById(R.id.container);
        mViewPager.setAdapter(mPagerAdapter);

        TabLayout tabLayout = view.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

    protected void initTab(){
        mPagerAdapter.addFragment(PasienFragment.newInstance(DetailVisitor.SRV_CODE_RJ), DetailVisitor.SRV_RJ);
        mPagerAdapter.addFragment(PasienFragment.newInstance(DetailVisitor.SRV_CODE_RI), DetailVisitor.SRV_RI);
        mPagerAdapter.addFragment(PasienFragment.newInstance(DetailVisitor.SRV_CODE_IGD), DetailVisitor.SRV_IGD);
    }
}