package net.williamott.plasma.providers;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;

import net.williamott.plasma.databases.PodtasticDB;

public class PodtasticProvider extends ContentProvider {
    private UriMatcher uriMatcher;
    private PodtasticDB db;

    public static final String AUTHORITY ="net.williamott.plasma.provider";

    public static final int NO_MATCH = -1;
    public static final int ALL_USERS_URI = 0;
    public static final int SINGLE_USER_URI = 1;
    public static final int ALL_SUBSCRIPTIONS_URI = 2;
    public static final int SINGLE_SUBSCRIPTION_URI = 3;
    public static final int ALL_EPISODES_URI = 4;
    public static final int SINGLE_EPISODE_URI = 5;

    public PodtasticProvider() {
    }

    @Override
    public int delete(Uri uri, String where, String[] whereArgs) {
        int deleteCount;
        switch(uriMatcher.match(uri)) {
            case SINGLE_USER_URI:
                String UserId = uri.getLastPathSegment();
                String where2 = PodtasticDB.USER_ID + " = ?";
                String[] whereArgs2 = { UserId };
                deleteCount = db.deleteUser(where2, whereArgs2);
                return deleteCount;
            case ALL_USERS_URI:
                deleteCount = db.deleteUser(where, whereArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return deleteCount;
            case SINGLE_SUBSCRIPTION_URI:
                String SubscriptionId = uri.getLastPathSegment();
                String where3 = PodtasticDB.SUBSCRIPTION_ID + " = ?";
                String[] whereArgs3 = { SubscriptionId };
                deleteCount = db.deleteSubscription(where3, whereArgs3);
                return deleteCount;
            case ALL_SUBSCRIPTIONS_URI:
                deleteCount = db.deleteSubscription(where, whereArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return deleteCount;
            case SINGLE_EPISODE_URI:
                String EpisodeId = uri.getLastPathSegment();
                String where4 = PodtasticDB.EPISODE_ID + " = ?";
                String[] whereArgs4 = { EpisodeId };
                deleteCount = db.deleteEpisode(where4, whereArgs4);
                return deleteCount;
            case ALL_EPISODES_URI:
                deleteCount = db.deleteEpisode(where, whereArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return deleteCount;
            default:
                throw new UnsupportedOperationException("URI " + uri + " is not supported.");
        }
    }

    @Override
    public String getType(Uri uri) {
        switch(uriMatcher.match(uri)) {
            case ALL_USERS_URI:
                return "vnd.android.cursor.dir/vnd.net.williamott.plasma.provider.users";
            case SINGLE_USER_URI:
                return "vnd.android.cursor.item/vnd.onet.williamott.plasma.provider.users";
            case ALL_SUBSCRIPTIONS_URI:
                return "vnd.android.cursor.dir/vnd.net.williamott.plasma.provider.subscriptions";
            case SINGLE_SUBSCRIPTION_URI:
                return "vnd.android.cursor.item/vnd.onet.williamott.plasma.provider.subscriptions";
            case ALL_EPISODES_URI:
                return "vnd.android.cursor.dir/vnd.net.williamott.plasma.provider.episodes";
            case SINGLE_EPISODE_URI:
                return "vnd.android.cursor.item/vnd.onet.williamott.plasma.provider.episodes";
            default:
                throw new UnsupportedOperationException("URI " + uri + " is not supported.");
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        switch(uriMatcher.match(uri)) {
            case ALL_USERS_URI:
                long userInsertId = db.insertUser(values);
                getContext().getContentResolver().notifyChange(uri, null);
                return uri.buildUpon().appendEncodedPath(Long.toString(userInsertId)).build();
            case ALL_EPISODES_URI:
                long episodeInsertId = db.insertEpisode(values);
                getContext().getContentResolver().notifyChange(uri, null);
                return uri.buildUpon().appendEncodedPath(Long.toString(episodeInsertId)).build();
            case ALL_SUBSCRIPTIONS_URI:
                long subscriptionInsertId = db.insertSubscription(values);
                getContext().getContentResolver().notifyChange(uri, null);
                return uri.buildUpon().appendEncodedPath(Long.toString(subscriptionInsertId)).build();
            default:
                throw new UnsupportedOperationException("URI " + uri + " is not supported.");
        }
    }

    @Override
    public boolean onCreate() {
        db = new PodtasticDB(getContext());

        uriMatcher = new UriMatcher(NO_MATCH);
        uriMatcher.addURI(AUTHORITY, "users", ALL_USERS_URI);
        uriMatcher.addURI(AUTHORITY, "users/#", SINGLE_USER_URI);
        uriMatcher.addURI(AUTHORITY, "subscriptions", ALL_SUBSCRIPTIONS_URI);
        uriMatcher.addURI(AUTHORITY, "subscriptions/#", SINGLE_SUBSCRIPTION_URI);
        uriMatcher.addURI(AUTHORITY, "episodes", ALL_EPISODES_URI);
        uriMatcher.addURI(AUTHORITY, "episodes/#", SINGLE_EPISODE_URI);

        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] columns, String where,
                        String[] whereArgs, String orderBy) {
        switch(uriMatcher.match(uri)) {
            case ALL_USERS_URI:
                return db.queryUsers(columns, where, whereArgs, orderBy);
            case ALL_SUBSCRIPTIONS_URI:
                Cursor subscriptionCursor = db.querySubscriptions(columns, where, whereArgs, orderBy);
                subscriptionCursor.setNotificationUri(getContext().getContentResolver(), uri);
                return subscriptionCursor;
            case ALL_EPISODES_URI:
                Cursor episodesCursor = db.queryEpisodes(columns, where, whereArgs, orderBy);
                episodesCursor.setNotificationUri(getContext().getContentResolver(), uri);
                return episodesCursor;
            default:
                throw new UnsupportedOperationException("URI " + uri + " is not supported.");
        }
    }

    @Override
    public int update(Uri uri, ContentValues values, String where,
                      String[] whereArgs) {
        int updateCount;
        switch(uriMatcher.match(uri)) {
            case SINGLE_USER_URI:
                String UserId = uri.getLastPathSegment();
                String where2 = PodtasticDB.USER_ID + " = ?";
                String[] whereArgs2 = { UserId };
                updateCount = db.updateUser(values, where2, whereArgs2);
                return updateCount;
            case ALL_USERS_URI:
                updateCount = db.updateUser(values, where, whereArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return updateCount;
            case SINGLE_SUBSCRIPTION_URI:
                String SubscriptionId = uri.getLastPathSegment();
                String where3 = PodtasticDB.SUBSCRIPTION_ID + " = ?";
                String[] whereArgs3 = { SubscriptionId };
                updateCount = db.updateSubscription(values, where3, whereArgs3);
                Uri subscriptionUri = ContentUris.withAppendedId(uri, updateCount);
                getContext().getContentResolver().notifyChange(subscriptionUri, null);
                return updateCount;
            case ALL_SUBSCRIPTIONS_URI:
                updateCount = db.updateSubscription(values, where, whereArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return updateCount;
            case SINGLE_EPISODE_URI:
                String EpisodeId = uri.getLastPathSegment();
                String where4 = PodtasticDB.EPISODE_ID + " = ?";
                String[] whereArgs4 = { EpisodeId };
                updateCount = db.updateEpisode(values, where4, whereArgs4);
                Uri episodeUri = ContentUris.withAppendedId(uri, updateCount);
                getContext().getContentResolver().notifyChange(episodeUri, null);
                return updateCount;
            case ALL_EPISODES_URI:
                updateCount = db.updateEpisode(values, where, whereArgs);
                getContext().getContentResolver().notifyChange(uri, null);
                return updateCount;
            default:
                throw new UnsupportedOperationException("URI " + uri + " is not supported.");
        }
    }
}
