package net.williamott.plasma.services;

import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.tonyodev.fetch.Fetch;
import com.tonyodev.fetch.listener.FetchListener;
import com.tonyodev.fetch.request.Request;

import net.williamott.plasma.activities.SearchActivity;
import net.williamott.plasma.classes.PodcastXmlParser;
import net.williamott.plasma.classes.SearchResults;
import net.williamott.plasma.classes.Subscription;
import net.williamott.plasma.classes.User;
import net.williamott.plasma.databases.PodtasticDB;
import net.williamott.plasma.databases.PodtasticDBHelper;
import net.williamott.plasma.R;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SubscriptionService extends Service {
    private final IBinder mBinder = new LocalBinder();
    private User mUser;
    private Context mContext;
    private ArrayList<Subscription> mSubscriptionsCheck;
    public static String FINISH_REFRESH_BROADCAST_ACTION = "net.williamott.plasma.broadcasts.FINISH_REFRESH";
    public static String EPISODE_DOWNLOAD_PROGRESS_BROADCAST_ACTION = "net.williamott.plasma.broadcasts.EPISODE_DOWNLOAD_PROGRESS";

    public SubscriptionService() {
    }

    public class LocalBinder extends Binder {
        public SubscriptionService getService() {
            return SubscriptionService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    public void updateSubscriptions() {
        mContext = this;
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        mUser = PodtasticDBHelper.getUserFromEmail(mContext, firebaseUser.getEmail());
        getSubscriptionsFromFirebase(firebaseUser);
    };

    private void getSubscriptionsFromFirebase(FirebaseUser firebaseUser) {
        DatabaseReference mRef = FirebaseDatabase.getInstance().getReference();
        DatabaseReference mSubscriptionReference = mRef.child("users").child(firebaseUser.getUid()).child("subscriptions");
        ValueEventListener subscriptionListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Subscription> subscriptions = new ArrayList<Subscription> ();
                for (DataSnapshot subscriptionSnapshot : dataSnapshot.getChildren()) {
                    Subscription subscription = subscriptionSnapshot.getValue(Subscription.class);
                    subscriptions.add(subscription);
                }
                checkSubscriptions(subscriptions);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };

        mSubscriptionReference.addListenerForSingleValueEvent(subscriptionListener);
    }

    private void checkSubscriptions(ArrayList<Subscription> subscriptions) {
        for (Subscription subscription: subscriptions) {
            if (PodtasticDBHelper.getSubscriptionFromTrackId(mContext, subscription.getTrackId()) != null) {
                checkIfSubscriptionIsLast(subscriptions, subscription);
            } else {
                addSubscriptionToDatabase(subscriptions, subscription);
            }
        }
    }

    private void addSubscriptionToDatabase(final ArrayList<Subscription> subscriptions, final Subscription subscription) {
        DatabaseIntentService.insertSubscriptionWithCallback(mContext, subscription.getTrackId(), subscription.getTrackName(), subscription.getReleaseDate(),
                subscription.getArtworkUrl(), subscription.getArtistName(), subscription.getFeedUrl(), mUser.getId(), 0, new ResultReceiver(new Handler()) {
                    @Override
                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                        checkIfSubscriptionIsLast(subscriptions, subscription);
                    }
                });
    }

    private void checkIfSubscriptionIsLast(ArrayList<Subscription> subscriptions, Subscription subscription) {
        if (subscriptions.get(subscriptions.size() - 1) == subscription) {
            refreshSubscriptions();
        }
    }

    private void refreshSubscriptions() {
        ArrayList<Subscription> subscriptions = PodtasticDBHelper.getSubscriptionsFromUserId(mContext, mUser.getId());
        mSubscriptionsCheck = subscriptions;
        for (Subscription subscription: subscriptions) {
            getPodcast(subscription);
        }
    }

    private void getPodcast(Subscription subscription) {
        new DownloadPodcastTask(subscription).execute();
    }

    private class DownloadPodcastTask extends AsyncTask<Void, Void, List<net.williamott.plasma.classes.Episode>> {
        private Subscription mSubsciption;

        public DownloadPodcastTask (Subscription subscription) {
            super();
            mSubsciption = subscription;
        }

        protected List<net.williamott.plasma.classes.Episode> doInBackground(Void ...unused) {
            try {
                InputStream stream = null;
                PodcastXmlParser podcastXmlParser = new PodcastXmlParser();
                List<net.williamott.plasma.classes.Episode> episodes = null;
                try {
                    stream = downloadUrl(mSubsciption.getFeedUrl());
                    episodes = podcastXmlParser.parse(stream);
                } finally {
                    if (stream != null) {
                        stream.close();
                    }
                }

                if (episodes.isEmpty()) {
                    return null;
                } else {
                    return episodes;
                }
            } catch (Exception e) {
                return null;
            }
        }

        protected void onProgressUpdate(Void ...unused) {
            //
        }

        protected void onPostExecute(List<net.williamott.plasma.classes.Episode> episodes) {
            updateEpisodesIsNew(episodes, mSubsciption);
        }
    }

    public void updateEpisodesIsNew(final List<net.williamott.plasma.classes.Episode> episodes, final Subscription subscripton) {
        DatabaseIntentService.updateEpisodesIsNew(mContext, Integer.toString(subscripton.getId()),  new ResultReceiver(new Handler()) {
            @Override
            protected void onReceiveResult(int resultCode, Bundle resultData) {
                new InsertEpisodesTask(episodes, subscripton).execute();
            }
        });
    }

    private class InsertEpisodesTask extends AsyncTask<Void, Void, Void> {
        private List<net.williamott.plasma.classes.Episode> mEpisodes;
        private Subscription mSubscription;
        private SimpleDateFormat mDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DateFormat mFormatter = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z");

        public InsertEpisodesTask (List<net.williamott.plasma.classes.Episode> episodes, Subscription subscription) {
            super();
            mEpisodes = episodes;
            mSubscription = subscription;
        }

        protected Void doInBackground(Void ...unused) {
            int episodeCount = 0;
            if (mEpisodes != null) {
                for (net.williamott.plasma.classes.Episode episode : mEpisodes) {
                    String episodePubDate = "";
                    try {
                        episodePubDate = mDateFormat.format(mFormatter.parse(episode.getPubDate()));
                    } catch (ParseException e){
                    }

                    if (PodtasticDBHelper.getEpisodeFromGuid(mContext, episode.getGuid()) != null) {
                        break;
                    } else {
                        episodeCount += 1;
                        DatabaseIntentService.insertEpisode(mContext, episode.getUrl(), episode.getAuthor(), episode.getDescription(), episode.getGuid(), episodePubDate, episode.getTitle(), mSubscription.getArtworkUrl(), Integer.toString(mSubscription.getId()), mSubscription.getArtistName(), 0, mSubscription.getUserId(), 1, mSubscription.getTrackName());

                    }
                }
            }
            DatabaseIntentService.updateSubscriptionNewEpisodeCount(mContext, Integer.toString(mSubscription.getId()), episodeCount);

            if (mSubscriptionsCheck.get(mSubscriptionsCheck.size() - 1) == mSubscription) {
                Intent broadcast = new Intent();
                broadcast.setAction(FINISH_REFRESH_BROADCAST_ACTION);
                sendBroadcast(broadcast);
            }

            return null;
        }

        protected void onProgressUpdate(Void ...unused) {
            //
        }

        protected void onPostExecute(Void ...unused) {

        }
    }

    public void downloadEpisode(final String episodeUrl, final String episodeId, final String episodeArtworkUrl, final String episodeTitle) {
        mContext = this;
        final Fetch fetch = Fetch.newInstance(this);
        final String fileName = Uri.parse(episodeUrl).getLastPathSegment();
        Request request = new Request(episodeUrl, mContext.getExternalFilesDir(Environment.DIRECTORY_PODCASTS).getPath(), fileName);
        long downloadId = fetch.enqueue(request);

        final int episodeIdInt = Integer.parseInt(episodeId);
        final NotificationManager notifyManager;
        final NotificationCompat.Builder builder;

        if(downloadId != Fetch.ENQUEUE_ERROR_ID) {
            notifyManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            builder = new NotificationCompat.Builder(mContext);
            builder.setContentTitle("Podcast Download")
                    .setContentText(episodeTitle)
                    .setSmallIcon(getNotificationIcon());

            fetch.addFetchListener(new FetchListener() {
                @Override
                public void onUpdate(long id, int status, int progress, long downloadedBytes, long fileSize, int error) {
                    Intent broadcast = new Intent();
                    broadcast.putExtra("EPISODE_ID", episodeId);
                    broadcast.putExtra("DOWNLOAD_PROGRESS", "STARTED");
                    broadcast.setAction(EPISODE_DOWNLOAD_PROGRESS_BROADCAST_ACTION);
                    sendBroadcast(broadcast);
                    if (fileSize > 0) {// only if total length is known
                        builder.setProgress(100, progress, false);
                        notifyManager.notify(episodeIdInt, builder.build());

                        if (status == Fetch.STATUS_DONE) {
                            builder.setContentText("Download complete")
                                    // Removes the progress bar
                                    .setProgress(0,0,false);
                            notifyManager.notify(episodeIdInt, builder.build());
                            Intent broadcast2 = new Intent();
                            broadcast2.putExtra("EPISODE_ID", episodeId);
                            broadcast2.putExtra("DOWNLOAD_PROGRESS", "FINISHED");
                            broadcast2.setAction(EPISODE_DOWNLOAD_PROGRESS_BROADCAST_ACTION);
                            sendBroadcast(broadcast2);
                            DatabaseIntentService.updateEpisodeDownloadLocation(mContext, episodeId, fileName);
                            fetch.release();
                        }
                    }
                }
            });
        }
    }

    private int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.drawable.ic_transparent_logo_circle_small : R.drawable.ic_plasma_circle_logo_black;
    }

    public void addSubsciption(final String title, final String feedUrl, final String authorTemp, final Integer trackIdTemp, final String artworkUrl, final String releaseDate, final ResultReceiver callback) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        mContext = this;
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final User user = PodtasticDBHelper.getUserFromEmail(mContext, firebaseUser.getEmail());
        if (user != null && title != null && feedUrl != null) {
            final String author = authorTemp == null ? title : authorTemp;
            final int trackId = trackIdTemp == null ? title.hashCode() : trackIdTemp;
            final int userId = user.getId();
            Subscription subscription = new Subscription(trackId, title, releaseDate, artworkUrl, author, feedUrl, userId, 0);
            database.child("users").child(user.getUid()).child("subscriptions").child(Integer.toString(trackId)).setValue(subscription).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    DatabaseIntentService.insertSubscription(mContext, trackId, title, releaseDate, artworkUrl, author, feedUrl, userId, 0);
                    Bundle resultData = new Bundle();
                    int resultCode = 0;

                    callback.send(resultCode, resultData);
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                }
            });
        }
    }

    private InputStream downloadUrl(String urlString) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setReadTimeout(10000);
        conn.setConnectTimeout(15000);
        conn.setRequestMethod("GET");
        conn.setDoInput(true);
        conn.connect();
        return conn.getInputStream();
    }
}
