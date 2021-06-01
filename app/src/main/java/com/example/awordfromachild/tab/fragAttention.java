package com.example.awordfromachild.tab;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.example.awordfromachild.R;
import com.example.awordfromachild.TwitterUtils;
import com.example.awordfromachild.asynctask.callBacksAttention;
import com.example.awordfromachild.common.fragmentBase;
import com.example.awordfromachild.constant.appSharedPreferences;
import com.example.awordfromachild.constant.twitterValue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
        return inflater.inflate(R.layout.fragattention_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //preferenceから設定値の呼び出し
        SharedPreferences preferences =
                getActivity().getSharedPreferences(appSharedPreferences.PREF_NAME, Context.MODE_PRIVATE);

        String set_countLike =
                preferences.getString(appSharedPreferences.SET_COUNT_LIKE, twitterValue.DEFAULT_LIKES);
        String set_countRetweet =
                preferences.getString(appSharedPreferences.SET_COUNT_RETWEET, twitterValue.DEFAULT_RETWEET);

        //下限いいね数、下限リツイート数をクエリに設定
        //_query = twitterValue.APP_HASH_TAG + " min_retweets:" + set_countLike + " min_faves:" + set_countRetweet;
        _query = "マヂラブ min_retweets:" + set_countLike + " min_faves:" + set_countRetweet;
        query = _query;
        //ツイート取得実行
        if (adapter == null || adapter.getCount() == 0) {
            runSearch(_query, null, null, 0,
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
                twitterValue.howToDisplayTweets.TWEET_HOW_TO_DISPLAY_UNSHIFT);
    }
}
