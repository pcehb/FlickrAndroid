package uk.ac.kent.pceh2.imageviewer;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.transition.Fade;
import android.view.MenuItem;
import android.view.Window;

/**
 * Created by Phoebe on 22/11/2017.
 */

public class AboutActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String PREFS_NAME = "prefs";
    private static final String PREF_DARK_THEME = "dark_theme";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
        setContentView(R.layout.activity_about);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle actiontoggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(actiontoggle);
        actiontoggle.syncState();

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
            Intent intent = new Intent(AboutActivity.this, MainActivity.class);
            ActivityOptions options = ActivityOptions
                    .makeSceneTransitionAnimation(AboutActivity.this);
            startActivity(intent, options.toBundle());
        } else if (id == R.id.nav_search) {
            Intent intent = new Intent(AboutActivity.this, SearchActivity.class);
            ActivityOptions options = ActivityOptions
                    .makeSceneTransitionAnimation(AboutActivity.this);
            startActivity(intent, options.toBundle());
        } else if (id == R.id.nav_fav) {
            Intent intent = new Intent(AboutActivity.this, FavActivity.class);
            ActivityOptions options = ActivityOptions
                    .makeSceneTransitionAnimation(AboutActivity.this);
            startActivity(intent, options.toBundle());
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(AboutActivity.this, SettingsActivity.class);
            ActivityOptions options = ActivityOptions
                    .makeSceneTransitionAnimation(AboutActivity.this);
            startActivity(intent, options.toBundle());
        } else if (id == R.id.nav_about) {
            Intent intent = new Intent(AboutActivity.this, AboutActivity.class);
            ActivityOptions options = ActivityOptions
                    .makeSceneTransitionAnimation(AboutActivity.this);
            startActivity(intent, options.toBundle());
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }


}
