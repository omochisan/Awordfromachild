package com.example.awordfromachild.asynctask;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.Map;

import twitter4j.ResponseList;
import twitter4j.Status;

public interface callBacksTimeLine extends callBacksBase{
    /**
     *
     * @param list 取得タイムライン
     */
    public void callBackGetTimeLine(ArrayList<Status> list);

    public void setHowToDisplay(String how);
}
