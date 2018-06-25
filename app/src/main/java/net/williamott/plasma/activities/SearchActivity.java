package net.williamott.plasma.activities;

import android.app.SearchManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ResultReceiver;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.gson.Gson;

import net.williamott.plasma.classes.Subscription;
import net.williamott.plasma.databases.PodtasticDB;
import net.williamott.plasma.R;
import net.williamott.plasma.classes.Result;
import net.williamott.plasma.classes.SearchResults;
import net.williamott.plasma.databases.PodtasticDBHelper;
import net.williamott.plasma.services.SubscriptionService;

import java.util.List;

public class SearchActivity extends AppCompatActivity {
    private String url;
    private String term;
    private String[] terms;
    private String newTerm;
    private RequestQueue queue;
    private Gson mGson;
    private SearchResults mSearchResults;
    private List<Result> mResults;
    private RecyclerView mSearchResultsRecyclerView;
    private Context mContext;
    private LinearLayout mSearchProgressLinear;
    private SubscriptionService mSubscriptionService;
    private boolean mSubscriptionServiceBound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        Toolbar myToolbar = findViewById(R.id.search_toolbar);
        setSupportActionBar(myToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        mContext = this;
        mSearchProgressLinear = findViewById(R.id.search_progress_linear);
        queue = Volley.newRequestQueue(this);
        url ="https://itunes.apple.com/search?term=&entity=podcast";

        mSearchResultsRecyclerView = findViewById(R.id.search_recycler_view);
        mSearchResultsRecyclerView.addItemDecoration(new DividerItemDecoration(mContext,
                DividerItemDecoration.VERTICAL));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.search_options_menu, menu);
        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search_search).getActionView();
        if (searchManager != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));
        }
        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint("Search for podcasts...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mSearchProgressLinear.setVisibility(View.VISIBLE);
                mSearchResultsRecyclerView.setVisibility(View.GONE);
                term = query;
                terms = term.split(" ");
                newTerm = "";
                for (String s : terms) {
                    if (newTerm.isEmpty()) {
                        newTerm = s;
                    } else {
                        StringBuilder sb = new StringBuilder();
                        newTerm = sb.append(newTerm).append("+").append(s).toString();
                    }
                }

                url = url + "&term=" + newTerm;

                StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                        new Response.Listener<String>() {
                            @Override
                            public void onResponse(String response) {
                                mGson = new Gson();
                                mSearchResults = mGson.fromJson(response, SearchResults.class);
                                mResults = mSearchResults.getResults();

                                mSearchProgressLinear.setVisibility(View.GONE);
                                mSearchResultsRecyclerView.setVisibility(View.VISIBLE);

                                ResultsAdapter adapter = new ResultsAdapter(mResults);
                                RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
                                mSearchResultsRecyclerView.setLayoutManager(mLayoutManager);
                                mSearchResultsRecyclerView.setAdapter(adapter);

                            }
                        }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Toast.makeText(SearchActivity.this, getString(R.string.search_error), Toast.LENGTH_SHORT).show();
                        mSearchProgressLinear.setVisibility(View.GONE);
                        mSearchResultsRecyclerView.setVisibility(View.VISIBLE);
                    }
                });

                queue.add(stringRequest);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                return false;
            }
        });

        return true;
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

    public class ResultsAdapter extends RecyclerView.Adapter<ResultsAdapter.MyViewHolder> {
        private List<Result> resultsList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView trackName;
            public TextView artistName;
            public ImageView albumArt;
            public Button subscribeButton;

            public MyViewHolder(View view) {
                super(view);
                trackName = (TextView) view.findViewById(R.id.search_result_track_name_text_view);
                artistName = (TextView) view.findViewById(R.id.search_result_artist_name_text_view);
                albumArt = (ImageView) view.findViewById(R.id.search_result_album_art_image_view);
                subscribeButton = (Button) view.findViewById(R.id.search_result_subscribe_button);
            }
        }

        public ResultsAdapter(List<Result> resultsList) {
            this.resultsList = resultsList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.search_result_layout, parent, false);

            return  new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, int position) {
            final Result result = resultsList.get(position);
            Subscription subscription = PodtasticDBHelper.getSubscriptionFromTrackId(mContext, result.getTrackId());

            if (subscription != null) {
                holder.setIsRecyclable(false);
                holder.trackName.setText(result.getTrackName());
                holder.artistName.setText(result.getArtistName());
                String url = result.getArtworkUrl600();
                Glide.with(mContext).load(url).error(PodtasticDBHelper.getFallbackDrawable(mContext)).into(holder.albumArt);
                Button subscribeButtonFinal = holder.subscribeButton.findViewById(R.id.search_result_subscribe_button);
                subscribeButtonFinal.setText(getString(R.string.subscribed));
                subscribeButtonFinal.setBackgroundResource(R.drawable.button_bg_rounded_corners_disabled);
                subscribeButtonFinal.setEnabled(false);
            } else {
                holder.setIsRecyclable(false);
                holder.trackName.setText(result.getTrackName());
                holder.artistName.setText(result.getArtistName());
                String url = result.getArtworkUrl600();
                Glide.with(mContext).load(url).error(PodtasticDBHelper.getFallbackDrawable(mContext)).into(holder.albumArt);
                holder.subscribeButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (mSubscriptionServiceBound) {
                            mSubscriptionService.addSubsciption(result.getTrackName(), result.getFeedUrl(), result.getArtistName(), result.getTrackId(), result.getArtworkUrl600(), result.getReleaseDate(), new ResultReceiver(new Handler()) {
                                @Override
                                protected void onReceiveResult(int resultCode, Bundle resultData) {
                                    Button subscribeButtonFinal = holder.subscribeButton.findViewById(R.id.search_result_subscribe_button);
                                    subscribeButtonFinal.setText(getString(R.string.subscribed));
                                    subscribeButtonFinal.setBackgroundResource(R.drawable.button_bg_rounded_corners_disabled);
                                    subscribeButtonFinal.setEnabled(false);
                                }
                            });
                        }
                    }
                });
            }
        }

        @Override
        public int getItemCount() {
            return resultsList.size();
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
}
