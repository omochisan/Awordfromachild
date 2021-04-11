package com.example.awordfromachild.tab;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.example.awordfromachild.R;
import com.example.awordfromachild.TwitterUtils;
import com.example.awordfromachild.asynctask.callBacksTimeLine;
import com.example.awordfromachild.common.fragmentBase;
import com.example.awordfromachild.constant.twitterValue;
import com.example.awordfromachild.library.SetDefaultTweetAdapter;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import twitter4j.Status;

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
    //取得ツイートの表示方法
    private String how_to_display;

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

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        twitterUtils = new TwitterUtils(this);
        twitterUtils.setTwitterInstance(getContext());
        mPopupWindow = new PopupWindow(getActivity());
        if (adapter == null ||adapter.getCount() == 0) {
            //タイムライン取得
            dispSpinner(mPopupWindow);
            how_to_display = twitterValue.TWEET_HOW_TO_DISPLAY_REWASH;
            twitterUtils.getTimeLine(twitterValue.HOME);
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
                int readStartCount = totalItemCount - (twitterValue.GET_COUNT_TIMELINE / 4);
                //スクロール位置が追加読込ポイント（最終から10行前）の場合、追加読込開始
                if (firstVisibleItem == readStartCount && now_readPoint != readStartCount) {
                    //スピナー表示
                    dispSpinner(mPopupWindow);
                    now_readPoint = readStartCount;
                    how_to_display = twitterValue.TWEET_HOW_TO_DISPLAY_PUSH;
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
    public void callBackGetTimeLine(ArrayList<Status> list) {
        if (checkViewDetach(this)) return;
        setListView(list);
        hideSpinner(mPopupWindow);
    }

    /**
     * 取得ツイートの追加方式を設定
     * @param how
     */
    @Override
    public void setHowToDisplay(String how) {
        how_to_display = how;
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
    private void setListView(ArrayList<Status> result) {
        //取得ツイートが０の場合、return
        if (result.size() == 0) {
            no_result();
            return;
        }

        latest_minId = ((twitter4j.Status) result.get(result.size() - 1)).getId();
        //カスタマイズしたリストviewに取得結果を表示
        if (adapter == null) {
            adapter = new SetDefaultTweetAdapter(getContext(), R.layout.tweet_default, result);
        }else{
            //最新ツイートを先頭に追加する＆一定以上の取得数の場合、追加ではなく洗い替えに変更
            //（古い順から取得ができないため）
            if(how_to_display.equals(twitterValue.TWEET_HOW_TO_DISPLAY_UNSHIFT) &&
            result.size() >= twitterValue.GET_COUNT_NEWER_TIMELINE){
                how_to_display = twitterValue.TWEET_HOW_TO_DISPLAY_REWASH;
            }
            switch (how_to_display){
                case twitterValue.TWEET_HOW_TO_DISPLAY_REWASH: //表示ツイート洗い替え
                    adapter.clear();
                    adapter.addItems(result);
                    adapter.notifyDataSetChanged();
                    break;

                case twitterValue.TWEET_HOW_TO_DISPLAY_UNSHIFT: //先頭に追加
                    putState(); //追加前に画面表示状態保持
                    adapter.unShiftItems(result);
                    adapter.notifyDataSetChanged();
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
            listView.setAdapter(adapter);
        }
    }

    /**
     * 最新ツイートを追加
     */
    public void addTheLatestTweets(){
        dispSpinner(mPopupWindow);
        long sinceID = ((twitter4j.Status) adapter.getItem(0)).getId();
        long maxID = ((twitter4j.Status) adapter.getItem(adapter.getCount() - 1)).getId();
        twitterUtils.getTimeLine(
                twitterValue.HOME, sinceID, maxID, twitterValue.GET_COUNT_NEWER_TIMELINE,
                twitterValue.TWEET_HOW_TO_DISPLAY_UNSHIFT);
    }

    /**
     * 画面の状態を保存
     */
    private void putState() {
        if(adapter == null || adapter.getCount() == 0) return;

        long maxID = ((Status) adapter.getItem(adapter.getCount() - 1)).getId();
        bundle.putLong(BUNDLE_KEY_ITEM_MAX_GET_ID, maxID);
        bundle.putInt(BUNDLE_KEY_ITEM_POSITION, listView.getFirstVisiblePosition());
    }
}