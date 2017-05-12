package com.example.wensttay.securecam_project;

import android.app.Activity;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.example.wensttay.securecam_project.activity.WebCamActivity;
import com.example.wensttay.securecam_project.exception.NotRegistredCamCode;
import com.example.wensttay.securecam_project.exception.SocketCannectionMissed;
import com.example.wensttay.securecam_project.exception.SocketConnectionFail;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;
import java.sql.Connection;

/**
 * Created by wensttay on 11/05/17.
 */

public class RegisterCodeProvider extends Service {

    public final static String CAM_PREFERENCES_TAG = "CAM-PREFERENCES-CONFIG";
    public final static String CAM_PREFERENCES_CODE_TAG = "CAM-PREFERENCES-CONFIG-CAM-CODE";

    public final static String CAM_SERVER_REQUEST_CODE_TAG = "REQUEST-CAM-CODE";
    public final static String CAM_SERVER_FAIL_RESPONSE = "FAIL-FIND-REGISTER";
    public final static String CAM_SERVER_ALREADY_REGISTERED_RESPONSE = "OK-FIND-REGISTER";
    public final static String CAM_SERVER_PING_ASK = "OK?";
    public final static String CAM_SERVER_PING_ANSWER = "OK";

    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;
    private Socket socket;
    private String code;

    @Override
    public void onCreate(){
        super.onCreate();
        preferences = getApplicationContext().getSharedPreferences(CAM_PREFERENCES_TAG, MODE_PRIVATE );
        editor = preferences.edit();
        code =  preferences.getString(CAM_PREFERENCES_CODE_TAG, CAM_SERVER_REQUEST_CODE_TAG);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.i("SECURE-CAM", "RegisterCodeProvider.onStartCommand()");
        //
        connectToServer();
        //
        return START_NOT_STICKY;
    }

    private void connectToServer() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                try {
                    try {
                        if(socket == null) {
                            socket = new Socket("192.168.1.101", 10990);
                            socket.getOutputStream().write(code.getBytes());
                            byte[] bytes = new byte[1024];
                            socket.getInputStream().read(bytes);
                            String serverResponse = new String(bytes).trim();
                            responseDecisionAction(serverResponse);
                        }else{
                            Socket socketPing = new Socket("192.168.1.101", 10990);

                            socketPing.getOutputStream().write(CAM_SERVER_PING_ASK.getBytes());
                            socketPing.getOutputStream().flush();
                            byte[] by = new byte[1024];

                            socketPing.getInputStream().read(by);

                            String serverResponse = new String(by).trim();
                            socketPing.close();

                            if(!CAM_SERVER_PING_ANSWER.equals(serverResponse)){
                                throw new SocketConnectionFail();
                            }

                        }
                    }catch (ConnectException e){
                        throw new SocketConnectionFail();
                    }catch (SocketException e){
                        throw new SocketCannectionMissed();

                    }

                    message.getData().putBoolean(StartMenuHandlerSingleton.CONNECT_SUCCESS, true);
                    Log.i("SECURECAM", "Answer: " + code);

                } catch (NotRegistredCamCode | SocketConnectionFail | SocketCannectionMissed e){
                    Log.i("SECURECAM", "ERROR: " + e.getMessage());
                    e.printStackTrace();

                    message.getData().putBoolean(StartMenuHandlerSingleton.CONNECT_SUCCESS, false);
                    message.getData().putString(StartMenuHandlerSingleton.CONNECT_ERROR_MESSAGE, e.getMessage());

                } catch (IOException e){
                    Log.i("SECURECAM", "ERROR: " + e.getMessage());
                    e.printStackTrace();
                }

                StartMenuHandlerSingleton.getHandler().sendMessage(message);
            }
        };
        new Thread(runnable).start();
    }

    private void responseDecisionAction(String serverResponse) throws NotRegistredCamCode {

        if(code.equals(CAM_SERVER_REQUEST_CODE_TAG)) {
            code = serverResponse;
            editor.putString(CAM_PREFERENCES_CODE_TAG, serverResponse);
            editor.commit();
//        } else {
        } else if (code.equals(CAM_SERVER_FAIL_RESPONSE)){
            if(serverResponse.equals(CAM_SERVER_ALREADY_REGISTERED_RESPONSE)){
                code = serverResponse;
            }else{
                socket = null;
                throw new NotRegistredCamCode();
            }
        }
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    @Override
    public void onDestroy(){
        if(this.socket != null){
            try {
                socket.close();
                socket = null;
            } catch (IOException e) {}
        }
        super.onDestroy();
    }

}
