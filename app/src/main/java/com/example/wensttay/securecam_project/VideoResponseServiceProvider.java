package com.example.wensttay.securecam_project;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class VideoResponseServiceProvider extends Service {
    public VideoResponseServiceProvider() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
