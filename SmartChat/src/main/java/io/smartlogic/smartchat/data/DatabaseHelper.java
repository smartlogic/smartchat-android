package io.smartlogic.smartchat.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import io.smartlogic.smartchat.Constants;
import io.smartlogic.smartchat.data.migrations.AddExpireInToNotifications;
import io.smartlogic.smartchat.data.migrations.AddNotifications;
import io.smartlogic.smartchat.data.migrations.AddUuidToNotifications;
import io.smartlogic.smartchat.data.migrations.AddViewedToNotifications;
import io.smartlogic.smartchat.data.migrations.InitialDatabase;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int SCHEMA = 5;

    public static final String DEFAULT_SORT_ORDER = "id";

    public DatabaseHelper(Context context) {
        super(context, Constants.DATABASE_NAME, null, SCHEMA);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.beginTransaction();

            new InitialDatabase(db).up();
            new AddNotifications(db).up();
            new AddViewedToNotifications(db).up();
            new AddExpireInToNotifications(db).up();
            new AddUuidToNotifications(db).up();

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        try {
            db.beginTransaction();

            switch (oldVersion) {
                case 1: // => 2
                    new AddNotifications(db).up();
                case 2: // => 3
                    new AddViewedToNotifications(db).up();
                case 3: // => 4
                    new AddExpireInToNotifications(db).up();
                case 4: // => 5
                    new AddUuidToNotifications(db).up();
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

}
