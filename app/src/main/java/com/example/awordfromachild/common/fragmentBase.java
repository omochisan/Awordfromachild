package com.example.awordfromachild.common;

import android.util.TypedValue;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.awordfromachild.R;
import com.example.awordfromachild.library.SetDefaultTweetAdapter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;

import androidx.fragment.app.Fragment;
import twitter4j.Status;

public class fragmentBase extends Fragment {
    WeakReference<Fragment> weak_fragment;

    public void setFragment(Fragment fragment) {
        weak_fragment = new WeakReference<Fragment>(fragment);
    }

    /**
     * フラグメントが破棄されたかチェック
     *
     * @param base
     * @return
     */
    public Boolean checkViewDetach(Fragment base) {
        weak_fragment = new WeakReference<Fragment>(base);
        Fragment fragment = weak_fragment.get();
        if (fragment.isDetached() || fragment.getActivity() == null) {
            return true;
        } else {
            return false;
        }
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
        ProgressBar spinner = new ProgressBar(getActivity());
        weak_pop.setContentView(spinner);
        weak_pop.setBackgroundDrawable(getResources().getDrawable(R.drawable.popup_lightgray));
        // 表示サイズの設定 今回は幅300dp
        float width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics());
        weak_pop.setWindowLayoutMode((int) width, WindowManager.LayoutParams.WRAP_CONTENT);
        weak_pop.setWidth((int) width);
        weak_pop.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        // 画面に表示
        weak_pop.showAtLocation(getActivity().findViewById(R.id.view_pager), Gravity.BOTTOM, 0, 0);
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

    /**
     * Adapter内のアイテムをすべて取得する。
     *
     * @return
     */
    public ArrayList<Status> getItemList(SetDefaultTweetAdapter adapter) {
        ArrayList<Status> statusList = new ArrayList<>();
        for (int i = 0; i < adapter.getCount(); i++) {
            statusList.add(adapter.getItem(i));
        }
        return statusList;
    }

    /**
     * TwitterAPIのレート制限中注意トーストを表示する。
     *
     * @param secondsUntilReset 解除されるまでの分数
     */
    public void ex_twitterAPILimit(int secondsUntilReset) {
        double minutes = Math.ceil(secondsUntilReset / 60) + 1;
        String minutes_str = String.valueOf(minutes);
        Toast.makeText(getContext(), "ごめんなさい、この操作は制限中です。\n" +
                minutes_str.substring(0, minutes_str.indexOf(".")) +
                "分後に解除されます。", Toast.LENGTH_LONG).show();
    }

    /**
     * 表示ツイートがない場合のトーストを表示
     */
    public void no_result() {
        Toast.makeText(getContext(),
                "表示するツイートがありませんでした。", Toast.LENGTH_LONG).show();
    }

    /**
     * チェック例外時、トースト表示
     */
    public void fail_result(){
        Toast.makeText(
                getContext(), "データの取得に失敗しました。\n後でまたお試しください。",
                Toast.LENGTH_LONG).show();
    }
}
