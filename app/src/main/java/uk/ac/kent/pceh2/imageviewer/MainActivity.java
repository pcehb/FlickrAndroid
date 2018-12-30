package uk.ac.kent.pceh2.imageviewer;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    private static final String PREFS_NAME = "prefs";
    private static final String PREF_DARK_THEME = "dark_theme";
    private static final String PREF_SAFE_SEARCH = "safe_search";
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    private ImageListAdapter adapter;
    private ProgressBar progressBar;
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Enable content transitions - before calling super.onCreate()
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setAllowEnterTransitionOverlap(true);
        // Specify the animation
        getWindow().setEnterTransition(new Fade(Fade.IN));
        getWindow().setExitTransition(new Fade(Fade.OUT));

        //gets prefs
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean useDarkTheme = preferences.getBoolean(PREF_DARK_THEME, false);
        boolean useSafeSearch = preferences.getBoolean(PREF_SAFE_SEARCH, false);

        if (useDarkTheme) {
            setTheme(R.style.AppTheme_Dark); //sets theme
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        swipeRefreshLayout = findViewById(R.id.swiperefresh);
        recyclerView = (RecyclerView) findViewById(R.id.photoListView);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.hasFixedSize();
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        isNetworkConnectionAvailable();

        adapter = new ImageListAdapter(this);
        recyclerView.setAdapter(adapter);

        adapter.imageList = NetworkMgr.getInstance(this).imageList;

        NetworkMgr netMgr = NetworkMgr.getInstance(getApplicationContext());
        RequestQueue requestQueue = netMgr.requestQueue;


        if (useSafeSearch) {
            String url = "https://api.flickr.com/services/rest/?method=flickr.photos.getRecent&api_key=3b39db216c7028189a1319b050b79fed&format=json&nojsoncallback=?&extras=description,owner_name,url_m,url_l,url_o,date_taken&safe_search=1&per_page=50";
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, responseListener, errorListener);

            progressBar = (ProgressBar) findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);

            requestQueue.add(request);
        } else {
            String url = "https://api.flickr.com/services/rest/?method=flickr.photos.getRecent&api_key=3b39db216c7028189a1319b050b79fed&format=json&nojsoncallback=?&extras=description,owner_name,url_m,url_l,url_o,date_taken&per_page=50";
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, responseListener, errorListener);

            progressBar = (ProgressBar) findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);

            requestQueue.add(request);
        }


        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        isNetworkConnectionAvailable();
                        refresh();
                    }
                }
        );
        //This are some optional methods for customizing
        // the colors and size of the loader.
        swipeRefreshLayout.setColorSchemeResources(
                R.color.blue,       //This method will rotate
                R.color.red,        //colors given to it when
                R.color.yellow,     //loader continues to
                R.color.green);     //refresh.

        //setSize() Method Sets The Size Of Loader
        swipeRefreshLayout.setSize(SwipeRefreshLayout.LARGE);
        //Below Method Will set background color of Loader
        swipeRefreshLayout.setProgressBackgroundColorSchemeResource(R.color.white);
        swipeRefreshLayout.setProgressViewOffset(false, 0, 250);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {
            Intent intent = new Intent(MainActivity.this, MainActivity.class);
            ActivityOptions options = ActivityOptions
                    .makeSceneTransitionAnimation(MainActivity.this);
            startActivity(intent, options.toBundle());
        } else if (id == R.id.nav_search) {
            Intent intent = new Intent(MainActivity.this, SearchActivity.class);
            ActivityOptions options = ActivityOptions
                    .makeSceneTransitionAnimation(MainActivity.this);
            startActivity(intent, options.toBundle());
        } else if (id == R.id.nav_fav) {
            Intent intent = new Intent(MainActivity.this, FavActivity.class);
            ActivityOptions options = ActivityOptions
                    .makeSceneTransitionAnimation(MainActivity.this);
            startActivity(intent, options.toBundle());
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            ActivityOptions options = ActivityOptions
                    .makeSceneTransitionAnimation(MainActivity.this);
            startActivity(intent, options.toBundle());
        } else if (id == R.id.nav_about) {
            Intent intent = new Intent(MainActivity.this, AboutActivity.class);
            ActivityOptions options = ActivityOptions
                    .makeSceneTransitionAnimation(MainActivity.this);
            startActivity(intent, options.toBundle());
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private Response.Listener<JSONObject> responseListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {

            try {
                JSONObject photos = response.getJSONObject("photos");
                JSONArray photoList = photos.getJSONArray("photo");

                for (int i = 0; i < photoList.length(); i++) {
                    ImageInfo newImage = new ImageInfo();

                    JSONObject photo = photoList.getJSONObject(i);

                    newImage.id = photo.getString("id");
                    newImage.title = photo.getString("title");
                    newImage.owner_name = photo.getString("ownername");
                    newImage.owner = photo.getString("owner");


                    if (photo.has("url_m")) {
                        newImage.url_m = photo.getString("url_m");
                    } else if (photo.has("url_l")) {
                        newImage.url_m = photo.getString("url_l");
                    } else if (photo.has("url_o")) {
                        newImage.url_m = photo.getString("url_o");
                    }

                    newImage.ownerId = photo.getString("owner");
                    newImage.owner = "https://www.flickr.com/buddyicons/" + newImage.owner + ".jpg";

                    JSONObject descrObj = photo.getJSONObject("description");
                    newImage.description = descrObj.getString("_content");

                    NetworkMgr.getInstance(MainActivity.this).imageList.add(newImage);
                }
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
            adapter.notifyDataSetChanged();
            if (swipeRefreshLayout.isRefreshing())
                swipeRefreshLayout.setRefreshing(false);
            progressBar.setVisibility(View.GONE);
        }
    };

    private Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
        }
    };


    void refresh() {
        adapter.imageList.clear();
        NetworkMgr netMgr = NetworkMgr.getInstance(getApplicationContext());
        RequestQueue requestQueue = netMgr.requestQueue;

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean useSafeSearch = preferences.getBoolean(PREF_SAFE_SEARCH, false);

        if (useSafeSearch) {
            String url = "https://api.flickr.com/services/rest/?method=flickr.photos.getRecent&api_key=3b39db216c7028189a1319b050b79fed&format=json&nojsoncallback=?&extras=description,owner_name,url_m,url_l,url_o,date_taken&safe_search=1&per_page=50";
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, responseListener, errorListener);

            progressBar = (ProgressBar) findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);

            requestQueue.add(request);
        } else {
            String url = "https://api.flickr.com/services/rest/?method=flickr.photos.getRecent&api_key=3b39db216c7028189a1319b050b79fed&format=json&nojsoncallback=?&extras=description,owner_name,url_m,url_l,url_o,date_taken&per_page=50";
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, responseListener, errorListener);

            progressBar = (ProgressBar) findViewById(R.id.progressBar);
            progressBar.setVisibility(View.VISIBLE);

            requestQueue.add(request);
        }
    }

    public void checkNetworkConnection() { //no internet alert
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No internet Connection");
        builder.setMessage("Please turn on internet connection to continue");
        builder.setNegativeButton("close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                swipeRefreshLayout.setRefreshing(false);
                progressBar.setVisibility(View.GONE);
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public boolean isNetworkConnectionAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();

        boolean isConnected = activeNetwork != null &&
                activeNetwork.isConnected();
        if (isConnected) {
            Log.d("Network", "Connected");
            return true;
        } else {
            checkNetworkConnection();
            Log.d("Network", "Not Connected");
            return false;
        }
    }


}