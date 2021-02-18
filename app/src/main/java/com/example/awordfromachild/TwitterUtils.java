package com.example.awordfromachild;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.awordfromachild.asynctask.callBacksBase;
import com.example.awordfromachild.asynctask.callBacksMain;
import com.example.awordfromachild.asynctask.callBacksSearch;
import com.example.awordfromachild.asynctask.callBacksTimeLine;
import com.example.awordfromachild.constant.appSharedPrerence;
import com.example.awordfromachild.constant.timelineType;
import com.example.awordfromachild.tab.fragSearch;

import java.lang.ref.WeakReference;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.ResponseList;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;

/**
 * Twitterの機能実装クラス（検索、ツイート等）
 */
public class TwitterUtils {
    //コールバック先インターフェース（弱参照）
    private WeakReference<callBacksBase> callBacks;
    private Twitter twitter;

    public static final String TOKEN = "token";
    public static final String TOKEN_SECRET = "token_secret";
    public static final String PREF_NAME = "awordfromachild_twitter_access_token";

    /**
     * コンストラクタ
     * （コールバック先の画面を弱参照）
     * @param callBacks
     */
    public TwitterUtils(callBacksBase callBacks) {
        this.callBacks = new WeakReference<>(callBacks);
    }

    /**
     * Twitterインスタンスの生成
     *
     * @param context
     * @return Twitterインスタンス
     */
    public static Twitter getTwitterInstance(Context context) {
        //Twitterオブジェクトのインスタンス
        //(キー等はtwitter4j.propertiesで定義済み)
        TwitterFactory factory = new TwitterFactory();
        Twitter _twitter = factory.getInstance();

        //トークンの設定
        if (hasAccessToken(context)) {
            _twitter.setOAuthAccessToken(loadAccessToken(context));
        }
        return _twitter;
    }

    /**
     * TwitterUtilsインスタンスを設定
     * @param context
     */
    public void setTwitterInstance(Context context){
        twitter = getTwitterInstance(context);
    }

    /**
     * Twitterの自ユーザー情報を取得
     * @return
     */
    public void getTwitterUserInfo() {
        android.os.AsyncTask<Void, Void, User> task = new android.os.AsyncTask<Void, Void, User>() {
            @SuppressLint("StaticFieldLeak")
            @Override
            protected User doInBackground(Void... aVoid) {
                try {
                    User user = twitter.verifyCredentials();//Userオブジェクトを作成
                    return user;
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                return null;
            }
            @Override
            protected void onPostExecute(User user){
                callBacksMain callback = (callBacksMain) callBacks.get();
                callback.callBackGetUser(user);
            }
        };
        task.execute();
    }

    /**
     * トークンの格納
     *
     * @param context
     * @param accessToken
     */
    public static void storeAccessToken(Context context, AccessToken accessToken) {
        //トークンの設定
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(TOKEN, accessToken.getToken());
        editor.putString(TOKEN_SECRET, accessToken.getTokenSecret());

        //トークンの保存
        editor.commit();
    }

    /**
     * トークンの読み込み
     *
     * @param context
     * @return
     */
    public static AccessToken loadAccessToken(Context context) {
        //preferenceからトークンの呼び出し
        SharedPreferences preferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        String token = preferences.getString(TOKEN, null);
        String tokenSecret = preferences.getString(TOKEN_SECRET, null);
        if (token != null && tokenSecret != null) {
            return new AccessToken(token, tokenSecret);
        } else {
            return null;
        }
    }

    /**
     * トークンの有無判定
     *
     * @param context
     * @return
     */
    public static boolean hasAccessToken(Context context) {
        return loadAccessToken(context) != null;
    }

    /**
     * ワード検索（部分一致）
     *
     * @param str
     */
    public void search(String str) {
        android.os.AsyncTask<Void, Void, QueryResult> task = new android.os.AsyncTask<Void, Void, QueryResult>() {
            @SuppressLint("StaticFieldLeak")
            @Override
            protected QueryResult doInBackground(Void... aVoid) {
                Query query = new Query();
                // 検索ワードをセット
                query.setQuery(str);
                // 1度のリクエストで取得するTweetの数（100が最大）
                query.setCount(30);
                // 検索実行
                try {
                    QueryResult result = twitter.search(query);
                    return result;
                } catch (TwitterException e) {
                    System.out.println("TwitterException:" + e);
                } catch (Exception e) {
                    System.out.println("Exception:" + e);
                }
                return null;
            }

            @Override
            protected void onPostExecute(QueryResult arr_view) {
                callBacksSearch callback = (callBacksSearch) callBacks.get();
                callback.callBackGetSearch(arr_view);
            }
        };
        task.execute();
    }

    /**
     * ツイート投稿
     *
     * @param
     */
    public void tweet() {
        android.os.AsyncTask<Void, Void, String> task = new android.os.AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... aVoid) {
                try {
                    twitter4j.Status status = twitter.updateStatus("Twitter4Jから初めてのツイート！ #twitter4j");
                    System.out.println("Successfully updated the status to [" + status.getText() + "].");
                } catch (TwitterException e) {

                }
                return null;
            }

            @Override
            protected void onPostExecute(String url) {

            }
        };
        task.execute();
    }

    /**
     * タイムラインを取得
     * @return
     */
    public void getTimeLine(String pattern){
        android.os.AsyncTask<Void, Void, ResponseList<twitter4j.Status>> task = new android.os.AsyncTask<Void, Void, ResponseList<twitter4j.Status>>() {
            @SuppressLint("StaticFieldLeak")
            @Override
            protected ResponseList<twitter4j.Status> doInBackground(Void... aVoid) {
                ResponseList<twitter4j.Status> result = null;
                try {
                    switch (pattern){
                        case timelineType.HOME:
                            result = twitter.getHomeTimeline();
                            break;

                        case timelineType.USER:
                            result = twitter.getUserTimeline();
                            break;

                        case timelineType.MENTIONS:
                            result = twitter.getMentionsTimeline();
                            break;

                        case timelineType.RT_OF_ME:
                            result = twitter.getRetweetsOfMe();
                            break;

                        /*case timelineType.PUBLIC:
                            result = twitter.getHomeTimeline();
                            break;

                        case timelineType.FRIEND:
                            result = twitter.getHomeTimeline();
                            break;

                        case timelineType.RT_BY_ME:
                            result = twitter.getHomeTimeline();
                            break;

                        case timelineType.RT_TO_ME:
                            result = twitter.getHomeTimeline();
                            break;*/
                    }
                    return result;
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(ResponseList<twitter4j.Status> status) {
                callBacksTimeLine callback = (callBacksTimeLine) callBacks.get();
                callback.callBackGetTimeLine(status);
            }
        };
        task.execute();
    }
}
