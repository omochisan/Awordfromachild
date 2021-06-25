package com.example.awordfromachild.common;

import com.example.awordfromachild.asynctask.callBacksBase;
import com.example.awordfromachild.constant.twitterValue;

import java.lang.ref.WeakReference;
import java.util.Objects;

import twitter4j.TwitterException;

public class exceptionHandling {
    /**
     * エラー処理
     * ーTwitterAPIException：レート制限エラーの場合、制限解除までの分数を表示
     * ーその他Exception：エラー発生表示
     *
     * @param err エラー内容
     * @param callBacks コールバック先
     */
    public void exceptionHand(Object err, WeakReference<callBacksBase> callBacks) {
        if (err.getClass() == TwitterException.class) {
            String msg = ((TwitterException) err).getMessage();
            boolean msg_seconds = Objects.requireNonNull(msg).matches("^-?\\d+$");
            if (((TwitterException) err).getErrorCode() == twitterValue.errorLimitsValue.TwitterAPI_RATE_ERRORCODE
                    || msg_seconds) {
                int resetSeconds;
                if (msg_seconds) {
                    //エラーメッセージにリセットまでの秒数が仕込んである場合
                    //（レート制限チェックよりリセット秒数取得した場合）
                    resetSeconds = Integer.parseInt(msg);
                } else {
                    //API実行前チェックが効かず、API実行制限エラーが発生してしまった場合
                    resetSeconds = ((TwitterException) err).getRateLimitStatus().getSecondsUntilReset();
                }
                callBacks.get().callBackTwitterLimit(resetSeconds);

            } else {
                callBacks.get().callBackException();
            }

        } else {
            //非チェック例外
            callBacks.get().callBackException();
        }
    }
}
