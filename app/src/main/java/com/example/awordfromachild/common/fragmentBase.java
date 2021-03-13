package com.example.awordfromachild.common;

import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.PopupWindow;
import android.widget.ProgressBar;

import com.example.awordfromachild.R;
import com.example.awordfromachild.library.SetDefaultTweetAdapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import androidx.fragment.app.Fragment;
import twitter4j.Status;

public class fragmentBase extends Fragment {
    WeakReference<Fragment> weak_fragment;

    public void setFragment(Fragment fragment){
        weak_fragment = new WeakReference<Fragment>(fragment);
    }

    public Boolean checkViewDetach(Fragment base){
        weak_fragment = new WeakReference<Fragment>(base);
        Fragment fragment = weak_fragment.get();
        if (fragment.isDetached() || fragment.getActivity() == null){
            return true;
        }else{
            return false;
        }
    }

    /**
     * スピナーを表示
     * @param mPopupWindow
     */
    public void dispSpinner(PopupWindow mPopupWindow){
        WeakReference<PopupWindow> _popupWindow = new WeakReference<PopupWindow>(mPopupWindow);
        PopupWindow weak_pop = _popupWindow.get();
        //スピナー表示
        ProgressBar spinner = new ProgressBar(getActivity());
        weak_pop.setContentView(spinner);
        weak_pop.setBackgroundDrawable(getResources().getDrawable(R.drawable.popup_lightgray));
        // 表示サイズの設定 今回は幅300dp
        float width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics());
        weak_pop.setWindowLayoutMode((int) width, WindowManager.LayoutParams.WRAP_CONTENT);
        weak_pop.setWidth((int) width);
        weak_pop.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        // 画面下に表示
        weak_pop.showAtLocation(getActivity().findViewById(R.id.ft_layout), Gravity.BOTTOM, 0, 0);
    }

    /**
     * スピナー非表示
     * @param mPopupWindow
     */
    public void hideSpinner(PopupWindow mPopupWindow){
        WeakReference<PopupWindow> _popupWindow = new WeakReference<PopupWindow>(mPopupWindow);
        PopupWindow weak_pop = _popupWindow.get();
        //スピナー退出
        if (weak_pop != null && weak_pop.isShowing()) {
            weak_pop.dismiss();
        }
    }

    /**
     * Adapter内のアイテムをすべて取得する。
     *
     * @return
     */
    public ArrayList<Status> getItemList(SetDefaultTweetAdapter adapter) {
        ArrayList<Status> statusList = new ArrayList<>();
        for(int i=0; i < adapter.getCount(); i++){
            statusList.add(adapter.getItem(i));
        }
        return statusList;
    }
}
