package com.haiblee.android;

import android.app.Application;
import android.content.Context;

/**
 * Created by Administrator on 2017/8/16.
 */

public class App extends Application {

    private static Application sContext = null;

    @Override
    public void onCreate() {
        super.onCreate();
        sContext = this;
    }

    public static Context getContext(){
        return sContext;
    }
}
