package com.example.awordfromachild.ui.dialog;

import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

/**
 * ダイアログフラグメントを管理
 */
public class DialogPagePagerAdapter extends FragmentStateAdapter {

    public DialogPagePagerAdapter(FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        // 指定されたページのフラグメントをインスタンス化するために呼び出されます。
        androidx.fragment.app.Fragment fragment = null;
        switch (position) {
            case 0:
                fragment = new dAppExplanNewArrival();
                break;
            case 1:
                fragment = new dAppExplainAttention();
                break;
        }
        return Objects.requireNonNull(fragment);
    }

    @Override
    public int getItemCount() {
        // Show 5 total pages.
        return 2;
    }
}
