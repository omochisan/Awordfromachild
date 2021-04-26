package com.example.awordfromachild.tab;

import android.content.Context;
import android.net.Uri;
import android.net.UrlQuerySanitizer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.example.awordfromachild.R;
import com.example.awordfromachild.TwitterUtils;
import com.example.awordfromachild.asynctask.callBacksBase;
import com.example.awordfromachild.asynctask.callBacksTimeLine;
import com.example.awordfromachild.common.exceptionHandling;
import com.example.awordfromachild.common.fragmentBase;
import com.example.awordfromachild.constant.appSharedPreferences;
import com.example.awordfromachild.constant.twitterValue;
import com.example.awordfromachild.library.SetDefaultTweetAdapter;

import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import twitter4j.PagableResponseList;
import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.User;

public class fragTimeLine extends fragmentBase implements callBacksTimeLine {
    //Bundleキー
    // 現在表示している中で、一番古いツイート
    private static final String BUNDLE_KEY_ITEM_MAX_GET_ID = "ft_item_max_get_id";
    // 現在のスクロール位置
    private static final String BUNDLE_KEY_ITEM_POSITION = "ft_item_position";
    //Twitter処理クラス
    private static TwitterUtils twitterUtils;
    //現在表示している中の最新？ツイートID
    private static long latest_minId;
    //現在実施中の読込開始ポイント
    private static int now_readPoint = 0;
    //スピナー用
    private static PopupWindow mPopupWindow;
    //onPuase時、ListView復元のため一時保存
    private static Bundle bundle = new Bundle();
    //ListViewアダプター
    SetDefaultTweetAdapter adapter;
    private ListView listView;
    //エラーハンドリング
    private exceptionHandling errHand;
    //続きのツイート群取得用クエリ
    private String next_results;

    private WeakReference<callBacksBase> callBacks = new WeakReference<callBacksBase>(this);

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
        return inflater.inflate(R.layout.fragtimeline_layout, container, false);
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

    /**
     * タイムライン取得用クエリ作成
     * @return
     */
    private Map<String, String> creteGetTimeLineQuery(){
        Map<String, String> str = new HashMap<>();
        if(next_results == null) {
            str.put("q","ナポリの男たち　filter:follows");
            str.put("count", String.valueOf(twitterValue.tweetCounts.ONE_TIME_DISPLAY_TWEET));
            str.put("result_type", twitterValue.resultType.RECENT);
        }
        return str;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        twitterUtils = new TwitterUtils(this);
        errHand = new exceptionHandling();
        mPopupWindow = new PopupWindow(getActivity());

        twitterUtils.setTwitterInstance(getContext());
        if (adapter == null || adapter.getCount() == 0) {
            //タイムライン取得
            dispSpinner(mPopupWindow);
            twitterUtils.getTimeLine(twitterValue.HOME);
            twitterUtils.search(creteGetTimeLineQuery());
        }

        listView = getActivity().findViewById(R.id.ft_main);

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
                    twitterUtils.getTimeLine(twitterValue.HOME, latest_minId);
                }
            }
        });
    }

    /**
     * コールバック
     * タイムライン取得後
     */
    @Override
    public void callBackGetTimeLine(ArrayList<Status> list, String howToDisplay) {
        if (checkViewDetach(this)) return;

        setListView(list, howToDisplay);
        hideSpinner(mPopupWindow);
    }

    /*public void callBackGetTimeLine(ArrayList<Status> list, String howToDisplay) {
        if (checkViewDetach(this)) return;

        setListView(list, howToDisplay);
        hideSpinner(mPopupWindow);
    }*/

    /**
     * フォローユーザー一覧取得
     */
    @Override
    public void callBackGetFriends(ArrayList<User> followList) {
        if(followList.size() == 0) return;

        long[] ids = new long[followList.size()];
        for(int i=0; i < followList.size(); i++){
            ids[i] = followList.get(i).getId();
        }
        twitterUtils.startStream(null, ids);
    }

    /**
     * Streamでの追跡結果を画面に追加する
     * @param status
     */
    @Override
    public void callBackStreamAddList(Status status) {
        if (checkViewDetach(this)) return;

        ArrayList<Status> list = new ArrayList<>();
        list.add(status);
        setListView(list, twitterValue.TWEET_HOW_TO_DISPLAY_UNSHIFT);
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

    /**
     * リストビューのスクロール位置を復元
     */
    private void restoreListViewSelection() {
        if (bundle.getInt(BUNDLE_KEY_ITEM_POSITION) >= 1) {
            listView.setSelection(bundle.getInt(BUNDLE_KEY_ITEM_POSITION));
        }
    }

    /**
     * ListViewにツイートをセット
     *
     * @param result
     */
    private void setListView(ArrayList<Status> result, String how_to_display) {
        //取得ツイートが０の場合、return
        if (result.size() == 0) {
            no_result();
            return;
        }

        latest_minId = ((twitter4j.Status) result.get(result.size() - 1)).getId();
        //カスタマイズしたリストviewに取得結果を表示
        if (adapter == null) {
            adapter = new SetDefaultTweetAdapter(getContext(), R.layout.tweet_default, result);
        } else {
            //最新ツイートを先頭に追加する＆一定以上の取得数の場合、追加ではなく洗い替えに変更
            //（古い順から取得ができないため）
            if (how_to_display.equals(twitterValue.TWEET_HOW_TO_DISPLAY_UNSHIFT) &&
                    result.size() >= twitterValue.tweetCounts.GET_COUNT_NEWER_TIMELINE) {
                how_to_display = twitterValue.TWEET_HOW_TO_DISPLAY_REWASH;
            }
            switch (how_to_display) {
                case twitterValue.TWEET_HOW_TO_DISPLAY_REWASH: //表示ツイート洗い替え
                    adapter.clear();
                    adapter.addItems(result);
                    adapter.notifyDataSetChanged();
                    break;

                case twitterValue.TWEET_HOW_TO_DISPLAY_UNSHIFT: //先頭に追加
                    putState(); //追加前に画面表示状態保持
                    adapter.unShiftItems(result);
                    try {
                        adapter.notifyDataSetChanged();
                    }catch (Exception e){
                        System.out.println(e);
                    }
                    restoreListViewSelection();
                    //スクロール位置復元
                    restoreListViewSelection();
                    break;

                case twitterValue.TWEET_HOW_TO_DISPLAY_PUSH: //末尾に追加
                    putState(); //追加前に画面表示状態保持

                    result.remove(result.size() - 1);
                    adapter.addItems(result);
                    adapter.notifyDataSetChanged();
                    restoreListViewSelection();
                    //位置復元
                    restoreListViewSelection();
                    break;
            }
        }

        //VIEWにアイテムが未登録の場合、登録
        if (listView.getAdapter() == null) {
            //監視スタート
            twitterUtils.getFriendIDs();
            listView.setAdapter(adapter);
        }
    }

    /**
     * 最新ツイートを追加
     */
    public void addTheLatestTweets() {
        //API制限中かチェック
        try {
            twitterUtils.checkAPIUnderRestriction(appSharedPreferences.API_RATE_DATE_GET_TIMELINE);
        } catch (ParseException e) {
            errHand.exceptionHand(e, callBacks);
        } catch (TwitterException e) {
            errHand.exceptionHand(e, callBacks);
        }

        dispSpinner(mPopupWindow);
        long sinceID = ((twitter4j.Status) adapter.getItem(0)).getId();
        long maxID = ((twitter4j.Status) adapter.getItem(adapter.getCount() - 1)).getId();
        twitterUtils.getTimeLine(
                twitterValue.HOME, sinceID, maxID, twitterValue.tweetCounts.GET_COUNT_NEWER_TIMELINE,
                twitterValue.TWEET_HOW_TO_DISPLAY_UNSHIFT);
    }

    /**
     * 画面の状態を保存
     */
    private void putState() {
        if (adapter == null || adapter.getCount() == 0) return;

        long maxID = ((Status) adapter.getItem(adapter.getCount() - 1)).getId();
        bundle.putLong(BUNDLE_KEY_ITEM_MAX_GET_ID, maxID);
        bundle.putInt(BUNDLE_KEY_ITEM_POSITION, listView.getFirstVisiblePosition());
    }
}