package com.example.awordfromachild.tab;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.example.awordfromachild.MainActivity;
import com.example.awordfromachild.R;
import com.example.awordfromachild.TwitterUtils;
import com.example.awordfromachild.asynctask.callBacksSearch;
import com.example.awordfromachild.common.fragmentBase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import twitter4j.QueryResult;
import twitter4j.Twitter;

public class fragSearch extends fragmentBase implements callBacksSearch {
    //画面
    public View fs_view;
    //Twitter処理クラス
    private TwitterUtils twitterUtils;
    //Twitter
    private Twitter twitter;
    //スピナー（プログレスバー）
    private ProgressBar spinner;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragsearch_layout,container,false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if (null != savedInstanceState) {
            //mUser = savedInstanceState.getSerializable("User");
        }

        fs_view = view;
        twitter = TwitterUtils.getTwitterInstance(getContext());
        //共通Twitter処理クラス
        MainActivity mainactivity = new MainActivity();
        twitterUtils = new TwitterUtils(this);
        //スピナー
        spinner = view.findViewById(R.id.fs_progressBar);

        //検索ボタン
        Button search_btn = view.findViewById(R.id.fs_btn_search);
        search_btn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                //スピナー表示
                spinner.setVisibility(android.widget.ProgressBar.VISIBLE);
                //非同期処理（Twitterへ接続）開始
                EditText value = view.findViewById(R.id.fs_input_tweet);
                String str = value.getText().toString();
                twitterUtils.search(str);
            }
        });
    }

    @Override
    /**
     * 画面のGC対策（画面復帰用）
     * ・メンバ変数の保存
     */
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        //savedInstanceState.putBoolean("Num", mNum);
    }

    /**
     * 非同期処理のコールバック関数
     * 取得したツイートを表示
     * @param getTweets
     */
    public void callBackGetSearch(QueryResult getTweets) {
        if(checkViewDetach(this)) return;

        LinearLayout view_result = fs_view.findViewById(R.id.fs_result);
        int index = 0;
        for (twitter4j.Status tweet : getTweets.getTweets()) {
            // TextView インスタンス生成
            TextView textView = new TextView(getContext());
            String t_text = tweet.getText();
            textView.setText(t_text);
            textView.setBackgroundResource(R.drawable.box_lightgray);
            view_result.addView(textView,
                    new LinearLayout.LayoutParams(
                            LinearLayout.LayoutParams.MATCH_PARENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT));
            index++;
        }
        //スピナー非表示
        spinner.setVisibility(android.widget.ProgressBar.INVISIBLE);
        //検索画面非表示
        LinearLayout layout_main = fs_view.findViewById(R.id.fs_linearLayout_main);
        layout_main.setVisibility(View.GONE);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        System.out.println("onDestroy");
    }
}
