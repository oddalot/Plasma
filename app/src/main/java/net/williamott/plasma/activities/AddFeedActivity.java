package net.williamott.plasma.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import net.williamott.plasma.R;
import net.williamott.plasma.classes.PodcastXmlParser;
import net.williamott.plasma.classes.Subscription;
import net.williamott.plasma.services.SubscriptionService;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;
import br.com.simplepass.loading_button_lib.interfaces.OnAnimationEndListener;

public class AddFeedActivity extends AppCompatActivity {
    private EditText mAddFeedEditText;
    private CircularProgressButton mAddFeedButton;
    private SubscriptionService mSubscriptionService;
    private boolean mSubscriptionServiceBound = false;
    private String mFeedUrl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_feed);

        mAddFeedEditText = findViewById(R.id.add_feed_edit_text);
        mAddFeedButton = findViewById(R.id.add_feed_button);

        mAddFeedButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFeedUrl = mAddFeedEditText.getText().toString();
                if (!mFeedUrl.isEmpty()) {
                    new DownloadPodcastTask(mFeedUrl).execute();
                }
            }
        });
    }
    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(this, SubscriptionService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mSubscriptionServiceBound) {
            unbindService(mServiceConnection);
            mSubscriptionServiceBound = false;
        }
    }

    public void addSubscription(Subscription subscription) {
        String title = subscription.getTrackName();
        String feedUrl = subscription.getFeedUrl() == null ? mFeedUrl : subscription.getFeedUrl();
        String releaseDate = subscription.getReleaseDate() == null ? "" : subscription.getReleaseDate();
        String artistName = subscription.getArtistName() == null ? subscription.getTrackName() : subscription.getArtistName();
        int trackId = subscription.getTrackName().hashCode();
        String artworkUrl = subscription.getArtworkUrl();

        if (mSubscriptionServiceBound) {
            mSubscriptionService.addSubsciption(title, feedUrl, artistName, trackId, artworkUrl, releaseDate, new ResultReceiver(new Handler()) {
                @Override
                protected void onReceiveResult(int resultCode, Bundle resultData) {
                    stopButtonAnimationWithToast(getString(R.string.add_feed_successful));
                }
            });
        }
    }

    private class DownloadPodcastTask extends AsyncTask<Void, Void, Subscription> {
        private String mFeedUrl;
        private Subscription mSubscription;

        public DownloadPodcastTask (String feedUrl) {
            super();
            mFeedUrl = feedUrl;
        }

        protected Subscription doInBackground(Void ...unused) {
            startButtonAnimation();
            try {
                InputStream stream = null;
                PodcastXmlParser podcastXmlParser = new PodcastXmlParser();
                try {
                    stream = downloadUrl(mFeedUrl);
                    mSubscription = podcastXmlParser.parseSubscription(stream);
                } finally {
                    if (stream != null) {
                        stream.close();
                    }
                }

                return mSubscription;
            } catch (Exception e) {
                stopButtonAnimationWithToast(getString(R.string.add_feed_error));
                return null;
            }
        }

        protected void onProgressUpdate(Void ...unused) {
            //
        }

        protected void onPostExecute(Subscription subscription) {
            if (subscription == null) {
                stopButtonAnimationWithToast(getString(R.string.add_feed_error));
            } else {
                addSubscription(subscription);
            }
        }
    }


    private ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder service) {
            SubscriptionService.LocalBinder binder = (SubscriptionService.LocalBinder) service;
            mSubscriptionService = binder.getService();
            mSubscriptionServiceBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
            mSubscriptionServiceBound = false;
        }
    };

    private void startButtonAnimation() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAddFeedButton.startAnimation();
            }
        });
    }

    private void stopButtonAnimationWithToast(final String toastMessage) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mAddFeedButton.revertAnimation(new OnAnimationEndListener() {
                    @Override
                    public void onAnimationEnd() {
                        mAddFeedButton.stopAnimation();
                        Toast.makeText(AddFeedActivity.this, toastMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
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
