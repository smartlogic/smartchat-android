package io.smartlogic.smartchat.models;

import android.content.ContentValues;
import android.database.Cursor;

import io.smartlogic.smartchat.Constants;

public class Notification {
    private int _id;
    private int creatorId;
    private String creatorUsername;
    private String fileUrl;
    private String drawingUrl;
    private boolean viewed = false;
    private int expireIn = Constants.DEFAULT_EXPIRE_IN;

    public static Notification fromCursor(Cursor cursor) {
        Notification notification = new Notification();

        if (cursor.getColumnIndex("_id") != -1) {
            notification.setDatabaseId(cursor.getInt(cursor.getColumnIndex("_id")));
        }

        if (cursor.getColumnIndex("creator_id") != -1) {
            notification.setCreatorId(cursor.getInt(cursor.getColumnIndex("creator_id")));
        }

        if (cursor.getColumnIndex("creator_username") != -1) {
            notification.setCreatorUsername(cursor.getString(cursor.getColumnIndex("creator_username")));
        }

        if (cursor.getColumnIndex("file_url") != -1) {
            notification.setFileUrl(cursor.getString(cursor.getColumnIndex("file_url")));
        }

        if (cursor.getColumnIndex("drawing_url") != -1) {
            notification.setDrawingUrl(cursor.getString(cursor.getColumnIndex("drawing_url")));
        }

        if (cursor.getColumnIndex("viewed") != -1) {
            notification.setViewed(cursor.getInt(cursor.getColumnIndex("viewed")) == 1);
        }

        if (cursor.getColumnIndex("expire_in") != -1) {
            notification.setExpireIn(cursor.getInt(cursor.getColumnIndex("expire_in")));
        }

        return notification;
    }

    public ContentValues getAttributes() {
        ContentValues cv = new ContentValues();

        cv.put("creator_id", getCreatorId());
        cv.put("creator_username", getCreatorUsername());
        cv.put("file_url", getFileUrl());
        cv.put("drawing_url", getDrawingUrl());
        cv.put("viewed", isViewed());
        cv.put("expire_in", getExpireIn());

        return cv;
    }

    @Override
    public String toString() {
        return "<Notification _id: " + getDatabaseId() + " creator_id: " + getCreatorId() + " creator_username: " + getCreatorUsername() +
                " file_url: " + getFileUrl() + " drawing_url: " + getDrawingUrl() + ">";
    }

    public void markViewed() {
        this.viewed = true;
    }

    public int getDatabaseId() {
        return _id;
    }

    public void setDatabaseId(int id) {
        this._id = id;
    }

    public int getCreatorId() {
        return creatorId;
    }

    public void setCreatorId(int creatorId) {
        this.creatorId = creatorId;
    }

    public String getCreatorUsername() {
        return creatorUsername;
    }

    public void setCreatorUsername(String creatorUsername) {
        this.creatorUsername = creatorUsername;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public String getDrawingUrl() {
        return drawingUrl;
    }

    public void setDrawingUrl(String drawingUrl) {
        this.drawingUrl = drawingUrl;
    }

    public boolean isViewed() {
        return viewed;
    }

    public void setViewed(boolean viewed) {
        this.viewed = viewed;
    }

    public int getExpireIn() {
        return expireIn;
    }

    public void setExpireIn(int expireIn) {
        this.expireIn = expireIn;
    }
}
