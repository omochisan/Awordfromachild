package com.example.awordfromachild.ui.main;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.example.awordfromachild.R;
import com.example.awordfromachild.tab.fragAttention;
import com.example.awordfromachild.tab.fragNoti;
import com.example.awordfromachild.tab.fragSearch;
import com.example.awordfromachild.tab.fragTimeLine;

/**
 * A [FragmentPagerAdapter] that returns a fragment corresponding to
 * one of the sections/tabs/pages.
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{
            R.string.tab_text_timeline,
            R.string.tab_text_attention,
            R.string.tab_text_search,
            R.string.tab_text_noti
    };
    private final Context mContext;

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        mContext = context;
    }

    @Override
    public Fragment getItem(int position) {
        // 指定されたページのフラグメントをインスタンス化するために呼び出されます。
        Fragment fragment = null;
        switch (position){
            case 0:
                fragment = new fragTimeLine();
                break;
            case 1:
                fragment = new fragAttention();
                break;
            case 2:
                fragment = new fragSearch();
                break;
            case 3:
                fragment = new fragNoti();
                break;
        }
        return fragment;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        // Show 4 total pages.
        return 4;
    }
}