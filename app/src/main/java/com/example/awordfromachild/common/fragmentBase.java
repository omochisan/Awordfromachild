package com.example.awordfromachild.common;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.awordfromachild.R;
import com.example.awordfromachild.TweetDetailActivity;
import com.example.awordfromachild.TwitterUtils;
import com.example.awordfromachild.asynctask.callBacksBase;
import com.example.awordfromachild.constant.appSharedPreferences;
import com.example.awordfromachild.constant.twitterValue;
import com.example.awordfromachild.library.SetDefaultTweetAdapter;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import twitter4j.HashtagEntity;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.TwitterException;

public abstract class fragmentBase extends Fragment implements callBacksBase {
    //Bundleキー
    // 現在表示している中で、一番古いツイート
    protected static final String BUNDLE_KEY_ITEM_MAX_GET_ID = "item_max_get_id";
    // 現在のスクロール位置
    protected static final String BUNDLE_KEY_ITEM_POSITION = "item_position";
    //onPuase時、ListView復元のため一時保存
    protected static Bundle bundle = new Bundle();
    //Twitter処理クラス
    protected static TwitterUtils twitterUtils;
    //現在実施中の読込開始ポイント
    protected static int now_readPoint = 0;
    //スピナー用
    public static PopupWindow mPopupWindow;
    //ListViewアダプター
    protected static SetDefaultTweetAdapter adapter;
    protected static ListView listView;
    //エラーハンドリング
    protected static exceptionHandling errHand;
    //検索クエリ
    protected static String query;
    static WeakReference<Fragment> weak_fragment;

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
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        twitterUtils.setTwitterInstance(getContext());
        errHand = new exceptionHandling();
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
                //追加読込できるツイート無しの場合、return
                if(adapter != null && adapter.frg_end) return;

                //追加読込を始める位置 ＝ トータルアイテム数-(一回のツイート読込数 / 4)
                int readStartCount = totalItemCount - (twitterValue.tweetCounts.ONE_TIME_DISPLAY_TWEET / 4);
                //スクロール位置が追加読込ポイント（最終から10行前）の場合、追加読込開始
                if (firstVisibleItem == readStartCount && now_readPoint != readStartCount) {
                    //スピナー表示
                    dispSpinner(mPopupWindow);
                    now_readPoint = readStartCount;
                    twitterUtils.search(query, returnLastID(),
                            twitterValue.howToDisplayTweets.TWEET_HOW_TO_DISPLAY_PUSH);
                }
            }
        });

        // 行選択イベント
        listView.setOnItemClickListener(new AbsListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                ListView _listView = (ListView) adapterView;
                if(!(_listView.getItemAtPosition(i) instanceof Status)){
                    return;
                }
                Status status = (Status) _listView.getItemAtPosition(i);
                Intent intent = new Intent(getActivity(), TweetDetailActivity.class);
                intent.putExtra("DATA", status);
                startActivity(intent);
            }
        });
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

    /**
     * ストリーミングのフィルタ条件を設定し、ストリーミング開始
     *
     * @param arr_str    フィルタ条件（文字列）
     * @param arr_follow フィルタ条件（フォローユーザー）
     */
    public void startStreaming(String[] arr_str, long[] arr_follow) {
        twitterUtils.startStream(arr_str, arr_follow);
    }

    /**
     * フラグメントが破棄されたかチェック
     *
     * @param base
     * @return
     */
    public Boolean checkViewDetach(Fragment base) {
        weak_fragment = new WeakReference<Fragment>(base);
        Fragment fragment = weak_fragment.get();
        if (fragment.isDetached() || fragment.getActivity() == null) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * スピナーを表示
     *
     */
    public void dispSpinner(PopupWindow mPopupWindow) {
        WeakReference<PopupWindow> _popupWindow = new WeakReference<PopupWindow>(mPopupWindow);
        PopupWindow weak_pop = _popupWindow.get();
        //スピナー表示
        ProgressBar spinner = new ProgressBar(getActivity());
        weak_pop.setContentView(spinner);
        weak_pop.setBackgroundDrawable(getResources().getDrawable(R.drawable.popup_lightgray));
        // 表示サイズの設定 今回は幅300dp
        float width = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 300, getResources().getDisplayMetrics());
        weak_pop.setWindowLayoutMode((int) width, WindowManager.LayoutParams.WRAP_CONTENT);
        weak_pop.setWidth((int) width);
        weak_pop.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        weak_pop.showAtLocation(getActivity().findViewById(R.id.view_pager), Gravity.BOTTOM, 0, 0);
    }

    /**
     * スピナー非表示
     *
     */
    public void hideSpinner(PopupWindow mPopupWindow) {
        WeakReference<PopupWindow> _popupWindow = new WeakReference<PopupWindow>(mPopupWindow);
        PopupWindow weak_pop = _popupWindow.get();
        //スピナー退出
        if (weak_pop != null && weak_pop.isShowing()) {
            weak_pop.dismiss();
        }
    }

    /**
     * ツイート検索実行
     */
    public void runSearch(String q_str, Long sinceID, Long maxID, int count, String howToDisplay){
        dispSpinner(mPopupWindow);
        twitterUtils.search(q_str, sinceID, maxID, count, howToDisplay);
    }

    /**
     * Adapter内のアイテムをすべて取得する。
     *
     * @return
     */
    public ArrayList<Status> getItemList() {
        ArrayList<Status> statusList = new ArrayList<>();
        for (int i = 0; i < adapter.getCount(); i++) {
            statusList.add(adapter.getItem(i));
        }
        return statusList;
    }

    /**
     * アプリ用ハッシュタグを含むツイートを抽出する。
     *
     * @param list
     * @return
     */
    public ArrayList<Status> filterList(ArrayList<Status> list, String queryStr) {
        HashtagEntity[] hashTags;
        ArrayList<Status> returnList = new ArrayList<>();
        for (Status status : list) {
            hashTags = status.getHashtagEntities();
            if (Arrays.asList(hashTags).contains(twitterValue.APP_HASH_TAG)) {
                returnList.add(status);
            }
        }
        return returnList;
    }

    /**
     * TwitterAPIのレート制限中注意トーストを表示する。
     *
     * @param secondsUntilReset 解除されるまでの分数
     */
    public void ex_twitterAPILimit(int secondsUntilReset) {
        double minutes = Math.ceil(secondsUntilReset / 60) + 1;
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
     * @return
     */
    protected long returnLastID() {
        return adapter.getItem(adapter.getCount() - 1).getId();
    }

    /**
     * ListViewにツイートをセット
     *
     * @param result
     */
    protected void setListView(List<Status> result, String how_to_display) {
        int getCount = result.size(); //取得したカウント

        //取得ツイートが０の場合
        if (getCount == 0) {
            no_result();
        }

        //カスタマイズしたlistViewに取得結果を表示
        if (adapter == null) {
            adapter = new SetDefaultTweetAdapter(getContext(), R.layout.tweet_default, result);
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
                    }catch (Exception e){
                        System.out.println(e);
                    }
                    restoreListViewSelection();
                    //スクロール位置復元
                    restoreListViewSelection();
                    break;

                case twitterValue.howToDisplayTweets.TWEET_HOW_TO_DISPLAY_PUSH: //末尾に追加
                    putState(); //追加前に画面表示状態保持

                    result.remove(0);
                    adapter.addItems(result);
                    adapter.notifyDataSetChanged();
                    restoreListViewSelection();
                    //位置復元
                    restoreListViewSelection();
                    break;
            }
        }

        //取得可能ツイートがもう無い場合
        if(getCount == 0 || getCount < twitterValue.tweetCounts.ONE_TIME_DISPLAY_TWEET){
            adapter.frg_end = true;
        }
        //VIEWにアイテムが未登録の場合、登録
        if (listView.getAdapter() == null) {
            listView.setAdapter(adapter);
        }
    }

    /**
     * 最新ツイートを追加
     */
    public void addTheLatestTweets(WeakReference<callBacksBase> callBacks) {
    }

    /**
     * 画面の状態を保存
     */
    protected void putState() {
        if (adapter == null || adapter.getCount() == 0) return;

        long maxID = ((Status) adapter.getItem(adapter.getCount() - 1)).getId();
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
    }

    /**
     * コールバック
     * ツイート取得後
     */
    @Override
    public void callBackGetTweets(Object list, String howToDisplay) {
        if (checkViewDetach(this)) return;

        //100以上の場合、洗い替え
        String _howToDisplay ="";
        List<Status> s_list = ((QueryResult) list).getTweets();
        if(howToDisplay.equals(twitterValue.howToDisplayTweets.TWEET_HOW_TO_DISPLAY_UNSHIFT)
                && s_list.size() >= 100){
            _howToDisplay = twitterValue.howToDisplayTweets.TWEET_HOW_TO_DISPLAY_REWASH;
        }else{
            _howToDisplay = howToDisplay;
        }

        setListView(s_list, _howToDisplay);
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
