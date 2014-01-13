package io.smartlogic.smartchat.data.migrations;

import android.database.sqlite.SQLiteDatabase;

public class InitialDatabase extends Migration {
    public InitialDatabase(SQLiteDatabase database) {
        super(database);
    }

    @Override
    public void up() {
        getDatabase().execSQL("CREATE TABLE friends (" +
                "_id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "id INTEGER," +
                "username TEXT);");
    }

    @Override
    public void down() {
        getDatabase().execSQL("DROP TABLE friends");
    }
}
