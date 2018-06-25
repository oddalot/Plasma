package net.williamott.plasma.databases;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by oddalot on 10/13/17.
 */

public class PodtasticDB {
    // database constants
    public static final String DB_NAME = "plasma.db";
    public static final int    DB_VERSION = 1;

    private static class DBHelper extends SQLiteOpenHelper {
        public DBHelper(Context context, String name,
                        SQLiteDatabase.CursorFactory factory, int version) {
            super(context, name, factory, version);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL(CREATE_USER_TABLE);
            db.execSQL(CREATE_SUBSCRIPTION_TABLE);
            db.execSQL(CREATE_EPISODE_TABLE);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            db.execSQL(PodtasticDB.DROP_USER_TABLE);
            db.execSQL(PodtasticDB.DROP_SUBSCRIPTION_TABLE);
            db.execSQL(PodtasticDB.DROP_EPISODE_TABLE);
            onCreate(db);
        }

        @Override
        public void onConfigure(SQLiteDatabase db) {
            db.setForeignKeyConstraintsEnabled(true);
        }
    }

    // database object and database helper object
    private SQLiteDatabase db;
    private DBHelper dbHelper;

    // constructor
    public PodtasticDB (Context context) {
        dbHelper = new DBHelper(context, DB_NAME, null, DB_VERSION);
    }

    // private methods
    private void openReadableDB() {
        db = dbHelper.getReadableDatabase();
    }

    private void openWritableDB() {
        db = dbHelper.getWritableDatabase();
    }

    private void closeDB() {
        if (db != null) {
            db.close();
        }
    }

    // user table constants
    public static final String USER_TABLE = "user";

    public static final String USER_ID = "_id";
    public static final int    USER_ID_COL = 0;

    public static final String USER_EMAIL = "email";
    public static final int    USER_EMAIL_COL = 1;

    public static final String USER_UID = "uid";
    public static final int    USER_UID_COL = 2;

    // CREATE and DROP TABLE statements
    public static final String CREATE_USER_TABLE =
            "CREATE TABLE "         + USER_TABLE + " (" +
                    USER_ID                     + " INTEGER PRIMARY KEY, " +
                    USER_EMAIL                 + " TEXT   NOT NULL  UNIQUE, " +
                    USER_UID   + " TEXT   NOT NULL  UNIQUE);";

    public static final String DROP_USER_TABLE =
            "DROP TABLE IF EXISTS " + USER_TABLE;

    public int deleteUser(int userNumber) {
        String where = USER_ID + "= ?";
        String[] whereArgs = { String.valueOf(userNumber) };

        this.openWritableDB();
        int rowCount = db.delete(USER_TABLE, where, whereArgs);

        this.closeDB();

        return rowCount;
    }

    public Cursor getUserCursor() {
        this.openReadableDB();
        return db.query(USER_TABLE, null, null, null, null, null, null);
    }

    public Cursor queryUsers(String[] columns, String where, String[] whereArgs, String orderBy) {
        this.openReadableDB();
        return db.query(USER_TABLE, columns, where, whereArgs, null, null, orderBy);
    }

    public long insertUser(ContentValues values) {
        this.openWritableDB();
        return db.insert(USER_TABLE, null, values);
    }

    public int updateUser(ContentValues values, String where, String[] whereArgs) {
        this.openWritableDB();
        return db.update(USER_TABLE, values, where, whereArgs);
    }

    public int deleteUser(String where, String[] whereArgs) {
        this.openWritableDB();
        return db.delete(USER_TABLE, where, whereArgs);
    }

    // subscription table constants
    public static final String SUBSCRIPTION_TABLE = "subscription";

    public static final String SUBSCRIPTION_ID = "_id";
    public static final int    SUBSCRIPTION_ID_COL = 0;

    public static final String SUBSCRIPTION_TRACK_ID = "track_id";
    public static final int    SUBSCRIPTION_TRACK_ID_COL = 1;

    public static final String SUBSCRIPTION_TRACK_NAME = "track_name";
    public static final int    SUBSCRIPTION_TRACK_NAME_COL = 2;

    public static final String SUBSCRIPTION_RELEASE_DATE = "release_date";
    public static final int    SUBSCRIPTION_RELEASE_DATE_COL = 3;

    public static final String SUBSCRIPTION_ARTWORK_URL = "artwork_url";
    public static final int    SUBSCRIPTION_ARTWORK_URL_COL = 4;

    public static final String SUBSCRIPTION_ARTIST_NAME = "artist_name";
    public static final int    SUBSCRIPTION_ARTIST_NAME_COL = 5;

    public static final String SUBSCRIPTION_FEED_URL = "feed_url";
    public static final int    SUBSCRIPTION_FEED_URL_COL = 6;

    public static final String SUBSCRIPTION_USER_ID = "user_id";
    public static final int    SUBSCRIPTION_USER_ID_COL = 7;

    public static final String SUBSCRIPTION_NEW_EPISODE_COUNT = "new_episode_count";
    public static final int    SUBSCRIPTION_NEW_EPISODE_COUNT_COL = 8;


    // CREATE and DROP TABLE statements
    public static final String CREATE_SUBSCRIPTION_TABLE =
            "CREATE TABLE "         + SUBSCRIPTION_TABLE + " (" +
                    SUBSCRIPTION_ID                     + " INTEGER PRIMARY KEY, " +
                    SUBSCRIPTION_TRACK_ID               + " INTEGER NOT NULL UNIQUE, " +
                    SUBSCRIPTION_TRACK_NAME             + " TEXT NOT NULL, " +
                    SUBSCRIPTION_RELEASE_DATE           + " TEXT, " +
                    SUBSCRIPTION_ARTWORK_URL            + " TEXT, " +
                    SUBSCRIPTION_ARTIST_NAME            + " TEXT, " +
                    SUBSCRIPTION_FEED_URL               + " TEXT   NOT NULL  UNIQUE, " +
                    SUBSCRIPTION_USER_ID                + " INTEGER NOT NULL, " +
                    SUBSCRIPTION_NEW_EPISODE_COUNT      + " INTEGER DEFAULT 0, " +
                    " FOREIGN KEY ("+SUBSCRIPTION_USER_ID+") REFERENCES "+USER_TABLE+"("+USER_ID+") ON DELETE CASCADE);";

    public static final String DROP_SUBSCRIPTION_TABLE =
            "DROP TABLE IF EXISTS " + SUBSCRIPTION_TABLE;

    public int deleteSubscription(int subscriptionNumber) {
        String where = SUBSCRIPTION_ID + "= ?";
        String[] whereArgs = { String.valueOf(subscriptionNumber) };

        this.openWritableDB();
        int rowCount = db.delete(SUBSCRIPTION_TABLE, where, whereArgs);

        this.closeDB();

        return rowCount;
    }

    public Cursor getSubscriptionCursor() {
        this.openReadableDB();
        return db.query(SUBSCRIPTION_TABLE, null, null, null, null, null, null);
    }

    public Cursor querySubscriptions(String[] columns, String where, String[] whereArgs, String orderBy) {
        this.openReadableDB();
        return db.query(SUBSCRIPTION_TABLE, columns, where, whereArgs, null, null, orderBy);
    }

    public long insertSubscription(ContentValues values) {
        this.openWritableDB();
        return db.insert(SUBSCRIPTION_TABLE, null, values);
    }

    public int updateSubscription(ContentValues values, String where, String[] whereArgs) {
        this.openWritableDB();
        return db.update(SUBSCRIPTION_TABLE, values, where, whereArgs);
    }

    public int deleteSubscription(String where, String[] whereArgs) {
        this.openWritableDB();
        return db.delete(SUBSCRIPTION_TABLE, where, whereArgs);
    }

    // episode table constants
    public static final String EPISODE_TABLE = "episode";

    public static final String EPISODE_ID = "_id";
    public static final int    EPISODE_ID_COL = 0;

    public static final String EPISODE_URL = "url";
    public static final int    EPISODE_URL_COL = 1;

    public static final String EPISODE_AUTHOR = "author";
    public static final int    EPISODE_AUTHOR_COL = 2;

    public static final String EPISODE_DESCRIPTION = "description";
    public static final int    EPISODE_DESCRIPTION_COL = 3;

    public static final String EPISODE_GUID = "guid";
    public static final int    EPISODE_GUID_COL = 4;

    public static final String EPISODE_PUB_DATE = "pub_date";
    public static final int    EPISODE_PUB_DATE_COL = 5;

    public static final String EPISODE_TITLE = "title";
    public static final int    EPISODE_TITLE_COL = 6;

    public static final String EPISODE_ALBUM_ART_URL = "album_art_url";
    public static final int    EPISODE_ALBUM_ART_URL_COL = 7;

    public static final String EPISODE_SUBSCRIPTION_ID = "subscription_id";
    public static final int    EPISODE_SUBSCRIPTION_ID_COL = 8;

    public static final String EPISODE_SUBSCRIPTION_TITLE = "subscription_title";
    public static final int    EPISODE_SUBSCRIPTION_TITLE_COL = 9;

    public static final String EPISODE_CURRENT_POSITION = "current_position";
    public static final int    EPISODE_CURRENT_POSITION_COL = 10;

    public static final String EPISODE_USER_ID = "user_id";
    public static final int    EPISODE_USER_ID_COL = 11;

    public static final String EPISODE_IS_NEW = "is_new";
    public static final int    EPISODE_IS_NEW_COL = 12;

    public static final String EPISODE_DOWNLOAD_LOCATION = "download_location";
    public static final int    EPISODE_DOWNLOAD_LOCATION_COL = 13;

    public static final String EPISODE_SUBSCRIPTION_TRACK_TITLE = "subscription_track_title";
    public static final int    EPISODE_SUBSCRIPTION_TRACK_TITLE_COL = 14;


    // CREATE and DROP TABLE statements
    public static final String CREATE_EPISODE_TABLE =
            "CREATE TABLE "         + EPISODE_TABLE + " (" +
                    EPISODE_ID                          + " INTEGER PRIMARY KEY, " +
                    EPISODE_URL                         + " TEXT, " +
                    EPISODE_AUTHOR                      + " TEXT, " +
                    EPISODE_DESCRIPTION                 + " TEXT, " +
                    EPISODE_GUID                        + " TEXT, " +
                    EPISODE_PUB_DATE                    + " TEXT, " +
                    EPISODE_TITLE                       + " TEXT, " +
                    EPISODE_ALBUM_ART_URL               + " TEXT, " +
                    EPISODE_SUBSCRIPTION_ID             + " INTEGER NOT NULL, " +
                    EPISODE_SUBSCRIPTION_TITLE          + " TEXT, " +
                    EPISODE_CURRENT_POSITION            + " INTEGER   DEFAULT 0, " +
                    EPISODE_USER_ID                     + " INTEGER NOT NULL, " +
                    EPISODE_IS_NEW                      + " INTEGER DEFAULT 0, " +
                    EPISODE_DOWNLOAD_LOCATION           + " TEXT, " +
                    EPISODE_SUBSCRIPTION_TRACK_TITLE    + " TEXT, " +
                    " FOREIGN KEY ("+EPISODE_USER_ID+") REFERENCES "+USER_TABLE+"("+USER_ID+"), " +
                    " FOREIGN KEY ("+EPISODE_SUBSCRIPTION_ID+") REFERENCES "+SUBSCRIPTION_TABLE+"("+SUBSCRIPTION_ID+") ON DELETE CASCADE);";

    public static final String DROP_EPISODE_TABLE =
            "DROP TABLE IF EXISTS " + EPISODE_TABLE;

    public int deleteEpisode(int episodeNumber) {
        String where = EPISODE_ID + "= ?";
        String[] whereArgs = { String.valueOf(episodeNumber) };

        this.openWritableDB();
        int rowCount = db.delete(EPISODE_TABLE, where, whereArgs);

        this.closeDB();

        return rowCount;
    }

    public Cursor getEpisodeCursor() {
        this.openReadableDB();
        return db.query(EPISODE_TABLE, null, null, null, null, null, null);
    }

    public Cursor queryEpisodes(String[] columns, String where, String[] whereArgs, String orderBy) {
        this.openReadableDB();
        return db.query(EPISODE_TABLE, columns, where, whereArgs, null, null, orderBy);
    }

    public long insertEpisode(ContentValues values) {
        this.openWritableDB();
        return db.insert(EPISODE_TABLE, null, values);
    }

    public int updateEpisode(ContentValues values, String where, String[] whereArgs) {
        this.openWritableDB();
        return db.update(EPISODE_TABLE, values, where, whereArgs);
    }

    public int deleteEpisode(String where, String[] whereArgs) {
        this.openWritableDB();
        return db.delete(EPISODE_TABLE, where, whereArgs);
    }
}
