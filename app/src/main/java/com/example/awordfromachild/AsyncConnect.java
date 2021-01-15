package com.example.awordfromachild;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.EditText;

import twitter4j.*;
import twitter4j.auth.*;
import androidx.appcompat.app.AppCompatActivity;

import com.example.awordfromachild.MainActivity;

public class AsyncConnect extends AsyncTask<String, Integer, Integer> {
    private RequestToken requestToken;
    private Query query = new Query();
    private Twitter twitter = TwitterFactory.getSingleton();
    private AppCompatActivity act = new AppCompatActivity();

    // 非同期処理
    @Override
    protected Integer doInBackground(String... params) {
        //Twitter連携機能を実行
        switch (params[0]){
            case "search": //検索
                search("マヂカルラブリー");
                break;

            case "tweet": //ツイート
                break;
        }
        search(params[0]);
        return  null;
    }

    // 非同期処理が終了後、結果をメインスレッドに返す
    @Override
    protected void onPostExecute(Integer result) {
    }

    public void search(String str) {
        // 検索ワードをセット
        query.setQuery("#" + str);
        // 1度のリクエストで取得するTweetの数（100が最大）
        query.setCount(10);
        // 検索実行
        try {
            QueryResult result = twitter.search(query);
            System.out.println("ヒット数 : " + result.getTweets().size());
            // 検索結果を見てみる
            for (twitter4j.Status tweet : result.getTweets()) {
                // 本文
                String t_text = tweet.getText();
                System.out.println(t_text);
            /*
            // ハッシュタグとURLの削除
            StringTokenizer sta = new StringTokenizer(t_text, " ");
            //トークンの出力
            while (sta.hasMoreTokens()) {
                String wk = sta.nextToken();
                if (wk.indexOf("#") == -1 && wk.indexOf("http") == -1
                        && wk.indexOf("RT") == -1 && wk.indexOf("@") == -1) {
                }
            }
            */
            }
        } catch (TwitterException e) {
            System.out.println(e);
        }
    }

    private void tweet() {
        try {
            twitter4j.Status status = twitter.updateStatus("Twitter4Jから初めてのツイート！ #twitter4j");
            System.out.println("Successfully updated the status to [" + status.getText() + "].");
        } catch (TwitterException e) {
            e.printStackTrace();
        }
    }

}