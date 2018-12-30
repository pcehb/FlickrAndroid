package uk.ac.kent.pceh2.imageviewer;

import android.app.ActivityOptions;
import android.app.DownloadManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
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
import android.text.method.ScrollingMovementMethod;
import android.transition.Fade;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class DetailsActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String PREFS_NAME = "prefs";
    private static final String PREF_DARK_THEME = "dark_theme";
    private static final String PREF_COMMENT_START = "comment_start";
    private int photoPosition;
    private ImageInfo photo;
    private TextView titleView;
    private TextView ownerView;
    private TextView descriptionView;
    private RecyclerView commentView;
    private ImageView imageView;
    ImageButton imageDownload;
    ImageButton imageShare;
    ImageButton imageHeart;
    ImageButton imageComment;
    private Boolean isFabOpen = true;

    private TextView noComment;


    private CommentListAdapter adapter;
    private ProgressBar progressBar;
    SwipeRefreshLayout swipeRefreshLayout;
    private RecyclerView recyclerView;

    BookmarkDBHelper dbHelper;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        // Enable content transitions - before calling super.onCreate()
        getWindow().requestFeature(Window.FEATURE_CONTENT_TRANSITIONS);
        getWindow().setAllowEnterTransitionOverlap(true);
        // Specify the animation
        getWindow().setEnterTransition(new Fade(Fade.IN));
        getWindow().setExitTransition(new Fade(Fade.OUT));

        //gets prefs
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean useDarkTheme = preferences.getBoolean(PREF_DARK_THEME, false);

        if (useDarkTheme) {
            setTheme(R.style.AppTheme_Dark); //sets theme
        }

        boolean commentStart = preferences.getBoolean(PREF_COMMENT_START, false);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);

        Intent intent = getIntent();
        photoPosition = intent.getIntExtra("PHOTO_POSITION", 0);

        photo = NetworkMgr.getInstance(this).imageList.get(photoPosition);

        titleView = (TextView) findViewById(R.id.imageTitle);
        titleView.setText(photo.title);
        titleView.setMovementMethod(new ScrollingMovementMethod());
        descriptionView = (TextView) findViewById(R.id.imageDescription);
        descriptionView.setText(photo.description);
        descriptionView.setMovementMethod(new ScrollingMovementMethod()); //set to allow scrolling

        noComment = (TextView) findViewById(R.id.imageNoCommentsText);
        noComment.setText("No comments yet on this photo");
        noComment.setMovementMethod(new ScrollingMovementMethod());

        commentView = (RecyclerView) findViewById(R.id.imageCommentsText);

        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        commentView.setLayoutManager(layoutManager);

        ownerView = (TextView) findViewById(R.id.imageOwner);
        ownerView.setText(photo.owner_name);
        imageView = findViewById(R.id.imageView);
        // Create DB Helper
        dbHelper = new BookmarkDBHelper(this);


        NetworkMgr netMgr = NetworkMgr.getInstance(getApplicationContext());

        if (photo.url_o != null) {
            netMgr.imageLoader.get(photo.url_o, imageListener);
        }
        if (photo.url_l != null) {
            netMgr.imageLoader.get(photo.url_l, imageListener);
        }
        if (photo.url_m != null) {
            netMgr.imageLoader.get(photo.url_m, imageListener);
        }

        addListenerOnButton();


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actiontoggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(actiontoggle);
        actiontoggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        descriptionView.setVisibility(View.VISIBLE); //des visable, comments invis
        noComment.setVisibility(View.INVISIBLE);
        commentView.setVisibility(View.INVISIBLE);

        isNetworkConnectionAvailable(); //check internet

        adapter = new CommentListAdapter(this);
        commentView.setAdapter(adapter);

        adapter.commentList = NetworkMgr.getInstance(this).commentList;

        RequestQueue requestQueue = netMgr.requestQueue;


        swipeRefreshLayout = findViewById(R.id.swiperefresh);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);

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


        imageComment = (ImageButton) findViewById(R.id.imageComments);

        if (commentStart) { //if comments open on start pref then change bool to true and animate the FAB
            isFabOpen = true;
            animateFAB();
        }

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
            Intent intent = new Intent(DetailsActivity.this, MainActivity.class);
            ActivityOptions options = ActivityOptions
                    .makeSceneTransitionAnimation(DetailsActivity.this);
            startActivity(intent, options.toBundle());
        } else if (id == R.id.nav_search) {
            Intent intent = new Intent(DetailsActivity.this, SearchActivity.class);
            ActivityOptions options = ActivityOptions
                    .makeSceneTransitionAnimation(DetailsActivity.this);
            startActivity(intent, options.toBundle());
        } else if (id == R.id.nav_fav) {
            Intent intent = new Intent(DetailsActivity.this, FavActivity.class);
            ActivityOptions options = ActivityOptions
                    .makeSceneTransitionAnimation(DetailsActivity.this);
            startActivity(intent, options.toBundle());
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(DetailsActivity.this, SettingsActivity.class);
            ActivityOptions options = ActivityOptions
                    .makeSceneTransitionAnimation(DetailsActivity.this);
            startActivity(intent, options.toBundle());
        } else if (id == R.id.nav_about) {
            Intent intent = new Intent(DetailsActivity.this, AboutActivity.class);
            ActivityOptions options = ActivityOptions
                    .makeSceneTransitionAnimation(DetailsActivity.this);
            startActivity(intent, options.toBundle());
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private Response.Listener<JSONObject> responseListener = new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {

            noComment.setVisibility(View.VISIBLE);

            adapter.commentList.clear();
            try {
                JSONObject comments = response.getJSONObject("comments");
                JSONArray commentList = comments.getJSONArray("comment");

                for (int i = 0; i < commentList.length(); i++) {
                    CommentInfo newComment = new CommentInfo();

                    JSONObject comment = commentList.getJSONObject(i);

                    newComment._content = comment.getString("_content");
                    newComment.realname = comment.getString("realname");

                    adapter.commentList.add(newComment);

                    noComment.setVisibility(View.INVISIBLE);
                }

            } catch (JSONException ex) {
                ex.printStackTrace();
            }
            adapter.notifyDataSetChanged();
            if (swipeRefreshLayout.isRefreshing()) {
                swipeRefreshLayout.setRefreshing(false);
                noComment.setVisibility(View.INVISIBLE);

            }
            progressBar.setVisibility(View.GONE);
        }
    };

    private Response.ErrorListener errorListener = new Response.ErrorListener() {
        @Override
        public void onErrorResponse(VolleyError error) {
        }
    };

    public void addListenerOnButton() {

        imageDownload = (ImageButton) findViewById(R.id.imageDownload);


        imageDownload.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) { //download image

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DetailsActivity.this);
                alertDialogBuilder.setMessage("Are you sure you want to download this image?");
                alertDialogBuilder.setPositiveButton("yes",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface arg0, int arg1) {

                                if (photo.url_o != null) {
                                    Uri image_uri = Uri.parse(photo.url_o);
                                    DownloadData(image_uri);
                                } else if (photo.url_l != null) {
                                    Uri image_uri = Uri.parse(photo.url_l);
                                    DownloadData(image_uri);
                                } else if (photo.url_m != null) {
                                    Uri image_uri = Uri.parse(photo.url_m);
                                    DownloadData(image_uri);
                                }


                                Toast.makeText(DetailsActivity.this, "Image Downloaded", Toast.LENGTH_LONG).show();
                            }
                        });

                alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                });

                AlertDialog alertDialog = alertDialogBuilder.create();
                alertDialog.show();

            }

        });

        imageShare = (ImageButton) findViewById(R.id.imageShare);

        imageShare.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) { //share iamge
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT, "https://www.flickr.com/photos/" + photo.ownerId + "/" + photo.id + "/");
                startActivity(Intent.createChooser(shareIntent, "Share link using"));
            }

        });

        imageHeart = (ImageButton) findViewById(R.id.imageDelete);

        imageHeart.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                askNewBookmark();
            }

        }); //add to faves

        imageComment = (ImageButton) findViewById(R.id.imageComments);

        imageComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) {
                animateFAB();
            }

        });//show comments

        ownerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View arg0) { //go to owners photos
                Intent intent = new Intent(DetailsActivity.this, UserActivity.class);
                ActivityOptions options = ActivityOptions
                        .makeSceneTransitionAnimation(DetailsActivity.this);
                intent.putExtra("ownerNumber", photo.ownerId); //pass information needed in UserActivity
                intent.putExtra("ownerName", photo.owner_name);
                startActivity(intent, options.toBundle());
            }

        });

    }


    public void animateFAB() {

        if (isFabOpen) { //hides des and shows comments. gets comments. changes FAB icon
            imageComment.setImageResource(R.drawable.icons8_info_filled_50);
            isFabOpen = false;
            descriptionView.setVisibility(View.INVISIBLE);
            commentView.setVisibility(View.VISIBLE);

            adapter.commentList.clear();
            NetworkMgr netMgr = NetworkMgr.getInstance(getApplicationContext());
            RequestQueue requestQueue = netMgr.requestQueue;
            String url = "https://api.flickr.com/services/rest/?method=flickr.photos.comments.getList&api_key=3b39db216c7028189a1319b050b79fed&format=json&nojsoncallback=1&photo_id=" + photo.id;
            JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, responseListener, errorListener);

            requestQueue.add(request);

        } else { //hides comments and shows des. changes FAB icon

            isFabOpen = true;

            imageComment.setImageResource(R.drawable.icons8_chat_bubble_filled_50);
            noComment.setVisibility(View.INVISIBLE);
            commentView.setVisibility(View.INVISIBLE);
            descriptionView.setVisibility(View.VISIBLE);


        }

    }


    private long DownloadData(Uri uri) { //downloads photo

        long downloadReference;

        // Create request for android download manager
        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        //Setting title of request
        request.setTitle("Data Download");

        //Setting description of request
        request.setDescription("Android Data download using DownloadManager.");

        //Set the local destination for the downloaded file to a path within the application's external files directory
        request.setDestinationInExternalFilesDir(DetailsActivity.this, Environment.DIRECTORY_DOWNLOADS, "" + photo.id + ".jpg");

        //Enqueue download and save into referenceId
        downloadReference = downloadManager.enqueue(request);

        return downloadReference;
    }

    private ImageLoader.ImageListener imageListener = new ImageLoader.ImageListener() {
        @Override
        public void onErrorResponse(VolleyError error) {

        }

        @Override
        public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
            if (response.getBitmap() != null)
                imageView.setImageBitmap(response.getBitmap());
        }
    };

    // Create dialog box to ask for values for the new entry
    private void askNewBookmark() {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DetailsActivity.this);
        alertDialogBuilder.setMessage("Are you sure you want to favouite this image?");
        alertDialogBuilder.setPositiveButton("yes",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface arg0, int arg1) {

                        String id = photo.id;
                        int imageResource = photo.imageResource;
                        String description = photo.description;
                        String owner_name = photo.owner_name;
                        String owner = photo.owner;
                        String url_l = photo.url_l;
                        String url_o = photo.url_o;
                        String url_m = photo.url_m;
                        String title = photo.title;


                        dbHelper.insert(id, imageResource, description, owner_name, owner, url_l, url_o, title, url_m);
                        Toast.makeText(DetailsActivity.this, "Image Faved", Toast.LENGTH_LONG).show();
                    }
                });

        alertDialogBuilder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }


    public void checkNetworkConnection() { //no internet connection
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

    void refresh() { //gets new comments
        adapter.commentList.clear();
        NetworkMgr netMgr = NetworkMgr.getInstance(getApplicationContext());
        RequestQueue requestQueue = netMgr.requestQueue;


        String url = "https://api.flickr.com/services/rest/?method=flickr.photos.comments.getList&api_key=3b39db216c7028189a1319b050b79fed&format=json&nojsoncallback=1&photo_id=" + photo.id;
        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null, responseListener, errorListener);

        requestQueue.add(request);


        isFabOpen = false;
        animateFAB();

    }
}
