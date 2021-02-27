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
    //listviewのアイテムを保存するためのキー
    private static final String BUNDLE_KEY_ITEM_LIST = "ft_item_list";
    //ListViewアダプター
    SetDefaultTweetAdapter adapter;
    //Twitter処理クラス
    private TwitterUtils twitterUtils;
    //最新の読込タイムライン
    private ArrayList<Status> latest_status;
    //現在実施中の読込開始ポイント
    private int now_readPoint = 0;
    //スピナー用
    private PopupWindow mPopupWindow;
    //onPuase時、ListView復元のため一時保存
    private ArrayList<Status> temp_listView;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragtimeline_layout, container, false);
    }

    @Override
    public void onCreate(Bundle b) {
        super.onCreate(b);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        //ListViewの復元
        if (temp_listView != null) {
            dispSpinner(mPopupWindow);
            adapter.clear();
            setListView(temp_listView);
            hideSpinner(mPopupWindow);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        twitterUtils = new TwitterUtils(this);
        twitterUtils.setTwitterInstance(getContext());
        //タイムライン取得
        twitterUtils.getTimeLine(twitterValue.HOME);

        mPopupWindow = new PopupWindow(getActivity());

        //スクロールイベント
        //ポイントまでスクロールした時、追加で40件読み込み
        ListView listView = getActivity().findViewById(R.id.ft_main);
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
     *
     * @param result
     */
    private void setListView(ArrayList<Status> result) {
        latest_status = result;
        ListView view_result = getActivity().findViewById(R.id.ft_main);
        //カスタマイズしたリストviewに取得結果を表示
        if (adapter == null) {
            adapter = new SetDefaultTweetAdapter(getContext(), R.layout.tweet_default, result);
            view_result.setAdapter(adapter);
        } else {
            adapter.addItems(result);
            adapter.notifyDataSetChanged();
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
        temp_listView = getItemList(adapter);
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