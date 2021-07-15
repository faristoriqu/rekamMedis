package com.sabin.digitalrm.fragments.prm.brm;



import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import com.google.android.material.tabs.TabLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.sabin.digitalrm.R;
import com.sabin.digitalrm.adapters.BrmAktifPagerAdapter;
import com.sabin.digitalrm.fragments.BaseFragment;
import com.sabin.digitalrm.models.DetailVisitor;

/**
 * A simple {@link Fragment} subclass.
 */
public class DaftarBrmAktifFragment extends BaseFragment {


    protected BrmAktifPagerAdapter mPagerAdapter;
    private ViewPager mViewPager;

    public DaftarBrmAktifFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        Baselog.d("[DaftarBRMAktif] onCreate: DPF");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_daftar_brm_aktif, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mPagerAdapter = new BrmAktifPagerAdapter(getChildFragmentManager());

        initView();

        mViewPager = view.findViewById(R.id.container);
        mViewPager.setAdapter(mPagerAdapter);

        TabLayout tabLayout = view.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);
    }

//    @Override
//    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
//        super.onCreateOptionsMenu(menu, inflater);
//        inflater.inflate(R.menu.menu_base, menu);
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        int id = item.getItemId();
//
//        switch (id){
//            case R.id.action_refresh:{
//                initView();
//                Baselog.d("Recreate Fragment View");
//                break;
//            }
//        }
//        return true;
//    }

    protected void initView(){
        mPagerAdapter.addFragment(BrmAktifFragment.newInstance(DetailVisitor.VISITOR_DMR_ACTIVE), DetailVisitor.BRM_STATUS_NAME[DetailVisitor.VISITOR_DMR_ACTIVE]);
        mPagerAdapter.addFragment(BrmAktifFragment.newInstance(DetailVisitor.VISITOR_DMR_HANDLING), DetailVisitor.BRM_STATUS_NAME[DetailVisitor.VISITOR_DMR_HANDLING]);
        mPagerAdapter.addFragment(BrmAktifFragment.newInstance(DetailVisitor.VISITOR_DMR_ANALITYC), DetailVisitor.BRM_STATUS_NAME[DetailVisitor.VISITOR_DMR_ANALITYC]);
        mPagerAdapter.addFragment(BrmAktifFragment.newInstance(DetailVisitor.VISITOR_DMR_CODING), DetailVisitor.BRM_STATUS_NAME[DetailVisitor.VISITOR_DMR_CODING]);
        mPagerAdapter.addFragment(BrmAktifFragment.newInstance(DetailVisitor.VISITOR_DMR_ALL), DetailVisitor.BRM_STATUS_NAME[0]);
    }
}
