package com.example.wensttay.securecam_project.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.wensttay.securecam_project.R;
import com.example.wensttay.securecam_project.RegisterCodeProvider;

public class StartMenuActivity extends AppCompatActivity {
    RegisterCodeProvider registerCodeProvider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_menu);
        setTitle("Menu inicial");

        registerCodeProvider = new RegisterCodeProvider(this);
    }

    public void startWebCamActivity(View view){
        registerCodeProvider.requestCode();
    }

    public void startControllConfigActivity(View view){
        Intent intent = new Intent(this, ControllConfigActivity.class);
        startActivity(intent);
    }

}
