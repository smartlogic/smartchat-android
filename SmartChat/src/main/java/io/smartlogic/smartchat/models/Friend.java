package io.smartlogic.smartchat.models;

import android.content.ContentValues;
import android.database.Cursor;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Friend {
    @JsonIgnore
    private int _id;
    @JsonProperty("id")
    private Integer id;
    @JsonProperty("email")
    private String email;
    @JsonProperty("username")
    private String username;

    public static Friend fromCursor(Cursor cursor) {
        Friend friend = new Friend();

        if (cursor.getColumnIndex("_id") != -1) {
            friend.setDatabaseId(cursor.getInt(cursor.getColumnIndex("_id")));
        }

        if (cursor.getColumnIndex("id") != -1) {
            friend.setId(cursor.getInt(cursor.getColumnIndex("id")));
        }

        if (cursor.getColumnIndex("username") != -1) {
            friend.setUsername(cursor.getString(cursor.getColumnIndex("username")));
        }

        return friend;
    }

    @Override
    public String toString() {
        return "<Friend _id: " + getDatabaseId() + " id: " + getId() + " username: " + getUsername() + ">";
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public ContentValues getAttributes() {
        ContentValues cv = new ContentValues();

        cv.put("id", getId());
        cv.put("username", getUsername());

        return cv;
    }

    public int getDatabaseId() {
        return _id;
    }

    public void setDatabaseId(int databaseId) {
        this._id = databaseId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }
}
