package com.example.wensttay.securecam_project.exception;

import java.io.IOException;

/**
 * Created by wensttay on 11/05/17.
 */

public class SocketConnectionFail extends IOException {
    public SocketConnectionFail() {
        super("Fail to connect with the server, please check the IP, PORT and your Internet Connection/Context.");
    }
}
