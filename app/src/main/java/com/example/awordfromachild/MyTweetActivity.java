package com.example.awordfromachild;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.awordfromachild.asynctask.callBacksBase;
import com.example.awordfromachild.asynctask.callBacksMyTweet;
import com.example.awordfromachild.asynctask.callBacksNewArrival;
import com.example.awordfromachild.common.activityBase;
import com.example.awordfromachild.common.exceptionHandling;
import com.example.awordfromachild.common.fragmentBase;
import com.example.awordfromachild.constant.appSharedPreferences;
import com.example.awordfromachild.constant.twitterValue;
import com.example.awordfromachild.library.SetDefaultTweetAdapter;
import com.example.awordfromachild.ui.main.SectionsPagerAdapter;
import com.google.android.material.tabs.TabLayout;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import twitter4j.QueryResult;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.User;

public class MyTweetActivity extends activityBase implements callBacksMyTweet {
    //onPuase時、ListView復元のため一時保存
    private static Bundle bundle = new Bundle();
    //Bundleキー
    // 現在表示している中で、一番古いツイート
    private static final String BUNDLE_KEY_ITEM_MAX_GET_ID = "amt_item_max_get_id";
    // 現在のスクロール位置
    private static final String BUNDLE_KEY_ITEM_POSITION = "amt_item_position";

    @Override
    public void onResume() {
        try{
            super.onResume();
            //ListViewの復元
            if (adapter != null) {
                listView.setAdapter(adapter);
                restoreListViewSelection();
            }
        }catch (Exception e){
            Log.e("エラー", e.toString());
        }

    }

    /**
     * onCreate*
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            setContentView(R.layout.activity_mytweet);//画面基礎描画
            mPopupWindow = new PopupWindow(MyTweetActivity.this);//スピナー
            vid_listView = R.id.fn_main;
            super.onCreate(savedInstanceState);

            if (adapter == null || adapter.getCount() == 0) {
                //クエリ生成
                twitterUtils.getTimeLine(twitterValue.timeLineType.USER);
            }
        } catch (Exception e) {
            Log.e("エラー", e.toString());
        }
    }

    @Override
    public void callBackTwitterLimit(int secondsUntilReset) {

    }

    @Override
    public void callBackStreamAddList(Status status) {

    }

    @Override
    public void callBackException() {

    }

    @Override
    public void callBackGetTweets(Object list, String howToDisplay) {
        try {
            if (checkViewDetach(this)) return;

            List<Status> result = (ResponseList<Status>) list;
            setListView(result, howToDisplay);
            hideSpinner(mPopupWindow);
        }catch (Exception e){
            Log.e("エラー", e.toString());
        }
    }
}