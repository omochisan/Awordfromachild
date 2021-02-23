package com.example.awordfromachild.common;

import android.util.TypedValue;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.ProgressBar;

import com.example.awordfromachild.R;

import java.lang.ref.WeakReference;
import androidx.fragment.app.Fragment;

public class fragmentBase extends Fragment {
    WeakReference<Fragment> weak_fragment;

    public Boolean checkViewDetach(Fragment base){
        weak_fragment = new WeakReference<Fragment>(base);
        Fragment fragment = weak_fragment.get();
        if (fragment.isDetached() || fragment.getActivity() == null){
            return true;
        }else{
            return false;
        }
    }

    public void dispSpinner(PopupWindow mPopupWindow){
        //スピナー表示
        ProgressBar spinner = new ProgressBar(getActivity());
        mPopupWindow.setContentView(spinner);
        mPopupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.popup_lightgray));
        // 表示サイズの設定 今回は幅300dp
        float width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics());
        mPopupWindow.setWindowLayoutMode((int) width, WindowManager.LayoutParams.WRAP_CONTENT);
        mPopupWindow.setWidth((int) width);
        mPopupWindow.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        // 画面下に表示
        mPopupWindow.showAtLocation(getActivity().findViewById(R.id.ft_layout), Gravity.BOTTOM, 0, 0);
    }
}
