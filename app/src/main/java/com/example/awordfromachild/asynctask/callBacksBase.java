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
     */
    void callBackGetTweets(Object list, String howToDisplay);
}
