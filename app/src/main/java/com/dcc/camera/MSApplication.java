package com.dcc.camera;

import android.app.Application;

/**
 * Created by ding on 05/12/2017.
 */

public class MSApplication extends Application {

    private static MSApplication app;

    @Override
    public void onCreate() {
        super.onCreate();

        app = this;
    }

    public static MSApplication getMSApp() {
        return app;
    }
}
