package com.example.awordfromachild;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

/**
 * Twitterアカウントの認証処理を行うActivity
 */
public class TwitterLoginActivity extends Activity {
    private static String callBackURL;
    private static Twitter twitter;
    private static RequestToken requestToken;

    private static TwitterLoginActivity twitterLoginActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //CallBack用URLの設定
        callBackURL = getString(R.string.twitter_callback_url);
        //Twitterインスタンスの取得
        twitter = TwitterUtils.getTwitterInstance(this);
        twitterLoginActivity = this;

        //認証開始
        TwitterLoginActivity.startAuthorize startAuthorize = new TwitterLoginActivity.startAuthorize();
        startAuthorize.execute();
    }

    /**
     * ブラウザでの認証終了後のコールバック処理
     *
     * @param intent インテント
     */
    @Override
    protected void onNewIntent(Intent intent) {
        getAccessToken getAccessToken = new getAccessToken(intent);
        //URLによって実行されたアプリから引数を取得する
        getAccessToken.execute(intent.getData().getQueryParameter("oauth_verifier"));
    }

    /**
     * 認証処理の開始
     * リクエストトークンの取得～アクセストークン取得先URLを開く
     */
    public static class startAuthorize extends AsyncTask<Void, Void, String> {
        @Override
        protected String doInBackground(Void... params) {
            try {
                //リクエストトークンの取得
                requestToken = twitter.getOAuthRequestToken(callBackURL);
                return requestToken.getAuthorizationURL();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String url) {
            try {
                if (url != null) {
                    //渡されたurlへアクティビティを遷移する
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                    twitterLoginActivity.startActivity(intent);
                } else {
                    // 失敗。。。
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static class getAccessToken extends AsyncTask<String, Void, AccessToken> {
        Intent intent;

        public getAccessToken(Intent intent) {
            this.intent = intent;
        }

        @Override
        protected AccessToken doInBackground(String... params) {
            try {
                if (intent == null || intent.getData() == null ||
                        !intent.getData().toString().startsWith(callBackURL)) {
                    return null;
                } else {
                    //アクセストークンの取得
                    return twitter.getOAuthAccessToken(requestToken, params[0]);
                }
            } catch (TwitterException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(AccessToken accessToken) {
            //トークンの登録
            if (accessToken != null) {
                successOAuth(accessToken);
            }
        }

        /**
         * （アクセストークン取得成功時）
         * ・アクセストークンをデバイスへ登録
         * ・メイン画面へ遷移
         *
         * @param accessToken
         */
        private void successOAuth(AccessToken accessToken) {
            //Utilクラスからトークン登録メソッドを呼び出し
            TwitterUtils.storeAccessToken(twitterLoginActivity, accessToken);
            //Twitterインスタンスへアクセストークンをセット
            twitter.setOAuthAccessToken(accessToken);

            //MainActivityへ遷移
            Intent intent = new Intent(twitterLoginActivity, MainActivity.class);
            twitterLoginActivity.startActivity(intent);

            //このアクティビティを終了する
            twitterLoginActivity.finish();
        }
    }
}
