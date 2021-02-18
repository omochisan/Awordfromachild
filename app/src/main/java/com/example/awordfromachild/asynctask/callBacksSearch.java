package com.example.awordfromachild.asynctask;

import twitter4j.QueryResult;

public interface callBacksSearch extends callBacksBase{
    public void callBackGetSearch(QueryResult getTweets);
}
