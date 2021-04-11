package com.example.awordfromachild.asynctask;

import twitter4j.Status;

public interface callBacksCreateTweet extends callBacksBase {
    public void callBackTweeting(Status result);
}
