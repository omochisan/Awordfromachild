package com.example.awordfromachild.constant;

import com.example.awordfromachild.R;

public class twitterValue {
    //TwitterAPI呼出回数制限
    // ※2021/3現在　ユーザー単位は75回、アプリ単位で300回の呼出制限がある。15分毎にリセットされる。
    // 余裕を持たせて、当アプリは70回で呼出制限が掛かるようにする
    public static final int TwitterAPI_LIMITS = 70;

    //タイムライン種別
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

    //1回の読込で取得する最大タイムライン数
    public static final Integer GET_COUNT_TIMELINE = 50;
    //最新ツイート読込で取得する最大タイムライン数
    public static final Integer GET_COUNT_NEWER_TIMELINE = 200;

    //ツイート取得タイプ
    // 最新のツイートを取得
    public static final String GET_TYPE_NEWEST = "newest";
    // 対象のツイートより古いツイート
    public static final String GET_TYPE_OLDER = "older";
    // 対象のツイートより新しいツイート
    public static final String GET_TYPE_EVEN_NEWER = "even_newer";

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

    //取得ツイートの表示方法
    //　洗い替え
    public static final String TWEET_HOW_TO_DISPLAY_REWASH = "rewash";
    //　先頭に追加
    public static final String TWEET_HOW_TO_DISPLAY_UNSHIFT = "add";
    //　末尾に追加
    public static final String TWEET_HOW_TO_DISPLAY_PUSH = "push";
}
