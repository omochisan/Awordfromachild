package com.example.awordfromachild;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Toast;

import com.example.awordfromachild.asynctask.callBacksBase;
import com.example.awordfromachild.asynctask.callBacksCreateTweet;
import com.example.awordfromachild.asynctask.callBacksMain;
import com.example.awordfromachild.asynctask.callBacksSearch;
import com.example.awordfromachild.asynctask.callBacksTimeLine;
import com.example.awordfromachild.constant.appSharedPrerence;
import com.example.awordfromachild.constant.twitterValue;
import com.example.awordfromachild.tab.fragTimeLine;

import java.lang.ref.WeakReference;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import twitter4j.Paging;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.RateLimitStatus;
import twitter4j.ResponseList;
import twitter4j.Status;
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
    //総取得ツイート
    private ArrayList<twitter4j.Status> list_allGetStatus = new ArrayList<twitter4j.Status>();
    private int count_page = 0;
    private ResponseList<Status> responseList;
    private Calendar calendar = Calendar.getInstance();

    //TwitterAPI実行エラーに関する情報
    private TwitterException twitter_err;
    private int apiResetSeconds = 0;

    public String howToDisplay;

    /**
     * コンストラクタ
     * （コールバック先の画面を弱参照）
     *
     * @param callBacks
     */
    public TwitterUtils(callBacksBase callBacks) {
        this.callBacks = new WeakReference<>(callBacks);
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
        SharedPreferences preferences = context.getSharedPreferences(appSharedPrerence.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(appSharedPrerence.TOKEN, accessToken.getToken());
        editor.putString(appSharedPrerence.TOKEN_SECRET, accessToken.getTokenSecret());

        //トークンの保存
        editor.commit();
    }

    /**
     * トークンの削除
     */
    public static void removeAccessToken(){
        Context app_context = ApplicationController.getInstance().getApplicationContext();
        SharedPreferences mSharedPreferences = app_context.getSharedPreferences(appSharedPrerence.PREF_NAME, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = mSharedPreferences.edit();
        editor.remove(appSharedPrerence.TOKEN);
        editor.remove(appSharedPrerence.TOKEN_SECRET);
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
        SharedPreferences preferences = context.getSharedPreferences(appSharedPrerence.PREF_NAME, Context.MODE_PRIVATE);
        String token = preferences.getString(appSharedPrerence.TOKEN, null);
        String tokenSecret = preferences.getString(appSharedPrerence.TOKEN_SECRET, null);
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
     * Twitterの自ユーザー情報を取得
     *
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
            protected void onPostExecute(User user) {
                callBacksMain callback = (callBacksMain) callBacks.get();
                callback.callBackGetUser(user);
            }
        };
        task.execute();
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
        android.os.AsyncTask<Void, Void, Boolean> task = new android.os.AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... aVoid) {
                try {
                    twitter4j.Status status = twitter.updateStatus("Twitter4Jから初めてのツイート！ #twitter4j");
                    return true;
                } catch (TwitterException e) {
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean result) {
                callBacksCreateTweet callback = (callBacksCreateTweet) callBacks.get();
                callback.callBackTweeting(result);
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
            case twitterValue.HOME:
                result = twitter.getHomeTimeline(p);
                break;

            case twitterValue.USER:
                result = twitter.getUserTimeline(p);
                break;

            case twitterValue.MENTIONS:
                result = twitter.getMentionsTimeline(p);
                break;

            case twitterValue.RT_OF_ME:
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
     * タイムラインを取得
     *
     * @param pattern
     */
    public void getTimeLine(String pattern) {
        getTimeLine(pattern, 0, 0, twitterValue.GET_TYPE_NEWEST, null);
    }

    public void getTimeLine(String pattern, long maxID, String getType) {
        getTimeLine(pattern, maxID, 0, getType, null);
    }

    public void getTimeLine(String pattern, long sinceID, String getType, String how) {
        getTimeLine(pattern, 0, sinceID, getType, how);
    }

    /**
     * タイムラインを取得
     *
     * @param pattern 取得タイムライン種別。twitterValueにて種別一覧記載。
     * @param maxID   取得ツイートのカーソル（～まで）
     * @param sinceID 取得ツイートのカーソル（～から）
     * @param getType 取得タイプ
     */
    public void getTimeLine(String pattern, long maxID, long sinceID, String getType, String how) {
        android.os.AsyncTask<Void, Void, ArrayList<twitter4j.Status>> task = new android.os.AsyncTask<Void, Void, ArrayList<twitter4j.Status>>() {
            @SuppressLint("StaticFieldLeak")
            @Override
            protected ArrayList<twitter4j.Status> doInBackground(Void... aVoid) {
                try {
                    if(how == null){
                        howToDisplay = twitterValue.TWEET_HOW_TO_DISPLAY_REWASH;
                    }else{
                        howToDisplay = how;
                    }

                    int _apiResetSeconds = checkAPIUnderRestriction(appSharedPrerence.API_RATE_DATE_GET_TIMELINE);
                    if(_apiResetSeconds != 0){
                        apiResetSeconds = _apiResetSeconds;
                        cancel(true);
                        return null;
                    }
                    ArrayList<twitter4j.Status> result = null;
                    Paging p = new Paging();

                    //ツイート取得タイプ別
                    switch (getType) {
                        //最新ツイートの取得
                        case twitterValue.GET_TYPE_NEWEST:
                            p.setCount(twitterValue.GET_COUNT_TIMELINE);
                            responseList = runGetTimeLine(pattern, p);
                            result = (ArrayList<twitter4j.Status>) responseList;
                            break;

                        //対象より新しいツイートを取得
                        case twitterValue.GET_TYPE_EVEN_NEWER:
                            count_page++;
                            p.setPage(count_page);
                            p.setSinceId(maxID);
                            //最新ツイート～表示中最新ツイートの間のツイートを、先頭200件まで取得する。
                            p.setCount(2);
                            responseList = runGetTimeLine(pattern, p);
                            result = (ArrayList<twitter4j.Status>) responseList;
                            break;

                        //対象より古いツイートを指定数分取得
                        case twitterValue.GET_TYPE_OLDER:
                            p.setMaxId(maxID);
                            p.setCount(twitterValue.GET_COUNT_TIMELINE);
                            responseList = runGetTimeLine(pattern, p);
                            result = (ArrayList<twitter4j.Status>) responseList;
                            //取得した中の先頭ツイート（前回読込分の最古ツイート）を削除
                            result.remove(0);
                            break;
                    }
                    return result;

                } catch (TwitterException e) {
                    e.printStackTrace();
                    cancel(true);
                    twitter_err = e;
                } catch (RuntimeException e){
                    e.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            /**
             * map
             * 1　ArrayList<twitter4j.Status>
             * 2　Boolean（全件取得完了フラグ）
             */
            protected void onPostExecute(ArrayList<twitter4j.Status> list) {
                /*if (type.equals(twitterValue.GET_TYPE_EVEN_NEWER)) {
                    while (true) {
                        if (list == null) {
                            break;
                        } else if (list.get(list.size() - 1).getId() == maxTweetID || list.size() < 200) {
                            list_allGetStatus.addAll(list);
                            break;
                        } else {
                            list_allGetStatus.addAll(list);
                            count_page++;
                            page.setPage(count_page);
                            getTimeLine(twitterValue.HOME, maxTweetID, twitterValue.GET_TYPE_EVEN_NEWER, page);
                        }
                    }
                }else{
                    list_allGetStatus = list;
                }*/
                callBacksTimeLine callback = (callBacksTimeLine) callBacks.get();
                callback.callBackGetTimeLine(list);
                callback.setHowToDisplay(howToDisplay);
                //API制限掛かったかチェック
                checkAPIRate(responseList);
            }

            /**
             * TwitterAPI制限中に
             * APIリクエストしようとした時の処理
             * （画面にトースト表示）
             *
             */
            @Override
            protected void onCancelled(){
                int returnResetSeconds = 0;
                if(apiResetSeconds != 0){
                    returnResetSeconds = apiResetSeconds;
                    apiResetSeconds = 0;
                }else if(twitter_err != null){
                    returnResetSeconds = twitter_err.getRateLimitStatus().getSecondsUntilReset();
                    twitter_err = null;
                }
                callBacksTimeLine callback = (callBacksTimeLine) callBacks.get();
                callback.callBackTwitterLimit(returnResetSeconds);
            }
        };
        task.execute();
    }

    /**
     * APIリクエスト返却値を参照し、
     * 制限が掛かったかを確認
     * @param responseList
     */
    private void checkAPIRate(ResponseList<Status> responseList) {
        RateLimitStatus rateLimit = responseList.getRateLimitStatus();
        //残りAPI使用可能数が0の場合、制限解除日時分を保存
        if (rateLimit.getRemaining() == 0) {
            //制限解除日時分を計算
            Date date = new Date();
            calendar.setTime(date);
            calendar.add(Calendar.SECOND, rateLimit.getSecondsUntilReset() + 10);
            date = calendar.getTime();

            SimpleDateFormat dateFormat = new SimpleDateFormat(appSharedPrerence.API_RATE_DATE_FORMAT);
            String strDate = dateFormat.format(date);

            //保存
            Context app_context = ApplicationController.getInstance().getApplicationContext();
            SharedPreferences preferences = app_context.getSharedPreferences(appSharedPrerence.PREF_NAME, Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = preferences.edit();
            editor.putString(appSharedPrerence.API_RATE_DATE_GET_TIMELINE, strDate);
            editor.commit();
        }
    }

    /**
     * 対象のAPIリクエストが使用制限中か確認
     * @return 0=未制限 1以上=制限中（制限解除までの秒数）
     */
    public int checkAPIUnderRestriction(String apiType) throws ParseException {
        //制限値取得
        Context app_context = ApplicationController.getInstance().getApplicationContext();
        SharedPreferences preferences = app_context.getSharedPreferences(appSharedPrerence.PREF_NAME, Context.MODE_PRIVATE);
        String date_string = preferences.getString(apiType, null);
        if(date_string == null) return 0;

        //制限解除日時分と現在を比較
        SimpleDateFormat dateFormat = new SimpleDateFormat(appSharedPrerence.API_RATE_DATE_FORMAT);
        Date untilReset_date = dateFormat.parse(date_string);
        Date now_date = new Date();
        boolean flg_reset = now_date.after(untilReset_date);
        if(!flg_reset){
            //使用制限中
            Calendar now_calendar = Calendar.getInstance();
            now_calendar.setTime(now_date);
            calendar.setTime(untilReset_date);
            //制限解除までの秒数を返却
            long diff = calendar.getTimeInMillis() - now_calendar.getTimeInMillis();
            return (int)(TimeUnit.MILLISECONDS.toSeconds(diff) + 1);
        }else{
            return 0;
        }
    }
}
