package com.example.awordfromachild.asynctask;

import twitter4j.QueryResult;
import twitter4j.Status;
import twitter4j.TwitterException;

public interface callBacksBase {
    /**
     * TwitterAPIリミット時
     */
    public void callBackTwitterLimit(int secondsUntilReset);

    /**
     * Streamでの追跡結果を画面に追加する
     * @param status
     */
    public void callBackStreamAddList(Status status);

    public void callBackException();

    /**
     * ツイート取得時
     * @param list 取得タイムライン
     */
    public void callBackGetTweets(Object list, String howToDisplay);
}
