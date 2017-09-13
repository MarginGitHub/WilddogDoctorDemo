package com.zd.wilddogdoctordemo.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.zd.wilddogdoctordemo.ui.doctor.fragments.AboutMeFragment;
import com.zd.wilddogdoctordemo.ui.doctor.fragments.HistoryRecordFragment;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by dongjijin on 2017/9/6 0006.
 */

public class VideoConversationFragmentPagerAdapter extends FragmentPagerAdapter {
    private List<Fragment> mFragments;
    public VideoConversationFragmentPagerAdapter(FragmentManager fm) {
        super(fm);
        mFragments = new ArrayList<>(3);
        mFragments.add(new HistoryRecordFragment());
        mFragments.add(new HistoryRecordFragment());
        mFragments.add(new AboutMeFragment());
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return 3;
    }

    public HistoryRecordFragment getOnlineDoctorListFragment() {
        return (HistoryRecordFragment) mFragments.get(0);
    }

    public String getFragmentTag(int viewId, int postion) {
        return "android:switcher:" + viewId + ":" + postion;
    }
}
