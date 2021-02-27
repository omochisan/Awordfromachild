package com.example.awordfromachild.asynctask;

import java.util.ArrayList;

import twitter4j.ResponseList;
import twitter4j.Status;

public interface callBacksTimeLine extends callBacksBase{
    public void callBackGetTimeLine(ArrayList<Status> result);
}
