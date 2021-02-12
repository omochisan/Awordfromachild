package com.example.awordfromachild;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.Toast;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

/**
 * Twitterアカウントの認証処理を行うActivity
 */
public class TwitterLoginActivity extends Activity {
    private String callBackURL;
    private Twitter twitter;
    private RequestToken requestToken;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //CallBack用URLの設定
        callBackURL = getString(R.string.twitter_callback_url);
        //Twitterインスタンスの取得
        twitter = TwitterUtils.getTwitterInstance(this);

        //認証開始
        startAuthorize();
    }

    /**
     * 認証処理の開始
     * リクエストトークンの取得～アクセストークン取得先URLを開く
     */
    public void startAuthorize() {
        //AsyncTaskによる非同期処理
        AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                try {
                    //リクエストトークンの取得
                    requestToken = twitter.getOAuthRequestToken(callBackURL);
                    return requestToken.getAuthorizationURL();
                }catch (Exception e){
                    e.printStackTrace();
                }
                return null;
            }
            @Override
            protected void onPostExecute(String url) {
                try{
                    if (url != null) {
                        //渡されたurlへアクティビティを遷移する
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                        startActivity(intent);
                    } else {
                        // 失敗。。。
                    }
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        };
        task.execute();
    }

    /**
     * ブラウザでの認証終了後のコールバック処理
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        getAccessToken(intent);
    }

    /**
     * アクセストークン取得
     * @param intent
     */
    public void getAccessToken(Intent intent) {
        if (intent == null || intent.getData() == null || !intent.getData().toString().startsWith(callBackURL)) {
            return;
        }
        //URLによって実行されたアプリから引数を取得する
        String verifier = intent.getData().getQueryParameter("oauth_verifier");

        AsyncTask<String, Void, AccessToken> task = new AsyncTask<String, Void, AccessToken>() {
            @Override
            protected AccessToken doInBackground(String... params) {
                try {
                    //アクセストークンの取得
                    return twitter.getOAuthAccessToken(requestToken, params[0]);
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(AccessToken accessToken) {
                //トークンの登録
                if (accessToken != null) {
                    // 認証成功！
                    showTokenResult("認証成功！");
                    successOAuth(accessToken);
                } else {
                    // 認証失敗。。。
                    showTokenResult("認証失敗。。。");
                }
            }
        };
        task.execute(verifier);
    }

    /**
     * （アクセストークン取得成功時）
     * ・アクセストークンをデバイスへ登録
     * ・メイン画面へ遷移
     * @param accessToken
     */
    private void successOAuth(AccessToken accessToken) {
        //Utilクラスからトークン登録メソッドを呼び出し
        TwitterUtils.storeAccessToken(this, accessToken);
        //Twitterインスタンスへアクセストークンをセット
        twitter.setOAuthAccessToken(accessToken);

        //MainActivityへ遷移
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);

        //このアクティビティを終了する
        finish();
    }

    //トーストを表示するメソッド
    private void showTokenResult(String text) {

    }
}
