package net.williamott.plasma.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import net.williamott.plasma.activities.MainActivity;
import net.williamott.plasma.activities.SubscriptionActivity;
import net.williamott.plasma.classes.User;
import net.williamott.plasma.databases.PodtasticDB;
import net.williamott.plasma.R;
import net.williamott.plasma.databases.PodtasticDBHelper;
import net.williamott.plasma.services.DatabaseIntentService;

public class SubscriptionsFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private Context mContext;
    private ListView mSubscriptionsListView;
    private DatabaseReference mRef;
    private FirebaseUser mUser;
    private FirebaseAuth mAuth;
    private SimpleCursorAdapter adapter;


    public SubscriptionsFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onResume() {
        super.onResume();
        setAdapter();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_subscriptions, container, false);
        mSubscriptionsListView = view.findViewById(R.id.subscriptions_list_view);

        mAuth = ((MainActivity)this.getActivity()).getFirebaseAuth();
        mRef = ((MainActivity)this.getActivity()).getFirebaseRef();
        mUser = ((MainActivity)this.getActivity()).getFirebaseUser();

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

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mUser != null) {
            User user = PodtasticDBHelper.getUserFromEmail(mContext, mUser.getEmail());

            if (user != null) {
                Uri uri = Uri.parse("content://net.williamott.plasma.provider/subscriptions");
                String selection = PodtasticDB.SUBSCRIPTION_USER_ID + "=?";
                String[] selectionArgs = {Integer.toString(user.getId())};
                return new CursorLoader(mContext, uri, null, selection, selectionArgs, PodtasticDB.SUBSCRIPTION_ID);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        adapter.swapCursor(data);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        adapter.swapCursor(null);
    }

    private void setAdapter () {
        String[] columns = new String[] { PodtasticDB.SUBSCRIPTION_ARTIST_NAME, PodtasticDB.SUBSCRIPTION_TRACK_NAME, PodtasticDB.SUBSCRIPTION_ARTWORK_URL, PodtasticDB.SUBSCRIPTION_NEW_EPISODE_COUNT, PodtasticDB.SUBSCRIPTION_ID, PodtasticDB.SUBSCRIPTION_FEED_URL, PodtasticDB.SUBSCRIPTION_ARTWORK_URL };
        int[] toViews = new int[] { R.id.main_podcast_artist_name_text_view, R.id.main_podcast_track_name_text_view, R.id.main_podcast_album_art_image_view, R.id.main_podcast_episode_count };

        adapter = new SimpleCursorAdapter(mContext, R.layout.main_podcast_layout, null, columns, toViews, 0);

        adapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (view.getId() == R.id.main_podcast_album_art_image_view) {
                    ImageView imageView = view.findViewById(R.id.main_podcast_album_art_image_view);
                    Glide.with(mContext).load(cursor.getString(PodtasticDB.SUBSCRIPTION_ARTWORK_URL_COL)).error(PodtasticDBHelper.getFallbackDrawable(mContext)).into(imageView);
                    return true;
                }
                if (view.getId() == R.id.main_podcast_episode_count) {
                    if (cursor.getInt(PodtasticDB.SUBSCRIPTION_NEW_EPISODE_COUNT_COL) == 0) {
                        TextView textView = view.findViewById(R.id.main_podcast_episode_count);
                        textView.setText("");
                    } else {
                        TextView textView = view.findViewById(R.id.main_podcast_episode_count);
                        if (cursor.getInt(PodtasticDB.SUBSCRIPTION_NEW_EPISODE_COUNT_COL) == 1) {
                            textView.setText(Integer.toString(cursor.getInt(PodtasticDB.SUBSCRIPTION_NEW_EPISODE_COUNT_COL)) + " new episode");
                        } else {
                            textView.setText(Integer.toString(cursor.getInt(PodtasticDB.SUBSCRIPTION_NEW_EPISODE_COUNT_COL)) + " new episodes");
                        }
                    }
                    return true;
                }
                return false;
            }
        });

        mSubscriptionsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor =  adapter.getCursor();
                cursor.moveToPosition(position);
                String subscriptionId = cursor.getString(PodtasticDB.SUBSCRIPTION_ID_COL);
                String subscriptionFeedUrl = cursor.getString(PodtasticDB.SUBSCRIPTION_FEED_URL_COL);
                String subscriptionArtworkUrl = cursor.getString(PodtasticDB.SUBSCRIPTION_ARTWORK_URL_COL);
                Intent intent = new Intent(mContext, SubscriptionActivity.class);
                intent.putExtra("SUBSCRIPTION_ID", subscriptionId);
                intent.putExtra("SUBSCRIPTION_FEED_URL", subscriptionFeedUrl);
                intent.putExtra("SUBSCRIPTION_ARTWORK_URL", subscriptionArtworkUrl);
                startActivity(intent);
            }
        });

        mSubscriptionsListView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder alert = new AlertDialog.Builder(mContext);
                alert.setTitle("Remove subscription");
                alert.setMessage("Are you sure you want to remove this subscription?");
                alert.setPositiveButton("YES", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Cursor cursor =  adapter.getCursor();
                        cursor.moveToPosition(position);
                        final String subscriptionId = cursor.getString(PodtasticDB.SUBSCRIPTION_ID_COL);
                        String trackId = cursor.getString(PodtasticDB.SUBSCRIPTION_TRACK_ID_COL);
                        mRef = FirebaseDatabase.getInstance().getReference();
                        mAuth = FirebaseAuth.getInstance();
                        mUser = mAuth.getCurrentUser();
                        DatabaseReference subscriptionReference = mRef.child("users").child(mUser.getUid()).child("subscriptions").child(trackId);

                        subscriptionReference.removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                DatabaseIntentService.deleteSubscription(mContext, subscriptionId, new ResultReceiver(new Handler()) {
                                    @Override
                                    protected void onReceiveResult(int resultCode, Bundle resultData) {
                                        if (resultData != null && resultCode == 0) {
                                            if (isAdded()) {
                                                getLoaderManager().restartLoader(0, null, SubscriptionsFragment.this);
                                            }
                                        } else if (resultData != null & resultCode == 1) {
                                            CharSequence text = "There was a problem removing the subscription!";
                                            int duration = Toast.LENGTH_SHORT;

                                            Toast toast = Toast.makeText(mContext, text, duration);
                                            toast.show();
                                        }
                                    }
                                });
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                            }
                        });

                        dialog.dismiss();
                    }
                });
                alert.setNegativeButton("NO", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                    }
                });

                alert.show();
                return true;
            }
        });

        mSubscriptionsListView.setAdapter(adapter);

        this.getLoaderManager().initLoader(0, null, this);
    }
}
