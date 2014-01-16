package io.smartlogic.smartchat.data.migrations;

import android.database.sqlite.SQLiteDatabase;

public class AddNotifications extends Migration {
    public AddNotifications(SQLiteDatabase database) {
        super(database);
    }

    @Override
    public void up() {
        getDatabase().execSQL(
                "CREATE TABLE notifications(" +
                        "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "creator_id INTEGER," +
                        "creator_username TEXT," +
                        "file_url TEXT," +
                        "drawing_url TEXT" +
                        ");"
        );
    }

    @Override
    public void down() {
        getDatabase().execSQL("DROP TABLE notifications");
    }
}
