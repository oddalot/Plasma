package net.williamott.plasma.databases;

import android.content.ContentResolver;
import android.content.Context;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.support.annotation.AnyRes;
import android.support.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;

import net.williamott.plasma.R;
import net.williamott.plasma.classes.Episode;
import net.williamott.plasma.classes.Subscription;
import net.williamott.plasma.classes.User;

import java.util.ArrayList;

public class PodtasticDBHelper {
    public static User getUserFromEmail(Context context, String email) {
        Uri uri = Uri.parse("content://net.williamott.plasma.provider/users");
        String selection = PodtasticDB.USER_EMAIL + "=?";
        String[] selectionArgs = {email};
        final Cursor cursor = context.getContentResolver().query(uri, null, selection, selectionArgs, null);

        User user = null;

        if (cursor != null && cursor.moveToFirst()) {
            user = new User(cursor.getInt(PodtasticDB.USER_ID_COL), cursor.getString(PodtasticDB.USER_EMAIL_COL), cursor.getString(PodtasticDB.USER_UID_COL));
        }
        if (cursor != null) {
            cursor.close();
        }

        return user;
    }

    public static Subscription getSubscriptionFromTrackId(Context context, int trackId) {
        Uri uri = Uri.parse("content://net.williamott.plasma.provider/subscriptions");
        String selection = PodtasticDB.SUBSCRIPTION_TRACK_ID + "=?";
        String[] selectionArgs = { Integer.toString(trackId) };
        Cursor cursor = context.getContentResolver().query(uri, null, selection, selectionArgs, null);

        Subscription subscription = null;

        if (cursor != null && cursor.moveToFirst()) {
            //Subscription(int id, int trackId, String trackName, String releaseDate, String artworkUrl30, String artworkUrl60, String artworkUrl100, String artworkUrl600, String artistName, String feedUrl, int userId, int newEpisodeCount)
            subscription = new Subscription(cursor.getInt(PodtasticDB.SUBSCRIPTION_ID_COL), cursor.getInt(PodtasticDB.SUBSCRIPTION_TRACK_ID_COL), cursor.getString(PodtasticDB.SUBSCRIPTION_TRACK_NAME_COL),
                                            cursor.getString(PodtasticDB.SUBSCRIPTION_RELEASE_DATE_COL), cursor.getString(PodtasticDB.SUBSCRIPTION_ARTWORK_URL_COL), cursor.getString(PodtasticDB.SUBSCRIPTION_ARTIST_NAME_COL),
                                            cursor.getString(PodtasticDB.SUBSCRIPTION_FEED_URL_COL), cursor.getInt(PodtasticDB.SUBSCRIPTION_USER_ID_COL),
                                            cursor.getInt(PodtasticDB.SUBSCRIPTION_NEW_EPISODE_COUNT_COL));
        }
        if (cursor != null) {
            cursor.close();
        }

        return subscription;
    }

    public static ArrayList<Subscription> getSubscriptionsFromUserId(Context context, int userId) {
        Uri uri = Uri.parse("content://net.williamott.plasma.provider/subscriptions");
        String selection = PodtasticDB.SUBSCRIPTION_USER_ID + "=?";
        String[] selectionArgs = {Integer.toString(userId)};
        final Cursor cursor = context.getContentResolver().query(uri, null, selection, selectionArgs, null);

        ArrayList<Subscription> subscriptions = new ArrayList<Subscription>();
        if (cursor != null) {
            while (cursor.moveToNext()) {
                Subscription subscription = new Subscription(cursor.getInt(PodtasticDB.SUBSCRIPTION_ID_COL), cursor.getInt(PodtasticDB.SUBSCRIPTION_TRACK_ID_COL), cursor.getString(PodtasticDB.SUBSCRIPTION_TRACK_NAME_COL),
                        cursor.getString(PodtasticDB.SUBSCRIPTION_RELEASE_DATE_COL), cursor.getString(PodtasticDB.SUBSCRIPTION_ARTWORK_URL_COL), cursor.getString(PodtasticDB.SUBSCRIPTION_ARTIST_NAME_COL),
                        cursor.getString(PodtasticDB.SUBSCRIPTION_FEED_URL_COL), cursor.getInt(PodtasticDB.SUBSCRIPTION_USER_ID_COL),
                        cursor.getInt(PodtasticDB.SUBSCRIPTION_NEW_EPISODE_COUNT_COL));
                subscriptions.add(subscription);
            }
            cursor.close();
        }

        return subscriptions;
    }

    public static Episode getEpisodeFromGuid(Context context, String guid) {
        Uri uri = Uri.parse("content://net.williamott.plasma.provider/episodes");
        String selection = PodtasticDB.EPISODE_GUID + "=?";
        String[] selectionArgs = { guid };
        Cursor cursor = context.getContentResolver().query(uri, null, selection, selectionArgs, null);

        Episode episode = null;

        if (cursor != null && cursor.moveToFirst()) {
            episode = new Episode();
        }
        if (cursor != null) {
            cursor.close();
        }

        return episode;
    }

    public static Episode getEpisodeFromId(Context context, String episodeId) {
        Uri uri = Uri.parse("content://net.williamott.plasma.provider/episodes");
        String selection = PodtasticDB.EPISODE_ID + "=?";
        String[] selectionArgs = { episodeId };
        Cursor cursor = context.getContentResolver().query(uri, null, selection, selectionArgs, null);

        Episode episode = null;

        if (cursor != null && cursor.moveToFirst()) {
            //Episode(int id, String url, String author, String description, String guid, String pubDate, String title, String albumArtUrl, String subscriptionId, String subscriptionTitle, int currentPosition, int userId, int isNew, String downloadLocation, String subscriptionTrackTitle)
            episode = new Episode(cursor.getInt(PodtasticDB.EPISODE_ID_COL), cursor.getString(PodtasticDB.EPISODE_URL_COL) ,cursor.getString(PodtasticDB.EPISODE_AUTHOR_COL),
                    cursor.getString(PodtasticDB.EPISODE_DESCRIPTION_COL), cursor.getString(PodtasticDB.EPISODE_GUID_COL),
                    cursor.getString(PodtasticDB.EPISODE_PUB_DATE_COL), cursor.getString(PodtasticDB.EPISODE_TITLE_COL),
                    cursor.getString(PodtasticDB.EPISODE_ALBUM_ART_URL_COL), cursor.getString(PodtasticDB.EPISODE_SUBSCRIPTION_ID_COL),
                    cursor.getString(PodtasticDB.EPISODE_SUBSCRIPTION_TITLE_COL), cursor.getInt(PodtasticDB.EPISODE_CURRENT_POSITION_COL),
                    cursor.getInt(PodtasticDB.EPISODE_USER_ID_COL), cursor.getInt(PodtasticDB.EPISODE_IS_NEW_COL),
                    cursor.getString(PodtasticDB.EPISODE_DOWNLOAD_LOCATION_COL), cursor.getString(PodtasticDB.EPISODE_SUBSCRIPTION_TRACK_TITLE_COL));
        }
        if (cursor != null) {
            cursor.close();
        }

        return episode;
    }

    public static final Uri getUriToResource(@NonNull Context context,
                                             @AnyRes int resId)
            throws Resources.NotFoundException {
        /** Return a Resources instance for your application's package. */
        Resources res = context.getResources();
        /**
         * Creates a Uri which parses the given encoded URI string.
         * @param uriString an RFC 2396-compliant, encoded URI
         * @throws NullPointerException if uriString is null
         * @return Uri for this given uri string
         */
        Uri resUri = Uri.parse(ContentResolver.SCHEME_ANDROID_RESOURCE +
                "://" + res.getResourcePackageName(resId)
                + '/' + res.getResourceTypeName(resId)
                + '/' + res.getResourceEntryName(resId));
        /** return uri */
        return resUri;
    }

    public static final RequestBuilder<Drawable> getFallbackDrawable(Context context) {
        RequestBuilder<Drawable> requestBuilder = Glide.with(context).load(R.drawable.ic_plasma_main_logo_white);
        return requestBuilder;
    }

    public static final RequestBuilder<Bitmap> getFallbackBitmap(Context context) {
        RequestBuilder<Bitmap> requestBuilder = Glide.with(context).asBitmap().load(R.drawable.ic_plasma_main_logo_white);
        return requestBuilder;
    }
}
