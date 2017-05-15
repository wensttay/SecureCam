package com.example.wensttay.securecam_project.exception;

/**
 * Created by wensttay on 14/05/17.
 */

public class AlreadyListeningAServer extends RuntimeException {

    public AlreadyListeningAServer() {
        super("This cam is already connected with a server.");
    }
}
