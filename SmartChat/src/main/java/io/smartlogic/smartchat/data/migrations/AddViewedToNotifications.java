package io.smartlogic.smartchat.data.migrations;

import android.database.sqlite.SQLiteDatabase;

public class AddViewedToNotifications extends Migration {
    public AddViewedToNotifications(SQLiteDatabase database) {
        super(database);
    }

    @Override
    public void up() {
        getDatabase().execSQL("ALTER TABLE notifications ADD COLUMN viewed INTEGER;");
    }

    @Override
    public void down() {
        // Can't remove columns
    }
}
