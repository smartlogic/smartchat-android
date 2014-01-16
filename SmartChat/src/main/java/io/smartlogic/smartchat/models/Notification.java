package io.smartlogic.smartchat.models;

import android.content.ContentValues;
import android.database.Cursor;

public class Notification {
    private int _id;
    private int creatorId;
    private String creatorUsername;
    private String fileUrl;
    private String drawingUrl;

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

        return notification;
    }

    public ContentValues getAttributes() {
        ContentValues cv = new ContentValues();

        cv.put("creator_id", getCreatorId());
        cv.put("creator_username", getCreatorUsername());
        cv.put("file_url", getFileUrl());
        cv.put("drawing_url", getDrawingUrl());

        return cv;
    }

    @Override
    public String toString() {
        return "<Notification _id: " + getDatabaseId() + " creator_id: " + getCreatorId() + " creator_username: " + getCreatorUsername() +
                " file_url: " + getFileUrl() + " drawing_url: " + getDrawingUrl() + ">";
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
}
