package com.example.awordfromachild.common;

import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.awordfromachild.R;
import com.example.awordfromachild.TweetDetailActivity;
import com.example.awordfromachild.asynctask.callBacksBase;
import com.example.awordfromachild.constant.twitterValue;
import com.example.awordfromachild.library.SetDefaultTweetAdapter;

import org.jetbrains.annotations.NotNull;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import twitter4j.DirectMessage;
import twitter4j.DirectMessageList;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.ResponseList;
import twitter4j.Status;

public abstract class fragmentBase extends Fragment implements callBacksBase {
    //Bundleキー
    // 現在表示している中で、一番古いツイート
    protected static final String BUNDLE_KEY_ITEM_MAX_GET_ID = "item_max_get_id";
    // 現在のスクロール位置
    protected static final String BUNDLE_KEY_ITEM_POSITION = "item_position";
    static WeakReference<Fragment> weak_fragment;
    //スピナー用
    public PopupWindow mPopupWindow;
    //onPuase時、ListView復元のため一時保存
    protected final Bundle bundle = new Bundle();
    //Twitter処理クラス
    protected TwitterUtils twitterUtils;
    //現在実施中の読込開始ポイント
    protected int now_readPoint = 0;
    //ListViewアダプター
    protected SetDefaultTweetAdapter adapter;
    protected ListView listView;
    //検索クエリ
    protected String query;
    //リストviewID
    protected int vid_listView = 0;
    //追加読込処理
    protected String getMethod;
    protected int p_count;

    private callBacksBase getThis() {
        return this;
    }

    /**
     * 検索機能取得
     *
     * @return 検索機能
     */
    protected TwitterUtils.search returnSearch() {
        return new TwitterUtils.search(getThis());
    }

    /**
     * お気に入り取得機能取得
     *
     * @return お気に入り取得機能
     */
    protected TwitterUtils.getFavorites returnGetFavorites() {
        return new TwitterUtils.getFavorites(getThis());
    }

    /**
     * タイムライン取得機能取得
     *
     * @return タイムライン取得機能
     */
    protected TwitterUtils.getTimeLine returnGetTimeLine() {
        return new TwitterUtils.getTimeLine(getThis());
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

    /**
     * 画面状態の保持（Fragment再生成用）
     */
    @Override
    public void onPause() {
        super.onPause();
        putState();
    }

    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        twitterUtils.setTwitterInstance(getContext());
        listView = requireActivity().findViewById(vid_listView);

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
                //追加読込できるツイート無しの場合、return
                if (adapter != null && adapter.frg_end) return;

                //追加読込を始める位置 ＝ トータルアイテム数-(一回のツイート読込数 / 4)
                int readStartCount = totalItemCount - (twitterValue.tweetCounts.ONE_TIME_DISPLAY_TWEET / 4);
                //スクロール位置が追加読込ポイント（最終から10行前）の場合、追加読込開始
                if (firstVisibleItem == readStartCount && now_readPoint != readStartCount) {
                    //スピナー表示
                    dispSpinner(mPopupWindow);
                    now_readPoint = readStartCount;
                    switch (getMethod) {
                        case twitterValue.getMethod.SEARCH:
                            TwitterUtils.search search = new TwitterUtils.search(getThis());
                            search.setParam(query, null, returnLastID(),
                                    twitterValue.tweetCounts.ONE_TIME_DISPLAY_TWEET,
                                    Query.ResultType.recent,
                                    twitterValue.howToDisplayTweets.TWEET_HOW_TO_DISPLAY_PUSH);
                            search.execute();
                            break;

                        case twitterValue.getMethod.FAVORITE:
                            p_count++;
                            TwitterUtils.getFavorites getFavorites = returnGetFavorites();
                            getFavorites.setParams(0,0,
                                    twitterValue.howToDisplayTweets.TWEET_HOW_TO_DISPLAY_PUSH);
                            getFavorites.execute();
                            break;

                        case twitterValue.getMethod.TIMELINE:
                            TwitterUtils.getTimeLine getTimeLine = returnGetTimeLine();
                            getTimeLine.setParam(twitterValue.timeLineType.USER, returnLastID(),
                                    0, twitterValue.tweetCounts.ONE_TIME_DISPLAY_TWEET_MAX,
                                    twitterValue.howToDisplayTweets.TWEET_HOW_TO_DISPLAY_PUSH);
                            break;

                        case twitterValue.getMethod.DM:

                            break;
                    }
                }
            }
        });

        // 行選択イベント
        listView.setOnItemClickListener((adapterView, view1, i, l) -> {
            ListView _listView = (ListView) adapterView;
            if (!(_listView.getItemAtPosition(i) instanceof Status)) {
                return;
            }
            Status status = (Status) _listView.getItemAtPosition(i);
            Intent intent = new Intent(getActivity(), TweetDetailActivity.class);
            intent.putExtra("DATA", status);
            startActivity(intent);
        });
    }

    /**
     * 画面状態の保持（Fragment再生成用）
     *
     * @param state 状態
     */
    @Override
    public void onSaveInstanceState(@NotNull Bundle state) {
        super.onSaveInstanceState(state);
        putState();
    }

    /**
     * ストリーミングのフィルタ条件を設定し、ストリーミング開始
     *
     * @param arr_str    フィルタ条件（文字列）
     * @param arr_follow フィルタ条件（フォローユーザー）
     */
    @SuppressWarnings("unused")
    public void startStreaming(String[] arr_str, long[] arr_follow) {
        twitterUtils.startStream(arr_str, arr_follow);
    }

    /**
     * フラグメントが破棄されたかチェック
     *
     * @param base 対象フラグメント
     * @return 破棄判定
     */
    public Boolean checkViewDetach(Fragment base) {
        weak_fragment = new WeakReference<>(base);
        Fragment fragment = weak_fragment.get();
        return fragment.isDetached() || fragment.getActivity() == null;
    }

    /**
     * スピナーを表示
     */
    public void dispSpinner(PopupWindow mPopupWindow) {
        WeakReference<PopupWindow> _popupWindow = new WeakReference<>(mPopupWindow);
        PopupWindow weak_pop = _popupWindow.get();
        //スピナー表示
        ProgressBar spinner = new ProgressBar(getActivity());
        weak_pop.setContentView(spinner);
        weak_pop.setBackgroundDrawable(
                ResourcesCompat.getDrawable(getResources(), R.drawable.popup_lightgray, null));
        // 表示サイズの設定 今回は幅300dp
        float width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics());
        weak_pop.setWindowLayoutMode((int) width, WindowManager.LayoutParams.WRAP_CONTENT);
        weak_pop.setWidth((int) width);
        weak_pop.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        weak_pop.showAtLocation(requireActivity().findViewById(R.id.view_pager), Gravity.BOTTOM, 0, 0);
    }

    /**
     * スピナー非表示
     */
    public void hideSpinner(PopupWindow mPopupWindow) {
        WeakReference<PopupWindow> _popupWindow = new WeakReference<>(mPopupWindow);
        PopupWindow weak_pop = _popupWindow.get();
        //スピナー退出
        if (weak_pop != null && weak_pop.isShowing()) {
            weak_pop.dismiss();
        }
    }

    /**
     * ツイート検索実行
     */
    public void runSearch(String q_str, Long sinceID, Long maxID, int count,
                          Query.ResultType resultType, String howToDisplay) {
        dispSpinner(mPopupWindow);
        TwitterUtils.search search = new TwitterUtils.search(getThis());
        search.setParam(q_str, sinceID, maxID, count, resultType, howToDisplay);
        search.execute();
    }

    /**
     * Adapter内のアイテムをすべて取得する。
     *
     * @return 全アイテム
     */
    public ArrayList<Status> getItemList() {
        ArrayList<Status> statusList = new ArrayList<>();
        for (int i = 0; i < adapter.getCount(); i++) {
            statusList.add(adapter.getItem(i));
        }
        return statusList;
    }

    /**
     * Adapter内のアイテムをすべて取得する。
     *
     * @return 全アイテム（ツイート）
     */
    public DirectMessageList getItemList_dm() {
        DirectMessageList dmList = null;
        for (int i = 0; i < adapter.getCount(); i++) {
            dmList.add((DirectMessage) adapter.getItem(i));
        }
        return dmList;
    }

    /**
     * TwitterAPIのレート制限中注意トーストを表示する。
     *
     * @param secondsUntilReset 解除されるまでの分数
     */
    public void ex_twitterAPILimit(int secondsUntilReset) {
        double minutes = Math.ceil((double) (int)secondsUntilReset / 60) + 1;
        String minutes_str = String.valueOf(minutes);
        Toast.makeText(getContext(), "ごめんなさい、この操作は制限中です。\n" +
                minutes_str.substring(0, minutes_str.indexOf(".")) +
                "分後に解除されます。", Toast.LENGTH_LONG).show();
    }

    /**
     * 表示ツイートがない場合のトーストを表示
     */
    protected void no_result() {
        Toast.makeText(getContext(),
                "表示するツイートがありませんでした。", Toast.LENGTH_LONG).show();
    }

    /**
     * チェック例外時、トースト表示
     */
    protected void fail_result() {
        Toast.makeText(
                getContext(), "データの取得に失敗しました。\n後でまたお試しください。",
                Toast.LENGTH_LONG).show();
    }

    /**
     * 表示中ツイートの中で、最後尾のツイートのIDを返却
     *
     * @return 最新ツイートのID
     */
    protected long returnLastID() {
        return adapter.getItem(adapter.getCount() - 1).getId();
    }

    /**
     * ListViewにツイートをセット
     *
     * @param result ツイート群
     */
    protected void setListView(List<Status> result, String how_to_display) {
        int getCount = result.size(); //取得したカウント

        //取得ツイートが０の場合
        if (getCount == 0) {
            no_result();
        }

        //カスタマイズしたlistViewに取得結果を表示
        if (adapter == null) {
            adapter = new SetDefaultTweetAdapter(getContext(), R.layout.tweet_default, result, null);
        } else {
            //最新ツイートを先頭に追加する＆一定以上の取得数の場合、追加ではなく洗い替えに変更
            //（古い順から取得ができないため）
            if (how_to_display.equals(twitterValue.howToDisplayTweets.TWEET_HOW_TO_DISPLAY_UNSHIFT) &&
                    result.size() >= twitterValue.tweetCounts.GET_COUNT_NEWER_TIMELINE) {
                how_to_display = twitterValue.howToDisplayTweets.TWEET_HOW_TO_DISPLAY_REWASH;
            }
            switch (how_to_display) {
                case twitterValue.howToDisplayTweets.TWEET_HOW_TO_DISPLAY_REWASH: //表示ツイート洗い替え
                    adapter.clear();
                    adapter.addItems(result, null);
                    adapter.notifyDataSetChanged();
                    break;

                case twitterValue.howToDisplayTweets.TWEET_HOW_TO_DISPLAY_UNSHIFT: //先頭に追加
                    putState(); //追加前に画面表示状態保持
                    adapter.unShiftItems(result, null);
                    try {
                        adapter.notifyDataSetChanged();
                    } catch (Exception ignored) {
                    }
                    restoreListViewSelection();
                    //スクロール位置復元
                    restoreListViewSelection();
                    break;

                case twitterValue.howToDisplayTweets.TWEET_HOW_TO_DISPLAY_PUSH: //末尾に追加
                    putState(); //追加前に画面表示状態保持

                    result.remove(0);
                    adapter.addItems(result, null);
                    adapter.notifyDataSetChanged();
                    restoreListViewSelection();
                    //位置復元
                    restoreListViewSelection();
                    break;
                default:
                    throw new IllegalStateException("Unexpected value: " + how_to_display);
            }
        }

        //取得可能ツイートがもう無い場合
        if (getCount == 0 || getCount < twitterValue.tweetCounts.ONE_TIME_DISPLAY_TWEET) {
            adapter.frg_end = true;
        }
        //VIEWにアイテムが未登録の場合、登録
        if (listView.getAdapter() == null) {
            listView.setAdapter(adapter);
        }
    }

    /**
     * ListViewにDMをセット
     *
     * @param result DM群
     */
    protected void setListView_directMessage(DirectMessageList result, String how_to_display) {
        int getCount = result.size(); //取得したカウント

        //取得ツイートが０の場合
        if (getCount == 0) {
            no_result();
        }

        //カスタマイズしたlistViewに取得結果を表示
        if (adapter == null) {
            adapter = new SetDefaultTweetAdapter(getContext(), R.layout.tweet_default, null, result);
        } else {
            //最新ツイートを先頭に追加する＆一定以上の取得数の場合、追加ではなく洗い替えに変更
            //（古い順から取得ができないため）
            if (how_to_display.equals(twitterValue.howToDisplayTweets.TWEET_HOW_TO_DISPLAY_UNSHIFT) &&
                    result.size() >= twitterValue.tweetCounts.GET_COUNT_NEWER_TIMELINE) {
                how_to_display = twitterValue.howToDisplayTweets.TWEET_HOW_TO_DISPLAY_REWASH;
            }
            switch (how_to_display) {
                case twitterValue.howToDisplayTweets.TWEET_HOW_TO_DISPLAY_REWASH: //表示ツイート洗い替え
                    adapter.clear();
                    adapter.addItems(null, result);
                    adapter.notifyDataSetChanged();
                    break;

                case twitterValue.howToDisplayTweets.TWEET_HOW_TO_DISPLAY_UNSHIFT: //先頭に追加
                    putState(); //追加前に画面表示状態保持
                    adapter.unShiftItems(null, result);
                    adapter.notifyDataSetChanged();
                    //スクロール位置復元
                    restoreListViewSelection();
                    break;

                case twitterValue.howToDisplayTweets.TWEET_HOW_TO_DISPLAY_PUSH: //末尾に追加
                    putState(); //追加前に画面表示状態保持

                    result.remove(0);
                    adapter.addItems(null, result);
                    adapter.notifyDataSetChanged();
                    //位置復元
                    restoreListViewSelection();
                    break;
            }
        }

        //取得可能ツイートがもう無い場合
        if (getCount == 0 || getCount < twitterValue.tweetCounts.ONE_TIME_DISPLAY_TWEET) {
            adapter.frg_end = true;
        }
        //VIEWにアイテムが未登録の場合、登録
        if (listView.getAdapter() == null) {
            listView.setAdapter(adapter);
        }
    }

    /**
     * 画面の状態を保存
     */
    protected void putState() {
        if (adapter == null || adapter.getCount() == 0) return;

        long maxID = (adapter.getItem(adapter.getCount() - 1)).getId();
        bundle.putLong(BUNDLE_KEY_ITEM_MAX_GET_ID, maxID);
        bundle.putInt(BUNDLE_KEY_ITEM_POSITION, listView.getFirstVisiblePosition());
    }

    /**
     * リストビューのスクロール位置を復元
     */
    protected void restoreListViewSelection() {
        if (bundle.getInt(BUNDLE_KEY_ITEM_POSITION) >= 1) {
            listView.setSelection(bundle.getInt(BUNDLE_KEY_ITEM_POSITION));
        }
    }

    /**
     * コールバック
     * Streamでの追跡結果を画面に追加する
     *
     * @param status 追跡ツイート
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
    }

    /**
     * 戻り値の型に合わせてキャスト
     *
     * @param obj キャスト前
     * @param <T> ジェネリクス
     * @return キャスト後
     */
    @SuppressWarnings("unchecked")
    public static <T> T autoCast(Object obj) {
        return (T) obj;
    }

    /**
     * コールバック
     * ツイート取得後
     */
    @Override
    public void callBackGetTweets(Object list, String howToDisplay) {
        if (checkViewDetach(this)) return;

        //100以上の場合、洗い替え
        String _howToDisplay;
        List<Status> s_list = null;
        DirectMessageList d_list = null;
        if (list instanceof ResponseList) {
            s_list = autoCast(list);
        } else if (list instanceof QueryResult) {
            QueryResult q_list = autoCast(list);
            s_list = q_list.getTweets();
        } else if (list instanceof DirectMessageList) {
            d_list = (DirectMessageList) list;
        }
        if (howToDisplay.equals(twitterValue.howToDisplayTweets.TWEET_HOW_TO_DISPLAY_UNSHIFT)
                && ((s_list != null && s_list.size() >= 100) || (d_list != null && d_list.size() >= 100))) {
            _howToDisplay = twitterValue.howToDisplayTweets.TWEET_HOW_TO_DISPLAY_REWASH;
        } else {
            _howToDisplay = howToDisplay;
        }

        //リストviewにセット
        if (s_list != null) {
            setListView(s_list, _howToDisplay);
        } else {
            setListView_directMessage(d_list, _howToDisplay);
        }
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
}
