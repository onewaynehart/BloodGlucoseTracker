package com.yourharts.www.bloodglucosetracker;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.yourharts.www.Adapters.GlucoseMeasurementAdapter;
import com.yourharts.www.Database.DBHelper;
import com.yourharts.www.Models.BloodMeasurementModel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.UUID;

import static android.support.v4.content.FileProvider.getUriForFile;

public class MainActivity extends Activity  {
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
        Button fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, AddMeasurementActivity.class);
                startActivity(intent);
            }
        });

        try {
            mDbHelper.prepareDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }

        LoadMeasurements();
    }



    private void LoadMeasurements() {
        List<BloodMeasurementModel> measurements = getmDbHelper().getBloodMeasurements();
        mAdapter = new GlucoseMeasurementAdapter(measurements);
        mAdapter.setmActivity(this);
        mMeasurementView.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
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
        if(id== R.id.menu_item_share){
            String csv = mDbHelper.GetMeasurementsCSVText();
            File csvFilePath = new File(getApplicationContext().getFilesDir().getPath(), "csv");
            csvFilePath.mkdirs();
            File newFile = new File(csvFilePath, UUID.randomUUID().toString()+".csv");
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
                sendIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,"Blood glucose measurements.");
                sendIntent.setDataAndType(null, getContentResolver().getType(contentUri));

                startActivity(sendIntent);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        return super.onOptionsItemSelected(item);
    }

    public DBHelper getmDbHelper() {
        return mDbHelper;
    }
}
