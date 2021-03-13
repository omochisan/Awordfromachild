package com.example.awordfromachild.tab;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ListView;
import android.widget.PopupWindow;

import com.example.awordfromachild.R;
import com.example.awordfromachild.TwitterUtils;
import com.example.awordfromachild.asynctask.callBacksTimeLine;
import com.example.awordfromachild.common.fragmentBase;
import com.example.awordfromachild.constant.twitterValue;
import com.example.awordfromachild.library.SetDefaultTweetAdapter;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import twitter4j.Status;

public class fragTimeLine extends fragmentBase implements callBacksTimeLine {
    //Bundleキー
    private static final String BUNDLE_KEY_ITEM_LIST = "ft_item_list";
    private static final String BUNDLE_KEY_ITEM_POSITION = "ft_item_position";
    //ListViewアダプター
    SetDefaultTweetAdapter adapter;
    //Twitter処理クラス
    private static TwitterUtils twitterUtils;
    //最新の読込タイムライン
    private static ArrayList<Status> latest_status;
    //現在実施中の読込開始ポイント
    private static int now_readPoint = 0;
    //スピナー用
    private static PopupWindow mPopupWindow;
    //onPuase時、ListView復元のため一時保存
    private static Bundle bundle = new Bundle();

    private ListView listView;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragtimeline_layout, container, false);
    }

    @Override
    public void onResume(){
        super.onResume();
        //ListViewの復元
        if (bundle.getSerializable(BUNDLE_KEY_ITEM_LIST) != null && adapter != null) {
            dispSpinner(mPopupWindow);
            adapter.clear();
            setListView((ArrayList<Status>)bundle.getSerializable(BUNDLE_KEY_ITEM_LIST));
            hideSpinner(mPopupWindow);
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
        //タイムライン取得
        twitterUtils.getTimeLine(twitterValue.HOME);

        //スクロールイベント
        //ポイントまでスクロールした時、追加で40件読み込み
        mPopupWindow = new PopupWindow(getActivity());
        listView = getActivity().findViewById(R.id.ft_main);
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
                    twitterUtils.getTimeLine(twitterValue.HOME, null, latest_status);
                }
            }
        });
    }

    /**
     * ListViewにツイートをセット
     * @param result
     */
    private void setListView(ArrayList<Status> result) {
        latest_status = result;
        //カスタマイズしたリストviewに取得結果を表示
        if (adapter == null) {
            adapter = new SetDefaultTweetAdapter(getContext(), R.layout.tweet_default, result);
        } else {
            adapter.addItems(result);
            adapter.notifyDataSetChanged();
        }
        //VIEWにアイテムが未登録の場合＝新規登録
        if(listView.getAdapter() == null){
            listView.setAdapter(adapter);
            //（画面復元の場合）スクロール位置を復元
            if(bundle.getInt(BUNDLE_KEY_ITEM_POSITION) >= 1){
                listView.setSelection(bundle.getInt(BUNDLE_KEY_ITEM_POSITION));
            }
        }
    }

    /**
     * コールバック
     * タイムライン取得後
     */
    @Override
    public void callBackGetTimeLine(ArrayList<Status> result) {
        if (checkViewDetach(this)) return;
        setListView(result);
        hideSpinner(mPopupWindow);
    }

    /**
     * ListViewの保持（Fragment再生成用）
     */
    @Override
    public void onPause() {
        super.onPause();
        bundle.putSerializable(BUNDLE_KEY_ITEM_LIST, getItemList(adapter));
        bundle.putInt(BUNDLE_KEY_ITEM_POSITION, listView.getFirstVisiblePosition());
    }

    /**
     * ListViewの保持（Fragment再生成用）
     *
     * @param state
     */
    @Override
    public void onSaveInstanceState(Bundle state) {
        super.onSaveInstanceState(state);
        state.putSerializable(BUNDLE_KEY_ITEM_LIST, getItemList(adapter));
    }
}