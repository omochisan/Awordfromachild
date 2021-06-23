package com.example.awordfromachild;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.PopupWindow;

import com.example.awordfromachild.asynctask.callBacksBase;
import com.example.awordfromachild.common.activityBase;
import com.example.awordfromachild.common.exceptionHandling;
import com.example.awordfromachild.library.SetDefaultTweetAdapter;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.RequiresApi;
import twitter4j.Status;

public class TweetDetailActivity extends activityBase implements callBacksBase {
    //Twitter処理クラス
    private TwitterUtils twitterUtils;

    @RequiresApi(api = Build.VERSION_CODES.N)
    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.tweet_detail);
        errHand = new exceptionHandling();
        mPopupWindow = new PopupWindow(TweetDetailActivity.this);//スピナー
        super.onCreate(savedInstanceState);

        //遷移元からツイートデータ取得
        Bundle bundle = getIntent().getExtras();
        Status getStatus = (Status) bundle.get("DATA");
        View view = findViewById(R.id.twd_parent);
        List<Status> list = new ArrayList<Status>(){
            {
                add(getStatus);
            }
        };;
        SetDefaultTweetAdapter tweetAdapter = new SetDefaultTweetAdapter(this, 0, list, null);
        //ユーザーアイコン設定
        tweetAdapter.setUserIcon(view, getStatus, R.id.twd_userIcon, R.id.twd_userName);
        //リツイートの場合、リツイートユーザー等を設定
        tweetAdapter.setReTweet(view, getStatus, R.id.twd_tweetHeader);
        //フッター設定
        tweetAdapter.setFooterIcon(view, 0, R.id.twd_like, R.id.twd_retweet);
        //ツイートの各種値設定
        tweetAdapter.setValue(view, getStatus, true,
                R.id.twd_time,
                R.id.twd_userID,
                R.id.twd_main,
                R.id.twd_like,
                R.id.twd_retweet,
                R.id.twd_reply);
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

    }
}
