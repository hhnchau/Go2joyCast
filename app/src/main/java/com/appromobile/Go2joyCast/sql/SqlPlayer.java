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
 * Created by appro on 11/12/2017.
 */

public class SqlPlayer extends SQLiteOpenHelper {
    private static SqlPlayer Instance = null;

    public static SqlPlayer getInstance(Context context) {
        if (Instance == null) {
            Instance = new SqlPlayer(context);
        }
        return Instance;
    }

    private static final String DATABASE_NAME = "Player";
    private static final int DATABASE_VERSION = 1;

    private final static String TABLE = "tbl_player";

    private final static String FIELD_ID = "id";
    private final static String FIELD_NAME = "name";

    private SqlPlayer(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_CONTACTS_TABLE =
                "CREATE TABLE " + TABLE + "(" + FIELD_ID + " INTEGER PRIMARY KEY," + FIELD_NAME + " TEXT" + ")";
        db.execSQL(CREATE_CONTACTS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    public void insert(List<String> player) {
        delete();
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values;
        for (int i = 0; i < player.size(); i++) {
            values = new ContentValues();
            values.put(FIELD_NAME, player.get(i));
            db.insert(TABLE, null, values);
        }

        db.close();

    }

    private void delete(){
        SQLiteDatabase db = this.getWritableDatabase();
        db.execSQL("delete from "+ TABLE);
        db.close();
    }

    public List<String> selectAll() {
        List<String> list = new ArrayList<>();

        String selectQuery = "SELECT " + FIELD_NAME + " FROM " + TABLE;

        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(selectQuery, null);

            if (cursor.moveToFirst()) {
                do {
                    list.add(cursor.getString(0));
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

}
