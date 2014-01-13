package io.smartlogic.smartchat.data.migrations;

import android.database.sqlite.SQLiteDatabase;

public abstract class Migration {
    private SQLiteDatabase mDatabase;

    public Migration(SQLiteDatabase database) {
        mDatabase = database;
    }

    public abstract void up();

    public abstract void down();

    public SQLiteDatabase getDatabase() {
        return mDatabase;
    }
}