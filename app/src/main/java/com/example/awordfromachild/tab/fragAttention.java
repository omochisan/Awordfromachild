package com.example.awordfromachild.tab;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.example.awordfromachild.ApplicationController;
import com.example.awordfromachild.R;
import com.example.awordfromachild.TwitterUtils;
import com.example.awordfromachild.asynctask.callBacksAttention;
import com.example.awordfromachild.common.fragmentBase;
import com.example.awordfromachild.constant.appSharedPreferences;
import com.example.awordfromachild.constant.twitterValue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;

public class fragAttention extends fragmentBase implements callBacksAttention {
    //static final String _query = twitterValue.APP_HASH_TAG + " exclude:retweets";
    static String _query = "";

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        twitterUtils = new TwitterUtils(this);
        mPopupWindow = new PopupWindow(getContext()); //スピナー用
        vid_listView = R.id.fa_main;
        getMethod = twitterValue.getMethod.SEARCH;
        return inflater.inflate(R.layout.fragattention_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //preferenceから設定値の呼び出し
        Context app_context = ApplicationController.getInstance().getApplicationContext();
        SharedPreferences preferences = app_context.getSharedPreferences(appSharedPreferences.PREF_NAME, Context.MODE_PRIVATE);
        //表示ツイート目安
        int criterionLike =
                preferences.getInt(appSharedPreferences.SET_CRITERION_LIKE, twitterValue.DEFAULT_LIKES);
        //いいね数の目安指定がある場合、最新ツイート取得。
        //数指定無い場合、取得タイプを「人気ツイート」に。
        Query.ResultType qResult = null;
        if(criterionLike != 0){
            qResult = Query.ResultType.recent;
            //_query = twitterValue.APP_HASH_TAG + " min_faves:" + criterionLike;
            _query = "マヂラブ" + " min_faves:" + criterionLike;
        }else{
            qResult = Query.ResultType.popular;
            //_query = twitterValue.APP_HASH_TAG;
            _query = "マヂラブ";
        }
        //ツイート取得実行
        if (adapter == null || adapter.getCount() == 0) {
            runSearch(_query, null, null, 0, qResult,
                    twitterValue.howToDisplayTweets.TWEET_HOW_TO_DISPLAY_REWASH);
        }
    }

    /**
     * リロードボタン押下時、
     * 最新ツイートを取得
     */
    public void addTheLatestTweets() {
        dispSpinner(mPopupWindow);
        long sinceID = ((Status) adapter.getItem(0)).getId();
        runSearch(_query, sinceID, null, twitterValue.tweetCounts.ONE_TIME_DISPLAY_TWEET_MAX,
                Query.ResultType.popular, twitterValue.howToDisplayTweets.TWEET_HOW_TO_DISPLAY_UNSHIFT);
    }
}
