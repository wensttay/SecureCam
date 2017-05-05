package com.example.wensttay.securecam_project.entity;

import android.graphics.Bitmap;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by wensttay on 02/05/17.
 */

public class RecordItem {

    private String id;
    private Date date;
    private String path;
    private Bitmap smallImg;

    public RecordItem(String id, Date date, String path, Bitmap smallImg) {
        this.id = id;
        this.date = date;
        this.path = path;
        this.smallImg = smallImg;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Bitmap getSmallImg() {
        return smallImg;
    }

    public void setSmallImg(Bitmap smallImg) {
        this.smallImg = smallImg;
    }

    public String getHour(){
        return new SimpleDateFormat("HH:mm").format(date);
    }

    public String getDay(){
        return new SimpleDateFormat("dd/MM/yyyy").format(date);
    }
}
