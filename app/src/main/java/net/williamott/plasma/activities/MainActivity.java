package net.williamott.plasma.activities;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import net.williamott.plasma.classes.BottomNavigationViewHelper;
import net.williamott.plasma.classes.User;
import net.williamott.plasma.databases.PodtasticDB;
import net.williamott.plasma.databases.PodtasticDBHelper;
import net.williamott.plasma.fragments.CurrentlyPlayingFragment;
import net.williamott.plasma.fragments.DownloadsFragment;
import net.williamott.plasma.fragments.EpisodesFragment;
import net.williamott.plasma.R;
import net.williamott.plasma.services.DatabaseIntentService;
import net.williamott.plasma.fragments.SubscriptionsFragment;
import net.williamott.plasma.services.SubscriptionService;

public class MainActivity extends AppCompatActivity {
    private Context mContext;
    private FirebaseAuth mAuth;
    private FirebaseUser mUser;
    private Menu mMenu;
    private SubscriptionService mSubscriptionService;
    private boolean mSubscriptionServiceBound = false;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            FragmentManager fragmentManager = getSupportFragmentManager();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            switch (item.getItemId()) {
                case R.id.navigation_subscriptions:
                    transaction.replace(R.id.content, new SubscriptionsFragment()).commit();
                    return true;
                case R.id.navigation_episodes:
                    transaction.replace(R.id.content, new EpisodesFragment()).commit();
                    return true;
                case R.id.navigation_downloads:
                    transaction.replace(R.id.content, new DownloadsFragment()).commit();
                    return true;
                case R.id.navigation_currently_playing:
                    transaction.replace(R.id.content, new CurrentlyPlayingFragment()).commit();
                    return true;
            }
            return false;
        }

    };

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(this, SubscriptionService.class);
        bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(SubscriptionService.FINISH_REFRESH_BROADCAST_ACTION);
        registerReceiver(receiver, filter);
    }

    @Override
    public void onResume() {
        super.onResume();

    }


    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mSubscriptionServiceBound) {
            unbindService(mServiceConnection);
            mSubscriptionServiceBound = false;
        }
        unregisterReceiver(receiver);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigation = findViewById(R.id.navigation);
        BottomNavigationViewHelper.disableShiftMode(navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        mContext = this;
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
        mAuth.addAuthStateListener(new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                mUser = firebaseAuth.getCurrentUser();
                if (mUser != null) {
                    User user = PodtasticDBHelper.getUserFromEmail(mContext, mUser.getEmail());

                    if (user == null) {
                        DatabaseIntentService.insertUser(mContext, mUser.getEmail(), mUser.getUid());
                    }
                } else {
                    Intent intent = new Intent(mContext, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });

        if (getIntent().getAction() != null && getIntent().getAction().equals("CURRENTLY_PLAYING_FRAGMENT")) {
            navigation.setSelectedItemId(R.id.navigation_currently_playing);
        } else if (savedInstanceState != null) {
            navigation.setSelectedItemId(savedInstanceState.getInt("FRAGMENT_ID"));
        } else {
            navigation.setSelectedItemId(R.id.navigation_subscriptions);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        BottomNavigationView navigation = findViewById(R.id.navigation);
        outState.putInt("FRAGMENT_ID", navigation.getSelectedItemId());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.action_bar, menu);
        mMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_log_out:
                SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
                SharedPreferences.Editor editor = sharedPref.edit();
                editor.putString("EPISODE_ID", null);
                editor.putInt("EPISODE_DURATION", 0);
                editor.putInt("EPISODE_CURRENT_POSITION", 0);
                editor.putString("EPISODE_TITLE", null);
                editor.putString("EPISODE_SUBSCRIPTION_TRACK_TITLE", null);
                editor.putString("EPISODE_ALBUM_ART_URL", null);
                editor.commit();
                if (mUser != null) {
                    mAuth.signOut();
                } else {
                    Intent intent = new Intent(mContext, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
                break;
            case R.id.action_add_rss_feed:
                Intent intent = new Intent(mContext, AddFeedActivity.class);
                startActivity(intent);
                break;
            case R.id.action_search:
                Intent searchIntent = new Intent(mContext, SearchActivity.class);
                startActivity(searchIntent);
                break;
            case R.id.action_refresh:
                setUpdating();
                if (mSubscriptionServiceBound) {
                    mSubscriptionService.updateSubscriptions();
                }
                break;
            default:
                break;
        }

        return true;
    }

    public FirebaseAuth getFirebaseAuth() {
        return mAuth;
    }

    public FirebaseUser getFirebaseUser() {
        return mUser;
    }

    public DatabaseReference getFirebaseRef() {
        return FirebaseDatabase.getInstance().getReference();
    }

    private void setUpdating() {
        LayoutInflater inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        if (inflater != null) {
            ImageView iv = (ImageView) inflater.inflate(R.layout.iv_refresh, null);
            Animation rotation = AnimationUtils.loadAnimation(this, R.anim.rotate_refresh);
            rotation.setRepeatCount(Animation.INFINITE);
            iv.startAnimation(rotation);
            mMenu.findItem(R.id.action_refresh).setActionView(iv);
        }
    }

    private void resetUpdating() {
        MenuItem m = mMenu.findItem(R.id.action_refresh);
        if(m.getActionView()!=null)
        {
            m.getActionView().clearAnimation();
            m.setActionView(null);
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

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            resetUpdating();
        }
    };
}
