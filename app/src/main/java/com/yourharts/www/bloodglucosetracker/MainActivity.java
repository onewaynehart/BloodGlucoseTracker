package com.yourharts.www.bloodglucosetracker;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

import com.yourharts.www.Adapters.GlucoseMeasurementAdapter;
import com.yourharts.www.Database.DBHelper;
import com.yourharts.www.Models.BloodMeasurementModel;
import com.yourharts.www.Models.LongLastingDrugModel;
import com.yourharts.www.Models.ShortLastingDrugModel;

import java.io.IOException;
import java.io.Serializable;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class MainActivity extends Activity {
    private RecyclerView mMeasurementView;
    private GlucoseMeasurementAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private DBHelper mDbHelper;
    private DateFormat dbDateFormat;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dbDateFormat = new SimpleDateFormat(getString(R.string.database_date_format));
        mDbHelper = new DBHelper(getApplicationContext(), getFilesDir().getPath());
        mMeasurementView = findViewById(R.id.bloodGlucoseMeasurementsRecyclerView);
        mMeasurementView.setHasFixedSize(true);
        mLayoutManager = new LinearLayoutManager(this);
        mMeasurementView.setLayoutManager(mLayoutManager);


        try {
            mDbHelper.prepareDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }


        //Toolbar toolbar = findViewById(R.id.toolbar);
        //setSupportActionBar(toolbar);

/*        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
       //     @Override
           public void onClick(View view) {
               Intent intent = new Intent(MainActivity.this, AddMeasurementActivity.class);
               startActivity(intent);
           }
        });*/

        LoadMeasurements();
    }

    private void LoadMeasurements() {


        List<BloodMeasurementModel> measurements = getmDbHelper().getBloodMeasurements();

        mAdapter = new GlucoseMeasurementAdapter(measurements);
        mAdapter.setmActivity(this);
        mMeasurementView.setAdapter(mAdapter);
    }
    @Override
    protected void onResume(){
        super.onResume();
        LoadMeasurements();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public DBHelper getmDbHelper() {
        return mDbHelper;
    }
}
