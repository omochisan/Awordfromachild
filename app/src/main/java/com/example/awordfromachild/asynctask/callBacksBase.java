package com.example.awordfromachild.asynctask;

import twitter4j.Status;

public interface callBacksBase {
    /**
     * TwitterAPIリミット時
     */
    void callBackTwitterLimit(int secondsUntilReset);

    /**
     * Streamでの追跡結果を画面に追加する
     * @param status 追加ツイート
     */
    void callBackStreamAddList(Status status);

    void callBackException();

    /**
     * ツイート取得時
     * @param list 取得タイムライン
     * @param howToDisplay 取得ツイート　画面追加方法
     */
    void callBackGetTweets(Object list, String howToDisplay);
}
