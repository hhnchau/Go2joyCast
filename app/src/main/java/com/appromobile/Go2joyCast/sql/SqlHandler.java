package com.appromobile.Go2joyCast.sql;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.appromobile.Go2joyCast.utils.Utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by appro on 11/09/2017.
 */
public class SqlHandler extends SQLiteOpenHelper {
    private static SqlHandler Instance = null;

    public static SqlHandler getInstance(Context context) {
        if (Instance == null) {
            Instance = new SqlHandler(context);
        }
        return Instance;
    }

    private static final String DATABASE_NAME = "Recentss";
    private static final int DATABASE_VERSION = 1;

    private final static String TABLE = "tbl_recent";

    private final static String FIELD_ID = "id";
    private final static String FIELD_SITE_URL = "siteUrl";
    private final static String FIELD_NONE = "none";

    private SqlHandler(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE =
                "CREATE TABLE " + TABLE + "(" + FIELD_ID + " INTEGER PRIMARY KEY," + FIELD_SITE_URL + " TEXT," + FIELD_NONE + " TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    public void insert(String siteUrl) {
        try {
            String temp = Utils.cutString(siteUrl);
            boolean exist = false;
            List<String> list = selectAll();
            if (list != null && list.size() > 0){
                for (int i = 0; i < list.size(); i++){
                    if (list.get(i).contains(temp)){
                        exist = true;
                    }
                }
            }

            if (!exist){
                SQLiteDatabase db = this.getWritableDatabase();
                ContentValues values = new ContentValues();

                values.put(FIELD_SITE_URL, siteUrl);
                values.put(FIELD_NONE, "");

                db.insert(TABLE, null, values);
                db.close();

                //Delete rows
                if (count() > 8) {
                    int id = selectOneTop();
                    delete(id);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String selectFromId(int id) {

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE, new String[]{FIELD_ID, FIELD_SITE_URL, FIELD_NONE}, FIELD_ID + "=?", new String[]{String.valueOf(id)}, null, null, null, null);

            if (cursor != null) {
                cursor.moveToFirst();
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return cursor.getString(1);
    }

    private int selectOneTop() {
        String selectQuery = "SELECT  * FROM " + TABLE + " LIMIT 1";
        int count = 0;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(selectQuery, null);

            if (cursor != null) {
                cursor.moveToFirst();
                count = Integer.parseInt(cursor.getString(0));
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return count;

    }

    public List<String> selectAll() {
        List<String> list = new ArrayList<>();

        String selectQuery = "SELECT  * FROM " + TABLE + " ORDER BY " + FIELD_ID + " DESC";

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    list.add(cursor.getString(1));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }

        return list;
    }

    public int update(int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(FIELD_ID, id);
        values.put(FIELD_NONE, "");

        return db.update(TABLE, values, FIELD_ID + " = ?", new String[]{String.valueOf(id)});
    }

    private void delete(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE, FIELD_ID + " = ?", new String[]{String.valueOf(id)});
        db.close();
    }

    public void deleteFromSn(int sn) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE, FIELD_SITE_URL + " = ?", new String[]{String.valueOf(sn)});
        db.close();
    }

    private int count() {
        String countQuery = "SELECT COUNT(*) FROM " + TABLE;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        int count = 0;
        try {
            cursor = db.rawQuery(countQuery, null);
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return count;
    }

    private int checkExist(String siteUrl) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        int count = 0;
        try {
            String query = "SELECT COUNT(*) FROM " + TABLE + " WHERE " + FIELD_SITE_URL + " LIKE ?";
            cursor = db.rawQuery(query, new String[]{"%" + siteUrl + "%"});
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
            if (db != null) {
                db.close();
            }
        }
        return count;
    }
}
