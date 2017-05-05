package com.example.wensttay.securecam_project.activity;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.example.wensttay.securecam_project.WebCamActivity;
import com.example.wensttay.securecam_project.R;

public class StartMenuActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_menu);
        setTitle("Menu inicial");
    }

    public void startWebCamActivity(View view){
        Intent intent = new Intent(this, WebCamActivity.class);
        startActivity(intent);
    }

    public void startControllConfigActivity(View view){
        Intent intent = new Intent(this, ControllConfigActivity.class);
        startActivity(intent);
    }

}
