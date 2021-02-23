package com.example.awordfromachild.constant;

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
}
