package com.yourharts.www.bloodglucosetracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;

import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.PopupWindow;
import android.widget.Switch;


import com.yourharts.www.Adapters.GlucoseMeasurementAdapter;
import com.yourharts.www.Database.DBHelper;
import com.yourharts.www.Models.BloodMeasurementModel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import static android.support.v4.content.FileProvider.getUriForFile;

public class MainActivity extends AppCompatActivity {
    private RecyclerView _measurementView;
    private GlucoseMeasurementAdapter _adapter;
    private RecyclerView.LayoutManager _layoutManager;
    private DBHelper _dbHelper;
    private DateFormat _dbDateFormat;
    private SharedPreferences _sharedPref;

    private CardView _gettingStartedCard;
    private FloatingActionButton _fab;
    private Switch _showHighOnlySW;
    private Switch _showBreakfastSW;
    private Switch _showLunchSW;
    private Switch _showDinnerSW;
    private Switch _showBedtimeSW;
    private View _popupFilterView;
    private MainActivityListener _mainActivitylistener;
    private List<BloodMeasurementModel> _filteredMeasurements = new ArrayList<>();
    PopupWindow _dropDownMenu;
    private DrawerLayout _drawerLayout;
    android.support.v7.app.ActionBarDrawerToggle _drawerToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);
        _drawerLayout = findViewById(R.id.drawer_layout);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        _sharedPref = getSharedPreferences(getString(R.string.pref_file_key), Context.MODE_PRIVATE);



        _mainActivitylistener = new MainActivityListener(MainActivity.this, _sharedPref);
        _popupFilterView = layoutInflater.inflate(R.layout.layout_drop_down_filters, null);
        _dropDownMenu = new PopupWindow(_popupFilterView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        _dropDownMenu.setOutsideTouchable(true);



        _dbDateFormat = new SimpleDateFormat(getString(R.string.database_date_time_format));
        _dbHelper = new DBHelper(getApplicationContext(), getFilesDir().getPath(), this);
        _measurementView = findViewById(R.id.bloodGlucoseMeasurementsRecyclerView);

        _gettingStartedCard = findViewById(R.id.getting_started_card);

        _measurementView.setHasFixedSize(true);

        _layoutManager = new LinearLayoutManager(this);
        _measurementView.setLayoutManager(_layoutManager);
        if (_sharedPref.getBoolean("IS_FIRST_RUN", true)){
            _gettingStartedCard.setVisibility(View.VISIBLE);
            SharedPreferences.Editor editor = _sharedPref.edit();
            editor.putBoolean("IS_FIRST_RUN", false);
            editor.apply();
            editor.commit();
        }
        else{
            _gettingStartedCard.setVisibility(View.GONE);
        }


        try {
            _dbHelper.prepareDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }


        setupListeners();

        loadMeasurements();
        setTitle(R.string.Measurements);
        setupNavDrawerAndTooldbar();
    }

    private void setupNavDrawerAndTooldbar() {
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(
                menuItem -> {


                    // close drawer when item is tapped
                    _drawerLayout.closeDrawers();

                    switch(menuItem.getItemId()) {
                        case R.id.nav_settings: {
                            Intent intent = new Intent(this, SettingsActivity.class);
                            startActivity(intent);
                            return true;
                        }
                        case R.id.nav_charts: {
                            Intent intent = new Intent(this, ChartsActivity.class);
                            startActivity(intent);
                            return true;
                        }
                        case R.id.nav_data: {
                            Intent intent = new Intent(this, DataActivity.class);
                            startActivity(intent);
                            return true;
                        }
                    }
                    return true;
                });
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_menu_black_24dp);
        _drawerToggle = new ActionBarDrawerToggle(this, _drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);

        _drawerLayout.setDrawerListener(_drawerToggle);
    }


    private void setupListeners() {
        _dropDownMenu.setOnDismissListener(_mainActivitylistener);
        _showHighOnlySW = _popupFilterView.findViewById(R.id.filter_switch_show_high_only);
        _showHighOnlySW.setOnCheckedChangeListener(_mainActivitylistener);
        _showBreakfastSW = _popupFilterView.findViewById(R.id.filter_switch_show_breakfast);
        _showBreakfastSW.setOnCheckedChangeListener(_mainActivitylistener);
        _showLunchSW = _popupFilterView.findViewById(R.id.filter_switch_show_lunch);
        _showLunchSW.setOnCheckedChangeListener(_mainActivitylistener);
        _showDinnerSW = _popupFilterView.findViewById(R.id.filter_switch_show_dinner);
        _showDinnerSW.setOnCheckedChangeListener(_mainActivitylistener);
        _showBedtimeSW = _popupFilterView.findViewById(R.id.filter_switch_show_bedtime);
        _showBedtimeSW.setOnCheckedChangeListener(_mainActivitylistener);
        _fab = findViewById(R.id.fab);
        _fab.setOnClickListener(_mainActivitylistener);
    }




    private void loadMeasurements() {
        LoadMeasurementsAsync backgroundWorker = new LoadMeasurementsAsync();
        backgroundWorker.execute();

    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMeasurements();

        if (_sharedPref.getBoolean("IS_FIRST_RUN", true)){
            _gettingStartedCard.setVisibility(View.VISIBLE);
            SharedPreferences.Editor editor = _sharedPref.edit();
            editor.putBoolean("IS_FIRST_RUN", false);
            editor.apply();
            editor.commit();
        }
        else{
            _gettingStartedCard.setVisibility(View.GONE);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
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
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        switch (item.getItemId()) {
            case android.R.id.home:
                _drawerLayout.openDrawer(GravityCompat.START);
                return true;
            case R.id.menu_item_share: {
                String delimiter = _sharedPref.getString("PREF_DEFAULT_CSVDELIMITER", "~");
                String csv = _dbHelper.getMeasurementsCSVText(delimiter);
                File csvFilePath = new File(getApplicationContext().getFilesDir().getPath(), "csv");
                csvFilePath.mkdirs();
                File newFile = new File(csvFilePath, UUID.randomUUID().toString() + ".csv");
                try {
                    FileWriter writer = new FileWriter(newFile);
                    writer.write(csv);
                    writer.flush();
                    writer.close();

                    Uri contentUri = getUriForFile(MainActivity.this, "com.yourharts.www.bloodglucosetracker.fileprovider", newFile);
                    Intent sendIntent = new Intent();
                    sendIntent.setAction(Intent.ACTION_SEND);
                    sendIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    sendIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                    sendIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Blood glucose measurements_" + _dbDateFormat.format(Calendar.getInstance().getTime()));
                    sendIntent.setDataAndType(null, getContentResolver().getType(contentUri));

                    startActivity(sendIntent);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
            case R.id.menu_item_filter: {
                _dropDownMenu.showAsDropDown(findViewById(R.id.menu_item_filter));
            }


        }
        return super.onOptionsItemSelected(item);
    }





    public DBHelper getDBHelper() {
        return _dbHelper;
    }


    public void notifyPreferencesChanged() {
        loadMeasurements();
    }


    private class LoadMeasurementsAsync extends AsyncTask<Void, Void,List< BloodMeasurementModel> >{
        @Override
        protected List<BloodMeasurementModel> doInBackground(Void... voids) {
            List<BloodMeasurementModel> measurements = _dbHelper.getAllBloodMeasurements();
            List<BloodMeasurementModel> filteredMeasurements = new ArrayList<>();
            for (BloodMeasurementModel bmm : measurements) {
                if (_showHighOnlySW.isChecked()) {
                    if (bmm.isHigh()) {
                        if(_dbHelper.getMeasurementPositionInList(filteredMeasurements, bmm.getId())== -1)
                            filteredMeasurements.add(bmm);
                    }
                }
                else{
                if (bmm.isBreakfast() && _showBreakfastSW.isChecked()) {
                    if(_dbHelper.getMeasurementPositionInList(filteredMeasurements, bmm.getId())== -1)
                        filteredMeasurements.add(bmm);
                }
                if (bmm.isLunch() && _showLunchSW.isChecked()) {
                    if(_dbHelper.getMeasurementPositionInList(filteredMeasurements, bmm.getId())== -1)
                        filteredMeasurements.add(bmm);
                }
                if (bmm.isDinner() && _showDinnerSW.isChecked()) {
                    if(_dbHelper.getMeasurementPositionInList(filteredMeasurements, bmm.getId())== -1)
                        filteredMeasurements.add(bmm);
                }
                if (bmm.isBedtime() && _showBedtimeSW.isChecked()) {
                    if(_dbHelper.getMeasurementPositionInList(filteredMeasurements, bmm.getId())== -1)
                        filteredMeasurements.add(bmm);
                }
                }
            }
            return filteredMeasurements;
        }
        @Override
        protected void onPostExecute(List<BloodMeasurementModel> result) {
            _adapter = new GlucoseMeasurementAdapter(result);
            _adapter.setActivity(MainActivity.this);
            _measurementView.setAdapter(_adapter);
            if(result.isEmpty()){
                _gettingStartedCard.setVisibility(View.VISIBLE);
            }
            else{
                _gettingStartedCard.setVisibility(View.GONE);
            }
        }
    }

}

    class MainActivityListener implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, PopupWindow.OnDismissListener {
        MainActivity _activity;
        SharedPreferences _sharedPreferences;

        public MainActivityListener(MainActivity activity, SharedPreferences preferences) {
            _activity = activity;
            _sharedPreferences = preferences;
        }

        @Override
        public void onClick(View view) {
            if (view.getId() == _activity.findViewById(R.id.fab).getId()) {
                Intent intent = new Intent(_activity, AddMeasurementActivity.class);
                _activity.startActivity(intent);
            }
        }

        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            _activity.notifyPreferencesChanged();

        }

        @Override
        public void onDismiss() {
            _activity.notifyPreferencesChanged();
        }
    }

