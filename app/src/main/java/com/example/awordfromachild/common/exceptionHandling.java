package com.example.awordfromachild.common;

import android.util.Log;

import com.example.awordfromachild.asynctask.callBacksBase;
import com.example.awordfromachild.asynctask.callBacksMain;
import com.example.awordfromachild.constant.twitterValue;

import java.lang.ref.WeakReference;

import twitter4j.TwitterException;

public class exceptionHandling {
    /**
     * エラー処理
     * ーTwitterAPIException：レート制限エラーの場合、制限解除までの分数を表示
     * ーその他Exception：エラー発生表示
     * @param err
     * @param callBacks
     */
    public void exceptionHand(Object err, WeakReference<callBacksBase> callBacks){
        if(err.getClass() == TwitterException.class){
            String msg = ((TwitterException) err).getMessage();
            boolean msg_seconds = msg.matches("^-?\\d+$");
            if(((TwitterException) err).getErrorCode() == twitterValue.TwitterAPI_RATE_ERRORCODE
            || msg_seconds){
                int resetSeconds = 0;
                if(msg_seconds){
                    //エラーメッセージにリセットまでの秒数が仕込んである場合
                    //（レート制限チェックよりリセット秒数取得した場合）
                    resetSeconds = Integer.parseInt(msg);
                }else{
                    //API実行前チェックが効かず、API実行制限エラーが発生してしまった場合
                    resetSeconds = ((TwitterException) err).getRateLimitStatus().getSecondsUntilReset();
                }
                callBacks.get().callBackTwitterLimit(resetSeconds);

            }else {
                callBacks.get().callBackException();
            }

        } else {
            //非チェック例外
            callBacks.get().callBackException();
        }
    }
}
