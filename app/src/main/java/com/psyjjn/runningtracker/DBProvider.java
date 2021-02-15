package com.psyjjn.runningtracker;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

public class DBProvider extends ContentProvider {
    /**
     * Declare variables
     */
    DBHelper dbHelper = null;
    /**
     * Add URIs based on table name
     */
    private static final UriMatcher uriMatcher;
    static {
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(DBProviderContract.AUTHORITY, "activities", 1);
        uriMatcher.addURI(DBProviderContract.AUTHORITY, "*", 4);
    }

    /**
     * New instance of DBHelper
     * @return
     */
    @Override
    public boolean onCreate() {
        this.dbHelper = new DBHelper(this.getContext());
        return true;
    }

    @Override
    public String getType(Uri uri) {
        if (uri.getLastPathSegment()==null) {
            return "vnd.android.cursor.dir/DBProvider.data.text";
        } else {
            return "vnd.android.cursor.item/DBProvider.data.text";
        }
    }

    /**
     * Override query for database
     * @param uri
     * @param projection
     * @param selection
     * @param selectionArgs
     * @param sortOrder
     * @return
     */
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        Log.d("g53mdp", uri.toString() + " " + uriMatcher.match(uri));
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        switch(uriMatcher.match(uri)) {
            case 1:
                return db.query("activities", projection, selection, selectionArgs, null, null, sortOrder);
            default:
                return null;
        }
    }

    /**
     * Override insert for database
     * @param uri
     * @param values
     * @return
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String tableName;

        switch(uriMatcher.match(uri)) {
            case 1:
                tableName = "activities";
                break;
            default:
                tableName = null;
                break;
        }

        long id = db.insert(tableName, null, values);
        db.close();
        Uri nu = ContentUris.withAppendedId(uri, id);
        Log.d("g53mdp", nu.toString());
        getContext().getContentResolver().notifyChange(nu, null);
        return nu;
    }

    /**
     * Override update for database
     * @param uri
     * @param values
     * @param selection
     * @param selectionArgs
     * @return
     */
    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        switch(uriMatcher.match(uri)) {
            case 1:
                return db.update("activities", values, selection, selectionArgs);
            default:
                return -1;
        }
    }

    /**
     * Override delete for database
     * @param uri
     * @param selection
     * @param selectionArgs
     * @return
     */
    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        switch(uriMatcher.match(uri)) {
            case 1:
                return db.delete("activities", selection, selectionArgs);
            default:
                return -1;
        }
    }
}
