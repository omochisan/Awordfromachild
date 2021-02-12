package com.example.awordfromachild;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;

import com.example.awordfromachild.tab.fragSearch;

import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;

/**
 * Twitterの機能実装クラス（検索、ツイート等）
 */
public class TwitterUtils {
    //SharedPrerence用のキー
    private static final String TOKEN = "token";
    private static final String TOKEN_SECRET = "token_secret";
    private static final String PREF_NAME = "awordfromachild_twitter_access_token";

    private fragSearch callback_fs;
    private MainActivity mainActivity;

    /*public TwitterUtils(Context callback) {
        this.callback = callback;
    }*/

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
        Twitter twitter = factory.getInstance();

        //トークンの設定
        if (hasAccessToken(context)) {
            twitter.setOAuthAccessToken(loadAccessToken(context));
        }
        return twitter;
    }

    /**
     * Twitterの自ユーザー情報を取得します
     * @param twitter
     * @return
     */
    public void getTwitterUserInfo(Twitter twitter, MainActivity callBack) {
        android.os.AsyncTask<Void, Void, User> task = new android.os.AsyncTask<Void, Void, User>() {
            @SuppressLint("StaticFieldLeak")
            @Override
            protected User doInBackground(Void... aVoid) {
                try {
                    mainActivity = callBack;
                    User user = twitter.verifyCredentials();//Userオブジェクトを作成
                    return user;
                } catch (TwitterException e) {
                    e.printStackTrace();
                }
                return null;
            }
            @Override
            protected void onPostExecute(User user){
                mainActivity.onAsyncFinished_getUserInfo(user);
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
    public void search(Twitter twitter, View view, String str) {
        callback_fs = fragSearch.getInstance();
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
                    System.out.println("ヒット数 : " + result.getTweets().size());
                    // 検索結果を見てみる
                    /*
                    TextView arr_view_tweet[] = new TextView[30];
                    int index = 0;
                    for (twitter4j.Status tweet : result.getTweets()) {
                        // 本文
                        String t_text = tweet.getText();
                        // TextView インスタンス生成
                        TextView textView = new TextView(context);
                        textView.setText(t_text);
                        arr_view_tweet[index] = textView;
                        index++;

                        // ハッシュタグとURLの削除
                        StringTokenizer sta = new StringTokenizer(t_text, " ");
                        //トークンの出力
                        while (sta.hasMoreTokens()) {
                            String wk = sta.nextToken();
                            if (wk.indexOf("#") == -1 && wk.indexOf("http") == -1
                                    && wk.indexOf("RT") == -1 && wk.indexOf("@") == -1) {
                            }
                        }

                    }*/
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
                callback_fs.onAsyncFinished_search(arr_view);
            }
        };
        task.execute();
    }

    /**
     * ツイート投稿
     *
     * @param twitter
     */
    public void tweet(Twitter twitter) {
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
}
