package com.example.wensttay.securecam_project.exception;

import java.net.ConnectException;

/**
 * Created by wensttay on 11/05/17.
 */

public class SocketConnectionWrongServer extends ConnectException{
    public SocketConnectionWrongServer(String msg) {
        super("Wrong Server Detected, please restart the application and try again.");
    }
}
