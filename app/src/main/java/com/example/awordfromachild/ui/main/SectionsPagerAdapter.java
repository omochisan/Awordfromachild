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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * 対応するフラグメントを返却する
 */
public class SectionsPagerAdapter extends FragmentStateAdapter {

    private final Context mContext;

    public SectionsPagerAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity);
        mContext = fragmentActivity;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
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

    /*
    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return mContext.getResources().getString(TAB_TITLES[position]);
    }*/

    @Override
    public int getItemCount() {
        // Show 5 total pages.
        return 5;
    }
}