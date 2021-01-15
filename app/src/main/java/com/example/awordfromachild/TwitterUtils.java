package com.example.awordfromachild;

import android.content.Context;
import android.content.SharedPreferences;

import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.conf.Configuration;

public class TwitterUtils {
    //SharedPrerence用のキー
    private static final String TOKEN = "token";
    private static final String TOKEN_SECRET = "token_secret";
    private static final String PREF_NAME = "twitter_access_token";


    //Twitterインスタンスの生成
    public static Twitter getTwitterInstance(Context context){
        //Twitterオブジェクトのインスタンス
        //(キー等はtwitter4j.propertiesで定義済み)
        TwitterFactory factory = new TwitterFactory();
        Twitter twitter = factory.getInstance();

        //トークンの設定
        if(hasAccessToken(context)){
            twitter.setOAuthAccessToken(loadAccessToken(context));
        }
        return twitter;
    }

    //トークンの格納
    public static void storeAccessToken(Context context, AccessToken accessToken) {

        //トークンの設定
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(TOKEN, accessToken.getToken());
        editor.putString(TOKEN_SECRET, accessToken.getTokenSecret());

        //トークンの保存
        editor.commit();
    }

    //トークンの読み込み
    public static AccessToken loadAccessToken(Context context) {

        //preferenceからトークンの呼び出し
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String token = preferences.getString(TOKEN, null);
        String tokenSecret = preferences.getString(TOKEN_SECRET, null);
        if(token != null && tokenSecret != null){
            return new AccessToken(token, tokenSecret);
        }
        else{
            return null;
        }
    }

    //トークンの有無判定
    public static boolean hasAccessToken(Context context) {
        return  loadAccessToken(context) != null;
    }
}
