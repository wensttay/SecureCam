package com.example.wensttay.securecam_project.activity;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentUris;
import android.content.DialogInterface;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.wensttay.securecam_project.R;

import java.io.File;

public class WatchRecordActivity extends AppCompatActivity {

    private VideoView myVideoView;
    private int position = 0;
    private ProgressDialog progressDialog;
    private MediaController mediaControls;
    private String pathVideo;
    private String idVideo;
    private ActionBar supportActionBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_watch_record);
        setTitle("Gravação");
        supportActionBar = getSupportActionBar();
        supportActionBar.setDisplayHomeAsUpEnabled(true);


        if (mediaControls == null) {
            mediaControls = new MediaController(WatchRecordActivity.this);
        }

        myVideoView = (VideoView) findViewById(R.id.video_view);
        TextView viewById = (TextView) findViewById(R.id.video_textViewId);

        progressDialog = new ProgressDialog(WatchRecordActivity.this);
        progressDialog.setMessage("Loading Video...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        pathVideo = getIntent().getExtras().getString("selectedVideoPath");
        idVideo = getIntent().getExtras().getString("selectedVideoId");
        viewById.setText(idVideo);

        try {
            myVideoView.setMediaController(mediaControls);
            myVideoView.setVideoURI(Uri.parse(pathVideo));

        } catch (Exception e) {
            Log.e("ERROR", e.getMessage());
            e.printStackTrace();
        }

        myVideoView.requestFocus();
        myVideoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                progressDialog.dismiss();

                if(myVideoView.getDuration() != position) {
                    myVideoView.seekTo(position);
                }

                if (position == 0) {
                    myVideoView.pause();
                }
            }
        });

        myVideoView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent motionEvent) {
                if (myVideoView.isPlaying()) {
                    myVideoView.pause();
                    if (!supportActionBar.isShowing()) {
                        supportActionBar.show();
                        mediaControls.show();
                    }
                    return false;
                } else {
                    if (supportActionBar.isShowing()) {
                        supportActionBar.hide();
                        mediaControls.hide();
                    }
                    myVideoView.start();
                    return false;
                }
            }
        });

        myVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                if (!supportActionBar.isShowing()) {
                    supportActionBar.show();
                }
                position = 0;
                myVideoView.seekTo(0);
            }
        });

        myVideoView.setOnErrorListener(new MediaPlayer.OnErrorListener(){
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                progressDialog.dismiss();
                Toast.makeText(getApplicationContext(), "ERROR: This file cannot be read or not exists", Toast.LENGTH_LONG).show();
                finish();
                return true;
            }
        });

    }

    public void deteleThisVideo(View view) {

        if(myVideoView.isPlaying()) {
            myVideoView.pause();
        }

        //Cria o gerador do AlertDialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        //define o titulo
        builder.setTitle("Deletar Video");
        //define a mensagem
        builder.setMessage("Você tem certeza que deseja apagar para sempre esse vídeo? " +
                " (Também não será removido do nosso servidor)");
        //define um botão como positivo
        builder.setPositiveButton("Sim", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                File file = new File(pathVideo);

                if(file.exists() && file.canWrite()){
                    getContentResolver().delete(getUriFromPath(pathVideo), null, null);
                    setResult(SelectRecordActivity.VIDEO_REMOVED);
                    finish();
                }
                Toast.makeText(WatchRecordActivity.this, "Vídeo deletado!", Toast.LENGTH_SHORT).show();
            }
        });
        //define um botão como negativo.
        builder.setNegativeButton("Não", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                Toast.makeText(WatchRecordActivity.this, "Ação Cancelada!", Toast.LENGTH_SHORT).show();

                if(myVideoView.getCurrentPosition() > 0) {
                    myVideoView.start();
                }
            }
        });
        //cria o AlertDialog
        AlertDialog alerta = builder.create();
        //Exibe
        alerta.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    @Override
    public void onPause(){
        super.onPause();
        if(myVideoView.getCurrentPosition() == position) {
            myVideoView.seekTo(0);
        }
        myVideoView.pause();
        this.position = myVideoView.getCurrentPosition();
    }

    @Override
    public void onResume(){
        myVideoView.resume();
        super.onResume();
    }

    @Override
    public void onStop(){
        myVideoView.stopPlayback();
        super.onStop();
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        super.onSaveInstanceState(savedInstanceState);
        savedInstanceState.putBoolean("isShowing", supportActionBar.isShowing());
        savedInstanceState.putInt("Position", myVideoView.getCurrentPosition());
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        position = savedInstanceState.getInt("Position");
        boolean isShowing = savedInstanceState.getBoolean("isShowing");
        myVideoView.seekTo(position);

        if(isShowing) {
            supportActionBar.show();
        }else{
            supportActionBar.hide();
            myVideoView.start();
        }
    }

    private Uri getUriFromPath(String filePath) {
        long videoId;
        Uri videoUri = MediaStore.Video.Media.getContentUri("external");

        String[] projection = {MediaStore.Video.Media._ID};

        Cursor cursor = getContentResolver()
                .query(videoUri,
                        projection,
                        MediaStore.Video.Media.DATA + " LIKE ?",
                        new String[] { filePath },
                        null);
        cursor.moveToFirst();

        int columnIndex = cursor.getColumnIndex(projection[0]);
        videoId = cursor.getLong(columnIndex);

        cursor.close();
        return ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, videoId);
    }

}