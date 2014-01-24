package io.smartlogic.smartchat.data.migrations;

import android.database.sqlite.SQLiteDatabase;

public class AddExpireInToNotifications extends Migration {
    public AddExpireInToNotifications(SQLiteDatabase database) {
        super(database);
    }

    @Override
    public void up() {
        getDatabase().execSQL("ALTER TABLE notifications ADD COLUMN expire_in INTEGER;");
    }

    @Override
    public void down() {
        // Cannot remove columns
    }
}
