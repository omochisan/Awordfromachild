package com.example.awordfromachild.common;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.awordfromachild.R;
import com.example.awordfromachild.TweetDetailActivity;
import com.example.awordfromachild.asynctask.callBacksBase;
import com.example.awordfromachild.constant.appSharedPreferences;
import com.example.awordfromachild.constant.twitterValue;
import com.example.awordfromachild.library.SetDefaultTweetAdapter;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.util.List;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import twitter4j.Status;
import twitter4j.TwitterException;

public class activityBase extends AppCompatActivity implements callBacksBase {
    //Bundleキー
    // 現在表示している中で、一番古いツイート
    protected static final String BUNDLE_KEY_ITEM_MAX_GET_ID = "item_max_get_id";
    // 現在のスクロール位置
    protected static final String BUNDLE_KEY_ITEM_POSITION = "item_position";
    static WeakReference<Activity> weak_activity;
    //スピナー用
    public PopupWindow mPopupWindow;
    //onPuase時、ListView復元のため一時保存
    protected final Bundle bundle = new Bundle();
    //ListViewアダプター
    protected SetDefaultTweetAdapter adapter;
    protected ListView listView;
    //エラーハンドリング
    protected exceptionHandling errHand;
    //Twitter処理クラス
    protected TwitterUtils twitterUtils;
    protected TwitterUtils.getTimeLine getTimeLine;
    //現在実施中の読込開始ポイント
    protected int now_readPoint = 0;
    //リストviewID
    protected int vid_listView = 0;

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
    protected void onCreate(Bundle savedInstanceState) {
        try {
            super.onCreate(savedInstanceState);
            getTimeLine = new TwitterUtils.getTimeLine(this);

            if (callBacksBase.class.isAssignableFrom(this.getClass())) {
                twitterUtils = new TwitterUtils(this);
                twitterUtils.setTwitterInstance(this);
            }
            if (vid_listView != 0) {
                listView = findViewById(vid_listView);
            }
            errHand = new exceptionHandling();

            if (listView != null) {
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
                            getTimeLine.setParam(twitterValue.timeLineType.USER, returnLastID(),
                                    0, twitterValue.tweetCounts.ONE_TIME_DISPLAY_TWEET,
                                    twitterValue.howToDisplayTweets.TWEET_HOW_TO_DISPLAY_PUSH);
                            getTimeLine.execute();
                        }
                    }
                });

                // 行選択イベント
                listView.setOnItemClickListener((adapterView, view, i, l) -> {
                    ListView _listView = (ListView) adapterView;
                    Status status = (Status) _listView.getItemAtPosition(i);
                    Intent intent = new Intent(getApplicationContext(), TweetDetailActivity.class);
                    intent.putExtra("DATA", status);
                    startActivity(intent);
                });
            }
        } catch (Exception e) {
            Log.e("エラー", e.toString());
        }
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
     * viewが破棄されたかチェック
     * @param base アクティビティ
     * @return 破棄／未破棄
     */
    public Boolean checkViewDetach(Activity base) {
        weak_activity = new WeakReference<>(base);
        Activity activity = weak_activity.get();
        return activity.isDestroyed();
    }

    /**
     * TwitterAPIのレート制限発生
     */
    public void ex_twitterAPILimit(int secondsUntilReset) {
        double minutes = Math.ceil((double) (int)secondsUntilReset / 60) + 1;
        String minutes_str = String.valueOf(minutes);
        Toast.makeText(this, "ごめんなさい、この操作は制限中です。\n" +
                minutes_str.substring(0, minutes_str.indexOf(".")) +
                "分後に解除されます。", Toast.LENGTH_LONG).show();
    }

    /**
     * チェック例外時、トースト表示
     */
    public void fail_result() {
        Toast.makeText(
                this, "データの取得に失敗しました。\n後でまたお試しください。",
                Toast.LENGTH_LONG).show();
    }

    /**
     * スピナーを表示
     *
     * @param mPopupWindow ポップアップ
     */
    public void dispSpinner(PopupWindow mPopupWindow, int viewID) {
        WeakReference<PopupWindow> _popupWindow = new WeakReference<>(mPopupWindow);
        PopupWindow weak_pop = _popupWindow.get();
        //スピナー表示
        ProgressBar spinner = new ProgressBar(this);
        weak_pop.setContentView(spinner);
        weak_pop.setBackgroundDrawable(
                ResourcesCompat.getDrawable(getResources(), R.drawable.popup_lightgray, null));
        // 表示サイズの設定 今回は幅300dp
        float width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics());
        weak_pop.setWindowLayoutMode((int) width, WindowManager.LayoutParams.WRAP_CONTENT);
        weak_pop.setWidth((int) width);
        weak_pop.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        // 画面下に表示
        weak_pop.showAtLocation(findViewById(viewID), Gravity.BOTTOM, 0, 0);
    }

    /**
     * スピナー非表示
     *
     * @param mPopupWindow ポップアップ
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
     * 表示ツイートがない場合のトーストを表示
     */
    protected void no_result() {
        Toast.makeText(getApplicationContext(),
                "表示するツイートがありませんでした。", Toast.LENGTH_LONG).show();
    }

    /**
     * 表示中ツイートの中で、最後尾のツイートのIDを返却
     *
     * @return 最後尾ツイートID
     */
    protected long returnLastID() {
        return adapter.getItem(adapter.getCount() - 1).getId();
    }

    /**
     * ListViewにツイートをセット
     *
     * @param result セットするツイート群
     * @param how_to_display セット方法（洗い替え、先頭に追加、末尾に追加）
     */
    protected void setListView(List<Status> result, String how_to_display) {
        int getCount = result.size(); //取得したカウント

        //取得ツイートが０の場合
        if (getCount == 0) {
            no_result();
        }

        //カスタマイズしたlistViewに取得結果を表示
        if (adapter == null) {
            adapter = new SetDefaultTweetAdapter(this, R.layout.tweet_default, result);
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
                    adapter.addItems(result);
                    adapter.notifyDataSetChanged();
                    break;

                case twitterValue.howToDisplayTweets.TWEET_HOW_TO_DISPLAY_UNSHIFT: //先頭に追加
                    putState(); //追加前に画面表示状態保持
                    adapter.unShiftItems(result);
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

                    result.remove(result.size() - 1);
                    adapter.addItems(result);
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
     * 最新ツイートを追加
     * ※現在不使用
     */
    @SuppressWarnings("unused")
    protected void addTheLatestTweets(WeakReference<callBacksBase> callBacks, int viewID) {
        //API制限中かチェック
        try {
            TwitterUtils.checkAPIUnderRestriction(appSharedPreferences.API_RATE_DATE_GET_TIMELINE);
        } catch (ParseException | TwitterException e) {
            errHand.exceptionHand(e, callBacks);
        }

        dispSpinner(mPopupWindow, viewID);
        long sinceID = (adapter.getItem(0)).getId();
        long maxID = (adapter.getItem(adapter.getCount() - 1)).getId();
        getTimeLine.setParam(
                twitterValue.timeLineType.HOME, sinceID, maxID, twitterValue.tweetCounts.GET_COUNT_NEWER_TIMELINE,
                twitterValue.howToDisplayTweets.TWEET_HOW_TO_DISPLAY_UNSHIFT);
        getTimeLine.execute();
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
     * 非チェック例外発生時
     */
    @Override
    public void callBackException() {
        fail_result();
    }

    @Override
    public void callBackTwitterLimit(int secondsUntilReset) {
    }

    @Override
    public void callBackStreamAddList(Status status) {

    }

    @Override
    public void callBackGetTweets(Object list, String howToDisplay) {

    }
}
