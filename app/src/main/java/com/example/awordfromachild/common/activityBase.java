package com.example.awordfromachild.common;

import android.app.Activity;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.awordfromachild.R;
import com.example.awordfromachild.constant.twitterValue;

import java.lang.ref.WeakReference;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import twitter4j.TwitterException;

public class activityBase extends AppCompatActivity {
    WeakReference<Activity> weak_activity;

    public Boolean checkViewDetach(Activity base){
        weak_activity = new WeakReference<Activity>(base);
        Activity activity = weak_activity.get();
        if (activity.isDestroyed()){
            return true;
        }else{
            return false;
        }
    }

    /**
     * TwitterAPIのレート制限発生
     */
    public void ex_twitterAPILimit(int secondsUntilReset){
        double minutes = Math.ceil(secondsUntilReset / 60);
        Toast.makeText(this, "ごめんなさい、この操作は制限中です。" + minutes +
                "分後にまたお試しください。", Toast.LENGTH_LONG).show();
    }

    /**
     * スピナーを表示
     *
     * @param mPopupWindow
     */
    public void dispSpinner(PopupWindow mPopupWindow) {
        WeakReference<PopupWindow> _popupWindow = new WeakReference<PopupWindow>(mPopupWindow);
        PopupWindow weak_pop = _popupWindow.get();
        //スピナー表示
        ProgressBar spinner = new ProgressBar(this);
        weak_pop.setContentView(spinner);
        weak_pop.setBackgroundDrawable(getResources().getDrawable(R.drawable.popup_lightgray));
        // 表示サイズの設定 今回は幅300dp
        float width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics());
        weak_pop.setWindowLayoutMode((int) width, WindowManager.LayoutParams.WRAP_CONTENT);
        weak_pop.setWidth((int) width);
        weak_pop.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        // 画面下に表示
        weak_pop.showAtLocation(findViewById(R.id.ft_layout), Gravity.BOTTOM, 0, 0);
    }

    /**
     * スピナー非表示
     *
     * @param mPopupWindow
     */
    public void hideSpinner(PopupWindow mPopupWindow) {
        WeakReference<PopupWindow> _popupWindow = new WeakReference<PopupWindow>(mPopupWindow);
        PopupWindow weak_pop = _popupWindow.get();
        //スピナー退出
        if (weak_pop != null && weak_pop.isShowing()) {
            weak_pop.dismiss();
        }
    }

}
