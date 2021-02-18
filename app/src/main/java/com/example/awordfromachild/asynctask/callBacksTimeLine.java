package com.example.awordfromachild.asynctask;

import twitter4j.ResponseList;
import twitter4j.Status;

public interface callBacksTimeLine extends callBacksBase{
    public void callBackGetTimeLine(ResponseList<Status> result);
}
