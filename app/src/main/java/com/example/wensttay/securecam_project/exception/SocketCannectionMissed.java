package com.example.wensttay.securecam_project.exception;

import java.net.SocketException;

/**
 * Created by wensttay on 11/05/17.
 */

public class SocketCannectionMissed extends SocketException {
    public SocketCannectionMissed() {
        super("Connection with server losted, please try again.");
    }
}
