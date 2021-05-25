package com.example.awordfromachild.tab;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.example.awordfromachild.R;
import com.example.awordfromachild.TweetDetailActivity;
import com.example.awordfromachild.TwitterUtils;
import com.example.awordfromachild.asynctask.callBacksNewArrival;
import com.example.awordfromachild.common.exceptionHandling;
import com.example.awordfromachild.common.fragmentBase;
import com.example.awordfromachild.constant.twitterValue;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import twitter4j.QueryResult;
import twitter4j.Status;

public class fragNewArrival extends fragmentBase implements callBacksNewArrival {

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
        return inflater.inflate(R.layout.fragnewarrival_layout, container, false);
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
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        twitterUtils = new TwitterUtils(this);
        errHand = new exceptionHandling();
        mPopupWindow = new PopupWindow(getActivity());

        twitterUtils.setTwitterInstance(getContext());
        if (adapter == null || adapter.getCount() == 0) {
            dispSpinner(mPopupWindow);
            //クエリ生成
            twitterUtils.search("マヂラブ exclude:retweets",
                    twitterValue.howToDisplayTweets.TWEET_HOW_TO_DISPLAY_REWASH);
        }

        //ストリーミング開始
        //String[] stream_filter = {"マヂラブ"};
        //twitterUtils.startStream(stream_filter, null);

        listView = getActivity().findViewById(R.id.fn_main);

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
                    dispSpinner(mPopupWindow);
                    now_readPoint = readStartCount;
                    twitterUtils.search(twitterValue.timeLineType.HOME, returnLastID(),
                            twitterValue.howToDisplayTweets.TWEET_HOW_TO_DISPLAY_PUSH);
                }
            }
        });

        // 行選択イベント
        listView.setOnItemClickListener(new AbsListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ListView _listView = (ListView) adapterView;
                Status status = (Status) _listView.getItemAtPosition(i);
                Intent intent = new Intent(getActivity(), TweetDetailActivity.class);
                intent.putExtra("DATA", status);
                startActivity(intent);
            }
        });
    }

    /**
     * コールバック
     * ツイート取得後
     */
    @Override
    public void callBackGetTweets(Object list, String howToDisplay) {
        if (checkViewDetach(this)) return;

        QueryResult queryResult = (QueryResult) list;
        List<Status> s_list = ((QueryResult) list).getTweets();
        setListView(s_list, howToDisplay);
        hideSpinner(mPopupWindow);
    }

    /**
     * Streamでの追跡結果を画面に追加する
     *
     * @param status
     */
    @Override
    public void callBackStreamAddList(Status status) {
        if (checkViewDetach(this)) return;

        ArrayList<Status> list = new ArrayList<>();
        list.add(status);
        setListView(list, twitterValue.howToDisplayTweets.TWEET_HOW_TO_DISPLAY_UNSHIFT);
        hideSpinner(mPopupWindow);
    }

    /**
     * コールバック
     * TwitterAPIリミット時
     */
    @Override
    public void callBackTwitterLimit(int secondsUntilReset) {
        ex_twitterAPILimit(secondsUntilReset);
        hideSpinner(mPopupWindow);
    }

    /**
     * コールバック
     * 非チェック例外発生時
     */
    @Override
    public void callBackException() {
        fail_result();
    }

    /**
     * 画面状態の保持（Fragment再生成用）
     */
    @Override
    public void onPause() {
        super.onPause();
        putState();
    }

    /**
     * 画面状態の保持（Fragment再生成用）
     *
     * @param state
     */
    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        putState();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}