package net.williamott.plasma.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.media.MediaMetadata;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.HandlerThread;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaBrowserServiceCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaButtonReceiver;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

import net.williamott.plasma.activities.MainActivity;
import net.williamott.plasma.classes.Episode;
import net.williamott.plasma.databases.PodtasticDB;
import net.williamott.plasma.R;
import net.williamott.plasma.databases.PodtasticDBHelper;

import java.util.List;

public class MediaPlaybackService extends MediaBrowserServiceCompat implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener {
    public static final String ACTION_PAUSE = "ACTION_PAUSE";
    public static final String ACTION_PLAY = "ACTION_PLAY";
    public static final String ACTION_STOP = "ACTION_STOP";
    public static final String ACTION_FAST_FORWARD = "ACTION_FAST_FORWARD";
    public static final String ACTION_REWIND = "ACTION_REWIND";
    public static final int NOTIFICATION_ID = 111234;
    private MediaSessionCompat mMediaSession;
    private PlaybackStateCompat.Builder mStateBuilder;
    private MediaPlayer mMediaPlayer;
    private Context mContext;
    private boolean mMediaPlaybackServiceStarted;
    private Episode mEpisode;
    private AudioManager mAudioManager;
    private Handler mHandler;
    private AudioManager.OnAudioFocusChangeListener mAfChangeListener;
    private IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
    private BecomingNoisyReceiver myNoisyAudioStreamReceiver = new BecomingNoisyReceiver();
    private boolean mNoisyReceiverRegistered = false;
    private static final String CHANNEL_ID = "media_playback_channel";
    private boolean mResumeOnFocusGain = false;


    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        mMediaSession = new MediaSessionCompat(mContext, MediaPlaybackService.class.getSimpleName());
        mMediaSession.setCallback(new MediaSessionCallback());
        mMediaSession.setFlags(MediaSessionCompat.FLAG_HANDLES_MEDIA_BUTTONS | MediaSessionCompat.FLAG_HANDLES_TRANSPORT_CONTROLS);
        setSessionToken(mMediaSession.getSessionToken());
        mStateBuilder = new PlaybackStateCompat.Builder()
                .setActions(
                        PlaybackStateCompat.ACTION_PLAY |
                                PlaybackStateCompat.ACTION_PLAY_PAUSE |
                                PlaybackStateCompat.ACTION_STOP |
                                PlaybackStateCompat.ACTION_FAST_FORWARD |
                                PlaybackStateCompat.ACTION_REWIND );
        mMediaSession.setPlaybackState(mStateBuilder.build());
        mMediaPlayer = new MediaPlayer();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

        mAfChangeListener = new AudioManager.OnAudioFocusChangeListener() {
            public void onAudioFocusChange(int focusChange) {
                //Log.d("here", "hereloss");
                if (mMediaPlayer != null && mMediaSession != null) {
                    if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT && mMediaPlayer.isPlaying()) {
                        mResumeOnFocusGain = true;
                        mMediaSession.getController().getTransportControls().pause();
                    } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK && mMediaPlayer.isPlaying()) {
                        mResumeOnFocusGain = true;
                        mMediaSession.getController().getTransportControls().pause();
                    } else if (focusChange == AudioManager.AUDIOFOCUS_GAIN) {
                        if (mResumeOnFocusGain) {
                            mResumeOnFocusGain = false;
                            mMediaSession.getController().getTransportControls().play();
                        }
                    } else if (focusChange == AudioManager.AUDIOFOCUS_LOSS) {
                        mResumeOnFocusGain = false;
                        mAudioManager.abandonAudioFocus(mAfChangeListener);
                        mMediaSession.getController().getTransportControls().pause();
                    }
                }
            }
        };

        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setClass(mContext, MediaButtonReceiver.class);
        PendingIntent mbrIntent = PendingIntent.getBroadcast(mContext, 0, mediaButtonIntent, 0);
        mMediaSession.setMediaButtonReceiver(mbrIntent);
    }

    @Override
    public void onDestroy() {
        mMediaSession.release();
        mMediaPlayer.stop();
    }

    @Override
    public BrowserRoot onGetRoot(String clientPackageName, int clientUid,
                                 Bundle rootHints) {
        return new BrowserRoot(getString(R.string.app_name), null);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        handleIntent(intent);
        MediaButtonReceiver.handleIntent(mMediaSession, intent);
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onLoadChildren(final String parentMediaId,
                               final Result<List<MediaBrowserCompat.MediaItem>> result) {
        result.sendResult(null);
    }

    private void handleIntent(Intent intent) {
        if( intent == null || intent.getAction() == null ) {
            return;
        }

        String action = intent.getAction();
        if (action.equals(ACTION_PAUSE)) {
            mMediaSession.getController().getTransportControls().pause();
        } else if (action.equals(ACTION_PLAY)) {
            mMediaSession.getController().getTransportControls().play();
        } else if (action.equals(ACTION_STOP)) {
            mMediaSession.getController().getTransportControls().stop();
        } else if (action.equals(ACTION_FAST_FORWARD)) {
            mMediaSession.getController().getTransportControls().fastForward();
        } else if (action.equals(ACTION_REWIND)) {
            mMediaSession.getController().getTransportControls().rewind();
        }

    }

    private final class MediaSessionCallback extends MediaSessionCompat.Callback {
        @Override
        public void onPlayFromMediaId(String mediaId, Bundle extras) {
            setmEpisode(mediaId);
            handlePlayRequest();
        }



        @Override
        public void onPlay() {
            if (mEpisode != null) {
                if (!mMediaSession.isActive()) {
                    mMediaSession.setActive(true);
                }
                if (!mNoisyReceiverRegistered) {
                    registerReceiver(myNoisyAudioStreamReceiver, intentFilter);
                    mNoisyReceiverRegistered = true;
                }
                mMediaPlayer.start();
                mMediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                        .setState(PlaybackStateCompat.STATE_PLAYING, mMediaPlayer.getCurrentPosition(), 0.0f)
                        .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_STOP | PlaybackStateCompat.ACTION_FAST_FORWARD | PlaybackStateCompat.ACTION_REWIND).build());
                showNotification("pause");
            }
        }

        @Override
        public void onFastForward() {
            if (mMediaPlayer != null && mEpisode != null) {
                int newPosition = mMediaPlayer.getCurrentPosition() + 10000;
                if (newPosition < mMediaPlayer.getDuration()) {
                    mMediaPlayer.seekTo(newPosition);
                }
            }
            super.onFastForward();
        }

        @Override
        public void onRewind() {
            if (mMediaPlayer != null && mEpisode != null) {
                int newPosition = mMediaPlayer.getCurrentPosition() - 10000;
                if (newPosition > 0) {
                    mMediaPlayer.seekTo(newPosition);
                }
            }
            super.onRewind();
        }

        @Override
        public void onPause() {
            handlePauseRequest();
            showNotification("play");
        }

        @Override
        public void onStop() {
            handleStopRequest();
        }

        @Override
        public void onSeekTo(long pos) {
            mMediaPlayer.seekTo((int) pos);
        }
    }

    private void handlePlayRequest() {
        if (!mMediaPlaybackServiceStarted) {
            startService(new Intent(getApplicationContext(), MediaPlaybackService.class));

            mMediaPlaybackServiceStarted = true;
        }
        if (!mMediaSession.isActive()) {
            mMediaSession.setActive(true);
        }

        if (mMediaPlayer.isPlaying()) {
            handlePauseRequest();
        }
        mMediaPlayer.release();
        mMediaPlayer = new MediaPlayer();
        try {
            if (mEpisode.getDownloadLocation() == null) {
                mMediaPlayer.setDataSource(mEpisode.getUrl());
            } else {
                String filePath = mContext.getExternalFilesDir(Environment.DIRECTORY_PODCASTS).getPath() + "/" + mEpisode.getDownloadLocation();
                mMediaPlayer.setDataSource(filePath);
            }
            mMediaPlayer.setOnPreparedListener(this);
            mMediaPlayer.setOnErrorListener(this);
        } catch (Exception e) {
        }
        mMediaPlayer.prepareAsync();
    }

    private void handleStopRequest() {

        if (mEpisode != null) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("EPISODE_ID", Integer.toString(mEpisode.getId()));
            editor.putInt("EPISODE_DURATION", mMediaPlayer.getDuration());
            editor.putInt("EPISODE_CURRENT_POSITION", mMediaPlayer.getCurrentPosition());
            editor.putString("EPISODE_TITLE", mEpisode.getTitle());
            editor.putString("EPISODE_SUBSCRIPTION_TRACK_TITLE", mEpisode.getSubscriptionTrackTitle());
            editor.putString("EPISODE_ALBUM_ART_URL", mEpisode.getAlbumArtUrl());
            editor.commit();

            mMediaPlayer.pause();
            DatabaseIntentService.updateEpisodePosition(mContext, Integer.toString(mEpisode.getId()), mMediaPlayer.getCurrentPosition());
            if (mNoisyReceiverRegistered) {
                unregisterReceiver(myNoisyAudioStreamReceiver);
                mNoisyReceiverRegistered = false;
            }
            mMediaPlaybackServiceStarted = false;
            mHandler = null;
            mMediaSession.setActive(false);
            stopForeground(true);
            mMediaPlayer.stop();
            mMediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_STOPPED, mMediaPlayer.getCurrentPosition(), 1.0f)
                    .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_STOP).build());
            mMediaSession.release();
            NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            mNotificationManager.cancel(NOTIFICATION_ID);
            stopSelf();
        }
    }

    private void handlePauseRequest() {
        if (mNoisyReceiverRegistered) {
            unregisterReceiver(myNoisyAudioStreamReceiver);
            mNoisyReceiverRegistered = false;
        }
        stopForeground(false);
        mMediaPlayer.pause();
        // mPodcastItem.setCurrentPosition(mMediaPlayer.getCurrentPosition());
        // mPodcastItem.save();
        DatabaseIntentService.updateEpisodePosition(mContext, Integer.toString(mEpisode.getId()), mMediaPlayer.getCurrentPosition());
        mMediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                .setState(PlaybackStateCompat.STATE_PAUSED, mMediaPlayer.getCurrentPosition(), 1.0f)
                .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_STOP).build());
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        stopSelf();
    }

    @Override
    public void onPrepared(MediaPlayer player) {
        int result = mAudioManager.requestAudioFocus(mAfChangeListener,
                // Use the music stream.
                AudioManager.STREAM_MUSIC,
                // Request permanent focus.
                AudioManager.AUDIOFOCUS_GAIN);

        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            if (!mNoisyReceiverRegistered) {
                registerReceiver(myNoisyAudioStreamReceiver, intentFilter);
                mNoisyReceiverRegistered = true;
            }
            player.seekTo(mEpisode.getCurrentPosition());
            player.start();
            mMediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                    .setState(PlaybackStateCompat.STATE_PLAYING, mEpisode.getCurrentPosition(), 0.0f)
                    .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE | PlaybackStateCompat.ACTION_STOP | PlaybackStateCompat.ACTION_FAST_FORWARD | PlaybackStateCompat.ACTION_REWIND).build());
            updateMetaData();

            mHandler = new Handler();
            final int delay = 1000; //milliseconds

            mHandler.postDelayed(new Runnable() {
                public void run() {
                    if (mHandler != null) {
                        int playbackState;
                        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
                        SharedPreferences.Editor editor = sharedPref.edit();
                        if (mMediaPlayer.isPlaying()) {
                            editor.putBoolean("EPISODE_IS_PLAYING", true);
                            playbackState = PlaybackStateCompat.STATE_PLAYING;
                        } else {
                            editor.putBoolean("EPISODE_IS_PLAYING", false);
                            playbackState = PlaybackStateCompat.STATE_PAUSED;
                        }

                        editor.putString("EPISODE_ID", Integer.toString(mEpisode.getId()));
                        editor.putInt("EPISODE_DURATION", mMediaPlayer.getDuration());
                        editor.putInt("EPISODE_CURRENT_POSITION", mMediaPlayer.getCurrentPosition());
                        editor.putString("EPISODE_TITLE", mEpisode.getTitle());
                        editor.putString("EPISODE_SUBSCRIPTION_TRACK_TITLE", mEpisode.getSubscriptionTrackTitle());
                        editor.putString("EPISODE_ALBUM_ART_URL", mEpisode.getAlbumArtUrl());
                        editor.commit();
                        mMediaSession.setPlaybackState(new PlaybackStateCompat.Builder()
                                .setState(playbackState, mMediaPlayer.getCurrentPosition(), 0.0f)
                                .setActions(PlaybackStateCompat.ACTION_PLAY_PAUSE).build());
                        mHandler.postDelayed(this, delay);
                    }
                }
            }, delay);

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
            SharedPreferences.Editor editor = sharedPref.edit();
            editor.putString("EPISODE_ID", Integer.toString(mEpisode.getId()));
            editor.putInt("EPISODE_DURATION", mMediaPlayer.getDuration());
            editor.putInt("EPISODE_CURRENT_POSITION", mMediaPlayer.getCurrentPosition());
            editor.putString("EPISODE_TITLE", mEpisode.getTitle());
            editor.putString("EPISODE_SUBSCRIPTION_TRACK_TITLE", mEpisode.getSubscriptionTrackTitle());
            editor.putString("EPISODE_ALBUM_ART_URL", mEpisode.getAlbumArtUrl());
            editor.commit();

            showNotification("pause");
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        return false;
    }

    private void showNotification (String playOrPause) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createChannel();
        }
        if (playOrPause.equals("pause")) {
            Glide.with(this).asBitmap().apply(new RequestOptions()).load(mEpisode.getAlbumArtUrl()).error(PodtasticDBHelper.getFallbackBitmap(mContext)).into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap bitmap, Transition<? super Bitmap> transition) {
                    Intent pauseIntent = new Intent(mContext, MediaPlaybackService.class);
                    pauseIntent.setAction(ACTION_PAUSE);
                    PendingIntent resultPendingIntent = PendingIntent.getService(mContext, 0, pauseIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    Intent fastForwardIntent = new Intent(mContext, MediaPlaybackService.class);
                    fastForwardIntent.setAction(ACTION_FAST_FORWARD);
                    PendingIntent fastForwardPendingIntent = PendingIntent.getService(mContext, 0, fastForwardIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    Intent rewindIntent = new Intent(mContext, MediaPlaybackService.class);
                    rewindIntent.setAction(ACTION_REWIND);
                    PendingIntent rewindPendingIntent = PendingIntent.getService(mContext, 0, rewindIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    Intent notificationIntent = new Intent(mContext, MainActivity.class);
                    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    notificationIntent.setAction("CURRENTLY_PLAYING_FRAGMENT");
                    PendingIntent notificationPendingIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    Notification notification = new NotificationCompat.Builder(mContext, "plasma_channel_01")
                            // Show controls on lock screen even when user hides sensitive content.
                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                            // Add media control buttons that invoke intents in your media service
                            .addAction(R.drawable.ic_rewind_button, "Rewind", rewindPendingIntent) // #0
                            .addAction(R.drawable.ic_pause_button, "Pause", resultPendingIntent)  // #1
                            .addAction(R.drawable.ic_forward_button, "Fast Forward", fastForwardPendingIntent)     // #2
                            // Apply the media style template
                            .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                                    .setShowActionsInCompactView(1 /* #1: pause button */)
                                    .setMediaSession(mMediaSession.getSessionToken()))
                            .setSmallIcon(getNotificationIcon())
                            .setColor(Color.BLACK)
                            .setLargeIcon(bitmap)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setContentTitle(mEpisode.getTitle())
                            .setContentText(mEpisode.getSubscriptionTitle())
                            .setContentIntent(notificationPendingIntent)
                            .setOngoing(true)
                            .build();

                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
                    startForeground(NOTIFICATION_ID, notification);
                }
            });
        } else if (playOrPause.equals("play")){
            Glide.with(this).asBitmap().apply(new RequestOptions()).load(mEpisode.getAlbumArtUrl()).error(PodtasticDBHelper.getFallbackBitmap(mContext)).into(new SimpleTarget<Bitmap>() {
                @Override
                public void onResourceReady(Bitmap bitmap, Transition<? super Bitmap> transition) {

                    Intent playIntent = new Intent(mContext, MediaPlaybackService.class);
                    playIntent.setAction(ACTION_PLAY);
                    PendingIntent resultPendingIntent = PendingIntent.getService(mContext, 0, playIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    Intent stopIntent = new Intent(mContext, MediaPlaybackService.class);
                    stopIntent.setAction(ACTION_STOP);
                    PendingIntent stopPendingIntent = PendingIntent.getService(mContext, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    Intent notificationIntent = new Intent(mContext, MainActivity.class);
                    notificationIntent.setAction("CURRENTLY_PLAYING_FRAGMENT");
                    notificationIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    PendingIntent notificationPendingIntent = PendingIntent.getActivity(mContext, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

                    Notification notification = new NotificationCompat.Builder(mContext, "plasma_channel_01")
                            // Show controls on lock screen even when user hides sensitive content.
                            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                            // Add media control buttons that invoke intents in your media service
                            .addAction(R.drawable.ic_play_button, "Play", resultPendingIntent)  // #1
                            .addAction(R.drawable.ic_close_button, "Close", stopPendingIntent)     // #2
                            // Apply the media style template
                            .setStyle(new android.support.v4.media.app.NotificationCompat.MediaStyle()
                                    .setShowActionsInCompactView(1 /* #1: pause button */)
                                    .setMediaSession(mMediaSession.getSessionToken()))
                            .setSmallIcon(getNotificationIcon())
                            .setColor(Color.BLACK)
                            .setLargeIcon(bitmap)
                            .setContentTitle(mEpisode.getTitle())
                            .setContentText(mEpisode.getSubscriptionTitle())
                            .setContentIntent(notificationPendingIntent)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .setOngoing(true)
                            .build();
                    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                    startForeground(NOTIFICATION_ID, notification);
                }
            });


        }
    }

    private int getNotificationIcon() {
        boolean useWhiteIcon = (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP);
        return useWhiteIcon ? R.drawable.ic_transparent_logo_circle_small : R.drawable.ic_plasma_circle_logo_black;
    }

    private void updateMetaData () {
        final MediaMetadataCompat.Builder metadataBuilder = new MediaMetadataCompat.Builder();
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_ARTIST, mEpisode.getSubscriptionTitle());
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_TITLE, mEpisode.getTitle());
        metadataBuilder.putString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID, Integer.toString(mEpisode.getId()));
        metadataBuilder.putLong(MediaMetadataCompat.METADATA_KEY_DURATION, mMediaPlayer.getDuration());

        Glide.with(this).asBitmap().apply(new RequestOptions()).load(mEpisode.getAlbumArtUrl()).error(PodtasticDBHelper.getFallbackBitmap(mContext)).into(new SimpleTarget<Bitmap>() {
            @Override
            public void onResourceReady(Bitmap bitmap, Transition<? super Bitmap> transition) {
                metadataBuilder.putBitmap(MediaMetadata.METADATA_KEY_ART, bitmap);
                mMediaSession.setMetadata(metadataBuilder.build());
            }
        });
    }

    private void setmEpisode (String episodeId) {
        mEpisode = PodtasticDBHelper.getEpisodeFromId(mContext, episodeId);
    }

    private class BecomingNoisyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (AudioManager.ACTION_AUDIO_BECOMING_NOISY.equals(intent.getAction())) {
                mMediaSession.getController().getTransportControls().pause();
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createChannel() {
        NotificationManager
                mNotificationManager =
                (NotificationManager) mContext
                        .getSystemService(Context.NOTIFICATION_SERVICE);
        // The id of the channel.
        String id = "plasma_channel_01";
        // The user-visible name of the channel.
        CharSequence name = "Media playback";
        // The user-visible description of the channel.
        String description = "Media playback controls";
        int importance = NotificationManager.IMPORTANCE_LOW;
        NotificationChannel mChannel = new NotificationChannel(id, name, importance);
        // Configure the notification channel.
        mChannel.setDescription(description);
        mChannel.setShowBadge(false);
        mChannel.setLockscreenVisibility(Notification.VISIBILITY_PUBLIC);
        mNotificationManager.createNotificationChannel(mChannel);
    }
}
