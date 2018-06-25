package net.williamott.plasma.fragments;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseUser;

import net.williamott.plasma.activities.EpisodeActivity;
import net.williamott.plasma.activities.MainActivity;
import net.williamott.plasma.classes.User;
import net.williamott.plasma.databases.PodtasticDB;
import net.williamott.plasma.R;
import net.williamott.plasma.databases.PodtasticDBHelper;

public class EpisodesFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
    private Context mContext;
    private FirebaseUser mUser;
    private SimpleCursorAdapter mAdapter;

    public EpisodesFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_episodes, container, false);
        mUser = ((MainActivity)this.getActivity()).getFirebaseUser();

        ListView episodesListView =  view.findViewById(R.id.episodes_list_view);

        String[] columns = new String[] { PodtasticDB.EPISODE_TITLE, PodtasticDB.EPISODE_SUBSCRIPTION_TRACK_TITLE, PodtasticDB.EPISODE_ALBUM_ART_URL, PodtasticDB.EPISODE_IS_NEW };
        int[] toViews = new int[] { R.id.episode_list_title, R.id.episode_list_subscription_track_title, R.id.episode_list_image, R.id.episode_list_is_new };

        mAdapter = new SimpleCursorAdapter(mContext, R.layout.episode_list_layout, null, columns, toViews, 0);
        episodesListView.setAdapter(mAdapter);

        mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (view.getId() == R.id.episode_list_image) {
                    ImageView imageView = view.findViewById(R.id.episode_list_image);
                    Glide.with(mContext).load(cursor.getString(PodtasticDB.EPISODE_ALBUM_ART_URL_COL)).into(imageView);
                    return true;
                }
                if (view.getId() == R.id.episode_list_is_new) {
                    TextView textView = view.findViewById(R.id.episode_list_is_new);
                    if (cursor.getInt(PodtasticDB.EPISODE_IS_NEW_COL) == 1) {
                        textView.setText(getString(R.string.new_episode));
                    } else {
                        textView.setText("");
                    }
                    return true;
                }
                return false;
            }
        });

        episodesListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor cursor =  mAdapter.getCursor();
                cursor.moveToPosition(position);
                String episodeId = cursor.getString(PodtasticDB.EPISODE_ID_COL);

                Intent intent = new Intent(mContext, EpisodeActivity.class);
                intent.putExtra("EPISODE_ID", episodeId);
                startActivity(intent);
            }
        });

        this.getLoaderManager().initLoader(0, null, this);

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
                Uri uri = Uri.parse("content://net.williamott.plasma.provider/episodes");
                String selection = PodtasticDB.EPISODE_USER_ID + "=?";
                String[] selectionArgs = {Integer.toString(user.getId())};
                return new CursorLoader(mContext, uri, null, selection, selectionArgs, PodtasticDB.EPISODE_PUB_DATE + " DESC LIMIT 50");
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
