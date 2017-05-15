package com.example.wensttay.securecam_project.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import static com.example.wensttay.securecam_project.ConnectionServiceProvider.CAM_PREF_CODE_TAG;
import static com.example.wensttay.securecam_project.ConnectionServiceProvider.CAM_PREF_CONNECT_ERROR_MESSAGE_TAG;
import static com.example.wensttay.securecam_project.ConnectionServiceProvider.CAM_PREF_CONNECT_SUCCESS_TAG;
import static com.example.wensttay.securecam_project.ConnectionServiceProvider.CAM_PREF_TAG;
import static com.example.wensttay.securecam_project.ConnectionServiceProvider.CAM_SERVER_REQUEST_CODE;
import static com.example.wensttay.securecam_project.ConnectionServiceProvider.CONNECTION_PROV_COMAND_TAG;
import static com.example.wensttay.securecam_project.ConnectionServiceProvider.CONNECTION_PROV_START;

import com.example.wensttay.securecam_project.R;
import com.example.wensttay.securecam_project.ConnectionServiceProvider;
import com.example.wensttay.securecam_project.handler.StartMenuHandlerSingleton;

public class StartMenuActivity extends AppCompatActivity implements Handler.Callback{

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_menu);
        setTitle("Menu inicial");
        StartMenuHandlerSingleton.init(this);

    }

    public void startWebCamActivity(View view){
        Bundle bundle = new Bundle();
        bundle.putString(CONNECTION_PROV_COMAND_TAG, CONNECTION_PROV_START);
        Intent intent = new Intent(this, ConnectionServiceProvider.class);
        intent.putExtras(bundle);
        startService(intent);
    }

    public void startControllConfigActivity(View view){
        Intent intent = new Intent(this, ControllConfigActivity.class);
        startActivity(intent);
    }

    @Override
    public boolean handleMessage(Message msg) {
        Log.i("SECURE-CAM", "Callback.handleMessage()");

        Bundle data = msg.getData();
        Context context = StartMenuActivity.this;

        boolean success = data.getBoolean(CAM_PREF_CONNECT_SUCCESS_TAG);
        String errorText = data.getString(CAM_PREF_CONNECT_ERROR_MESSAGE_TAG);

        SharedPreferences preferences = getApplicationContext()
                .getSharedPreferences(CAM_PREF_TAG, MODE_PRIVATE );

        String camCode = preferences.getString(CAM_PREF_CODE_TAG,
                CAM_SERVER_REQUEST_CODE);

        if(success){

            Intent intent = new Intent(context, WebCamActivity.class);
            Bundle bundle = new Bundle();
            bundle.putString(CAM_PREF_CODE_TAG, camCode);
            intent.putExtras(bundle);
            context.startActivity(intent);

        }else{
            Toast.makeText(getApplicationContext(), errorText, Toast.LENGTH_LONG).show();
        }

        return false;
    }
}
