package com.example.awordfromachild.constant;

public class appSharedPreferences {
    //SharedPrerence用のキー
    public static final String PREF_NAME = "awordfromachild0325";
    public static final String TOKEN = "token";
    public static final String TOKEN_SECRET = "token_secret";
    public static final String SET_DISPLAY_TYPE_TWEET_CREATE = "display_type_tweet_creation";
    public static final String SET_COUNT_LIKE = "count_like";
    public static final String SET_COUNT_RETWEET = "count_retweet";

    //API レート制限情報用
    // 日時フォーマット
    public static final String API_RATE_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    // タイムライン系
    public static final String API_RATE_DATE_GET_TIMELINE = "apiRate_getTimeline";
    // お気に入り系
    public static final String API_RATE_DATE_GET_FAVORITE = "apiRate_getFavorite";
    // 検索系
    public static final String API_RATE_DATE_GET_SEARCH = "apiRate_getSearch";
    // 投稿系
    public static final String API_RATE_DATE_GET_POST_TWEET = "apiRate_getPostTweet";
    // ユーザー情報取得系
    public static final String API_RATE_DATE_GET_USER_INFO= "apiRate_getUser";
}
