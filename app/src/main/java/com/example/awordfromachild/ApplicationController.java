package com.example.awordfromachild;

import android.app.Application;

/**
 * ApplicationContext取得用シングルトン
 */
public class ApplicationController extends Application {
    private static ApplicationController sInstance;

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
    }

    public static synchronized ApplicationController getInstance() {
        return sInstance;
    }
}
