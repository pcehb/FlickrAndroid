package uk.ac.kent.pceh2.imageviewer;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
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
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class FavActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String PREFS_NAME = "prefs";
    private static final String PREF_DARK_THEME = "dark_theme";
    private RecyclerView recyclerView;
    private LinearLayoutManager layoutManager;
    RecyclerView bookmarkListView;
    private ImageInfo photo;
    BookmarkAdapter adapter;
    BookmarkDBHelper dbHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Enable content transitions - before calling super.onCreate()
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setAllowEnterTransitionOverlap(true);
        // Specify the animation
        getWindow().setEnterTransition(new Fade(Fade.IN));
        getWindow().setExitTransition(new Fade(Fade.OUT));

        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean useDarkTheme = preferences.getBoolean(PREF_DARK_THEME, false);

        if (useDarkTheme) {
            setTheme(R.style.AppTheme_Dark);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fav);

        recyclerView = (RecyclerView) findViewById(R.id.photoListView);
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);

        recyclerView.hasFixedSize();
        recyclerView.setItemViewCacheSize(20);
        recyclerView.setDrawingCacheEnabled(true);
        recyclerView.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);

        isNetworkConnectionAvailable();

        // Create DB Helper
        dbHelper = new BookmarkDBHelper(this);


        bookmarkListView = (RecyclerView) findViewById(R.id.photoListView);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        bookmarkListView.setLayoutManager(layoutManager);
        adapter = new BookmarkAdapter(this);
        bookmarkListView.setAdapter(adapter);

        loadBookmarks();

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        TextView faveText = (TextView) findViewById(R.id.faveText);

        Animation out = new AlphaAnimation(1.0f, 0.0f);
        out.setDuration(5000);

        faveText.startAnimation(out);

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
            Intent intent = new Intent(FavActivity.this, MainActivity.class);
            ActivityOptions options = ActivityOptions
                    .makeSceneTransitionAnimation(FavActivity.this);
            startActivity(intent, options.toBundle());
        } else if (id == R.id.nav_search) {
            Intent intent = new Intent(FavActivity.this, SearchActivity.class);
            ActivityOptions options = ActivityOptions
                    .makeSceneTransitionAnimation(FavActivity.this);
            startActivity(intent, options.toBundle());
        } else if (id == R.id.nav_fav) {
            Intent intent = new Intent(FavActivity.this, FavActivity.class);
            ActivityOptions options = ActivityOptions
                    .makeSceneTransitionAnimation(FavActivity.this);
            startActivity(intent, options.toBundle());
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(FavActivity.this, SettingsActivity.class);
            ActivityOptions options = ActivityOptions
                    .makeSceneTransitionAnimation(FavActivity.this);
            startActivity(intent, options.toBundle());
        } else if (id == R.id.nav_about) {
            Intent intent = new Intent(FavActivity.this, AboutActivity.class);
            ActivityOptions options = ActivityOptions
                    .makeSceneTransitionAnimation(FavActivity.this);
            startActivity(intent, options.toBundle());
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


    private Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
        }
    };

    public void checkNetworkConnection() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("No internet Connection");
        builder.setMessage("Please turn on internet connection to continue");
        builder.setNegativeButton("close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public boolean isNetworkConnectionAvailable() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

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

    // Load data from the database
    private void loadBookmarks() {

        // Clear recyclerview contents
        adapter.bookmarkList.clear();

        // Prepare query
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] columns = {"bookmarkID", "id", "imageResource", "description", "owner_name", "owner", "url_l", "url_o", "title", "url_m"};
        Cursor cursor = db.query("bookmarks", columns, null, null, null, null, "bookmarkId");

        Log.d("DBDEMO", "" + cursor.getCount());

        // Go through all the entries in the database
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            int bookmarkID = cursor.getInt(0);
            String id = cursor.getString(1);
            int imageResource = cursor.getInt(2);
            String description = cursor.getString(3);
            String owner_name = cursor.getString(4);
            String owner = cursor.getString(5);
            String url_l = cursor.getString(6);
            String url_o = cursor.getString(7);
            String title = cursor.getString(8);
            String url_m = cursor.getString(9);


            // Add data to the array list for the recyclerview
            BookmarkInfo bookmark = new BookmarkInfo(bookmarkID, id, imageResource, description, owner_name, owner, url_l, url_o, title, url_m);
            adapter.bookmarkList.add(bookmark);

            // Move to next entry
            cursor.moveToNext();
        }

        cursor.close();
        db.close();

        adapter.notifyDataSetChanged();
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

                    NetworkMgr.getInstance(FavActivity.this).imageList.add(newImage);
                }
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
            adapter.notifyDataSetChanged();
        }
    };

}