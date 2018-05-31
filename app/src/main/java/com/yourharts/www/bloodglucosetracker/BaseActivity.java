package com.yourharts.www.bloodglucosetracker;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import static android.app.PendingIntent.getActivity;

public class BaseActivity extends AppCompatActivity {
    private DrawerLayout _drawerLayout;
    private NavigationView _navDrawerView;
    android.support.v7.app.ActionBarDrawerToggle _drawerToggle;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);


    }


    @Override
    public void setContentView(@LayoutRes int layoutResID)
    {
        final DrawerLayout fullView = (DrawerLayout) getLayoutInflater().inflate(R.layout.layout_base, null);
        FrameLayout activityContainer = fullView.findViewById(R.id.content_frame);
        getLayoutInflater().inflate(layoutResID, activityContainer, true);

        super.setContentView(fullView);
        setupActionBarAndNavigation();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                _drawerLayout.openDrawer(GravityCompat.START);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void setupActionBarAndNavigation() {
        _drawerLayout = findViewById(R.id.drawer_layout);
        _navDrawerView = findViewById(R.id.nav_view);
        Toolbar toolbar = findViewById(R.id.toolbar);
        (BaseActivity.this).setSupportActionBar(toolbar);
        ActionBar actionbar = BaseActivity.this.getSupportActionBar();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        _drawerToggle = new ActionBarDrawerToggle(this, _drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        _drawerLayout.setDrawerListener(_drawerToggle);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);


        _navDrawerView.setNavigationItemSelectedListener(
                menuItem -> {
                    // set item as selected to persist highlight
                    menuItem.setChecked(true);
                    // close drawer when item is tapped
                    _drawerLayout.closeDrawers();

                    switch (menuItem.getItemId()){
                        case R.id.nav_settings : {
                            Intent intent = new Intent(this, SettingsActivity.class);
                            startActivity(intent);
                            return true;
                        }
                        case R.id.nav_charts: {
                            Intent intent = new Intent(this, ChartsActivity.class);
                            startActivity(intent);
                            return true;
                        }
                        case R.id.nav_data:{
                            Intent intent = new Intent(this, DataActivity.class);
                            startActivity(intent);
                            return true;
                        }
                        case R.id.nav_main:{
                            Intent intent = new Intent(this, MainActivity.class);
                            startActivity(intent);
                            return true;
                        }
                    }

                    return true;
                });

    }
    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if(_drawerToggle != null)
            _drawerToggle.syncState();
    }

    @Override
    public void finish() {
        super.finish();
        overridePendingTransitionExit();
    }

    @Override
    public void startActivity(Intent intent) {
        super.startActivity(intent);
        overridePendingTransitionEnter();
    }

    /**
     * Overrides the pending Activity transition by performing the "Enter" animation.
     */
    protected void overridePendingTransitionEnter() {
        overridePendingTransition(R.anim.slide_from_right, R.anim.slide_to_left);
    }

    /**
     * Overrides the pending Activity transition by performing the "Exit" animation.
     */
    protected void overridePendingTransitionExit() {
        overridePendingTransition(R.anim.slide_from_left, R.anim.slide_to_right);
    }


}
