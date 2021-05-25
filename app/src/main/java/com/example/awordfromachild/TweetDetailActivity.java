package com.example.awordfromachild;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.example.awordfromachild.common.activityBase;
import com.example.awordfromachild.library.SetDefaultTweetAdapter;

import twitter4j.Status;

public class TweetDetailActivity extends activityBase {
    //Twitter処理クラス
    private TwitterUtils twitterUtils;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //twitterUtils = new TwitterUtils(this);
        setContentView(R.layout.tweet_detail);
        Bundle bundle = getIntent().getExtras();
        Status getStatus = (Status) bundle.get("DATA");
        SetDefaultTweetAdapter.setUserIcon(findViewById(R.id.twd_parent), getStatus, R.id.twd_userIcon, R.id.twd_userName);
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
}
