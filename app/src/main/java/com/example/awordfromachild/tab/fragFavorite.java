package com.example.awordfromachild.tab;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;

import com.example.awordfromachild.R;
import com.example.awordfromachild.TwitterUtils;
import com.example.awordfromachild.asynctask.callBacksFavorite;
import com.example.awordfromachild.common.fragmentBase;
import com.example.awordfromachild.constant.twitterValue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import twitter4j.Paging;

public class fragFavorite extends fragmentBase implements callBacksFavorite {
    //static final String _query = twitterValue.APP_HASH_TAG + " exclude:retweets";
    static final String _query = "マヂラブ exclude:retweets";
    static TwitterUtils.getFavorites getFavorites;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        twitterUtils = new TwitterUtils(this);
        mPopupWindow = new PopupWindow(getContext()); //スピナー用
        vid_listView = R.id.fg_main;
        query = _query;
        getMethod = twitterValue.getMethod.FAVORITE;
        paging = new Paging(1, twitterValue.tweetCounts.ONE_TIME_DISPLAY_TWEET);
        getFavorites = new TwitterUtils.getFavorites(this);
        return inflater.inflate(R.layout.fraggood_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //ツイート取得実行
        if (adapter == null || adapter.getCount() == 0) {
            dispSpinner(mPopupWindow);
            getFavorites.setParams(paging, twitterValue.howToDisplayTweets.TWEET_HOW_TO_DISPLAY_REWASH);
            getFavorites.execute();
        }
    }

    /**
     * リロードボタン押下時、
     * 最新ツイートを取得
     */
    public void addTheLatestTweets() {
        dispSpinner(mPopupWindow);
        paging.setPage(1);
        getFavorites.setHowToDisplay(twitterValue.howToDisplayTweets.TWEET_HOW_TO_DISPLAY_UNSHIFT);
        getFavorites.execute();
    }
}