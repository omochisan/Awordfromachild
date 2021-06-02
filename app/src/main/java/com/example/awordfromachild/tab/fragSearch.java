package com.example.awordfromachild.tab;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.ProgressBar;
import android.widget.SearchView;
import android.widget.TextView;

import com.example.awordfromachild.MainActivity;
import com.example.awordfromachild.R;
import com.example.awordfromachild.TwitterUtils;
import com.example.awordfromachild.asynctask.callBacksSearch;
import com.example.awordfromachild.common.fragmentBase;
import com.example.awordfromachild.constant.twitterValue;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.Twitter;

public class fragSearch extends fragmentBase implements callBacksSearch {
    static String _query = "";

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
        return inflater.inflate(R.layout.fragsearch_layout,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //ビュー イベント設定
        SearchView searchView = view.findViewById(R.id.fs_input_word);
        searchView.setOnQueryTextListener(onQueryTextListener);
        Button backButton = view.findViewById(R.id.fs_back);
        backButton.setOnClickListener(onBackClickListener);
        ImageView searchIcon = view.findViewById(R.id.fs_searchIcon);
        searchIcon.setOnClickListener(onSearchIconClickListener);
        //初期表示時は検索結果欄を非表示
        searchIcon.setVisibility(View.INVISIBLE);
    }

    private View.OnClickListener onBackClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            //検索窓非表示
            LinearLayout layout_main = getActivity().findViewById(R.id.fs_linearLayout_main);
            layout_main.setVisibility(View.GONE);
            //検索アイコン表示
            ImageView searchIcon = getActivity().findViewById(R.id.fs_searchIcon);
            searchIcon.setVisibility(View.VISIBLE);
        }
    };

    private View.OnClickListener onSearchIconClickListener = new View.OnClickListener(){
        @Override
        public void onClick(View view) {
            //検索窓再表示
            LinearLayout layout_main = getActivity().findViewById(R.id.fs_linearLayout_main);
            layout_main.setVisibility(View.VISIBLE);
        }
    };

    //検索ボックス　イベント
    private SearchView.OnQueryTextListener onQueryTextListener = new SearchView.OnQueryTextListener() {
        @Override
        // SubmitボタンorEnterKeyを押されたら呼び出されるメソッド
        public boolean onQueryTextSubmit(String searchWord) {
            //検索実行
            dispSpinner(mPopupWindow);
            //_query = twitterValue.APP_HASH_TAG + " " + searchWord;
            _query = searchWord;
            query = _query;
            twitterUtils.search(_query, null, null, 0, Query.ResultType.recent,
                    twitterValue.howToDisplayTweets.TWEET_HOW_TO_DISPLAY_REWASH);
            return false;
        }

        @Override
        public boolean onQueryTextChange(String newText) {
            // 入力される度に呼び出される
            return false;
        }
    };

    /**
     * 非同期処理のコールバック関数
     * 取得したツイートを表示
     */
    @Override
    public void callBackGetTweets(Object list, String howToDisplay) {
        super.callBackGetTweets(list, howToDisplay);
        //検索窓非表示
        LinearLayout layout_main = getActivity().findViewById(R.id.fs_linearLayout_main);
        layout_main.setVisibility(View.GONE);
        //検索アイコン表示
        ImageView searchIcon = getActivity().findViewById(R.id.fs_searchIcon);
        searchIcon.setVisibility(View.INVISIBLE);
    }
}
