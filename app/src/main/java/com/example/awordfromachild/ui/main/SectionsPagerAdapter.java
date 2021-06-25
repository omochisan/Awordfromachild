package com.example.awordfromachild.ui.main;

import android.content.Context;

import com.example.awordfromachild.R;
import com.example.awordfromachild.tab.fragAttention;
import com.example.awordfromachild.tab.fragFavorite;
import com.example.awordfromachild.tab.fragNewArrival;
import com.example.awordfromachild.tab.fragNoti;
import com.example.awordfromachild.tab.fragSearch;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

/**
 * 対応するフラグメントを返却する
 */
public class SectionsPagerAdapter extends FragmentPagerAdapter {

    @StringRes
    private static final int[] TAB_TITLES = new int[]{
            R.string.tab_text_newArrival,
            R.string.tab_text_attention,
            R.string.tab_text_search,
            R.string.tab_text_favorite,
            R.string.tab_text_noti
    };
    private final Context mContext;

    public SectionsPagerAdapter(Context context, FragmentManager fm) {
        super(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        mContext = context;
    }

    @NotNull
    @Override
    public androidx.fragment.app.Fragment getItem(int position) {
        // 指定されたページのフラグメントをインスタンス化するために呼び出されます。
        androidx.fragment.app.Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new fragNewArrival();
                break;
            case 1:
                fragment = new fragAttention();
                break;
            case 2:
                fragment = new fragSearch();
                break;
            case 3:
                fragment = new fragFavorite();
                break;
            case 4:
                fragment = new fragNoti();
                break;
        }
        return Objects.requireNonNull(fragment);
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }

    @Override
    public int getCount() {
        // Show 5 total pages.
        return 5;
    }
}