package com.example.awordfromachild;

import android.os.Bundle;
import android.util.Log;
import android.widget.PopupWindow;

import com.example.awordfromachild.asynctask.callBacksBase;
import com.example.awordfromachild.asynctask.callBacksMyTweet;
import com.example.awordfromachild.common.activityBase;
import com.example.awordfromachild.constant.twitterValue;

import java.lang.ref.WeakReference;
import java.util.List;

import twitter4j.ResponseList;
import twitter4j.Status;

public class MyTweetActivity extends activityBase implements callBacksMyTweet {
    WeakReference<callBacksBase> callBacks;
    static TwitterUtils.getTimeLine getTimeLine;

    @Override
    public void onResume() {
        try {
            super.onResume();
            //ListViewの復元
            if (adapter != null) {
                listView.setAdapter(adapter);
                restoreListViewSelection();
            }
        } catch (Exception e) {
            errHand.exceptionHand(e, callBacks);
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            setContentView(R.layout.activity_mytweet);//画面基礎描画
            mPopupWindow = new PopupWindow(MyTweetActivity.this);
            callBacks = new WeakReference<>(this);
            vid_listView = R.id.amt_main;
            getTimeLine = new TwitterUtils.getTimeLine(this);
            super.onCreate(savedInstanceState);

            if (adapter == null || adapter.getCount() == 0) {
                //クエリ生成
                getTimeLine.setParam(twitterValue.timeLineType.USER, 0, 0,
                        0, null);
                getTimeLine.execute();
            }
        } catch (Exception e) {
            errHand.exceptionHand(e, callBacks);
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

            List<Status> result = autoCast(list);
            setListView(result, howToDisplay);
            hideSpinner(mPopupWindow);
        } catch (Exception e) {
            Log.e("エラー", e.toString());
        }
    }
}