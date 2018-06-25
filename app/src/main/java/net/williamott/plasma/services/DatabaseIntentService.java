package net.williamott.plasma.services;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Intent;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.util.Log;

import net.williamott.plasma.databases.PodtasticDB;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class DatabaseIntentService extends IntentService {
    // TODO: Rename actions, choose action names that describe tasks that this
    // IntentService can perform, e.g. ACTION_FETCH_NEW_ITEMS
    private static final String ACTION_INSERT_SUBSCRIPTION = "net.williamott.plasma.action.INSERT_SUBSCRIPTION";
    private static final String ACTION_INSERT_SUBSCRIPTION_WITH_CALLBACK = "net.williamott.plasma.action.INSERT_SUBSCRIPTION_WITH_CALLBACK";
    private static final String ACTION_INSERT_EPISODE = "net.williamott.plasma.action.INSERT_EPISODE";
    private static final String ACTION_UPDATE_EPISODES_IS_NEW = "net.williamott.plasma.action.UPDATE_EPISODES_IS_NEW";
    private static final String ACTION_UPDATE_SUBSCRIPTION_NEW_EPISODE_COUNT = "net.williamott.plasma.action.UPDATE_SUBSCRIPTION_NEW_EPISODE_COUNT";
    private static final String ACTION_UPDATE_EPISODE_POSITION = "net.williamott.plasma.action.ACTION_UPDATE_EPISODE_POSITION";
    private static final String ACTION_UPDATE_EPISODE_DOWNLOAD_LOCATION = "net.williamott.plasma.action.ACTION_UPDATE_EPISODE_DOWNLOAD_LOCATION";
    private static final String ACTION_DELETE_SUBSCRIPTION = "net.williamott.plasma.action.DELETE_SUBSCRIPTION";
    private static final String ACTION_INSERT_USER = "net.williamott.plasma.action.INSERT_USER";
    private static final String ACTION_BAZ = "net.williamott.plasma.action.BAZ";

    // TODO: Rename parameters
    private static final String EXTRA_PARAM1 = "net.williamott.plasma.extra.PARAM1";
    private static final String EXTRA_PARAM2 = "net.williamott.plasma.extra.PARAM2";
    private static final String EXTRA_PARAM3 = "net.williamott.plasma.extra.PARAM3";
    private static final String EXTRA_PARAM4 = "net.williamott.plasma.extra.PARAM4";
    private static final String EXTRA_PARAM5 = "net.williamott.plasma.extra.PARAM5";
    private static final String EXTRA_PARAM6 = "net.williamott.plasma.extra.PARAM6";
    private static final String EXTRA_PARAM7 = "net.williamott.plasma.extra.PARAM7";
    private static final String EXTRA_PARAM8 = "net.williamott.plasma.extra.PARAM8";
    private static final String EXTRA_PARAM9 = "net.williamott.plasma.extra.PARAM9";
    private static final String EXTRA_PARAM10 = "net.williamott.plasma.extra.PARAM10";
    private static final String EXTRA_PARAM11 = "net.williamott.plasma.extra.PARAM11";
    private static final String EXTRA_PARAM12 = "net.williamott.plasma.extra.PARAM12";
    private static final String EXTRA_PARAM13 = "net.williamott.plasma.extra.PARAM13";

    private static final String EXTRA_CALLBACK = "net.williamott.plasma.extra.CALLBACK";

    public DatabaseIntentService() {
        super("DatabaseIntentService");
    }

    /**
     * Starts this service to perform action Foo with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void insertSubscription(Context context, int param1, String param2, String param3, String param4, String param5, String param6, int param7, int param8) {
        Intent intent = new Intent(context, DatabaseIntentService.class);
        intent.setAction(ACTION_INSERT_SUBSCRIPTION);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        intent.putExtra(EXTRA_PARAM3, param3);
        intent.putExtra(EXTRA_PARAM4, param4);
        intent.putExtra(EXTRA_PARAM5, param5);
        intent.putExtra(EXTRA_PARAM6, param6);
        intent.putExtra(EXTRA_PARAM7, param7);
        intent.putExtra(EXTRA_PARAM8, param8);
        context.startService(intent);
    }

    public static void insertSubscriptionWithCallback(Context context, int param1, String param2, String param3, String param4, String param5, String param6, int param7, int param8, ResultReceiver resultReceiver) {
        Intent intent = new Intent(context, DatabaseIntentService.class);
        intent.setAction(ACTION_INSERT_SUBSCRIPTION_WITH_CALLBACK);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        intent.putExtra(EXTRA_PARAM3, param3);
        intent.putExtra(EXTRA_PARAM4, param4);
        intent.putExtra(EXTRA_PARAM5, param5);
        intent.putExtra(EXTRA_PARAM6, param6);
        intent.putExtra(EXTRA_PARAM7, param7);
        intent.putExtra(EXTRA_PARAM8, param8);
        intent.putExtra(EXTRA_CALLBACK, resultReceiver);
        context.startService(intent);
    }

    /**
     * Starts this service to perform action Baz with the given parameters. If
     * the service is already performing a task this action will be queued.
     *
     * @see IntentService
     */
    // TODO: Customize helper method
    public static void insertEpisode(Context context, String param1, String param2, String param3, String param4, String param5, String param6, String param7, String param8, String param9, int param10, int param11, int param12, String param13) {
        Intent intent = new Intent(context, DatabaseIntentService.class);
        intent.setAction(ACTION_INSERT_EPISODE);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        intent.putExtra(EXTRA_PARAM3, param3);
        intent.putExtra(EXTRA_PARAM4, param4);
        intent.putExtra(EXTRA_PARAM5, param5);
        intent.putExtra(EXTRA_PARAM6, param6);
        intent.putExtra(EXTRA_PARAM7, param7);
        intent.putExtra(EXTRA_PARAM8, param8);
        intent.putExtra(EXTRA_PARAM9, param9);
        intent.putExtra(EXTRA_PARAM10, param10);
        intent.putExtra(EXTRA_PARAM11, param11);
        intent.putExtra(EXTRA_PARAM12, param12);
        intent.putExtra(EXTRA_PARAM13, param13);
        context.startService(intent);
    }

    public static void insertUser(Context context, String param1, String param2) {
        Intent intent = new Intent(context, DatabaseIntentService.class);
        intent.setAction(ACTION_INSERT_USER);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    public static void updateEpisodePosition(Context context, String param1, int param2) {
        Intent intent = new Intent(context, DatabaseIntentService.class);
        intent.setAction(ACTION_UPDATE_EPISODE_POSITION);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    public static void updateEpisodeDownloadLocation(Context context, String param1, String param2) {
        Intent intent = new Intent(context, DatabaseIntentService.class);
        intent.setAction(ACTION_UPDATE_EPISODE_DOWNLOAD_LOCATION);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    public static void updateSubscriptionNewEpisodeCount(Context context, String param1, int param2) {
        Intent intent = new Intent(context, DatabaseIntentService.class);
        intent.setAction(ACTION_UPDATE_SUBSCRIPTION_NEW_EPISODE_COUNT);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_PARAM2, param2);
        context.startService(intent);
    }

    public static void deleteSubscription(Context context, String param1, ResultReceiver resultReceiver) {
        Intent intent = new Intent(context, DatabaseIntentService.class);
        intent.setAction(ACTION_DELETE_SUBSCRIPTION);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_CALLBACK, resultReceiver);
        context.startService(intent);
    }

    public static void updateEpisodesIsNew(Context context, String param1, ResultReceiver resultReceiver) {
        Intent intent = new Intent(context, DatabaseIntentService.class);
        intent.setAction(ACTION_UPDATE_EPISODES_IS_NEW);
        intent.putExtra(EXTRA_PARAM1, param1);
        intent.putExtra(EXTRA_CALLBACK, resultReceiver);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_INSERT_SUBSCRIPTION.equals(action)) {
                final int param1 = intent.getIntExtra(EXTRA_PARAM1, 0);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                final String param3 = intent.getStringExtra(EXTRA_PARAM3);
                final String param4 = intent.getStringExtra(EXTRA_PARAM4);
                final String param5 = intent.getStringExtra(EXTRA_PARAM5);
                final String param6 = intent.getStringExtra(EXTRA_PARAM6);
                final int param7 = intent.getIntExtra(EXTRA_PARAM7, 0);
                final int param8 = intent.getIntExtra(EXTRA_PARAM8, 0);

                handleActionInsertSubscription(param1, param2, param3, param4, param5, param6, param7, param8);
            } else if (ACTION_INSERT_SUBSCRIPTION_WITH_CALLBACK.equals(action)) {
                final int param1 = intent.getIntExtra(EXTRA_PARAM1, 0);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                final String param3 = intent.getStringExtra(EXTRA_PARAM3);
                final String param4 = intent.getStringExtra(EXTRA_PARAM4);
                final String param5 = intent.getStringExtra(EXTRA_PARAM5);
                final String param6 = intent.getStringExtra(EXTRA_PARAM6);
                final int param7 = intent.getIntExtra(EXTRA_PARAM7, 0);
                final int param8 = intent.getIntExtra(EXTRA_PARAM8, 0);
                ResultReceiver callback = intent.getParcelableExtra(EXTRA_CALLBACK);

                handleActionInsertSubscriptionWithCallback(param1, param2, param3, param4, param5, param6, param7, param8, callback);
            } else if (ACTION_INSERT_EPISODE.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                final String param3 = intent.getStringExtra(EXTRA_PARAM3);
                final String param4 = intent.getStringExtra(EXTRA_PARAM4);
                final String param5 = intent.getStringExtra(EXTRA_PARAM5);
                final String param6 = intent.getStringExtra(EXTRA_PARAM6);
                final String param7 = intent.getStringExtra(EXTRA_PARAM7);
                final String param8 = intent.getStringExtra(EXTRA_PARAM8);
                final String param9 = intent.getStringExtra(EXTRA_PARAM9);
                final int param10 = intent.getIntExtra(EXTRA_PARAM10, 0);
                final int param11 = intent.getIntExtra(EXTRA_PARAM11, 0);
                final int param12 = intent.getIntExtra(EXTRA_PARAM11, 0);
                final String param13 = intent.getStringExtra(EXTRA_PARAM13);

                handleActionInsertEpisode(param1, param2, param3, param4, param5, param6, param7, param8, param9, param10, param11, param12, param13);
            } else if (ACTION_UPDATE_EPISODE_POSITION.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final int param2 = intent.getIntExtra(EXTRA_PARAM2, 0);
                handleActionUpdateEpisodePosition(param1, param2);
            } else if (ACTION_UPDATE_EPISODE_DOWNLOAD_LOCATION.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);
                handleActionUpdateEpisodeDownloadLocation(param1, param2);
            } else if (ACTION_UPDATE_EPISODES_IS_NEW.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                ResultReceiver callback = intent.getParcelableExtra(EXTRA_CALLBACK);

                handleActionUpdateEpisodesIsNew(param1, callback);
            } else if (ACTION_UPDATE_SUBSCRIPTION_NEW_EPISODE_COUNT.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final int param2 = intent.getIntExtra(EXTRA_PARAM2, 0);

                handleActionUpdateSubscriptionNewEpisodeCount(param1, param2);
            } else if (ACTION_INSERT_USER.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                final String param2 = intent.getStringExtra(EXTRA_PARAM2);

                handleActionInsertUser(param1, param2);
            }  else if (ACTION_DELETE_SUBSCRIPTION.equals(action)) {
                final String param1 = intent.getStringExtra(EXTRA_PARAM1);
                ResultReceiver callback = intent.getParcelableExtra(EXTRA_CALLBACK);

                handleActionDeleteSubscription(param1, callback);
            }
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     *
     */
    private void handleActionInsertSubscription(int trackId, String trackName, String releaseDate, String artworkUrl, String artistName, String feedUrl, int userId, int newEpisodeCount) {
        Uri uri = Uri.parse("content://net.williamott.plasma.provider/subscriptions/");
        ContentValues cv = new ContentValues();
        cv.put(PodtasticDB.SUBSCRIPTION_TRACK_ID, trackId);
        cv.put(PodtasticDB.SUBSCRIPTION_TRACK_NAME, trackName);
        cv.put(PodtasticDB.SUBSCRIPTION_RELEASE_DATE, releaseDate);
        cv.put(PodtasticDB.SUBSCRIPTION_ARTWORK_URL, artworkUrl);
        cv.put(PodtasticDB.SUBSCRIPTION_ARTIST_NAME, artistName);
        cv.put(PodtasticDB.SUBSCRIPTION_FEED_URL, feedUrl);
        cv.put(PodtasticDB.SUBSCRIPTION_USER_ID, userId);

        cv.put(PodtasticDB.SUBSCRIPTION_NEW_EPISODE_COUNT, newEpisodeCount);

        getContentResolver().insert(uri, cv);
    }

    private void handleActionInsertSubscriptionWithCallback(int trackId, String trackName, String releaseDate, String artworkUrl, String artistName, String feedUrl, int userId, int newEpisodeCount, ResultReceiver callback) {
        Uri uri = Uri.parse("content://net.williamott.plasma.provider/subscriptions/");
        ContentValues cv = new ContentValues();
        cv.put(PodtasticDB.SUBSCRIPTION_TRACK_ID, trackId);
        cv.put(PodtasticDB.SUBSCRIPTION_TRACK_NAME, trackName);
        cv.put(PodtasticDB.SUBSCRIPTION_RELEASE_DATE, releaseDate);
        cv.put(PodtasticDB.SUBSCRIPTION_ARTWORK_URL, artworkUrl);
        cv.put(PodtasticDB.SUBSCRIPTION_ARTIST_NAME, artistName);
        cv.put(PodtasticDB.SUBSCRIPTION_FEED_URL, feedUrl);
        cv.put(PodtasticDB.SUBSCRIPTION_USER_ID, userId);
        cv.put(PodtasticDB.SUBSCRIPTION_NEW_EPISODE_COUNT, newEpisodeCount);

        getContentResolver().insert(uri, cv);

        Bundle resultData = new Bundle();
        int resultCode = 0;

        callback.send(resultCode, resultData);
    }

    /**
     * Handle action Baz in the provided background thread with the provided
     * parameters.
     */
    private void handleActionInsertEpisode(String url, String author, String description, String guid, String pubDate, String title, String album_art_url, String subscriptionId, String subscriptionTitle, int currentPosition, int userId, int isNew, String subscriptionTrackTitle) {
        Uri uri = Uri.parse("content://net.williamott.plasma.provider/episodes/");
        ContentValues cv = new ContentValues();
        cv.put(PodtasticDB.EPISODE_URL, url);
        cv.put(PodtasticDB.EPISODE_AUTHOR, author);
        cv.put(PodtasticDB.EPISODE_DESCRIPTION, description);
        cv.put(PodtasticDB.EPISODE_GUID, guid);
        cv.put(PodtasticDB.EPISODE_PUB_DATE, pubDate);
        cv.put(PodtasticDB.EPISODE_TITLE, title);
        cv.put(PodtasticDB.EPISODE_ALBUM_ART_URL, album_art_url);
        cv.put(PodtasticDB.EPISODE_SUBSCRIPTION_ID, subscriptionId);
        cv.put(PodtasticDB.EPISODE_SUBSCRIPTION_TITLE, subscriptionTitle);
        cv.put(PodtasticDB.EPISODE_CURRENT_POSITION, currentPosition);
        cv.put(PodtasticDB.EPISODE_USER_ID, userId);
        cv.put(PodtasticDB.EPISODE_IS_NEW, isNew);
        cv.put(PodtasticDB.EPISODE_SUBSCRIPTION_TRACK_TITLE, subscriptionTrackTitle);

        getContentResolver().insert(uri, cv);
    }

    private void handleActionUpdateEpisodePosition(String episodeId, int currentPosition) {
        Uri uri = Uri.parse("content://net.williamott.plasma.provider/episodes/" + episodeId);
        ContentValues cv = new ContentValues();
        cv.put(PodtasticDB.EPISODE_CURRENT_POSITION, currentPosition);
        getContentResolver().update(uri, cv, null, null);
    }

    private void handleActionUpdateEpisodeDownloadLocation(String episodeId, String downloadLocation) {
        Uri uri = Uri.parse("content://net.williamott.plasma.provider/episodes/" + episodeId);
        ContentValues cv = new ContentValues();
        cv.put(PodtasticDB.EPISODE_DOWNLOAD_LOCATION, downloadLocation);
        getContentResolver().update(uri, cv, null, null);
    }

    private void handleActionUpdateEpisodesIsNew(String subscriptionId, ResultReceiver callback) {
        Uri uri = Uri.parse("content://net.williamott.plasma.provider/episodes/");
        String where = PodtasticDB.EPISODE_SUBSCRIPTION_ID + "=?";
        String[] whereArgs = { subscriptionId };
        ContentValues cv = new ContentValues();
        cv.put(PodtasticDB.EPISODE_IS_NEW, 0);
        getContentResolver().update(uri, cv, where, whereArgs);

        Bundle resultData = new Bundle();
        int resultCode = 0;

        callback.send(resultCode, resultData);
    }

    private void handleActionUpdateSubscriptionNewEpisodeCount(String subscriptionId, int newEpisodeCount) {
        Uri uri = Uri.parse("content://net.williamott.plasma.provider/subscriptions/" + subscriptionId);
        ContentValues cv = new ContentValues();
        cv.put(PodtasticDB.SUBSCRIPTION_NEW_EPISODE_COUNT, newEpisodeCount);
        getContentResolver().update(uri, cv, null, null);
    }

    private void handleActionInsertUser(String email, String uid) {
        Uri uri = Uri.parse("content://net.williamott.plasma.provider/users/");
        ContentValues cv = new ContentValues();
        cv.put(PodtasticDB.USER_EMAIL, email);
        cv.put(PodtasticDB.USER_UID, uid);

        getContentResolver().insert(uri, cv);
    }

    private void handleActionDeleteSubscription(String episodeId, ResultReceiver callback) {
        Uri uri = Uri.parse("content://net.williamott.plasma.provider/subscriptions/" + episodeId);
        int deleteCount = getContentResolver().delete(uri, null, null);

        Bundle resultData = new Bundle();
        int resultCode = 0;

        if (deleteCount != 1) {
            resultCode = 1;
        }

        callback.send(resultCode, resultData);
    }
}
