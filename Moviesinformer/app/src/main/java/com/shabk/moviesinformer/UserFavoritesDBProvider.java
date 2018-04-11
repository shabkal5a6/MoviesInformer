package com.shabk.moviesinformer;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import java.util.HashMap;


/**
 * Created by abdullah on 28-Mar-18.
 * reference :::
 * https://www.tutorialspoint.com/android/android_content_providers.htm
 */

public class UserFavoritesDBProvider extends ContentProvider {
    static final String PROVIDER_NAME = "com.shabk.Provider";
    static final String URL = "content://" + PROVIDER_NAME + "/movies";
    private SQLiteDatabase db;
    static final Uri CONTENT_URI = Uri.parse(URL);

    //static final String NAME = "title";
    //static final String GRADE = "overview";
    private static HashMap<String, String> MOVIES_PROJECTION_MAP;
   // static final String _ID = "id";
    static final int MOVIES = 1;
    static final int MOVIES_ID = 2;
    static final UriMatcher uriMatcher;
    static{
        uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
        uriMatcher.addURI(PROVIDER_NAME, "movies", MOVIES);
        uriMatcher.addURI(PROVIDER_NAME, "movies/#", MOVIES_ID);
    }
    //dp table stuff
    private static final String DATABASE_NAME = "Favorites_DB";
    private static final int DATABASE_VERSION = 6;
    private static final String Movies_Table_Name = "movies";
     static final String ID = "id";
    static final String MOVIE_ID = "movie_id";
     static final String TITLE = "title";
     static final String DESCRIBTION = "overview";
     static final String IMAGE = "poster_path";
     static final String RATE = "vote_average";
     static final String DATE = "release_date";

    private static final String CREATE_CONTACTS_TABLE = "CREATE TABLE " + Movies_Table_Name + "("
            + ID + " INTEGER PRIMARY KEY, "
            + MOVIE_ID + " INTEGER,"
            + TITLE + " TEXT, "
            + DESCRIBTION + " TEXT, "
            + IMAGE + " TEXT, "
            + RATE + " TEXT, "
            + DATE + " TEXT "
            +  ")";

    private static class DBHandler extends SQLiteOpenHelper{
        DBHandler(Context context){
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_CONTACTS_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL("DROP TABLE IF EXISTS " +  Movies_Table_Name);
            onCreate(db);
        }
    }





    @Override
    public boolean onCreate() {
        Context context = getContext();
        DBHandler dbHandler = new DBHandler(context);
        db = dbHandler.getWritableDatabase();

        return (db == null)? false:true;
    }


    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                        @Nullable String selection, @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {
        //DeleteAllFavs();
            SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
            qb.setTables(Movies_Table_Name);

            switch (uriMatcher.match(uri)) {
                case MOVIES:
                    qb.setProjectionMap(MOVIES_PROJECTION_MAP);
                    break;

                case MOVIES_ID:
                    qb.appendWhere( ID + "=" + uri.getPathSegments().get(1));
                    break;

                default:
            }

            if (sortOrder == null || sortOrder == ""){
                /**
                 * By default sort on student names
                 */
                sortOrder = TITLE;
            }

            Cursor c = qb.query(db,	projection,	selection,
                    selectionArgs,null, null, sortOrder);
            /**
             * register to watch a content URI for changes
             */
            c.setNotificationUri(getContext().getContentResolver(), uri);
            return c;
    }


    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)){
            /**
             * Get all student records
             */
            case MOVIES:
                return "vnd.android.cursor.dir/vnd.shabk.movies";
            /**
             * Get a particular student
             */
            case MOVIES_ID:
                return "vnd.android.cursor.item/vnd.shabk.movies";
            default:
                throw new IllegalArgumentException("Unsupported URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        //put in values
        //DeleteAllFavs();
        /**
         * Add a new favorite movie
         */
        long rowID = db.insert(Movies_Table_Name, "", values);

        /**
         * If record is added successfully
         */
        if (rowID > 0) {
            Uri _uri = ContentUris.withAppendedId(CONTENT_URI, rowID);
            getContext().getContentResolver().notifyChange(_uri, null);
            return _uri;
        }

        throw new SQLException("Failed to add a record into " + uri);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        int count = 0;
        switch (uriMatcher.match(uri)){
            case MOVIES:
                count = db.delete(Movies_Table_Name, selection, selectionArgs);
                break;

            case MOVIES_ID:
                String id = uri.getPathSegments().get(1);
                count = db.delete( Movies_Table_Name, ID +  " = " + id +
                                (!TextUtils.isEmpty(selection) ? " AND ("
                                        + selection + ')' : ""), selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return count;
    }

    public void DeleteAllFavs(){
       // SQLiteDatabase db = this.getWritableDatabase();
        String CREATE_CONTACTS_TABLE = "DELETE FROM "+ Movies_Table_Name;
        db.execSQL(CREATE_CONTACTS_TABLE);

    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {

            int count = 0;
            switch (uriMatcher.match(uri)) {
                case MOVIES:
                    count = db.update(Movies_Table_Name, values, selection, selectionArgs);
                    break;

                case MOVIES_ID:
                    count = db.update(Movies_Table_Name, values,
                            ID + " = " + uri.getPathSegments().get(1) +
                                    (!TextUtils.isEmpty(selection) ? " AND ("
                                            +selection + ')' : ""), selectionArgs);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown URI " + uri );
            }

            getContext().getContentResolver().notifyChange(uri, null);
            return count;
    }
}
