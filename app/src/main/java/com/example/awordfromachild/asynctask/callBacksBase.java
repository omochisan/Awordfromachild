package com.example.awordfromachild.asynctask;

import twitter4j.TwitterException;

public interface callBacksBase {
    /**
     * TwitterAPIリミット時
     */
    public void callBackTwitterLimit(int secondsUntilReset);
}
