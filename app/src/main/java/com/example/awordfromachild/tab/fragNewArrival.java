package com.example.awordfromachild.tab;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.example.awordfromachild.R;
import com.example.awordfromachild.TwitterUtils;
import com.example.awordfromachild.asynctask.callBacksNewArrival;
import com.example.awordfromachild.common.fragmentBase;
import com.example.awordfromachild.constant.twitterValue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import twitter4j.Status;

public class fragNewArrival extends fragmentBase implements callBacksNewArrival {

    //static final String _query = twitterValue.APP_HASH_TAG + " exclude:retweets";
    static final String _query = "マヂラブ exclude:retweets";

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
        twitterUtils = new TwitterUtils(this);
        mPopupWindow = new PopupWindow(getContext()); //スピナー用
        query = _query;
        return inflater.inflate(R.layout.fragnewarrival_layout, container, false);
    }

    @Override
    public void onResume() {
        super.onResume();
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

        //ツイート取得実行
        if (adapter == null || adapter.getCount() == 0) {
            runSearch(_query, null, null, 0,
                    twitterValue.howToDisplayTweets.TWEET_HOW_TO_DISPLAY_REWASH);
        }

        //ストリーミング開始
        //※タスクが溜まりすぎて？すぐ落ちるため、現在未使用。
        /*long[] l = {};
        String[] str = {"マヂラブ"};
        startStreaming(str, l);*/
    }

    /**
     * コールバック
     * ツイート取得後
     */
    @Override
    public void callBackGetTweets(Object list, String howToDisplay) {
        super.callBackGetTweets(list, howToDisplay);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    /**
     * リロードボタン押下時、
     * 最新ツイートを取得
     */
    public void addTheLatestTweets() {
        dispSpinner(mPopupWindow);
        long sinceID = ((Status) adapter.getItem(0)).getId();
        runSearch(_query, sinceID, null, twitterValue.tweetCounts.ONE_TIME_DISPLAY_TWEET_MAX,
                twitterValue.howToDisplayTweets.TWEET_HOW_TO_DISPLAY_UNSHIFT);
    }
}