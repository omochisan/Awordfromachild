package com.example.awordfromachild;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Handler;

import com.example.awordfromachild.asynctask.callBacksBase;
import com.example.awordfromachild.asynctask.callBacksCreateTweet;
import com.example.awordfromachild.asynctask.callBacksFavorite;
import com.example.awordfromachild.asynctask.callBacksMain;
import com.example.awordfromachild.asynctask.callBacksNewArrival;
import com.example.awordfromachild.asynctask.callBacksNoti;
import com.example.awordfromachild.common.exceptionHandling;
import com.example.awordfromachild.common.httpConnection;
import com.example.awordfromachild.constant.*;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import androidx.annotation.RequiresApi;
import twitter4j.DirectMessageList;
import twitter4j.FilterQuery;
import twitter4j.PagableResponseList;
import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.RateLimitStatus;
import twitter4j.ResponseList;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusAdapter;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.OAuth2Token;
import twitter4j.auth.RequestToken;
import twitter4j.conf.ConfigurationBuilder;

/**
 * Twitterの機能実装クラス（検索、ツイート等）
 */
public class TwitterUtils {
    //コールバック先インターフェース（弱参照）
    private WeakReference<callBacksBase> callBacks;
    private Object callBackClass;
    private static Twitter twitter;
    private static ResponseList<Status> responseList;
    private static Calendar calendar = Calendar.getInstance();
    //エラーハンドリング
    private static exceptionHandling errHand;
    //フォローユーザーリスト
    private static ArrayList<Long> friendIDs_list = new ArrayList<Long>();
    //ストリーミング
    private static TwitterStream twitterStream;

    /**
     * コンストラクタ
     * （コールバック先の画面を弱参照）
     *
     * @param callBacks
     */
    public TwitterUtils(callBacksBase callBacks) {
        this.callBacks = new WeakReference<>(callBacks);
        errHand = new exceptionHandling();
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
     * トークンの格納
     *
     * @param context
     * @param accessToken
     */
    public static void storeAccessToken(Context context, AccessToken accessToken) {
        //トークンの設定
        SharedPreferences preferences = context.getSharedPreferences(appSharedPreferences.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(appSharedPreferences.TOKEN, accessToken.getToken());
        editor.putString(appSharedPreferences.TOKEN_SECRET, accessToken.getTokenSecret());

        //トークンの保存
        editor.commit();
    }

    /**
     * トークンの削除
     */
    public static void removeAccessToken() {
        Context app_context = ApplicationController.getInstance().getApplicationContext();
        SharedPreferences mSharedPreferences = app_context.getSharedPreferences(appSharedPreferences.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.remove(appSharedPreferences.TOKEN);
        editor.remove(appSharedPreferences.TOKEN_SECRET);
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
        SharedPreferences preferences = context.getSharedPreferences(appSharedPreferences.PREF_NAME, Context.MODE_PRIVATE);
        String token = preferences.getString(appSharedPreferences.TOKEN, null);
        String tokenSecret = preferences.getString(appSharedPreferences.TOKEN_SECRET, null);
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
     * TwitterUtilsインスタンスを設定
     *
     * @param context
     */
    public void setTwitterInstance(Context context) {
        twitter = getTwitterInstance(context);
    }

    /**
     * ユーザーがいいねしたツイートを取得
     */
    public void getFavorites(Paging paging, String howToDisplay) {
        android.os.AsyncTask<Void, Void, Object> task = new android.os.AsyncTask<Void, Void, Object>() {
            @SuppressLint("StaticFieldLeak")
            @Override
            protected Object doInBackground(Void... aVoid) {
                try {
                    //API制限中かチェック
                    checkAPIUnderRestriction(appSharedPreferences.API_RATE_DATE_GET_FAVORITE);
                    ResponseList<twitter4j.Status> result = twitter.getFavorites(paging);
                    return result;
                } catch (TwitterException e) {
                    cancel(true);
                    return e;
                } catch (ParseException e) {
                    cancel(true);
                    return e;
                }
            }

            @Override
            protected void onPostExecute(Object responseList) {
                //API制限チェック
                checkAPIRate(((ResponseList<twitter4j.Status>) responseList).getRateLimitStatus(),
                        appSharedPreferences.API_RATE_DATE_GET_FAVORITE);
                //取得情報返却
                callBacks.get().callBackGetTweets(
                        (ResponseList<twitter4j.Status>) responseList, howToDisplay);
            }

            @Override
            protected void onCancelled(Object err) {
                errHand.exceptionHand(err, callBacks);
            }
        };
        task.execute();
    }


    /**
     * 指定ツイートをいいねする
     *
     * @param id
     */
    public void createFavorite(long id) {
        android.os.AsyncTask<Void, Void, Object> task = new android.os.AsyncTask<Void, Void, Object>() {
            @SuppressLint("StaticFieldLeak")
            @Override
            protected Object doInBackground(Void... aVoid) {
                try {
                    //API制限中かチェック
                    checkAPIUnderRestriction(appSharedPreferences.API_RATE_DATE_PUT_FAVORITE);
                    twitter4j.Status status = twitter.createFavorite(id);
                    return status;
                } catch (TwitterException e) {
                    cancel(true);
                    return e;
                } catch (ParseException e) {
                    cancel(true);
                    return e;
                }
            }

            @Override
            protected void onPostExecute(Object status) {
                //API制限チェック
                checkAPIRate(((twitter4j.Status) status).getRateLimitStatus(), appSharedPreferences.API_RATE_DATE_PUT_FAVORITE);
            }

            @Override
            protected void onCancelled(Object err) {
                errHand.exceptionHand(err, callBacks);
            }
        };
        task.execute();
    }

    /**
     * 指定ツイートのいいねを取り消す
     *
     * @param id
     */
    public void destroyFavorite(long id) {
        android.os.AsyncTask<Void, Void, Object> task = new android.os.AsyncTask<Void, Void, Object>() {
            @SuppressLint("StaticFieldLeak")
            @Override
            protected Object doInBackground(Void... aVoid) {
                try {
                    //API制限中かチェック
                    checkAPIUnderRestriction(appSharedPreferences.API_RATE_DATE_DES_FAVORITE);

                    twitter4j.Status status = twitter.destroyFavorite(id);
                    return status;
                } catch (TwitterException e) {
                    cancel(true);
                    return e;
                } catch (ParseException e) {
                    cancel(true);
                    return e;
                }
            }

            @Override
            protected void onPostExecute(Object status) {
                //API制限チェック
                checkAPIRate(((twitter4j.Status) status).getRateLimitStatus(), appSharedPreferences.API_RATE_DATE_DES_FAVORITE);
            }

            @Override
            protected void onCancelled(Object err) {
                errHand.exceptionHand(err, callBacks);
            }
        };
        task.execute();
    }

    /**
     * 指定ツイートをリツイートする
     *
     * @param id
     */
    public void createReTweet(long id) {
        android.os.AsyncTask<Void, Void, Object> task = new android.os.AsyncTask<Void, Void, Object>() {
            @SuppressLint("StaticFieldLeak")
            @Override
            protected Object doInBackground(Void... aVoid) {
                try {
                    //API制限中かチェック
                    checkAPIUnderRestriction(appSharedPreferences.API_RATE_DATE_PUT_RETWEET);

                    twitter4j.Status status = twitter.retweetStatus(id);
                    return status;
                } catch (TwitterException e) {
                    cancel(true);
                    return e;
                } catch (ParseException e) {
                    cancel(true);
                    return e;
                }
            }

            @Override
            protected void onPostExecute(Object status) {
                //API制限チェック
                checkAPIRate(((twitter4j.Status) status).getRateLimitStatus(), appSharedPreferences.API_RATE_DATE_PUT_RETWEET);
            }

            @Override
            protected void onCancelled(Object err) {
                errHand.exceptionHand(err, callBacks);
            }
        };
        task.execute();
    }

    /**
     * 指定ツイートのリツイートを取り消す
     *
     * @param id
     */
    public void destroyReTweet(long id) {
        android.os.AsyncTask<Void, Void, Object> task = new android.os.AsyncTask<Void, Void, Object>() {
            @SuppressLint("StaticFieldLeak")
            @Override
            protected Object doInBackground(Void... aVoid) {
                try {
                    //API制限中かチェック
                    checkAPIUnderRestriction(appSharedPreferences.API_RATE_DATE_DES_RETWEET);

                    twitter4j.Status status = twitter.unRetweetStatus(id);
                    return status;
                } catch (TwitterException e) {
                    cancel(true);
                    return e;
                } catch (ParseException e) {
                    cancel(true);
                    return e;
                }
            }

            @Override
            protected void onPostExecute(Object status) {
                //API制限チェック
                checkAPIRate(((twitter4j.Status) status).getRateLimitStatus(), appSharedPreferences.API_RATE_DATE_DES_RETWEET);
            }

            @Override
            protected void onCancelled(Object err) {
                errHand.exceptionHand(err, callBacks);
            }
        };
        task.execute();
    }

    public void getDirectMessages(String getNextCursor, String howToDisplay) {
        android.os.AsyncTask<Void, Void, Object> task = new android.os.AsyncTask<Void, Void, Object>() {
            @SuppressLint("StaticFieldLeak")
            @Override
            protected Object doInBackground(Void... aVoid) {
                try {
                    //API制限中かチェック
                    checkAPIUnderRestriction(appSharedPreferences.API_RATE_DATE_GET_DM);

                    DirectMessageList dm_list = null;
                    if(getNextCursor != null){
                        twitter.getDirectMessages(twitterValue.tweetCounts.ONE_TIME_DISPLAY_TWEET);
                    }else{
                        twitter.getDirectMessages(twitterValue.tweetCounts.ONE_TIME_DISPLAY_TWEET, getNextCursor);
                    }
                    return dm_list;
                } catch (TwitterException e) {
                    cancel(true);
                    return e;
                } catch (ParseException e) {
                    cancel(true);
                    return e;
                }
            }

            @SuppressLint("StaticFieldLeak")
            @Override
            protected void onPostExecute(Object dm_list) {
                //API制限チェック
                checkAPIRate(((DirectMessageList) dm_list).getRateLimitStatus(), appSharedPreferences.API_RATE_DATE_GET_DM);
                //取得情報返却
                callBacks.get().callBackGetTweets(dm_list, howToDisplay);
            }

            @Override
            protected void onCancelled(Object err) {
                errHand.exceptionHand(err, callBacks);
            }
        };
        task.execute();
    }


    /**
     * Twitterの自ユーザー情報を取得
     *
     * @return
     */
    public void getTwitterUserInfo() {
        android.os.AsyncTask<Void, Void, Object> task = new android.os.AsyncTask<Void, Void, Object>() {
            @SuppressLint("StaticFieldLeak")
            @Override
            protected Object doInBackground(Void... aVoid) {
                try {
                    //API制限中かチェック
                    checkAPIUnderRestriction(appSharedPreferences.API_RATE_DATE_GET_USER_INFO);

                    User user = twitter.verifyCredentials();//Userオブジェクトを作成
                    return user;
                } catch (TwitterException e) {
                    cancel(true);
                    return e;
                } catch (ParseException e) {
                    cancel(true);
                    return e;
                }
            }

            @SuppressLint("StaticFieldLeak")
            @Override
            protected void onPostExecute(Object user) {
                //API制限チェック
                checkAPIRate(((User) user).getRateLimitStatus(), appSharedPreferences.API_RATE_DATE_GET_USER_INFO);
                //取得情報返却
                callBacksMain callback = (callBacksMain) callBacks.get();
                callback.callBackGetUser(((User) user));
            }

            @Override
            protected void onCancelled(Object err) {
                errHand.exceptionHand(err, callBacks);
            }
        };
        task.execute();
    }

    /**
     * オーバーロード
     * search
     * @param q_str
     * @param maxID
     * @param howToDisplay
     */
    public void search(String q_str, Long maxID, String howToDisplay) {
        search(q_str, null, maxID, 0, null, howToDisplay);
    }

    /**
     * ツイート検索
     * @param q_str クエリ文字列
     * @param sinceID　ツイートID（これを含まず、これより未来のツイートを取得）
     * @param maxID　ツイートID（これを含まず、これより過去のツイートを取得）
     * @param resultType　取得するツイートの種類（人気、最新、全て）
     * @param count　取得する数
     * @param howToDisplay　取得ツイートの画面追加方法
     */
    public void search(String q_str, Long sinceID, Long maxID, int count, Query.ResultType resultType, String howToDisplay) {
        android.os.AsyncTask<Void, Void, Object> task = new android.os.AsyncTask<Void, Void, Object>() {
            @Override
            protected Object doInBackground(Void... aVoid) {
                try {
                    //API制限中かチェック
                    checkAPIUnderRestriction(appSharedPreferences.API_RATE_DATE_GET_SEARCH);

                    Query query = new Query();
                    if(sinceID != null) query.setSinceId(sinceID);
                    if(maxID != null) query.setMaxId(maxID);
                    if(count != 0){
                        query.setCount(count);
                    }else{
                        query.setCount(twitterValue.tweetCounts.ONE_TIME_DISPLAY_TWEET);
                    }
                    if(resultType != null){
                        query.setResultType(resultType);
                    }else{
                        query.setResultType(Query.ResultType.recent);
                    }
                    query.setQuery(q_str);
                    // 検索実行
                    QueryResult result = twitter.search(query);
                    return result;
                } catch (TwitterException e) {
                    cancel(true);
                    return e;

                } catch (Exception e) {
                    cancel(true);
                    return e;
                }
            }

            @Override
            protected void onPostExecute(Object result) {
                QueryResult q_result = (QueryResult)result;
                //API制限チェック
                checkAPIRate(q_result.getRateLimitStatus(), appSharedPreferences.API_RATE_DATE_GET_SEARCH);
                callBacks.get().callBackGetTweets(q_result, howToDisplay);
            }

            @Override
            protected void onCancelled(Object err) {
                errHand.exceptionHand(err, callBacks);
            }
        };
        task.execute();
    }

    /**
     * HTTP通信（GET)
     *
     * @param endpoint
     * @return
     * @throws IOException
     * @throws JSONException
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    private JSONObject getHttpRequest(String endpoint, Map<String, String> map_q_word) throws IOException {
        HttpURLConnection httpConn = null;

        /*StringJoiner s_url = new StringJoiner("&", endpoint + "?", "");
        for (Map.Entry<String, String> parameter : map_q_word.entrySet()) {
            s_url.add(parameter.getKey() + "=" + parameter.getValue());
        }*/

        httpConnection createHttpConnection = new httpConnection();
        try {
            // ヘッダに設定する文字列を取得
            String headerString = createHttpConnection.generateHeaderString(map_q_word, "GET", endpoint);
            // ベースURLとパラメータからURIを生成
            URL url = new URL(createHttpConnection.createUrlString(endpoint, map_q_word));
            // リクエストを生成する
            httpConn = (HttpURLConnection) url.openConnection();// 接続用HttpURLConnectionオブジェクト作成
            httpConn.setRequestMethod("GET"); // リクエストメソッドの設定
            httpConn.setInstanceFollowRedirects(false);// リダイレクトを自動で許可しない設定
            httpConn.setDoInput(true); // URL接続からデータを読み取る場合はtrue
            httpConn.setDoOutput(false);// URL接続にデータを書き込む場合はtrue
            httpConn.setConnectTimeout(twitterValue.httpConnection.CONNECTION_TIMEOUT);// 接続にかかる時間
            httpConn.setReadTimeout(twitterValue.httpConnection.READ_TIMEOUT);//データの読み込みにかかる時間
            httpConn.setRequestProperty("Authorization", headerString);
        } catch (IOException ioe) {
            httpConn.disconnect();
        }

        // 接続
        httpConn.connect();
        // 本文の取得
        InputStream in = httpConn.getInputStream();
        StringBuffer sb = new StringBuffer();
        String st = "";
        BufferedReader br = new BufferedReader(new InputStreamReader(in, "UTF-8"));
        while ((st = br.readLine()) != null) {
            sb.append(st);
        }
        st = sb.toString();
        in.close();

        //JSONに変換
        JsonNode jsonResult = null;
        JSONArray jsonData_status = null;
        JSONObject jsonData_meta = null;
        try {
            ObjectMapper mapper = new ObjectMapper();
            jsonResult = mapper.readTree(st);
            jsonData_status = new JSONObject(st).getJSONObject("statuses").getJSONArray("n");
            jsonData_meta = new JSONObject(st).getJSONObject("search_metadata");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        httpConn.disconnect();
        return jsonData_meta;
    }

    /**
     * ツイート投稿
     *
     * @param
     */
    public void tweet() {
        android.os.AsyncTask<Void, Void, Object> task = new android.os.AsyncTask<Void, Void, Object>() {
            @Override
            protected Object doInBackground(Void... aVoid) {
                try {
                    //API制限中かチェック
                    checkAPIUnderRestriction(appSharedPreferences.API_RATE_DATE_GET_POST_TWEET);

                    twitter4j.Status status = twitter.updateStatus("Twitter4Jから初めてのツイート！ #twitter4j");
                    return status;
                } catch (TwitterException e) {
                    cancel(true);
                    return e;
                } catch (ParseException e) {
                    cancel(true);
                    return e;
                }
            }

            @Override
            protected void onPostExecute(Object status) {
                //API制限チェック
                checkAPIRate(((twitter4j.Status) status).getRateLimitStatus(),
                        appSharedPreferences.API_RATE_DATE_GET_POST_TWEET);

                callBacksCreateTweet callback = (callBacksCreateTweet) callBacks.get();
                callback.callBackTweeting(((twitter4j.Status) status));
            }

            @Override
            protected void onCancelled(Object err) {
                errHand.exceptionHand(err, callBacks);
            }
        };
        task.execute();
    }

    /**
     * streamingを開始
     *
     * @param arr_strFilter
     * @param arr_follow
     */
    public void startStream(String[] arr_strFilter, long[] arr_follow) {
        twitterStream = new TwitterStreamFactory().getSingleton();
        twitterStream.setOAuthAccessToken(
                loadAccessToken(ApplicationController.getInstance().getApplicationContext()));
        twitterStream.addListener(new MyTweetListener());
        FilterQuery filterQuery = new FilterQuery();
        if (arr_strFilter != null) filterQuery.track(arr_strFilter);
        if (arr_follow != null) filterQuery.follow(arr_follow);
        twitterStream.filter(filterQuery);
    }

    /**
     * streamingを終了
     */
    public void endStream(){
        twitterStream.shutdown();
    }

    /**
     * フォローユーザー一覧を取得
     */
    public void getFriendIDs() {
        android.os.AsyncTask<Void, Void, Object> task = new android.os.AsyncTask<Void, Void, Object>() {
            @Override
            protected Object doInBackground(Void... aVoid) {
                // カーソル初期値。現状のt4jのjavadocは 1オリジンだが、Twitter API Documentでは -1オリジンなのでそちらに準拠
                long cursor = -1L;
                // 一時的にIDを格納するオブジェクト
                PagableResponseList<twitter4j.User> result;
                ArrayList<User> result_list = new ArrayList<>();
                do {
                    try {
                        //API制限中かチェック
                        checkAPIUnderRestriction(appSharedPreferences.API_RATE_DATE_GET_FRIEND_LIST);
                        result = twitter.getFriendsList(twitter.getId(), cursor, twitterValue.tweetCounts.GET_FOLLOW_LIST);
                        result_list.addAll(result);
                    } catch (TwitterException e) {
                        cancel(true);
                        return e;
                    } catch (ParseException e) {
                        cancel(true);
                        return e;
                    }
                    // 次のページへのカーソル取得。ない場合は0のようだが、念のためループ条件はhasNextで見る
                    cursor = result.getNextCursor();
                } while (result.hasNext());

                //API制限掛かったかチェック
                checkAPIRate(result.getRateLimitStatus(),
                        appSharedPreferences.API_RATE_DATE_GET_FRIEND_LIST);

                //フォローユーザーIDリストを生成
                for (User user : result_list) {
                    friendIDs_list.add(user.getId());
                }

                return result_list;
            }

            @Override
            protected void onPostExecute(Object followerIDs) {
            }

            @Override
            protected void onCancelled(Object err) {
                errHand.exceptionHand(err, callBacks);
            }
        };
        task.execute();
    }

    /**
     * タイムライン取得実行
     *
     * @param pattern
     * @param p
     * @return
     * @throws TwitterException
     */
    private ResponseList<twitter4j.Status> runGetTimeLine(String pattern, Paging p) throws TwitterException {
        ResponseList<twitter4j.Status> result = null;
        switch (pattern) {
            case twitterValue.timeLineType.HOME:
                result = twitter.getHomeTimeline(p);
                break;

            case twitterValue.timeLineType.USER:
                result = twitter.getUserTimeline(p);
                break;

            case twitterValue.timeLineType.MENTIONS:
                result = twitter.getMentionsTimeline(p);
                break;

            case twitterValue.timeLineType.RT_OF_ME:
                result = twitter.getRetweetsOfMe(p);
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
    }

    /**
     * オーバーロード
     * getTimeLine
     *
     * @param pattern 取得タイムライン種別。twitterValueにて種別一覧記載。
     */
    public void getTimeLine(String pattern) {
        getTimeLine(pattern, 0, 0, 0, null);
    }

    /**
     * タイムラインを取得
     *
     * @param pattern 取得タイムライン種別。twitterValueにて種別一覧記載。
     * @param maxID   取得ツイートのカーソル（～まで）
     * @param sinceID 取得ツイートのカーソル（～から）
     * @param how     取得ツイートの追加方式
     */
    public void getTimeLine(String pattern, long maxID, long sinceID, int getCount, String how) {
        android.os.AsyncTask<Void, Void, Object> task = new android.os.AsyncTask<Void, Void, Object>() {
            @SuppressLint("StaticFieldLeak")
            @Override
            protected Object doInBackground(Void... aVoid) {
                try {
                    //API制限中かチェック
                    checkAPIUnderRestriction(appSharedPreferences.API_RATE_DATE_GET_TIMELINE);

                    //ページング設定
                    Paging p = new Paging();
                    if (maxID != 0) p.setMaxId(maxID);
                    if (sinceID != 0) p.setSinceId(sinceID);
                    if (getCount != 0) {
                        p.setCount(getCount);
                    } else {
                        p.setCount(twitterValue.tweetCounts.ONE_TIME_DISPLAY_TWEET);
                    }

                    responseList = runGetTimeLine(pattern, p);
                    return (ArrayList<twitter4j.Status>) responseList;

                } catch (TwitterException e) {
                    cancel(true);
                    return e;
                } catch (RuntimeException e) {
                    cancel(true);
                    return e;
                } catch (ParseException e) {
                    cancel(true);
                    return e;
                }
            }

            @Override
            /**
             * map
             * 1　ArrayList<twitter4j.Status>
             * 2　Boolean（全件取得完了フラグ）
             */
            protected void onPostExecute(Object list) {
                String howToDisplay = "";
                if (how == null) {
                    howToDisplay = twitterValue.howToDisplayTweets.TWEET_HOW_TO_DISPLAY_REWASH;
                } else {
                    howToDisplay = how;
                }
                callBacks.get().callBackGetTweets(list, how);
                //API制限掛かったかチェック
                checkAPIRate(responseList.getRateLimitStatus(), appSharedPreferences.API_RATE_DATE_GET_TIMELINE);
            }

            /**
             * エラー発生時　もしくは
             * TwitterAPI制限中にAPIリクエストしようとした時の処理
             * （画面にトースト表示）
             *
             */
            @Override
            protected void onCancelled(Object err) {
                errHand.exceptionHand(err, callBacks);
            }
        };
        task.execute();
    }

    /**
     * APIリクエスト返却値を参照し、
     * 制限が掛かったかを確認
     *
     * @param rateLimit
     * @param sharedPreferencesKey
     */
    private void checkAPIRate(RateLimitStatus rateLimit, String sharedPreferencesKey) {
        //残りAPI使用可能数が0の場合、制限解除日時分を保存
        if (rateLimit.getRemaining() == 0) {
            //制限解除日時分を計算
            Date date = new Date();
            calendar.setTime(date);
            calendar.add(Calendar.SECOND, rateLimit.getSecondsUntilReset() + 10);
            date = calendar.getTime();

            SimpleDateFormat dateFormat = new SimpleDateFormat(appSharedPreferences.API_RATE_DATE_FORMAT);
            String strDate = dateFormat.format(date);

            //保存
            Context app_context = ApplicationController.getInstance().getApplicationContext();
            SharedPreferences preferences = app_context.getSharedPreferences(appSharedPreferences.PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(sharedPreferencesKey, strDate);
            editor.commit();
        }
    }

    /**
     * 対象のAPIリクエストが使用制限中か確認
     *
     * @param apiType
     * @return 0=未制限 1以上=制限中（制限解除までの秒数）
     */
    public void checkAPIUnderRestriction(String apiType) throws ParseException, TwitterException {
        //制限値取得
        Context app_context = ApplicationController.getInstance().getApplicationContext();
        SharedPreferences preferences = app_context.getSharedPreferences(appSharedPreferences.PREF_NAME, Context.MODE_PRIVATE);
        String date_string = preferences.getString(apiType, null);
        if (date_string == null) return;

        //制限解除日時分と現在を比較
        SimpleDateFormat dateFormat = new SimpleDateFormat(appSharedPreferences.API_RATE_DATE_FORMAT);
        Date untilReset_date = dateFormat.parse(date_string);
        Date now_date = new Date();
        boolean flg_reset = now_date.after(untilReset_date);
        if (!flg_reset) {
            //使用制限中
            Calendar now_calendar = Calendar.getInstance();
            now_calendar.setTime(now_date);
            calendar.setTime(untilReset_date);
            //制限解除までの秒数を返却
            long diff = calendar.getTimeInMillis() - now_calendar.getTimeInMillis();
            throw new TwitterException(String.valueOf(TimeUnit.MILLISECONDS.toSeconds(diff) + 1));
        }
    }

    /**
     * streaming用クラス
     */
    class MyTweetListener extends StatusAdapter {
        //外部からview操作するためHandlerを利用
        final Handler handler = new Handler();

        /**
         * ツイートが追加されたとき
         *
         * @param status
         */
        @Override
        public void onStatus(Status status) {
            super.onStatus(status);

            callBacksNewArrival callback = (callBacksNewArrival) callBacks.get();
            handler.post(new Runnable() {
                @Override
                public void run() {
                    callback.callBackStreamAddList(status);
                }
            });
            //API制限掛かったかチェック
            //checkAPIRate(((twitter4j.Status) status).getRateLimitStatus(), appSharedPreferences.API_RATE_DATE_STREAM);
        }

        /**
         * エラー発生時
         *
         * @param err
         */
        @Override
        public void onException(Exception err) {
            super.onException(err);
            handler.post(new Runnable() {
                @Override
                public void run() {
                    errHand.exceptionHand(err, callBacks);
                }
            });
        }

        @Override
        public void onStallWarning(StallWarning stallWarning) {
            System.out.println(stallWarning);
        }
    }
}
