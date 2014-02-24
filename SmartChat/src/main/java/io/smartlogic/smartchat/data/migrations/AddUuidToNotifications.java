package io.smartlogic.smartchat.data.migrations;

import android.database.sqlite.SQLiteDatabase;

public class AddUuidToNotifications extends Migration {
    public AddUuidToNotifications(SQLiteDatabase database) {
        super(database);
    }

    @Override
    public void up() {
        getDatabase().execSQL("ALTER TABLE notifications ADD COLUMN uuid TEXT;");
    }

    @Override
    public void down() {
        // Cannot remove columns
    }
}
