package com.example.awordfromachild.asynctask;

import twitter4j.Status;

public interface callBacksDefaultTweet extends callBacksBase{

    public void callBackCreateFavo(Status status);
    public void callBackDestroyFavo(Status status);
}
