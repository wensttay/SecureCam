package com.example.wensttay.securecam_project.handler;

import android.os.Handler;

/**
 * Created by wensttay on 14/05/17.
 */

public class WebCamHandlerSIngleton {

    private static Handler handler = null;

    public static void init(Handler.Callback callback){
        handler = new Handler(callback);
    }

    public static Handler getHandler() {
        return handler;
    }
}
