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
    //Bundleキー
    // 現在表示している中で、一番古いツイート
    private static final String BUNDLE_KEY_ITEM_MAX_GET_ID = "amt_item_max_get_id";
    // 現在のスクロール位置
    private static final String BUNDLE_KEY_ITEM_POSITION = "amt_item_position";
    //onPuase時、ListView復元のため一時保存
    private static Bundle bundle = new Bundle();
    WeakReference<callBacksBase> callBacks;

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

    /**
     * onCreate*
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        try {
            setContentView(R.layout.activity_mytweet);//画面基礎描画
            mPopupWindow = new PopupWindow(MyTweetActivity.this);
            callBacks = new WeakReference<>(this);
            vid_listView = R.id.fn_main;
            super.onCreate(savedInstanceState);

            adapter.getItem(1000);
            if (adapter == null || adapter.getCount() == 0) {
                //クエリ生成
                twitterUtils.getTimeLine(twitterValue.timeLineType.USER, 0, 0, 0, null);
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

            List<Status> result = (ResponseList<Status>) list;
            setListView(result, howToDisplay);
            hideSpinner(mPopupWindow);
        } catch (Exception e) {
            Log.e("エラー", e.toString());
        }
    }
}