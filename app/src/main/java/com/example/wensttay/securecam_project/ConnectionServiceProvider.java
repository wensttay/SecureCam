package com.example.wensttay.securecam_project;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;

import com.example.wensttay.securecam_project.exception.NotRegistredCamCode;
import com.example.wensttay.securecam_project.exception.SocketCannectionMissed;
import com.example.wensttay.securecam_project.exception.SocketConnectionFail;
import com.example.wensttay.securecam_project.exception.AlreadyListeningAServer;
import com.example.wensttay.securecam_project.handler.StartMenuHandlerSingleton;
import com.example.wensttay.securecam_project.handler.WebCamHandlerSIngleton;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.SocketException;

/**
 * Created by wensttay on 11/05/17.
 */

public class ConnectionServiceProvider extends Service {

    public final static String CAM_PREF_TAG = "CAM-PREFERENCES-CONFIG";
    public final static String CAM_PREF_CODE_TAG = "CAM-PREFERENCES-CONFIG-CAM-CODE";
    public final static String CAM_PREF_CONNECT_SUCCESS_TAG = "CONNECT-SUCCESS";
    public final static String CAM_PREF_CONNECT_ERROR_MESSAGE_TAG = "ERROR-MESSAGE";

    public final static String CAM_SERVER_REQUEST_CODE = "REQUEST-CAM-CODE";
    public final static String CAM_SERVER_FAIL_RESPONSE = "FAIL-FIND-REGISTER";
    public final static String CAM_SERVER_ALREADY_REGISTERED_RESPONSE = "OK-FIND-REGISTER";
    public final static String CAM_SERVER_PING_ASK = "OK?";
    public final static String CAM_SERVER_PING_ANSWER = "OK";

    public final static String CAM_SERVER_COMMAND_TAG = "CAM_SERVER_COMMAND_TAG";
    public final static String CAM_SERVER_COMMAND_RECORD_A_MINUT = "CAM_SERVER_COMMAND_RECORD_A_MINUT";
    public final static String CAM_SERVER_COMMAND_STREAM = "CAM_SERVER_COMMAND_STREAM";

    public final static String CAM_SERVER_COMMAND_FAIL = "CAM_SERVER_COMMAND_FAIL";

    public final static String CONNECTION_PROV_COMAND_TAG = "SERVICE_COMAND";
    public final static String CONNECTION_PROV_START = "START_CONNECTION";
    public final static String CONNECTION_PROV_LISTEN = "LISTEN_CONNECTION";
    public final static String CONNECTION_PROV_SEND_A_FRAME = "CONNECTION_PROV_SEND_A_FRAME";
    public final static String CONNECTION_PROV_SEND_A_FRAME_BYTEARRAY = "CONNECTION_PROV_SEND_A_FRAME_BYTEARRAY";
    public final static String CONNECTION_PROV_LISTEN_STOP = "CONNECTION_PROV_LISTEN_STOP";
    public final static String FINISH_CONNECTION = "FINISH_CONNECTION";

    public final static String HOST = "192.168.0.105";
    public final static int PORT = 10990;

    private boolean isListenAServer = false;
    private SharedPreferences preferences;
    private SharedPreferences.Editor editor;

    private Socket socket;
    private String code;

    private InputStream listenServerInputStrem = null;
    private OutputStream listenServerOutputStream = null;

    @Override
    public void onCreate(){
        super.onCreate();
        preferences = getApplicationContext().getSharedPreferences(CAM_PREF_TAG, MODE_PRIVATE );
        editor = preferences.edit();
        code =  preferences.getString(CAM_PREF_CODE_TAG, CAM_SERVER_REQUEST_CODE);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
//        Log.i("SECURE-CAM", "ConnectionServiceProvider.onStartCommand()");

        String comand = intent.getExtras().getString(CONNECTION_PROV_COMAND_TAG);

        switch (comand){
            case CONNECTION_PROV_START:
                connectToServer();
                break;
            case CONNECTION_PROV_LISTEN:
                listenServer();
                break;
            case CONNECTION_PROV_LISTEN_STOP:
                stopListenServer();
                break;
            case CONNECTION_PROV_SEND_A_FRAME:
                byte[] byteArray = intent.getExtras().getByteArray(CONNECTION_PROV_SEND_A_FRAME_BYTEARRAY);
                sendAFrame(byteArray);
                break;
            case FINISH_CONNECTION:
                Log.i("SECURE-CAM", "FINISH_CONNECTION Option selected");
                finishConnection();
        }

        return START_NOT_STICKY;
    }


    private void sendAFrame(byte[] byteArray) {
        if(isListenAServer){
            try {
                listenServerOutputStream = socket.getOutputStream();
                Log.i("SECURE-CAM", "Sending a array of bytes: " + byteArray.length);
                listenServerOutputStream.write(byteArray);
                listenServerOutputStream.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void stopListenServer(){
        isListenAServer = false;
        if(listenServerInputStrem != null){
            try {
                listenServerInputStrem.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        listenServerInputStrem = null;
        if(listenServerOutputStream != null){
            try {
                listenServerOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        listenServerOutputStream = null;
    }

    private void listenServer() {
        Log.i("SECURE-CAM", "Starting listenServer()");
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

                try {
                    if (isListenAServer){
                        throw new AlreadyListeningAServer();
                    } else {
                        isListenAServer = true;

                        while (isListenAServer) {
                            Message message = new Message();
                            byte[] bytes = new byte[1024 * 2];

                            Log.i("SECURE-CAM", "Waiting a command ...");
                            listenServerInputStrem = socket.getInputStream();
                            listenServerInputStrem.read(bytes);

                            String response = new String(bytes).trim();
                            Log.i("SECURE-CAM", "Recieved Response: " + response);

                            if(isListenAServer) {
                                Log.i("SECURE-CAM", "Sending Response to WebCam ...");
                                message.getData().putString(CAM_SERVER_COMMAND_TAG, response);
                                WebCamHandlerSIngleton.getHandler().sendMessage(message);
                            }
                        }

                    }
                } catch (AlreadyListeningAServer e){
                    Message message = new Message();
                    message.getData().putString(CAM_SERVER_COMMAND_FAIL, e.getMessage());
                    WebCamHandlerSIngleton.getHandler().sendMessage(message);
                } catch (IOException e) {
                    isListenAServer = false;
                    e.printStackTrace();
                }

                listenServerInputStrem = null;
            }
        };
        new Thread(runnable).start();
    }

    private void connectToServer() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                Message message = new Message();
                try {
                    connectOrPing();
                    message.getData().putBoolean(CAM_PREF_CONNECT_SUCCESS_TAG, true);
                    Log.i("SECURECAM", "Answer: " + code);

                } catch (NotRegistredCamCode | SocketConnectionFail | SocketCannectionMissed e){
                    Log.i("SECURECAM", "ERROR: " + e.getMessage());
                    e.printStackTrace();

                    message.getData().putBoolean(CAM_PREF_CONNECT_SUCCESS_TAG, false);
                    message.getData().putString(CAM_PREF_CONNECT_ERROR_MESSAGE_TAG, e.getMessage());

                } catch (IOException e){
                    Log.i("SECURECAM", "ERROR: " + e.getMessage());
                    e.printStackTrace();

                    message.getData().putBoolean(CAM_PREF_CONNECT_SUCCESS_TAG, false);
                    message.getData().putString(CAM_PREF_CONNECT_ERROR_MESSAGE_TAG,
                            "Fail to Connect.");
                }

                StartMenuHandlerSingleton.getHandler().sendMessage(message);
            }
        };
        new Thread(runnable).start();
    }

    private void connectOrPing() throws IOException {
        try {
            if(socket == null) {
                socket = new Socket(HOST, PORT);
                socket.getOutputStream().write(code.getBytes());
                socket.getOutputStream().flush();

                byte[] bytes = new byte[1024];
                socket.getInputStream().read(bytes);
                String serverResponse = new String(bytes).trim();

                doForConnectionResponse(serverResponse);
            }else{
                Socket socketPing = new Socket(HOST, PORT);
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
    }

    private void doForConnectionResponse(String serverResponse) throws NotRegistredCamCode {

        if(code.equals(CAM_SERVER_REQUEST_CODE)) {
            code = serverResponse;
            editor.putString(CAM_PREF_CODE_TAG, serverResponse);
            editor.commit();
        } else {
            switch (serverResponse) {
                case CAM_SERVER_ALREADY_REGISTERED_RESPONSE:
                    code = serverResponse;
                    break;
                case CAM_SERVER_FAIL_RESPONSE:
                    socket = null;
                    throw new NotRegistredCamCode();
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy(){
        finishConnection();
        super.onDestroy();
    }

    private void finishConnection() {
        if(this.socket != null){
            try {
                socket.close();
                isListenAServer = false;
            } catch (IOException e) {}

            socket = null;
        }
    }

}
