package com.hzh.chabaike.adapters;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.view.ViewGroup;

import java.util.List;

/**
 * Created by my on 2016/11/14.
 */
public class MyFragmentPagerAdapter extends FragmentPagerAdapter {
    private List<Fragment> data;
    private String[] titles;
    public MyFragmentPagerAdapter(FragmentManager supportFragmentManager, List<Fragment> fragments, String[] titles) {
        super(supportFragmentManager);
        this.data = fragments;
        this.titles = titles;
    }

    @Override
    public Fragment getItem(int position) {
        return data.get(position);
    }

    @Override
    public int getCount() {
        return data!=null?data.size():0;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return titles[position];
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
//        super.destroyItem(container, position, object);
    }
}
