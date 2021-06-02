package com.example.awordfromachild.constant;

public class twitterValue {
    //アプリのハッシュタグ
    public static final String APP_HASH_TAG = "#こどものひとこと";
    public static final String APP_TAG = "こどものひとこと";
    //TwitterAPI RATE制限　エラーコード
    public static final int TwitterAPI_RATE_ERRORCODE = 429;

    //ツイート文字数制限（自由入力パターン）
    public static final int CHARALIMIT_FREE = 131;
    //ツイート文字数制限（フォーム入力パターン）
    public static final int CHARALIMIT_FORM = 131;

    //デフォルトのツイート作成画面
    public static final String DEFAULT_TYPE_OF_TWEET_CREATION = "free";
    //ツイート作成画面タイプ（自由入力）
    public static final String TYPE_OF_TWEET_CREATION_FREE = "free";
    //ツイート作成画面タイプ（フォーム入力）
    public static final String TYPE_OF_TWEET_CREATION_FORM = "form";

    //注目タブ（一定のいいね以上がされたツイートが表示される）
    public static final int CRITERION_LIKE_S = 10;
    public static final int CRITERION_LIKE_M = 10;
    public static final int CRITERION_LIKE_L = 10;
    // デフォルトのいいね目安
    public static final String DEFAULT_LIKES = "10";

    /**
     * 取得ツイートの表示方法
     */
    public static class howToDisplayTweets {
        //　洗い替え
        public static final String TWEET_HOW_TO_DISPLAY_REWASH = "rewash";
        //　先頭に追加
        public static final String TWEET_HOW_TO_DISPLAY_UNSHIFT = "add";
        //　末尾に追加
        public static final String TWEET_HOW_TO_DISPLAY_PUSH = "push";
    }

    /**
     * タイムライン種別
     */
    public static class timeLineType {
        // 公開タイムライン
        public static final String PUBLIC = "public_timeline";
        // 自分のデフォルトタイムライン
        public static final String HOME = "home_timeline";
        // （公式RTを含まない）デフォルトタイムライン
        public static final String FRIEND = "friend_timeline";
        // 自分のツイートのみのタイムライン
        public static final String USER = "user_timeline";
        // 自分のIDが含まれたツイートをまとめたタイムライン
        public static final String MENTIONS = "mentions";
        // 自分がした公式RTツイートのみのタイムライン
        public static final String RT_BY_ME = "retweeted_by_me";
        // 自分がフォローしているユーザーがした公式RTのみのタイムライン
        public static final String RT_TO_ME = "retweeted_to_me";
        // 公式RTされた自分のツイートのみのタイムライン
        public static final String RT_OF_ME = "retweeted_of_me";
    }

    /**
     * ツイートの数管理
     * （1度に取得する数、最大取得数等）
     */
    public static class tweetCounts {
        //1回の読込で表示するツイート数
        public static final Integer ONE_TIME_DISPLAY_TWEET = 50;
        //最新ツイート読込で取得する最大タイムライン数（未使用）
        public static final Integer GET_COUNT_NEWER_TIMELINE = 200;
        //1回で取得する最大フォローユーザー数
        public static final Integer GET_FOLLOW_LIST = 200;
        //1回で取得する最大追加読込数
        public static final Integer ONE_TIME_DISPLAY_TWEET_MAX = 200;
    }

    /**
     * HTTP通信に関する定数
     */
    public static class httpConnection {
        //接続タイムアウト
        public static final int CONNECTION_TIMEOUT = 10000;
        //読込タイムアウト
        public static final int READ_TIMEOUT = 10000;
    }

    /**
     * TwitterAPI　エンドポイント
     */
    public static class TwitterAPIEndPoint {
        public static final String search = "https://api.twitter.com/1.1/search/tweets.json";
    }

    /**
     * Twitter取得ツイートタイプ
     */
    public static class resultType {
        //人気のツイート
        public static final String POPULAR = "popular";
        //最新のツイート。
        public static final String RECENT = "recent";
        //全てのツイート。
        public static final String mixed = "MIXED";
    }
}
