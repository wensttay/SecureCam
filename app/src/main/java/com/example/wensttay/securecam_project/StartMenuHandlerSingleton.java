package com.example.wensttay.securecam_project;

import android.os.Handler;

/**
 * Created by wensttay on 11/05/17.
 */

public class StartMenuHandlerSingleton {

    public static final String CONNECT_SUCCESS = "CONNECT-SUCCESS";
    public static final String CONNECT_ERROR_MESSAGE = "ERROR-MESSAGE";
    private static Handler handler = null;

    public static void init(Handler.Callback callback){

        if(handler == null) {
            handler = new Handler(callback);
        }else{
            throw new RuntimeException("ERRO MEU");
        }
    }

    public static Handler getHandler() {
        return handler;
    }
}
