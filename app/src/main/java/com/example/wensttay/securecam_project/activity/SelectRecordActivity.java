package com.example.wensttay.securecam_project.activity;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.wensttay.securecam_project.R;
import com.example.wensttay.securecam_project.adapter.RecordItemAdapter;
import com.example.wensttay.securecam_project.entity.RecordItem;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

public class SelectRecordActivity extends AppCompatActivity {

    public final static int VIDEO_REMOVED = 333 + 333;

    List<RecordItem> recordItems = new ArrayList<>();
    RecordItemAdapter recordItemAdapter;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_record);
        setTitle("Gravações");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        recordItemAdapter = new RecordItemAdapter(this, recordItems);
        refill(getSecureVideos(this));

        listView = (ListView) findViewById(R.id.recordList);
        listView.setAdapter(recordItemAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), WatchRecordActivity.class);
                Bundle bundle = new Bundle();
                bundle.putString("selectedVideoPath", ( (RecordItem) recordItemAdapter.getItem(position) ).getPath());
                bundle.putString("selectedVideoId", ( (RecordItem) recordItemAdapter.getItem(position) ).getId());
                intent.putExtras(bundle);
                startActivityForResult(intent, VIDEO_REMOVED);
            }
        });
        listView.setCacheColorHint(Color.TRANSPARENT);
        listView.setFastScrollEnabled(true);
        listView.setScrollingCacheEnabled(false);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == VIDEO_REMOVED){
            refill(getSecureVideos(this));
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
    }

    public void refill(List<RecordItem> recordItems) {
        recordItemAdapter.getRecordItems().clear();
        recordItemAdapter.getRecordItems().addAll(recordItems);
        recordItemAdapter.notifyDataSetChanged();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return (super.onOptionsItemSelected(menuItem));
    }

    public static List<RecordItem> getSecureVideos(Activity activity) {

        List<RecordItem> recordItem = new ArrayList<>();

        Uri uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {
                MediaStore.Video.VideoColumns.DISPLAY_NAME,
                MediaStore.Video.VideoColumns.DATE_TAKEN,
                MediaStore.Video.VideoColumns.DATA};

        String selection =  MediaStore.Video.VideoColumns.DATA +" LIKE '%SecureCAM%'";
//        String[] selectionArgs = new String[] { "SecureCAM" };
        Cursor cursor = activity.getContentResolver().query(uri,
                projection,
                selection,
                null,
                null);

        Bitmap videoThumbnail;
        ByteArrayOutputStream os;
        byte[] array;

        while (cursor.moveToNext()) {
            String recordName = cursor.getString(cursor.getColumnIndexOrThrow(projection[0])).substring(0, 12);
            Long recordDate = cursor.getLong(cursor.getColumnIndexOrThrow(projection[1]));
            String recordPath = cursor.getString(cursor.getColumnIndexOrThrow(projection[2]));

            try {
                videoThumbnail = ThumbnailUtils.createVideoThumbnail(recordPath, MediaStore.Images.Thumbnails.MINI_KIND);
                os = new ByteArrayOutputStream();
                videoThumbnail.compress(Bitmap.CompressFormat.JPEG, 10, os);
                array = os.toByteArray();
                videoThumbnail = BitmapFactory.decodeByteArray(array, 0, array.length);
//            videoThumbnail = rotateBitmap(videoThumbnail, 90);
                recordItem.add(new RecordItem(recordName, new Date(recordDate), recordPath, videoThumbnail));
            }catch (Exception e){
                e.printStackTrace();
            }
//            String[] columnNames = cursor.getColumnNames();
//            for (String columnName : columnNames) {
//                Log.i("SECURECAM", columnName + ": " + cursor.getString(cursor.getColumnIndex(columnName)));
//            }
//            break;
        }
        Collections.reverse(recordItem);
        return recordItem;
    }

//    public static Bitmap rotateBitmap(Bitmap source, float angle) {
//        Matrix matrix = new Matrix();
//        matrix.postRotate(angle);
//        return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
//    }

}
