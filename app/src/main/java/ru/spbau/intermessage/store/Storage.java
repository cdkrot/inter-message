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
    private final KeyValueStore store = new KeyValueStore(Intermessage.getAppContext());

    @Override
    public Union get(String key) {
        try (SQLiteDatabase sqldb = store.getReadableDatabase();
             Cursor c = sqldb.rawQuery("select * from " + store.tableName + " where id like '" + key + "'", null)) {
            UnionImpl result = new UnionImpl(key);
            if (c != null && c.moveToFirst()) {
                if (!c.isNull(1)) {
                    result.string = c.getString(1);
                } else if (!c.isNull(2)) {
                    result.v = c.getInt(2);
                } else if (!c.isNull(3)) {
                    result.data = c.getBlob(3);
                }
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

    @Override
    public IList getList(String key) {
        return new UnionList(key);
    }

    private class KeyValueStore extends SQLiteOpenHelper{
        private final String tableName = "keyValueStore";
        private final String CREATE_TABLE = "create table " + tableName
                + " (id text primary key,"
                + "string text,"
                + "number integer,"
                + "binary blob" + ");";

        KeyValueStore(Context context) {
            super(context, "keyValueStore", null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int i, int i1) {
            db.execSQL("DROP TABLE IF EXISTS " + tableName);
            onCreate(db);
        }
    }

    private class UnionImpl implements Union {

        String key;
        String string = null;
        byte[] data = null;
        Integer v = null;


        UnionImpl(String k) {
            key = k;
        }

        @Override
        public ObjectType getType() {
            if (string != null) {
                return ObjectType.STRING;
            }

            if (data != null) {
                return ObjectType.BYTE_ARRAY;
            }

            if (v != null) {
                return ObjectType.INTEGER;
            }

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
            if (was) {
                update();
            } else {
                add();
            }
        }

        @Override
        public void setData(byte[] d) {
            boolean was = getType() != ObjectType.NULL;
            clear();
            data = d;
            if (was) {
                update();
            } else {
                add();
            }
        }

        @Override
        public void setInt(int nt) {
            boolean was = getType() != ObjectType.NULL;
            clear();
            v = nt;
            if (was) {
                update();
            } else {
                add();
            }
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
                cv.put("binary", data);
                cv.put("number", v);

                sqldb.update(store.tableName, cv, "id like '" + key + "'", null);
            }
        }

        private void add() {
            try (SQLiteDatabase sqldb = store.getWritableDatabase()) {
                ContentValues cv = new ContentValues();
                cv.put("id", key);
                cv.put("string", string);
                cv.put("binary", data);
                cv.put("number", v);

                long k = sqldb.insert(store.tableName, null, cv);
                if (k == -1)
                    throw new NullPointerException("fdfgdggfddfgdgdfdfdfgdgffgddfgfdfddfdgdgdrhrekvhewviuweyiuewyveuyreuivryneiyvtverycireiyveriteiu omcwporewriuy reoimpvyynperiu yniptveyu");
            }
        }
    }

    private class ListStore extends SQLiteOpenHelper {
        private final String tableName = "listStore";
        private final String CREATE_TABLE = "create table " + tableName +
                " (id integer primary key autoincrement,"
                + "string text,"
                + "number integer,"
                + "binary blob" + ");";;


        ListStore(Context context, String key) {
            super(context, "listStore" + key, null, 1);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int i, int i1) {
            db.execSQL("DROP TABLE IF EXISTS " + tableName);
            onCreate(db);
        }

        private void delete(SQLiteDatabase db) {
            db.execSQL("DROP TABLE IF EXISTS " + tableName);
            db.execSQL(CREATE_TABLE);
        }
    }

    private class UnionList implements IList {

        final private ListStore store;

        UnionList(String key) {
            store = new ListStore(Intermessage.getAppContext(), key);
        }

        @Override
        public int size() {
            try (SQLiteDatabase sqldb = store.getReadableDatabase();
                 Cursor c = sqldb.rawQuery("select COALESCE(MAX(id), 0) from " + store.tableName, null)) {
                c.moveToFirst();
                return c.getInt(0);
            }
        }

        @Override
        public Union get(int key) {
            key++;
            try (SQLiteDatabase sqldb = store.getReadableDatabase();
                 Cursor c = sqldb.rawQuery("select * from " + store.tableName + " where id = " + key, null)) {
                ListUnion result = new ListUnion(key);
                if (c != null && c.moveToFirst()){
                    if (!c.isNull(1)) {
                        result.string = c.getString(1);
                    } else if (!c.isNull(2)) {
                        result.v = c.getInt(2);
                    } else if (!c.isNull(3)) {
                        result.data = c.getBlob(3);
                    }
                    return result;
                } else {
                    return result;
                }
            }
        }

        /**
         * Returns Unions with ids from key to key + cnt - 1
         */
        @Override
        public Union[] getBatch(int key, int cnt) {
            key++;
            List<Union> result = new ArrayList<>();

            try (SQLiteDatabase sqldb = store.getReadableDatabase();
                 Cursor c = sqldb.rawQuery("select * from " + store.tableName + " where id BETWEEN " + key + " AND " + (key + cnt - 1), null)) {
                if (c != null && c.moveToFirst()){
                    do {
                        ListUnion union = new ListUnion(key);
                        if (!c.isNull(1)) {
                            union.string = c.getString(1);
                        } else if (!c.isNull(2)) {
                            union.v = c.getInt(2);
                        } else if (!c.isNull(3)) {
                            union.data = c.getBlob(3);
                        }
                        result.add(union);
                    } while (c.moveToNext());
                } else {
                    return new Union[0];
                }
            }

            return (Union[]) result.toArray();
        }

        @Override
        public void push(int value) {
            add(value, null, null);
        }

        @Override
        public void push(String value) {
            add(null, value, null);
        }

        @Override
        public void push(byte[] value) {
            add(null, null, value);
        }

        private void add(Integer v, String s, byte[] b) {
            ContentValues cv = new ContentValues();
            cv.put("string", s);
            cv.put("binary", b);
            cv.put("number", v);
            try (SQLiteDatabase sqldb = store.getWritableDatabase()) {
                sqldb.insert(store.tableName, null, cv);
            }
        }

        @Override
        public void delete() {
            store.delete(store.getWritableDatabase());
        }

        private class ListUnion implements Union {

            int key;
            String string = null;
            byte[] data = null;
            Integer v = null;


            ListUnion(int k) {
                key = k;
            }

            @Override
            public ObjectType getType() {
                if (string != null) {
                    return ObjectType.STRING;
                }

                if (data != null) {
                    return ObjectType.BYTE_ARRAY;
                }

                if (v != null) {
                    return ObjectType.INTEGER;
                }
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
                if (was) {
                    update();
                } else {
                    add();
                }
            }

            @Override
            public void setData(byte[] d) {
                boolean was = getType() != ObjectType.NULL;
                clear();
                data = d;
                if (was) {
                    update();
                } else {
                    add();
                }
            }

            @Override
            public void setInt(int nt) {
                boolean was = getType() != ObjectType.NULL;
                clear();
                v = nt;
                if (was) {
                    update();
                } else {
                    add();
                }
            }

            @Override
            public void setNull() {
                throw new UnsupportedOperationException();
            }

            private void clear() {
                string = null;
                v = null;
                data = null;
            }

            private void update() {
                try (SQLiteDatabase sqldb = store.getWritableDatabase()) {
                    ContentValues cv = new ContentValues();
                    cv.put("string", string);
                    cv.put("binary", data);
                    cv.put("number", v);

                    sqldb.update(store.tableName, cv, "id = " + key, null);
                }
            }

            private void add() {
                throw new UnsupportedOperationException();
            }
        }
    }
}
