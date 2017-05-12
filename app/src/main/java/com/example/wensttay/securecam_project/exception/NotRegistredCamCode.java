package com.example.wensttay.securecam_project.exception;

/**
 * Created by wensttay on 11/05/17.
 */

public class NotRegistredCamCode extends IllegalArgumentException {
    public NotRegistredCamCode() {
        super("The Cam-code passed not existes on server");
    }
}
