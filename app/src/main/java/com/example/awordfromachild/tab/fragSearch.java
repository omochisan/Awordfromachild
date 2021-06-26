package com.example.awordfromachild.tab;

import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.PopupWindow;
import android.widget.SearchView;

import com.example.awordfromachild.R;
import com.example.awordfromachild.common.TwitterUtils;
import com.example.awordfromachild.asynctask.callBacksSearch;
import com.example.awordfromachild.common.fragmentBase;
import com.example.awordfromachild.constant.twitterValue;

import org.jetbrains.annotations.NotNull;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import twitter4j.Query;

public class fragSearch extends fragmentBase implements callBacksSearch {
    static String _query = "";
    //検索ボックス　イベント
    private final SearchView.OnQueryTextListener onQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        // SubmitボタンorEnterKeyを押されたら呼び出されるメソッド
        public boolean onQueryTextSubmit(String searchWord) {
            //検索実行
            dispSpinner(mPopupWindow);
            CheckBox checkBox = requireActivity().findViewById(R.id.fs_tagInclude);
            //チェックされてる場合、タグ含める
            if (checkBox.isChecked()) {
                _query = twitterValue.APP_HASH_TAG + " " + searchWord;
            } else {
                _query = searchWord;
            }
            query = _query;
            TwitterUtils.search search = returnSearch();
            search.setParam(_query, null, null, 0, Query.ResultType.recent,
                    twitterValue.howToDisplayTweets.TWEET_HOW_TO_DISPLAY_REWASH);
            search.execute();
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            // 入力される度に呼び出される
            return false;
        }
    };

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        twitterUtils = new TwitterUtils(this);
        mPopupWindow = new PopupWindow(getContext()); //スピナー用
        vid_listView = R.id.fs_main;
        query = _query;
        getMethod = twitterValue.getMethod.SEARCH;
        return inflater.inflate(R.layout.fragsearch_layout, container, false);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    public void onViewCreated(@NotNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //ビュー イベント設定
        SearchView searchView = view.findViewById(R.id.fs_input_word);
        searchView.setOnQueryTextListener(onQueryTextListener);
    }

    /**
     * リロードボタン押下時、
     * 最新ツイートを取得
     */
    public void addTheLatestTweets() {
        dispSpinner(mPopupWindow);
        long sinceID = (adapter.getItem(0)).getId();
        runSearch(query, sinceID, null, twitterValue.tweetCounts.ONE_TIME_DISPLAY_TWEET_MAX,
                Query.ResultType.recent, twitterValue.howToDisplayTweets.TWEET_HOW_TO_DISPLAY_UNSHIFT);
    }
}
