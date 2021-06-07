package com.example.awordfromachild.tab;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.RadioGroup;

import com.example.awordfromachild.R;
import com.example.awordfromachild.TwitterUtils;
import com.example.awordfromachild.asynctask.callBacksNoti;
import com.example.awordfromachild.common.fragmentBase;
import com.example.awordfromachild.constant.twitterValue;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import twitter4j.DirectMessageList;
import twitter4j.Query;
import twitter4j.Status;
import twitter4j.User;

public class fragNoti extends fragmentBase implements callBacksNoti {
    static String _query_favorite = "min_faves:1";
    static String _query_reTweet = "min_retweets:1";
    static ArrayList<Status> bk_list_favorite;
    static ArrayList<Status> bk_list_reTweet;
    static DirectMessageList bk_list_dm;
    static int vid_nowChecked;
    /**
     * 画面タイプ変更時
     */
    private final RadioGroup.OnCheckedChangeListener radioChanged = new RadioGroup.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(RadioGroup radioGroup, int i) {
            int checkRadioID = radioGroup.getCheckedRadioButtonId();
            setGetMethod(checkRadioID);
            vid_nowChecked = checkRadioID; //現在の選択状態を保持
        }
    };

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        twitterUtils = new TwitterUtils(this);
        mPopupWindow = new PopupWindow(getContext()); //スピナー用
        vid_listView = R.id.fno_main;
        return inflater.inflate(R.layout.fragnoti_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        RadioGroup radioGroup = view.findViewById(R.id.fno_select);
        int checkRadioID = radioGroup.getCheckedRadioButtonId();
        twitterUtils.getTwitterUserInfo();
        setGetMethod(checkRadioID); //選択ラジオボタンごとにデータ取得

        super.onViewCreated(view, savedInstanceState);

        //ラジオボタン処理
        radioGroup.setOnCheckedChangeListener(radioChanged);
    }

    /**
     * リロードボタン押下時、
     * 最新ツイートを取得
     */
    public void addTheLatestTweets() {
        dispSpinner(mPopupWindow);

        RadioGroup radioGroup = getActivity().findViewById(R.id.fno_select);
        int checkRadioID = radioGroup.getCheckedRadioButtonId();
        setGetMethod(checkRadioID); //選択ラジオボタンごとにデータ取得

        long sinceID = ((Status) adapter.getItem(0)).getId();
        runSearch(query, sinceID, null, twitterValue.tweetCounts.ONE_TIME_DISPLAY_TWEET_MAX,
                Query.ResultType.recent, twitterValue.howToDisplayTweets.TWEET_HOW_TO_DISPLAY_UNSHIFT);
    }

    /**
     * 選択ラジオボタンごとにデータ取得
     *
     * @param checkRadioID
     */
    private void setGetMethod(int checkRadioID) {
        switch (checkRadioID) {
            case R.id.fno_rb_favorite:
                query = _query_favorite;
                getMethod = twitterValue.getMethod.SEARCH;
                break;

            case R.id.fno_rb_retweet:
                query = _query_reTweet;
                getMethod = twitterValue.getMethod.SEARCH;
                break;

            case R.id.fno_rb_dm:
                getMethod = twitterValue.getMethod.DM;
                break;
        }
        //バックアップ
        switch (vid_nowChecked){
            case R.id.fno_rb_favorite:
                bk_list_favorite = getItemList();
                break;

            case R.id.fno_rb_retweet:
                bk_list_reTweet = getItemList();
                break;

            case R.id.fno_rb_dm:
                bk_list_dm = getItemList_dm();
                break;
        }
    }

    private void getData(int checkRadioID){
        switch (checkRadioID) {
            case R.id.fno_rb_favorite:
                //ツイート取得実行
                if (adapter == null || adapter.getCount() == 0) {
                    if(bk_list_favorite != null) {

                    }else{
                        runSearch(_query_favorite, null, null, 0, Query.ResultType.recent,
                                twitterValue.howToDisplayTweets.TWEET_HOW_TO_DISPLAY_REWASH);
                    }
                }
                break;

            case R.id.fno_rb_retweet:
                query = _query_reTweet;
                getMethod = twitterValue.getMethod.SEARCH;
                break;

            case R.id.fno_rb_dm:
                getMethod = twitterValue.getMethod.DM;
                break;
        }
    }

    @Override
    public void callBackGetUser(User user) {
        _query_favorite = "from:@" + user.getScreenName() + " " + " min_faves:1";
        _query_reTweet = "from:@" + user.getScreenName() + " " + " min_retweets:1";
        RadioGroup radioGroup = getActivity().findViewById(R.id.fno_select);
        int checkRadioID = radioGroup.getCheckedRadioButtonId();
        //選択ラジオボタンごとにデータ取得
        setGetMethod(checkRadioID);
        getData(checkRadioID);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        bk_list_favorite = null;
        bk_list_reTweet = null;
        bk_list_dm = null;
    }
}
