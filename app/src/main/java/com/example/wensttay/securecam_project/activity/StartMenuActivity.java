package com.example.wensttay.securecam_project.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.example.wensttay.securecam_project.R;
import com.example.wensttay.securecam_project.RegisterCodeProvider;
import com.example.wensttay.securecam_project.StartMenuHandlerSingleton;

public class StartMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_menu);
        setTitle("Menu inicial");

        StartMenuHandlerSingleton.init(new Handler.Callback() {
            @Override
            public boolean handleMessage(Message msg) {
                Log.i("SECURE-CAM", "Callback.handleMessage()");

                Bundle data = msg.getData();
                Context context = StartMenuActivity.this;

                boolean success = data.getBoolean(StartMenuHandlerSingleton.CONNECT_SUCCESS);
                String errorText = data.getString(StartMenuHandlerSingleton.CONNECT_ERROR_MESSAGE);
                SharedPreferences preferences = getApplicationContext()
                        .getSharedPreferences(RegisterCodeProvider.CAM_PREFERENCES_TAG, MODE_PRIVATE );

                String camCode = preferences.getString(RegisterCodeProvider.CAM_PREFERENCES_CODE_TAG,
                        RegisterCodeProvider.CAM_SERVER_REQUEST_CODE_TAG);

                if(success && !camCode.equals(RegisterCodeProvider.CAM_SERVER_REQUEST_CODE_TAG)){
                    Intent intent = new Intent(context, WebCamActivity.class);
                    Bundle bundle = new Bundle();
                    bundle.putString(RegisterCodeProvider.CAM_PREFERENCES_CODE_TAG, camCode);
                    intent.putExtras(bundle);
                    context.startActivity(intent);
                }else{
                    Toast.makeText(getApplicationContext(), errorText, Toast.LENGTH_LONG).show();
                }

                //Checar esse retorno
                return false;
            }
        });

    }

    public void startWebCamActivity(View view){
        startService(new Intent(this, RegisterCodeProvider.class));
//        registerCodeProvider.requestCode();
    }

    public void startControllConfigActivity(View view){
        Intent intent = new Intent(this, ControllConfigActivity.class);
        startActivity(intent);
    }

}
