package ru.spbau.intermessage.store;

import android.content.ContentValues;
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
        try (SQLiteDatabase sqldb = store.getReadableDatabase();
             Cursor c = sqldb.rawQuery("select * from " + store.tableName + " where id like '" + key + "'", null)) {
            ObjectImpl result = new ObjectImpl(key);
            if (c != null && c.moveToFirst()){
                if (!c.isNull(1))
                    result.string = c.getString(1);
                else if (!c.isNull(2))
                    result.v = c.getInt(2);
                else if (!c.isNull(3))
                    result.data = c.getBlob(3);
                return result;
            } else
                return result;
        }
    }

    @Override
    public List<String> getMatching(String group) {
        List<String> result = new ArrayList<>();

        try (SQLiteDatabase sqldb = store.getReadableDatabase();
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
            onCreate(db);
        }
    }

    private class ObjectImpl implements IStorage.IObject {


        String key;
        String string = null;
        byte[] data = null;
        Integer v = null;


        ObjectImpl(String k) {
            key = k;
        }

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
            boolean was = getType() != ObjectType.NULL;
            clear();
            string = str;
            if (was)
                update();
            else
                add();
        }

        @Override
        public void setData(byte[] d) {
            boolean was = getType() != ObjectType.NULL;
            clear();
            data = d;
            if (was)
                update();
            else
                add();
        }

        @Override
        public void setInt(int nt) {
            boolean was = getType() != ObjectType.NULL;
            clear();
            v = nt;
            if (was)
                update();
            else
                add();
        }

        @Override
        public void setNull() {
            if (getType() != ObjectType.NULL) {
                clear();
                try (SQLiteDatabase sqldb = store.getWritableDatabase()) {
                    sqldb.delete(store.tableName, "id like '" + key + "'", null);
                }
            }
        }

        private void clear() {
            string = null;
            v = null;
            data = null;
        }

        private void update() {
            try (SQLiteDatabase sqldb = store.getWritableDatabase()) {
                ContentValues cv = new ContentValues();
                cv.put("id", key);
                cv.put("string", string);
                cv.put("data", data);
                cv.put("number", v);

                sqldb.update(store.tableName, cv, "id like '" + key + "'", null);
            }
        }

        private void add() {
            try (SQLiteDatabase sqldb = store.getWritableDatabase()) {
                ContentValues cv = new ContentValues();
                cv.put("id", key);
                cv.put("string", string);
                cv.put("data", data);
                cv.put("number", v);

                sqldb.insert(store.tableName, null, cv);
            }
        }
    }
}
