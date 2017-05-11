package com.example.wensttay.securecam_project;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.example.wensttay.securecam_project.activity.WebCamActivity;

import java.io.IOException;
import java.net.Socket;

/**
 * Created by wensttay on 11/05/17.
 */

public class RegisterCodeProvider extends Activity {

    private SharedPreferences preferences;
    private Socket socket;
    private String code = "REQUEST-CAM-CODE";
    private Activity context;

    public RegisterCodeProvider(Activity context) {
        this.context = context;
        preferences = context.getSharedPreferences("CAM-CONFIG", MODE_PRIVATE );

        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.putString("CAM-CODE", code);
        editor.commit();
    }

    public void requestCode() {
        runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    connectToServer();
                    startCam();
                }
            });
    }

    private void connectToServer() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                try {
                    if(socket == null) {
                        socket = new Socket("192.168.1.101", 10990);
                        socket.getOutputStream().write(code.getBytes());

                        byte[] bytes = new byte[1024];
                        socket.getInputStream().read(bytes);
                        code = new String(bytes).trim();

                    }
                    Log.i("SECURECAM", "Answer: " + code);
                } catch (IOException e) {
                    Log.i("SECURECAM", "ERROR: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        };
        new Thread(runnable).start();
    }

    private void startCam() {
        Intent intent = new Intent(context, WebCamActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("CAM-CODE", code);

        intent.putExtras(bundle);
        context.startActivity(intent);

        context.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, "CODE-CAM: " + code, Toast.LENGTH_LONG).show();
            }
        });
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
}
