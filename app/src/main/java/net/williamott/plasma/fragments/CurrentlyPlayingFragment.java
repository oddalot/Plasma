package net.williamott.plasma.fragments;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.RemoteException;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.media.MediaBrowserCompat;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaControllerCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.RotateAnimation;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import net.williamott.plasma.classes.Episode;
import net.williamott.plasma.databases.PodtasticDB;
import net.williamott.plasma.R;
import net.williamott.plasma.databases.PodtasticDBHelper;
import net.williamott.plasma.services.MediaPlaybackService;


public class CurrentlyPlayingFragment extends Fragment {
    private ImageView mItemImage;
    private TextView mItemSubscriptionTrackTitle;
    private TextView mItemEpisodeTitle;
    private Context mContext;
    private ImageButton mPlayButton;
    private MediaBrowserCompat mMediaBrowser;
    private PlaybackStateCompat mPlayerState;
    private MediaMetadataCompat mMetadata;
    private SeekBar mItemSeekBar;
    private TextView mItemDuration;
    private TextView mItemPosition;
    private Episode mEpisode;
    private String mEpisodeArtworkUrlOld;
    private Boolean mIsScrubbing;

    public CurrentlyPlayingFragment() {
        // Required empty public constructor
    }

    @Override
    public void onStop() {
        if (MediaControllerCompat.getMediaController(getActivity()) != null) {
            MediaControllerCompat.getMediaController(getActivity()).unregisterCallback(controllerCallback);
        }
        mMediaBrowser.disconnect();
        super.onStop();

    }

    @Override
    public void onStart() {
        mIsScrubbing = false;
        mMediaBrowser = new MediaBrowserCompat(getActivity(), new ComponentName(getActivity(), MediaPlaybackService.class), mConnectionCallbacks, null);
        mMediaBrowser.connect();
        super.onStart();
    }

    @Override
    public void onPause() {
        if (MediaControllerCompat.getMediaController(getActivity()) != null) {
            MediaControllerCompat.getMediaController(getActivity()).unregisterCallback(controllerCallback);
        }
        mMediaBrowser.disconnect();
        super.onPause();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        mMediaBrowser = getMediaBrowser();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_currently_playing, container, false);
        mPlayButton = view.findViewById(R.id.podcast_item_play_button);
        ImageButton fastForwardButton = view.findViewById(R.id.podcast_item_fast_forward_button);
        ImageButton rewindButton = view.findViewById(R.id.podcast_item_rewind_button);

        mItemImage = view.findViewById(R.id.item_image);
        mItemEpisodeTitle = view.findViewById(R.id.item_episode_title);
        mItemSubscriptionTrackTitle = view.findViewById(R.id.item_subscription_track_title);
        mItemPosition = view.findViewById(R.id.item_seek_position);
        mItemDuration = view.findViewById(R.id.item_seek_duration);
        mItemSeekBar = view.findViewById(R.id.item_seek_bar);


        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        String episodeAlbumArtUrl = sharedPref.getString("EPISODE_ALBUM_ART_URL", "");
        String episodeTitle = sharedPref.getString("EPISODE_TITLE", "");
        String episodeSubscriptionTrackTitle = sharedPref.getString("EPISODE_SUBSCRIPTION_TRACK_TITLE", "");
        int episodeCurrentPosition = sharedPref.getInt("EPISODE_CURRENT_POSITION", 0);
        int episodeDuration = sharedPref.getInt("EPISODE_DURATION", 0);
        boolean episodeIsPlaying = sharedPref.getBoolean("EPISODE_IS_PLAYING", false);

        if (!episodeAlbumArtUrl.equals("")) {
            Glide.with(mContext).load(episodeAlbumArtUrl).error(PodtasticDBHelper.getFallbackDrawable(mContext)).into(mItemImage);
        }

        if (!episodeTitle.equals("")) {
            mItemEpisodeTitle.setText(episodeTitle);
        }

        if (!episodeSubscriptionTrackTitle.equals("")) {
            mItemSubscriptionTrackTitle.setText(episodeSubscriptionTrackTitle);
        }

        if (episodeDuration != 0) {
            mItemSeekBar.setMax(episodeDuration);
            mItemSeekBar.setProgress(episodeCurrentPosition);
            mItemPosition.setText(getTimeString(episodeCurrentPosition));
            mItemDuration.setText(getTimeString(episodeDuration));
        }

        if (episodeIsPlaying) {
            mPlayButton.setBackgroundResource(R.drawable.ic_pause_button);
        } else {
            mPlayButton.setBackgroundResource(R.drawable.ic_play_button);
        }

        if (mItemSeekBar != null) {
            updateMetadata(mMetadata);
        }

        if (mPlayerState != null) {
            updatePlayerState(mPlayerState);
        }


        mPlayButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEpisode != null) {
                    int pbState = MediaControllerCompat.getMediaController(getActivity()).getPlaybackState().getState();
                    if (pbState == PlaybackStateCompat.STATE_PLAYING) {
                        MediaControllerCompat.getMediaController(getActivity()).getTransportControls().pause();
                    } else if (pbState == PlaybackStateCompat.STATE_PAUSED) {
                        MediaControllerCompat.getMediaController(getActivity()).getTransportControls().play();
                    } else {
                        MediaControllerCompat.getMediaController(getActivity()).getTransportControls().playFromMediaId(Integer.toString(mEpisode.getId()), new Bundle());
                        setBuffering();
                    }
                }
            }
        });

        fastForwardButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mEpisode != null) {
                    MediaControllerCompat.getMediaController(getActivity()).getTransportControls().fastForward();
                }
            }
        });

        rewindButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mEpisode != null) {
                    MediaControllerCompat.getMediaController(getActivity()).getTransportControls().rewind();
                }
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void updateMetadata (MediaMetadataCompat metadata) {
        mMetadata = metadata;
        if (mMetadata == null) {
            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
            String episodeId = sharedPref.getString("EPISODE_ID", "NULL");
            int episodeDuration = sharedPref.getInt("EPISODE_DURATION", 0);
            if (!episodeId.equals("NULL")) {
                mEpisode = PodtasticDBHelper.getEpisodeFromId(mContext, episodeId);
                if (mEpisode != null) {
                    Glide.with(mContext).load(mEpisode.getAlbumArtUrl()).error(PodtasticDBHelper.getFallbackDrawable(mContext)).into(mItemImage);
                }
                if (mItemSeekBar != null) {
                    mItemSeekBar.setMax(episodeDuration);
                    mItemSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            mItemPosition.setText(getTimeString(progress));
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {
                            mIsScrubbing = true;
                        }

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {
                            MediaControllerCompat.getMediaController(getActivity()).getTransportControls().seekTo(seekBar.getProgress());
                            mIsScrubbing = false;
                        }
                    });
                }
            }

        } else {
            mEpisode = PodtasticDBHelper.getEpisodeFromId(mContext, metadata.getString(MediaMetadataCompat.METADATA_KEY_MEDIA_ID));
            if (mItemSeekBar != null) {
                mItemSeekBar.setMax((int) mMetadata.getLong(MediaMetadataCompat.METADATA_KEY_DURATION));
                mItemSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                        mItemPosition.setText(getTimeString(progress));
                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {
                        mIsScrubbing = true;
                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        MediaControllerCompat.getMediaController(getActivity()).getTransportControls().seekTo(seekBar.getProgress());
                        mIsScrubbing = false;
                    }
                });
            }
        }
    }

    public void updatePlayerState (PlaybackStateCompat state) {
        if (mEpisode != null && !mIsScrubbing) {
            mPlayerState = state;
            if (mPlayButton != null) {
                if (state.getState() == PlaybackStateCompat.STATE_PLAYING) {
                    resetBuffering();
                    mPlayButton.setBackgroundResource(R.drawable.ic_pause_button);
                } else {
                    mPlayButton.setBackgroundResource(R.drawable.ic_play_button);
                }
                if (!mEpisode.getAlbumArtUrl().equals(mEpisodeArtworkUrlOld)) {
                    mEpisodeArtworkUrlOld = mEpisode.getAlbumArtUrl();
                    Glide.with(mContext).load(mEpisode.getAlbumArtUrl()).error(PodtasticDBHelper.getFallbackDrawable(mContext)).into(mItemImage);
                }
                mItemEpisodeTitle.setText(mEpisode.getTitle());
                mItemSubscriptionTrackTitle.setText(mEpisode.getSubscriptionTrackTitle());
            }
            if (mItemSeekBar != null) {
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
                int episodeCurrentPosition = sharedPref.getInt("EPISODE_CURRENT_POSITION", 0);
                if (state.getPosition() == 0 && episodeCurrentPosition != 0) {
                    mItemSeekBar.setProgress(episodeCurrentPosition);
                    mItemPosition.setText(getTimeString(episodeCurrentPosition));
                    mItemDuration.setText(getTimeString(mItemSeekBar.getMax()));
                } else {
                    mItemSeekBar.setProgress((int) state.getPosition());
                    mItemPosition.setText(getTimeString(state.getPosition()));
                    mItemDuration.setText(getTimeString(mItemSeekBar.getMax()));
                }
            }
        }
    }

    private final MediaBrowserCompat.ConnectionCallback mConnectionCallbacks =
            new MediaBrowserCompat.ConnectionCallback() {
                @Override
                public void onConnected() {
                    MediaSessionCompat.Token token = mMediaBrowser.getSessionToken();

                    try {
                        MediaControllerCompat mediaController = new MediaControllerCompat(getActivity(), token);
                        MediaControllerCompat.setMediaController(getActivity(), mediaController);
                        mediaController.registerCallback(controllerCallback);

                    } catch (RemoteException e) {
                    }

                    if (MediaControllerCompat.getMediaController(getActivity()) != null && MediaControllerCompat.getMediaController(getActivity()) != null) {
                        updateMetadata(MediaControllerCompat.getMediaController(getActivity()).getMetadata());
                        updatePlayerState(MediaControllerCompat.getMediaController(getActivity()).getPlaybackState());
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
                    updateMetadata(metadata);
                }

                @Override
                public void onPlaybackStateChanged(PlaybackStateCompat state) {
                    updatePlayerState(state);
                }
            };

    public MediaBrowserCompat getMediaBrowser () {
        return mMediaBrowser;
    }

    private String getTimeString(long millis) {
        StringBuffer buf = new StringBuffer();

        int hours = (int) (millis / (1000 * 60 * 60));
        int minutes = (int) ((millis % (1000 * 60 * 60)) / (1000 * 60));
        int seconds = (int) (((millis % (1000 * 60 * 60)) % (1000 * 60)) / 1000);

        buf
                .append(String.format("%02d", hours))
                .append(":")
                .append(String.format("%02d", minutes))
                .append(":")
                .append(String.format("%02d", seconds));

        return buf.toString();
    }

    private void setBuffering() {
        RotateAnimation rotateAnimation = (RotateAnimation) AnimationUtils.loadAnimation(mContext, R.anim.rotate_refresh);
        rotateAnimation.setRepeatCount(Animation.INFINITE);
        mPlayButton.startAnimation(rotateAnimation);
    }

    private void resetBuffering()
    {
        mPlayButton.clearAnimation();
    }
}
