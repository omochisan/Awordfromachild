package com.example.awordfromachild.common;

import com.example.awordfromachild.asynctask.callBacksBase;
import com.example.awordfromachild.asynctask.callBacksMain;

import java.lang.ref.WeakReference;

import twitter4j.TwitterException;

public class exceptionHandling {
    public void exceptionHand(Object err, WeakReference<callBacksBase> callBacks){
        if(err.getClass() == TwitterException.class){
            callBacksMain callback = (callBacksMain) callBacks.get();
            //callback.callBackTwitterLimit(returnResetSeconds);
        } else {

        }
    }
}
