package com.example.awordfromachild.tab;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.example.awordfromachild.R;
import com.example.awordfromachild.TweetDetailActivity;
import com.example.awordfromachild.TwitterUtils;
import com.example.awordfromachild.asynctask.callBacksNewArrival;
import com.example.awordfromachild.common.exceptionHandling;
import com.example.awordfromachild.common.fragmentBase;
import com.example.awordfromachild.constant.twitterValue;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import twitter4j.QueryResult;
import twitter4j.Status;

public class fragNewArrival extends fragmentBase implements callBacksNewArrival {

    static final String query = twitterValue.APP_HASH_TAG + " exclude:retweets";

    @Nullable
    @Override
    public Context getContext() {
        return super.getContext();
    }

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragnewarrival_layout, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
        //ListViewの復元
        if (adapter != null) {
            listView.setAdapter(adapter);
            restoreListViewSelection();
        }
    }

    /**
     * 画面状態の保持（Fragment再生成用）
     */
    @Override
    public void onPause() {
        super.onPause();
    }

    /**
     * 画面状態の保持（Fragment再生成用）
     *
     * @param state
     */
    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //スピナー用
        mPopupWindow = new PopupWindow(getContext());

        //ツイート取得実行
        if (adapter == null || adapter.getCount() == 0) {
            dispSpinner(mPopupWindow);
            runSearch("マヂラブ exclude:retweets");
        }

        //ストリーミング開始
        long[] l = {};
        String[] str = {"マヂラブ"};
        startStreaming(str, l);
    }

    /**
     * コールバック
     * ツイート取得後
     */
    @Override
    public void callBackGetTweets(Object list, String howToDisplay) {
        super.callBackGetTweets(list, howToDisplay);
        hideSpinner(mPopupWindow);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}