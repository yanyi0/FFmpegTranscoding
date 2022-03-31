package com.fish.ffmpegtranscoding;

import android.app.Application;

/**
 * @author Charles
 * @date 2022/3/8
 */
public class MyApplication extends Application {

    public static Application myApplication;

    @Override
    public void onCreate() {
        super.onCreate();
        myApplication = this;
    }
}
