package com.example.awordfromachild.common;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;

import com.example.awordfromachild.ApplicationController;
import com.example.awordfromachild.asynctask.callBacksBase;
import com.example.awordfromachild.asynctask.callBacksCreateTweet;
import com.example.awordfromachild.asynctask.callBacksMain;
import com.example.awordfromachild.asynctask.callBacksNewArrival;
import com.example.awordfromachild.asynctask.callBacksNoti;
import com.example.awordfromachild.constant.appSharedPreferences;
import com.example.awordfromachild.constant.twitterValue;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.ref.WeakReference;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
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

/**
 * Twitterの機能実装クラス（検索、ツイート等）
 */
public class TwitterUtils {
    private static final Calendar calendar = Calendar.getInstance();
    //フォローユーザーリスト（現在フォロー一覧情報不使用）
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private static final ArrayList<Long> friendIDs_list = new ArrayList<>();
    static Twitter twitter;
    private static ResponseList<Status> responseList;
    //エラーハンドリング
    private static exceptionHandling errHand;
    //ストリーミング
    private static TwitterStream twitterStream;
    //コールバック先インターフェース（弱参照）
    private final WeakReference<callBacksBase> callBacks;

    /**
     * コンストラクタ
     * （コールバック先の画面を弱参照）
     *
     * @param callBacks コールバック先
     */
    public TwitterUtils(callBacksBase callBacks) {
        this.callBacks = new WeakReference<>(callBacks);
        errHand = new exceptionHandling();
    }

    /**
     * Twitterインスタンスの生成
     *
     * @param context コンテキスト
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
     * @param context     コンテキスト
     * @param accessToken アクセストークン
     */
    public static void storeAccessToken(Context context, AccessToken accessToken) {
        //トークンの設定
        SharedPreferences preferences = context.getSharedPreferences(appSharedPreferences.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(appSharedPreferences.TOKEN, accessToken.getToken());
        editor.putString(appSharedPreferences.TOKEN_SECRET, accessToken.getTokenSecret());

        //トークンの保存
        editor.apply();
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
        editor.apply();
    }

    /**
     * トークンの読み込み
     *
     * @param context コンテキスト
     * @return トークン
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
     * @param context コンテキスト
     * @return トークンの有無
     */
    public static boolean hasAccessToken(Context context) {
        return loadAccessToken(context) != null;
    }

    /**
     * 戻り値の型に合わせてキャスト
     *
     * @param obj キャスト前
     * @param <T> ジェネリクス
     * @return キャスト後
     */
    @SuppressWarnings("unchecked")
    public static <T> T autoCast(Object obj) {
        return (T) obj;
    }

    /**
     * APIリクエスト返却値を参照し、
     * 制限が掛かったかを確認
     *
     * @param rateLimit            対象APIのレート情報
     * @param sharedPreferencesKey SharedPreferencesキー
     */
    private static void checkAPIRate(RateLimitStatus rateLimit, String sharedPreferencesKey) {
        //残りAPI使用可能数が0の場合、制限解除日時分を保存
        if (rateLimit.getRemaining() == 0) {
            //制限解除日時分を計算
            Date date = new Date();
            calendar.setTime(date);
            calendar.add(Calendar.SECOND, rateLimit.getSecondsUntilReset() + 10);
            date = calendar.getTime();

            SimpleDateFormat dateFormat = new SimpleDateFormat(
                    appSharedPreferences.API_RATE_DATE_FORMAT, Locale.JAPANESE);
            String strDate = dateFormat.format(date);

            //保存
            Context app_context = ApplicationController.getInstance().getApplicationContext();
            SharedPreferences preferences = app_context.getSharedPreferences(appSharedPreferences.PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(sharedPreferencesKey, strDate);
            editor.apply();
        }
    }

    /**
     * 対象のAPIリクエストが使用制限中か確認
     *
     * @param apiType API
     */
    public static void checkAPIUnderRestriction(String apiType) throws ParseException, TwitterException {
        //制限値取得
        Context app_context = ApplicationController.getInstance().getApplicationContext();
        SharedPreferences preferences = app_context.getSharedPreferences(appSharedPreferences.PREF_NAME, Context.MODE_PRIVATE);
        String date_string = preferences.getString(apiType, null);
        if (date_string == null) return;

        //制限解除日時分と現在を比較
        SimpleDateFormat dateFormat = new SimpleDateFormat(
                appSharedPreferences.API_RATE_DATE_FORMAT, Locale.JAPANESE);
        Date untilReset_date = dateFormat.parse(date_string);
        Date now_date = new Date();
        boolean flg_reset = now_date.after(untilReset_date);
        if (!flg_reset) {
            //使用制限中
            Calendar now_calendar = Calendar.getInstance();
            now_calendar.setTime(now_date);
            calendar.setTime(Objects.requireNonNull(untilReset_date));
            //制限解除までの秒数を返却
            long diff = calendar.getTimeInMillis() - now_calendar.getTimeInMillis();
            throw new TwitterException(String.valueOf(TimeUnit.MILLISECONDS.toSeconds(diff) + 1));
        }
    }

    /**
     * TwitterUtilsインスタンスを設定
     *
     * @param context コンテキスト
     */
    public void setTwitterInstance(Context context) {
        twitter = getTwitterInstance(context);
    }

    /**
     * HTTP通信（GET)
     * ※現在不使用
     *
     * @param endpoint エンドポイント
     * @return 取得JSON
     * @throws IOException   入出力エラー
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
            Objects.requireNonNull(httpConn).disconnect();
        }

        // 接続
        httpConn.connect();
        // 本文の取得
        InputStream in = httpConn.getInputStream();
        StringBuilder sb = new StringBuilder();
        String st;
        BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
        while ((st = br.readLine()) != null) {
            sb.append(st);
        }
        st = sb.toString();
        in.close();

        //JSONに変換
        JSONObject jsonData_meta = null;
        try {
            jsonData_meta = new JSONObject(st).getJSONObject("search_metadata");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        httpConn.disconnect();
        return jsonData_meta;
    }

    /**
     * streamingを開始
     *
     * @param arr_strFilter フィルター文字列
     * @param arr_follow    フォロー一覧（フォロー中のユーザーのツイートを対象にstreaming）
     */
    public void startStream(String[] arr_strFilter, long[] arr_follow) {
        new TwitterStreamFactory();
        twitterStream = TwitterStreamFactory.getSingleton();
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
     * ※現在不使用
     */
    @SuppressWarnings("unused")
    public void endStream() {
        twitterStream.shutdown();
    }

    /**
     * ツイートを投稿
     */
    public static class tweet extends AsyncTask<Void, Void, Object> {
        final WeakReference<callBacksBase> callBacks;
        String text;

        /**
         * コンストラクタ
         *
         * @param callBacks コールバック先
         */
        public tweet(callBacksBase callBacks) {
            this.callBacks = new WeakReference<>(callBacks);
        }

        /**
         * クエリ文字列をセット
         *
         * @param text クエリ
         */
        public void setText(String text) {
            this.text = text;
        }

        @Override
        protected Object doInBackground(Void... aVoid) {
            try {
                //API制限中かチェック
                checkAPIUnderRestriction(appSharedPreferences.API_RATE_DATE_GET_POST_TWEET);

                return twitter.updateStatus(text);
            } catch (TwitterException | ParseException e) {
                cancel(true);
                return e;
            }
        }

        @Override
        protected void onPostExecute(Object status) {
            callBacksCreateTweet callback = (callBacksCreateTweet) callBacks.get();
            callback.callBackTweeting(((twitter4j.Status) status));
        }

        @Override
        protected void onCancelled(Object err) {
            errHand.exceptionHand(err, callBacks);
        }
    }

    /**
     * フォローユーザー一覧を取得
     * ※現在不使用
     */
    @SuppressWarnings("unused")
    public static class getFriendIDs extends AsyncTask<Void, Void, Object> {
        final WeakReference<callBacksBase> callBacks;

        /**
         * コンストラクタ
         *
         * @param callBacks コールバック先
         */
        public getFriendIDs(callBacksBase callBacks) {
            this.callBacks = new WeakReference<>(callBacks);
        }

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
                } catch (TwitterException | ParseException e) {
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
    }

    /**
     * タイムラインを取得
     */
    public static class getTimeLine extends AsyncTask<Void, Void, Object> {
        final WeakReference<callBacksBase> callBacks;
        String pattern;
        long maxID;
        long sinceID;
        int getCount;
        String how;

        /**
         * コンストラクタ
         *
         * @param callBacks コールバック先
         */
        public getTimeLine(callBacksBase callBacks) {
            this.callBacks = new WeakReference<>(callBacks);
        }

        /**
         * タイムラインを取得
         *
         * @param pattern 取得タイムライン種別。twitterValueにて種別一覧記載。
         * @param maxID   取得ツイートのカーソル（～まで）
         * @param sinceID 取得ツイートのカーソル（～から）
         * @param how     取得ツイートの追加方式
         */
        public void setParam(String pattern, long maxID, long sinceID, int getCount, String how) {
            this.pattern = pattern;
            this.maxID = maxID;
            this.sinceID = sinceID;
            this.getCount = getCount;
            this.how = how;
        }

        /**
         * タイムライン取得実行
         *
         * @param pattern 取得タイムライン種別
         * @param p       ページング
         * @return 取得タイムライン
         * @throws TwitterException twitter4jエラー
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
                return TwitterUtils.<ArrayList<twitter4j.Status>>autoCast(responseList);

            } catch (TwitterException | RuntimeException | ParseException e) {
                cancel(true);
                return e;
            }
        }

        /**
         * map
         * 1　ArrayList<twitter4j.Status>
         * 2　Boolean（全件取得完了フラグ）
         */
        @Override
        protected void onPostExecute(Object list) {
            callBacks.get().callBackGetTweets(list, how);
            //API制限掛かったかチェック
            checkAPIRate(responseList.getRateLimitStatus(), appSharedPreferences.API_RATE_DATE_GET_TIMELINE);
        }

        /**
         * エラー発生時　もしくは
         * TwitterAPI制限中にAPIリクエストしようとした時の処理
         * （画面にトースト表示）
         */
        @Override
        protected void onCancelled(Object err) {
            errHand.exceptionHand(err, callBacks);
        }
    }

    /**
     * Twitterの自ユーザー情報を取得
     */
    public static class getTwitterMyUserInfo extends AsyncTask<Void, Void, Object> {
        final WeakReference<callBacksBase> callBacks;

        /**
         * コンストラクタ
         *
         * @param callBacks コールバック先
         */
        public getTwitterMyUserInfo(callBacksBase callBacks) {
            this.callBacks = new WeakReference<>(callBacks);
        }

        @Override
        protected Object doInBackground(Void... aVoid) {
            try {
                //API制限中かチェック
                checkAPIUnderRestriction(appSharedPreferences.API_RATE_DATE_GET_USER_INFO);

                return twitter.verifyCredentials();
            } catch (TwitterException | ParseException e) {
                cancel(true);
                return e;
            }
        }

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
    }

    /**
     * Twitterのユーザー情報を取得
     */
    public static class getTwitterUserInfo extends AsyncTask<Void, Void, Object> {
        private final WeakReference<callBacksBase> callBacks;
        private long userID;
        private String howToDisplay;

        /**
         * コンストラクタ
         *
         * @param callBacks コールバック先
         */
        public getTwitterUserInfo(callBacksBase callBacks) {
            this.callBacks = new WeakReference<>(callBacks);
        }

        public void setParam(long userID, String howToDisplay){
            this.userID = userID;
            this.howToDisplay = howToDisplay;
        }

        @Override
        protected Object doInBackground(Void... aVoid) {
            try {
                //API制限中かチェック
                checkAPIUnderRestriction(appSharedPreferences.API_RATE_DATE_GET_USER_INFO);

                return twitter.showUser(userID);
            } catch (TwitterException | ParseException e) {
                cancel(true);
                return e;
            }
        }

        @Override
        protected void onPostExecute(Object user) {
            //API制限チェック
            checkAPIRate(((User) user).getRateLimitStatus(), appSharedPreferences.API_RATE_DATE_GET_USER_INFO);
            //取得情報返却
            callBacksNoti callback = (callBacksNoti) callBacks.get();
            callback.callBackGetUser(((User) user), howToDisplay);
        }

        @Override
        protected void onCancelled(Object err) {
            errHand.exceptionHand(err, callBacks);
        }
    }

    /**
     * 文字列検索
     */
    public static class search extends AsyncTask<Void, Void, Object> {
        final WeakReference<callBacksBase> callBacks;
        String q_str;
        Long sinceID;
        Long maxID;
        int count;
        Query.ResultType resultType;
        String howToDisplay;

        /**
         * コンストラクタ
         *
         * @param callBacks コールバック先
         */
        public search(callBacksBase callBacks) {
            this.callBacks = new WeakReference<>(callBacks);
        }

        /**
         * 取得用パラメータを設定
         *
         * @param q_str        クエリ文字列
         * @param sinceID      　ツイートID（これを含まず、これより未来のツイートを取得）
         * @param maxID        　ツイートID（これを含まず、これより過去のツイートを取得）
         * @param resultType   　取得するツイートの種類（人気、最新、全て）
         * @param count        　取得する数
         * @param howToDisplay 　取得ツイートの画面追加方法
         */
        public void setParam(String q_str, Long sinceID, Long maxID, int count,
                             Query.ResultType resultType, String howToDisplay) {
            this.q_str = q_str;
            this.sinceID = sinceID;
            this.maxID = maxID;
            this.count = count;
            this.resultType = resultType;
            this.howToDisplay = howToDisplay;
        }

        @Override
        protected Object doInBackground(Void... aVoid) {
            try {
                //API制限中かチェック
                checkAPIUnderRestriction(appSharedPreferences.API_RATE_DATE_GET_SEARCH);

                Query query = new Query();
                if (sinceID != null) query.setSinceId(sinceID);
                if (maxID != null) query.setMaxId(maxID);
                if (count != 0) {
                    query.setCount(count);
                } else {
                    query.setCount(twitterValue.tweetCounts.ONE_TIME_DISPLAY_TWEET);
                }
                if (resultType != null) {
                    query.setResultType(resultType);
                } else {
                    query.setResultType(Query.ResultType.recent);
                }
                query.setQuery(q_str);
                // 検索実行
                return twitter.search(query);
            } catch (Exception e) {
                cancel(true);
                return e;

            }
        }

        @Override
        protected void onPostExecute(Object result) {
            QueryResult q_result = (QueryResult) result;
            //API制限チェック
            checkAPIRate(q_result.getRateLimitStatus(), appSharedPreferences.API_RATE_DATE_GET_SEARCH);
            callBacks.get().callBackGetTweets(q_result, howToDisplay);
        }

        @Override
        protected void onCancelled(Object err) {
            errHand.exceptionHand(err, callBacks);
        }
    }

    /**
     * ユーザーがいいねしたツイートを取得
     */
    public static class getFavorites extends AsyncTask<Void, Void, Object> {
        String howToDisplay;
        long maxID;
        long sinceId;
        final WeakReference<callBacksBase> callBacks;

        /**
         * コンストラクタ
         *
         * @param callBacks コールバック先
         */
        public getFavorites(callBacksBase callBacks) {
            this.callBacks = new WeakReference<>(callBacks);
        }

        /**
         * 取得用パラメータをセット
         *
         * @param maxID       ページング
         * @param howToDisplay 取得ツイートの画面追加方法
         */
        public void setParams(long maxID, long sinceID, String howToDisplay) {
            this.maxID = maxID;
            this.sinceId = sinceID;
            this.howToDisplay = howToDisplay;
        }

        @Override
        protected Object doInBackground(Void... aVoid) {
            try {
                Paging p = new Paging();
                if (maxID != 0) p.setMaxId(maxID);
                if (sinceId != 0) p.setSinceId(sinceId);

                //API制限中かチェック
                checkAPIUnderRestriction(appSharedPreferences.API_RATE_DATE_GET_FAVORITE);
                return twitter.getFavorites(p);
            } catch (TwitterException | ParseException e) {
                cancel(true);
                return e;
            }
        }

        @Override
        protected void onPostExecute(Object responseList) {
            //API制限状態登録
            ResponseList<twitter4j.Status> _responseList = autoCast(responseList);
            checkAPIRate(_responseList.getRateLimitStatus(),
                    appSharedPreferences.API_RATE_DATE_GET_FAVORITE);
            callBacks.get().callBackGetTweets(_responseList, howToDisplay);
        }

        @Override
        protected void onCancelled(Object err) {
            errHand.exceptionHand(err, callBacks);
        }
    }

    /**
     * 指定ツイートをいいねする
     */
    public static class createFavorite extends AsyncTask<Void, Void, Object> {
        long id;
        final WeakReference<callBacksBase> callBacks;

        /**
         * コンストラクタ
         *
         * @param callBacks コールバック先
         */
        public createFavorite(callBacksBase callBacks) {
            this.callBacks = new WeakReference<>(callBacks);
        }

        /**
         * 対象のツイートIDをセット
         *
         * @param id 対象ツイートID
         */
        public void setTweetId(Long id) {
            this.id = id;
        }

        @Override
        protected Object doInBackground(Void... aVoid) {
            try {
                //API制限中かチェック
                checkAPIUnderRestriction(appSharedPreferences.API_RATE_DATE_PUT_FAVORITE);
                return twitter.createFavorite(id);
            } catch (TwitterException | ParseException e) {
                cancel(true);
                return e;
            }
        }

        @Override
        protected void onPostExecute(Object status) {
        }

        @Override
        protected void onCancelled(Object err) {
            errHand.exceptionHand(err, callBacks);
        }
    }

    /**
     * 指定ツイートのいいねを取り消す
     */
    public static class destroyFavorite extends AsyncTask<Void, Void, Object> {
        long id;
        final WeakReference<callBacksBase> callBacks;

        /**
         * コンストラクタ
         *
         * @param callBacks コールバック先
         */
        public destroyFavorite(callBacksBase callBacks) {
            this.callBacks = new WeakReference<>(callBacks);
        }

        /**
         * 対象のツイートIDをセット
         *
         * @param id 　対象ツイートID
         */
        public void setTweetId(Long id) {
            this.id = id;
        }

        @Override
        protected Object doInBackground(Void... aVoid) {
            try {
                //API制限中かチェック
                checkAPIUnderRestriction(appSharedPreferences.API_RATE_DATE_DES_FAVORITE);

                return twitter.destroyFavorite(id);
            } catch (TwitterException | ParseException e) {
                cancel(true);
                return e;
            }
        }

        @Override
        protected void onCancelled(Object err) {
            errHand.exceptionHand(err, callBacks);
        }
    }

    /**
     * 指定ツイートをリツイートする
     */
    public static class createReTweet extends AsyncTask<Void, Void, Object> {
        long id;
        final WeakReference<callBacksBase> callBacks;

        /**
         * コンストラクタ
         *
         * @param callBacks コールバック先
         */
        public createReTweet(callBacksBase callBacks) {
            this.callBacks = new WeakReference<>(callBacks);
        }

        /**
         * 対象のツイートIDをセット
         *
         * @param id 対象ツイートID
         */
        public void setTweetId(Long id) {
            this.id = id;
        }

        @Override
        protected Object doInBackground(Void... aVoid) {
            try {
                //API制限中かチェック
                checkAPIUnderRestriction(appSharedPreferences.API_RATE_DATE_PUT_RETWEET);

                return twitter.retweetStatus(id);
            } catch (TwitterException | ParseException e) {
                cancel(true);
                return e;
            }
        }

        @Override
        protected void onCancelled(Object err) {
            errHand.exceptionHand(err, callBacks);
        }
    }

    /**
     * 指定ツイートのリツイートを取り消す
     */
    public static class destroyReTweet extends AsyncTask<Void, Void, Object> {
        long id;
        final WeakReference<callBacksBase> callBacks;

        /**
         * コンストラクタ
         *
         * @param callBacks コールバック先
         */
        public destroyReTweet(callBacksBase callBacks) {
            this.callBacks = new WeakReference<>(callBacks);
        }

        /**
         * 対象のツイートIDをセット
         *
         * @param id 　対象ツイートID
         */
        public void setTweetId(Long id) {
            this.id = id;
        }

        @Override
        protected Object doInBackground(Void... aVoid) {
            try {
                //API制限中かチェック
                checkAPIUnderRestriction(appSharedPreferences.API_RATE_DATE_DES_RETWEET);

                return twitter.unRetweetStatus(id);
            } catch (TwitterException | ParseException e) {
                cancel(true);
                return e;
            }
        }

        @Override
        protected void onCancelled(Object err) {
            errHand.exceptionHand(err, callBacks);
        }
    }

    /**
     * DMを取得
     */
    public static class getDirectMessages extends AsyncTask<Void, Void, Object> {
        final WeakReference<callBacksBase> callBacks;
        String getNextCursor;
        String howToDisplay;

        /**
         * コンストラクタ
         *
         * @param callBacks コールバック先
         */
        public getDirectMessages(callBacksBase callBacks) {
            this.callBacks = new WeakReference<>(callBacks);
        }

        /**
         * 取得パラメータをセット
         *
         * @param getNextCursor カーソル
         * @param howToDisplay  取得ツイートの画面追加方法
         */
        public void setGetParam(String getNextCursor, String howToDisplay) {
            this.howToDisplay = howToDisplay;
            this.getNextCursor = getNextCursor;
        }

        protected Object doInBackground(Void... aVoid) {
            try {
                //API制限中かチェック
                checkAPIUnderRestriction(appSharedPreferences.API_RATE_DATE_GET_DM);

                DirectMessageList dm_list;
                if (getNextCursor == null) {
                    dm_list = twitter.getDirectMessages(twitterValue.tweetCounts.ONE_TIME_DISPLAY_TWEET);
                } else {
                    dm_list = twitter.getDirectMessages(twitterValue.tweetCounts.ONE_TIME_DISPLAY_TWEET, getNextCursor);
                }
                return dm_list;
            } catch (TwitterException | ParseException e) {
                cancel(true);
                return e;
            }
        }

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
         * @param status 　追加ツイート
         */
        @Override
        public void onStatus(Status status) {
            super.onStatus(status);

            callBacksNewArrival callback = (callBacksNewArrival) callBacks.get();
            handler.post(() -> callback.callBackStreamAddList(status));
            //API制限掛かったかチェック
            //checkAPIRate(((twitter4j.Status) status).getRateLimitStatus(), appSharedPreferences.API_RATE_DATE_STREAM);
        }

        /**
         * エラー発生時
         *
         * @param err エラー内容
         */
        @Override
        public void onException(Exception err) {
            super.onException(err);
            handler.post(() -> errHand.exceptionHand(err, callBacks));
        }

        @Override
        public void onStallWarning(StallWarning stallWarning) {
            System.out.println(stallWarning);
        }
    }
}
