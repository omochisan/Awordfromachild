package com.example.awordfromachild.constant;

import com.example.awordfromachild.R;

public class twitterValue {
    //公開タイムライン
    public static final String PUBLIC = "public_timeline";
    //自分のデフォルトタイムライン
    public static final String HOME = "home_timeline";
    //（公式RTを含まない）デフォルトタイムライン
    public static final String FRIEND = "friend_timeline";
    //自分のツイートのみのタイムライン
    public static final String USER = "user_timeline";
    //自分のIDが含まれたツイートをまとめたタイムライン
    public static final String MENTIONS = "mentions";
    //自分がした公式RTツイートのみのタイムライン
    public static final String RT_BY_ME = "retweeted_by_me";
    //自分がフォローしているユーザーがした公式RTのみのタイムライン
    public static final String RT_TO_ME = "retweeted_to_me";
    //公式RTされた自分のツイートのみのタイムライン
    public static final String RT_OF_ME = "retweeted_of_me";

    //1回の読込で取得するタイムライン数
    public static final Integer GET_COUNT_TIMELINE = 40;

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
    //注目タブ（一定のいいね＆リツイートがされているツイートが表示される）
    // デフォルトのいいね数
    public static final String DEFAULT_LIKES = "10";
    // デフォルトのリツイート数
    public static final String DEFAULT_RETWEET = "10";
}
