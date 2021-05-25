package com.example.awordfromachild;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.awordfromachild.asynctask.callBacksBase;
import com.example.awordfromachild.asynctask.callBacksMyTweet;
import com.example.awordfromachild.asynctask.callBacksNewArrival;
import com.example.awordfromachild.common.activityBase;
import com.example.awordfromachild.common.exceptionHandling;
import com.example.awordfromachild.common.fragmentBase;
import com.example.awordfromachild.constant.appSharedPreferences;
import com.example.awordfromachild.constant.twitterValue;
import com.example.awordfromachild.library.SetDefaultTweetAdapter;
import com.example.awordfromachild.ui.main.SectionsPagerAdapter;
import com.google.android.material.tabs.TabLayout;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.ViewPager;
import twitter4j.QueryResult;
import twitter4j.ResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.User;

public class MyTweetActivity extends activityBase implements callBacksMyTweet {
    //onPuase時、ListView復元のため一時保存
    private static Bundle bundle = new Bundle();
    //Bundleキー
    // 現在表示している中で、一番古いツイート
    private static final String BUNDLE_KEY_ITEM_MAX_GET_ID = "amt_item_max_get_id";
    // 現在のスクロール位置
    private static final String BUNDLE_KEY_ITEM_POSITION = "amt_item_position";
    //Twitter処理クラス
    private static TwitterUtils twitterUtils;
    //現在表示している中の最新？ツイートID
    private static long latest_minId;
    //現在実施中の読込開始ポイント
    private static int now_readPoint = 0;
    //スピナー用
    private static PopupWindow mPopupWindow;
    //ListViewアダプター
    SetDefaultTweetAdapter adapter;
    //エラーハンドリング
    private exceptionHandling errHand;

    private WeakReference<callBacksBase> callBacks = new WeakReference<callBacksBase>(this);

    @Override
    public void onResume() {
        try{
            super.onResume();
            //ListViewの復元
            if (adapter != null) {
                listView.setAdapter(adapter);
                restoreListViewSelection();
            }
        }catch (Exception e){
            Log.e("エラー", e.toString());
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
            super.onCreate(savedInstanceState);

            setContentView(R.layout.activity_mytweet);//画面基礎描画
            twitterUtils = new TwitterUtils(this);
            twitterUtils.setTwitterInstance(this);
            errHand = new exceptionHandling();
            mPopupWindow = new PopupWindow(MyTweetActivity.this);//スピナー

            if (adapter == null || adapter.getCount() == 0) {
                //dispSpinner(mPopupWindow, R.id.amt_main);
                //クエリ生成
                twitterUtils.getTimeLine(twitterValue.timeLineType.USER);
            }

            listView = findViewById(R.id.amt_main);

            //リストビューイベント
            //　スクロール
            //　ポイントまでスクロールした時、追加で40件読み込み
            listView.setOnScrollListener(new AbsListView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(AbsListView view, int scrollState) {
                }

                @Override
                public void onScroll(final AbsListView view, int firstVisibleItem,
                                     int visibleItemCount, int totalItemCount) {
                    //追加読込を始める位置 ＝ トータルアイテム数-(一回のツイート読込数 / 4)
                    int readStartCount = totalItemCount - (twitterValue.tweetCounts.ONE_TIME_DISPLAY_TWEET / 4);
                    //スクロール位置が追加読込ポイント（最終から10行前）の場合、追加読込開始
                    if (firstVisibleItem == readStartCount && now_readPoint != readStartCount) {
                        //スピナー表示
                        dispSpinner(mPopupWindow, R.id.amt_main);
                        now_readPoint = readStartCount;
                        twitterUtils.search(twitterValue.timeLineType.USER, returnLastID(),
                                twitterValue.howToDisplayTweets.TWEET_HOW_TO_DISPLAY_PUSH);
                    }
                }
            });

            // 行選択イベント
            listView.setOnItemClickListener(new AbsListView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    ListView _listView = (ListView)adapterView;
                    Status status = (Status) _listView.getItemAtPosition(i);
                    Intent intent = new Intent(getApplicationContext(), TweetDetailActivity.class);
                    intent.putExtra("DATA", status);
                    startActivity(intent);
                }
            });
        } catch (Exception e) {
            Log.e("エラー", e.toString());
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
        }catch (Exception e){
            Log.e("エラー", e.toString());
        }
    }
}