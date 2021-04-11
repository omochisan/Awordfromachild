package com.example.awordfromachild;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.example.awordfromachild.asynctask.callBacksBase;
import com.example.awordfromachild.asynctask.callBacksCreateTweet;
import com.example.awordfromachild.asynctask.callBacksMain;
import com.example.awordfromachild.asynctask.callBacksSearch;
import com.example.awordfromachild.asynctask.callBacksTimeLine;
import com.example.awordfromachild.common.exceptionHandling;
import com.example.awordfromachild.constant.appSharedPreferences;
import com.example.awordfromachild.constant.twitterValue;

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
    public String howToDisplay;
    //コールバック先インターフェース（弱参照）
    private WeakReference<callBacksBase> callBacks;
    private Twitter twitter;
    //総取得ツイート
    private ArrayList<twitter4j.Status> list_allGetStatus = new ArrayList<twitter4j.Status>();
    private ResponseList<Status> responseList;
    private Calendar calendar = Calendar.getInstance();
    //TwitterAPI実行エラーに関する情報
    private TwitterException twitter_err;
    private int apiResetSeconds = 0;
    private exceptionHandling errHand;

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
     * 指定ツイートをいいねする
     *
     * @param id
     *
     */
    public void createFavorite(long id) {
        android.os.AsyncTask<Void, Void, Object> task = new android.os.AsyncTask<Void, Void, Object>() {
            @SuppressLint("StaticFieldLeak")
            @Override
            protected Object doInBackground(Void... aVoid) {
                try {
                    int _apiResetSeconds = checkAPIUnderRestriction(appSharedPreferences.API_RATE_DATE_GET_TIMELINE);
                    if (_apiResetSeconds != 0) {
                        throw new TwitterException(String.valueOf(_apiResetSeconds));
                    }

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
                checkAPIRate(((twitter4j.Status)status).getRateLimitStatus(), appSharedPreferences.API_RATE_DATE_GET_FAVORITE);
            }

            @Override
            protected void onCancelled(Object err) {
                int returnResetSeconds = ResetSecondsInCancel();
                callBacksMain callback = (callBacksMain) callBacks.get();
                callback.callBackTwitterLimit(returnResetSeconds);
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
                    int _apiResetSeconds = checkAPIUnderRestriction(appSharedPreferences.API_RATE_DATE_GET_TIMELINE);
                    if (_apiResetSeconds != 0) {
                        apiResetSeconds = _apiResetSeconds;
                        cancel(true);
                        return null;
                    }
                    twitter4j.Status status = twitter.destroyFavorite(id);
                    return status;
                } catch (TwitterException e) {
                    cancel(true);
                    twitter_err = e;
                } catch (ParseException e) {
                    cancel(true);
                }
                return null;
            }

            @Override
            protected void onPostExecute(Object status) {
                //API制限チェック
                checkAPIRate(((twitter4j.Status)status).getRateLimitStatus(), appSharedPreferences.API_RATE_DATE_GET_FAVORITE);
            }

            @Override
            protected void onCancelled(Object err) {
                int returnResetSeconds = ResetSecondsInCancel();
                callBacksMain callback = (callBacksMain) callBacks.get();
                callback.callBackTwitterLimit(returnResetSeconds);
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
        android.os.AsyncTask<Void, Void, User> task = new android.os.AsyncTask<Void, Void, User>() {
            @SuppressLint("StaticFieldLeak")
            @Override
            protected User doInBackground(Void... aVoid) {
                try {
                    int _apiResetSeconds = checkAPIUnderRestriction(appSharedPreferences.API_RATE_DATE_GET_TIMELINE);
                    if (_apiResetSeconds != 0) {
                        apiResetSeconds = _apiResetSeconds;
                        cancel(true);
                        return null;
                    }

                    User user = twitter.verifyCredentials();//Userオブジェクトを作成
                    return user;
                } catch (TwitterException e) {
                    cancel(true);
                    twitter_err = e;
                } catch (ParseException e) {
                    cancel(true);
                }
                return null;
            }

            @Override
            protected void onPostExecute(User user) {
                //API制限チェック
                checkAPIRate(user.getRateLimitStatus(), appSharedPreferences.API_RATE_DATE_GET_USER_INFO);
                //取得情報返却
                callBacksMain callback = (callBacksMain) callBacks.get();
                callback.callBackGetUser(user);
            }

            @Override
            protected void onCancelled() {
                int returnResetSeconds = ResetSecondsInCancel();
                callBacksMain callback = (callBacksMain) callBacks.get();
                callback.callBackTwitterLimit(returnResetSeconds);
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
                try {
                    int _apiResetSeconds = checkAPIUnderRestriction(appSharedPreferences.API_RATE_DATE_GET_TIMELINE);
                    if (_apiResetSeconds != 0) {
                        apiResetSeconds = _apiResetSeconds;
                        cancel(true);
                        return null;
                    }

                    Query query = new Query();
                    // 検索ワードをセット
                    query.setQuery(str);
                    // 1度のリクエストで取得するTweetの数（100が最大）
                    query.setCount(30);
                    // 検索実行
                    QueryResult result = twitter.search(query);
                    return result;
                } catch (TwitterException e) {
                    cancel(true);
                    twitter_err = e;
                } catch (Exception e) {
                    cancel(true);
                }
                return null;
            }

            @Override
            protected void onPostExecute(QueryResult arr_view) {
                callBacksSearch callback = (callBacksSearch) callBacks.get();
                callback.callBackGetSearch(arr_view);
            }

            @Override
            protected void onCancelled() {
                int returnResetSeconds = ResetSecondsInCancel();
                callBacksSearch callback = (callBacksSearch) callBacks.get();
                callback.callBackTwitterLimit(returnResetSeconds);
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
        android.os.AsyncTask<Void, Void, twitter4j.Status> task = new android.os.AsyncTask<Void, Void, twitter4j.Status>() {
            @Override
            protected twitter4j.Status doInBackground(Void... aVoid) {
                try {
                    int _apiResetSeconds = checkAPIUnderRestriction(appSharedPreferences.API_RATE_DATE_GET_TIMELINE);
                    if (_apiResetSeconds != 0) {
                        apiResetSeconds = _apiResetSeconds;
                        cancel(true);
                        return null;
                    }

                    twitter4j.Status status = twitter.updateStatus("Twitter4Jから初めてのツイート！ #twitter4j");
                    return status;
                } catch (TwitterException e) {
                    cancel(true);
                    twitter_err = e;
                } catch (ParseException e) {
                    cancel(true);
                }
                return null;
            }

            @Override
            protected void onPostExecute(twitter4j.Status status) {
                //API制限チェック
                checkAPIRate(status.getRateLimitStatus(), appSharedPreferences.API_RATE_DATE_GET_USER_INFO);

                callBacksCreateTweet callback = (callBacksCreateTweet) callBacks.get();
                callback.callBackTweeting(status);
            }

            /**
             * TwitterAPI制限中に
             * APIリクエストしようとした時の処理
             * （画面にトースト表示）
             *
             */
            @Override
            protected void onCancelled() {
                int returnResetSeconds = ResetSecondsInCancel();
                callBacksCreateTweet callback = (callBacksCreateTweet) callBacks.get();
                callback.callBackTwitterLimit(returnResetSeconds);
            }
        };
        task.execute();
    }

    /**
     * 制限解除までの秒数を返却
     *
     * @return 制限解除秒数
     */
    private int ResetSecondsInCancel() {
        int returnResetSeconds = 0;
        if (apiResetSeconds != 0) {
            returnResetSeconds = apiResetSeconds;
            apiResetSeconds = 0;
        } else if (twitter_err != null) {
            returnResetSeconds = twitter_err.getRateLimitStatus().getSecondsUntilReset();
            twitter_err = null;
        }
        return returnResetSeconds;
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
     * オーバーロード
     * getTimeLine
     *
     * @param pattern
     */
    public void getTimeLine(String pattern) {
        getTimeLine(pattern, 0, 0, 0, null);
    }

    public void getTimeLine(String pattern, long maxID) {
        getTimeLine(pattern, maxID, 0, 0, null);
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
        android.os.AsyncTask<Void, Void, ArrayList<twitter4j.Status>> task = new android.os.AsyncTask<Void, Void, ArrayList<twitter4j.Status>>() {
            @SuppressLint("StaticFieldLeak")
            @Override
            protected ArrayList<twitter4j.Status> doInBackground(Void... aVoid) {
                try {
                    if (how == null) {
                        howToDisplay = twitterValue.TWEET_HOW_TO_DISPLAY_REWASH;
                    } else {
                        howToDisplay = how;
                    }

                    int _apiResetSeconds = checkAPIUnderRestriction(appSharedPreferences.API_RATE_DATE_GET_TIMELINE);
                    if (_apiResetSeconds != 0) {
                        apiResetSeconds = _apiResetSeconds;
                        cancel(true);
                        return null;
                    }

                    //ページング設定
                    Paging p = new Paging();
                    if (maxID != 0) p.setMaxId(maxID);
                    if (sinceID != 0) p.setSinceId(sinceID);
                    if (getCount != 0) {
                        p.setCount(getCount);
                    } else {
                        p.setCount(twitterValue.GET_COUNT_TIMELINE);
                    }

                    responseList = runGetTimeLine(pattern, p);
                    return (ArrayList<twitter4j.Status>) responseList;

                } catch (TwitterException e) {
                    cancel(true);
                    twitter_err = e;
                } catch (RuntimeException e) {
                    cancel(true);
                } catch (ParseException e) {
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
                callback.setHowToDisplay(howToDisplay);
                callback.callBackGetTimeLine(list);
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
            protected void onCancelled() {
                int returnResetSeconds = ResetSecondsInCancel();
                callBacksTimeLine callback = (callBacksTimeLine) callBacks.get();
                callback.callBackTwitterLimit(returnResetSeconds);
            }
        };
        task.execute();
    }

    /**
     * APIリクエスト返却値を参照し、
     * 制限が掛かったかを確認
     *
     * @param rateLimit
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
     * @return 0=未制限 1以上=制限中（制限解除までの秒数）
     */
    public int checkAPIUnderRestriction(String apiType) throws ParseException {
        //制限値取得
        Context app_context = ApplicationController.getInstance().getApplicationContext();
        SharedPreferences preferences = app_context.getSharedPreferences(appSharedPreferences.PREF_NAME, Context.MODE_PRIVATE);
        String date_string = preferences.getString(apiType, null);
        if (date_string == null) return 0;

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
            return (int) (TimeUnit.MILLISECONDS.toSeconds(diff) + 1);
        } else {
            return 0;
        }
    }
}
