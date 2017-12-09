package ru.spbau.intermessage.store;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class Storage extends SQLiteOpenHelper {

    private final String tableName = "keyValueStore";
    private final String CREATE_TABLE = "";
    private SQLiteDatabase db;

    public Storage(Context context) {
        super(context, "storage", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
            db.execSQL("DROP TABLE IF EXISTS " + tableName);
    }
}
