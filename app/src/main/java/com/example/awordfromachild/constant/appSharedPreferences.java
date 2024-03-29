package com.example.awordfromachild.constant;

public class appSharedPreferences {
    //SharedPrerence用のキー
    public static final String PREF_NAME = "awordfromachild0325";
    public static final String TOKEN = "token";
    public static final String TOKEN_SECRET = "token_secret";
    public static final String SET_DISPLAY_TYPE_TWEET_CREATE = "display_type_tweet_creation";
    public static final String SET_CRITERION_LIKE = "criterion_like";
    public static final String FLG_FIRST_START = "flg_first_start";

    //API レート制限情報用
    // 日時フォーマット
    public static final String API_RATE_DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";
    // タイムライン系
    public static final String API_RATE_DATE_GET_TIMELINE = "apiRate_getTimeline";
    // お気に入り系
    public static final String API_RATE_DATE_GET_FAVORITE = "apiRate_getFavorite";
    public static final String API_RATE_DATE_PUT_FAVORITE = "apiRate_putFavorite";
    public static final String API_RATE_DATE_DES_FAVORITE = "apiRate_desFavorite";
    // リツイート系
    public static final String API_RATE_DATE_PUT_RETWEET = "apiRate_putReTweet";
    public static final String API_RATE_DATE_DES_RETWEET = "apiRate_desReTweet";

    // 検索系
    public static final String API_RATE_DATE_GET_SEARCH = "apiRate_getSearch";
    // 投稿系
    public static final String API_RATE_DATE_GET_POST_TWEET = "apiRate_getPostTweet";
    // ユーザー情報取得系
    public static final String API_RATE_DATE_GET_USER_INFO= "apiRate_getUser";
    //DM系
    public static final String API_RATE_DATE_GET_DM = "apiRate_getDM";
    //フォロー一覧取得
    public static final String API_RATE_DATE_GET_FRIEND_LIST= "apiRate_getFriendList";
    //ストリーミング
    @SuppressWarnings("unused")
    public static final String API_RATE_DATE_STREAM = "apiRate_stream";
}
