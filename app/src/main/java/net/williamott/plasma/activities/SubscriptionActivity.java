package net.williamott.plasma.activities;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import net.williamott.plasma.databases.PodtasticDB;
import net.williamott.plasma.R;
import net.williamott.plasma.databases.PodtasticDBHelper;

public class SubscriptionActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private Context mContext;
    private SimpleCursorAdapter mAdapter;
    private String mSubscriptionId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_subscription);

        mSubscriptionId = getIntent().getStringExtra("SUBSCRIPTION_ID");
        mContext = this;
        ListView subscriptionListView = findViewById(R.id.subscription_list_view);

        String[] columns = new String[] { PodtasticDB.EPISODE_TITLE, PodtasticDB.EPISODE_SUBSCRIPTION_TRACK_TITLE, PodtasticDB.EPISODE_ALBUM_ART_URL, PodtasticDB.EPISODE_IS_NEW };
        int[] toViews = new int[] { R.id.episode_list_title, R.id.episode_list_subscription_track_title, R.id.episode_list_image, R.id.episode_list_is_new };

        mAdapter = new SimpleCursorAdapter(mContext, R.layout.episode_list_layout, null, columns, toViews, 0);
        subscriptionListView.setAdapter(mAdapter);

        subscriptionListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
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

        mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int columnIndex) {
                if (view.getId() == R.id.episode_list_image) {
                    ImageView imageView = view.findViewById(R.id.episode_list_image);
                    Glide.with(mContext).load(cursor.getString(PodtasticDB.EPISODE_ALBUM_ART_URL_COL)).error(PodtasticDBHelper.getFallbackDrawable(mContext)).into(imageView);
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
        getSupportLoaderManager().initLoader(0, null, this);
    }

    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Uri uri = Uri.parse("content://net.williamott.plasma.provider/episodes");
        String selection = PodtasticDB.EPISODE_SUBSCRIPTION_ID + "=?";
        String[] selectionArgs = {mSubscriptionId};
        return new CursorLoader(mContext, uri, null, selection, selectionArgs, PodtasticDB.EPISODE_PUB_DATE + " DESC");
    }

    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
