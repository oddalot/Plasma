package net.williamott.plasma.activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.MultiTransformation;

import net.williamott.plasma.classes.Episode;
import net.williamott.plasma.databases.PodtasticDB;
import net.williamott.plasma.R;
import net.williamott.plasma.databases.PodtasticDBHelper;
import net.williamott.plasma.services.MediaPlaybackService;
import net.williamott.plasma.services.SubscriptionService;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import br.com.simplepass.loading_button_lib.customViews.CircularProgressButton;
import br.com.simplepass.loading_button_lib.interfaces.OnAnimationEndListener;
import jp.wasabeef.glide.transformations.BlurTransformation;
import jp.wasabeef.glide.transformations.ColorFilterTransformation;
import static com.bumptech.glide.request.RequestOptions.bitmapTransform;

public class EpisodeActivity extends AppCompatActivity {
    private Context mContext;
    private FloatingActionButton mFab;
    private MediaBrowserCompat mMediaBrowser;
    private Cursor mCursor;
    private String mEpisodeId;
    private String mMetaEpisodeId;
    private String mEpisodeArtworkUrl;
    private CircularProgressButton mDownloadButton;
    private SubscriptionService mSubscriptionService;
    private boolean mSubscriptionServiceBound = false;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        mMediaBrowser.connect();
        Intent intent = new Intent(this, SubscriptionService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(SubscriptionService.EPISODE_DOWNLOAD_PROGRESS_BROADCAST_ACTION);
        registerReceiver(receiver, filter);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (MediaControllerCompat.getMediaController(EpisodeActivity.this) != null) {
            MediaControllerCompat.getMediaController(EpisodeActivity.this).unregisterCallback(controllerCallback);
        }
        mMediaBrowser.disconnect();
        if (mSubscriptionServiceBound) {
            unbindService(mServiceConnection);
            mSubscriptionServiceBound = false;
        }
        unregisterReceiver(receiver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        mDownloadButton.dispose();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_episode);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        mContext = this;
        ImageView toolbarImageView = findViewById(R.id.episode_toolbar_image_view);
        ImageView toolbarBackgroundImage = findViewById(R.id.episode_toolbar_background_image);
        TextView episodeToolbarTitle = findViewById(R.id.episode_toolbar_title);
        TextView episodeToolbarSubscriptionTitle = findViewById(R.id.episode_toolbar_subscription_title);

        TextView episodeTitle = findViewById(R.id.episode_title);
        TextView episodeDate = findViewById(R.id.episode_date);
        TextView episodeDescription = findViewById(R.id.episode_description);

        mDownloadButton = findViewById(R.id.episode_download_button);
        mEpisodeId = getIntent().getStringExtra("EPISODE_ID");

        mMediaBrowser = new MediaBrowserCompat(this, new ComponentName(this, MediaPlaybackService.class), mConnectionCallbacks, null);

        mFab = findViewById(R.id.episode_fab);
        mFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if (mEpisodeId != null && mMetaEpisodeId != null && mEpisodeId.equals(mMetaEpisodeId)) {
                    int pbState = MediaControllerCompat.getMediaController(EpisodeActivity.this).getPlaybackState().getState();
                    if (pbState == PlaybackStateCompat.STATE_PLAYING) {
                        MediaControllerCompat.getMediaController(EpisodeActivity.this).getTransportControls().pause();
                    } else {
                        MediaControllerCompat.getMediaController(EpisodeActivity.this).getTransportControls().play();
                    }
                } else if (mEpisodeId != null) {
                    MediaControllerCompat.getMediaController(EpisodeActivity.this).getTransportControls().playFromMediaId(mEpisodeId, new Bundle());
                    setBuffering();
                }
            }
        });
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        final Episode episode = PodtasticDBHelper.getEpisodeFromId(mContext, mEpisodeId);

        if (episode != null) {
            mEpisodeArtworkUrl = episode.getAlbumArtUrl();
            MultiTransformation multi = new MultiTransformation(
                    new BlurTransformation(25),
                    new ColorFilterTransformation(Color.argb(80, 80, 80, 80)));
            Glide.with(mContext).load(mEpisodeArtworkUrl).error(PodtasticDBHelper.getFallbackDrawable(mContext)).into(toolbarImageView);
            Glide.with(mContext).load(mEpisodeArtworkUrl).apply(bitmapTransform(multi)).error(PodtasticDBHelper.getFallbackDrawable(mContext)).into(toolbarBackgroundImage);;
            episodeToolbarTitle.setText(episode.getTitle());
            episodeToolbarSubscriptionTitle.setText(episode.getSubscriptionTitle());
            episodeTitle.setText(episode.getTitle());
            String strCurrentDate = episode.getPubDate();
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss", Locale.US);
            Date newDate;
            try {
                newDate = format.parse(strCurrentDate);
            } catch (ParseException e) {
                newDate = new Date();
            }

            format = new SimpleDateFormat("EEE MMM dd, yyyy", Locale.US);
            String date = format.format(newDate);

            episodeDate.setText(date);
            episodeDescription.setText(stripHtml(episode.getDescription()));

            if (episode.getDownloadLocation() == null) {
                mDownloadButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mSubscriptionServiceBound) {
                            mDownloadButton.startAnimation();
                            mSubscriptionService.downloadEpisode(episode.getUrl(), mEpisodeId, mEpisodeArtworkUrl, episode.getTitle());
                        }
                    }
                });
            } else {
                mDownloadButton.setText(getString(R.string.downloaded));
                mDownloadButton.setBackgroundResource(R.drawable.button_bg_rounded_corners_disabled);
                mDownloadButton.setEnabled(false);
            }
        }
    }

    private String stripHtml(String html) {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
            return Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY).toString();
        } else {
            return Html.fromHtml(html).toString();
        }
    }

    private final MediaBrowserCompat.ConnectionCallback mConnectionCallbacks =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    MediaSessionCompat.Token token = mMediaBrowser.getSessionToken();

                    try {
                        MediaControllerCompat mediaController = new MediaControllerCompat(EpisodeActivity.this, token);
                        MediaControllerCompat.setMediaController(EpisodeActivity.this, mediaController);
                        mediaController.registerCallback(controllerCallback);
                        if (mediaController.getMetadata() != null) {
                            mMetaEpisodeId = mediaController.getMetadata().getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
                        }
                    } catch (RemoteException e) {
                    }

                }

                @Override
                public void onConnectionSuspended() {
                    // The Service has crashed. Disable transport controls until it automatically reconnects
                }

                @Override
                public void onConnectionFailed() {
                    // The Service has refused our connection
                }
            };

    MediaControllerCompat.Callback controllerCallback =
            new MediaControllerCompat.Callback() {
                @Override
                public void onMetadataChanged(MediaMetadataCompat metadata) {
                    mMetaEpisodeId = metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID);
                }

                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat state) {
                    if (mEpisodeId != null && mMetaEpisodeId != null && mEpisodeId.equals(mMetaEpisodeId)) {
                        if (state.getState() == PlaybackStateCompat.STATE_PLAYING) {
                            resetBuffering();
                            mFab.setImageResource(R.drawable.ic_action_pause);
                        } else {
                            mFab.setImageResource(R.drawable.ic_action_play);
                        }
                    }
                }
            };

    private void setBuffering() {
        RotateAnimation rotateAnimation = (RotateAnimation) AnimationUtils.loadAnimation(mContext, R.anim.rotate_refresh);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        mFab.startAnimation(rotateAnimation);
    }

    private void resetBuffering()
    {
        mFab.clearAnimation();
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

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String episodeId = intent.getStringExtra("EPISODE_ID");
            String episodeProgress = intent.getStringExtra("DOWNLOAD_PROGRESS");
            if (episodeId.equals(mEpisodeId)) {
                if (episodeProgress.equals("STARTED")) {
                    mDownloadButton.revertAnimation(new OnAnimationEndListener() {
                        @Override
                        public void onAnimationEnd() {
                            mDownloadButton.stopAnimation();
                            mDownloadButton.setText(getString(R.string.downloading));
                            mDownloadButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                            mDownloadButton.setBackgroundResource(R.drawable.button_bg_rounded_corners_disabled);
                            mDownloadButton.setEnabled(false);
                        }
                    });
                } else if (episodeProgress.equals("FINISHED")) {
                    mDownloadButton.stopAnimation();
                    mDownloadButton.setText(getString(R.string.downloaded));
                    mDownloadButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 10);
                    mDownloadButton.setBackgroundResource(R.drawable.button_bg_rounded_corners_disabled);
                    mDownloadButton.setEnabled(false);
                }
            }
        }
    };
}
