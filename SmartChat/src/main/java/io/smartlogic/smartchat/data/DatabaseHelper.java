package io.smartlogic.smartchat.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import io.smartlogic.smartchat.Constants;
import io.smartlogic.smartchat.data.migrations.InitialDatabase;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String TAG = "DatabaseHelper";
    private static final int SCHEMA = 1;

    public static final String DEFAULT_SORT_ORDER = "id";

    public DatabaseHelper(Context context) {
        super(context, Constants.DATABASE_NAME, null, SCHEMA);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        try {
            db.beginTransaction();

            new InitialDatabase(db).up();

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
            }

            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

}
