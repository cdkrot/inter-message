package ru.spbau.intermessage.store;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

import ru.spbau.intermessage.Intermessage;

public class Storage implements IStorage {
    private final keyValueStore store = new keyValueStore(Intermessage.getAppContext());

    @Override
    public IObject get(String key) {
        try (SQLiteDatabase sqldb = store.getWritableDatabase();
             Cursor c = sqldb.rawQuery("select * from " + store.tableName + " where id like '" + key + "'", null)) {
            if (c != null && c.moveToFirst()){
                ObjectImpl result = new ObjectImpl();
                if (!c.isNull(1))
                    result.string = c.getString(1);
                if (!c.isNull(2))
                    result.v = c.getInt(2);
                if (!c.isNull(3))
                    result.data = c.getBlob(3);
                return result;
            } else
                return new ObjectImpl();
        }
    }

    @Override
    public List<String> getMatching(String group) {
        List<String> result = new ArrayList<>();

        try (SQLiteDatabase sqldb = store.getWritableDatabase();
             Cursor c = sqldb.rawQuery("select id from " + store.tableName + " where id like '" + group + "%'", null)) {
            if (c != null && c.moveToFirst()) {
                do {
                    result.add(c.getString(0));
                } while (c.moveToNext());
            }
        }
        return result;
    }

    private class keyValueStore extends SQLiteOpenHelper{
        private final String tableName = "keyValueStore";
        private final String CREATE_TABLE = "create table " + tableName
                        + " (id text primary key,"
                        + "string text,"
                        + "number integer,"
                        + "binary blob" + ");";
        private SQLiteDatabase db;

        keyValueStore(Context context) {
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

    private class ObjectImpl implements IStorage.IObject {

        String string = null;
        byte[] data = null;
        Integer v = null;

        @Override
        public ObjectType getType() {
            if (string != null)
                return ObjectType.STRING;
            if (data != null)
                return ObjectType.BYTE_ARRAY;
            if (v != null)
                return ObjectType.INTEGER;
            return ObjectType.NULL;
        }

        @Override
        public String getString() {
            return string;
        }

        @Override
        public byte[] getData() {
            return data;
        }

        @Override
        public int getInt() {
            return v;
        }

        @Override
        public void setString(String str) {
            string = str;
        }

        @Override
        public void setData(byte[] d) {
            data = d;
        }

        @Override
        public void setInt(int nt) {
            v = nt;
        }
    }
}
