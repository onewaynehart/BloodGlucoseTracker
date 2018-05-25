package com.yourharts.www.bloodglucosetracker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.yourharts.www.Adapters.GlucoseMeasurementAdapter;
import com.yourharts.www.Database.DBHelper;
import com.yourharts.www.Models.BloodMeasurementModel;
import com.yourharts.www.Models.DataModelInterface;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static android.support.v4.content.FileProvider.getUriForFile;

public class MainActivity extends Activity {
    private RecyclerView mMeasurementView;
    private GlucoseMeasurementAdapter mAdapter;
    private RecyclerView.LayoutManager mLayoutManager;
    private DBHelper mDbHelper;
    private DateFormat dbDateFormat;
    private SharedPreferences _sharedPref;
    private CardView _summaryCard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        _sharedPref = getSharedPreferences(getString(R.string.pref_file_key), Context.MODE_PRIVATE);
        dbDateFormat = new SimpleDateFormat(getString(R.string.database_date_format));
        mDbHelper = new DBHelper(getApplicationContext(), getFilesDir().getPath());
        mMeasurementView = findViewById(R.id.bloodGlucoseMeasurementsRecyclerView);
        _summaryCard = findViewById(R.id.summary_card);
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
        boolean showSummary = _sharedPref.getBoolean("PREF_SHOW_SUMMARY_CARD",true);
        if(showSummary)
            LoadSummary();
        _summaryCard.setVisibility(showSummary?View.VISIBLE : View.GONE);
    }

    private void LoadSummary() {
        List<BloodMeasurementModel> measurements = getmDbHelper().getBloodMeasurements();
        List<DataModelInterface> measurementUnits = getmDbHelper().getMeasurementUnits();
        TextView highestRecorded = findViewById(R.id.summary_highest_TV);
        TextView lowestRecorded = findViewById(R.id.summary_lowest_TV);
        TextView averageCorrective = findViewById(R.id.summary_average_corrective_TV);
        TextView highestRecordedDate = findViewById(R.id.summary_highest_date_TV);
        TextView highestTimeOfDay = findViewById(R.id.summary_highest_time_of_day_TV);
        BloodMeasurementModel inUse = measurements.get(0);
        for(BloodMeasurementModel bmm : measurements){
            if(bmm.getGlucoseMeasurement() >= inUse.getGlucoseMeasurement())
                inUse = bmm;
        }
        if(inUse!=null)
        {
            String highestUnits = measurementUnits.get(mDbHelper.getPosition(measurementUnits, inUse.getGlucoseMeasurementUnitID())).getString();
            highestRecorded.setText(Double.toString(inUse.getGlucoseMeasurement()) + " "+highestUnits);
            highestRecordedDate.setText(inUse.getGlucoseMeasurementDate());
        }
        inUse = measurements.get(0);
        for(BloodMeasurementModel bmm : measurements){
            if(bmm.getGlucoseMeasurement() <= inUse.getGlucoseMeasurement())
                inUse = bmm;
        }
        if(inUse!=null)
        {
            String lowestUnits = measurementUnits.get(mDbHelper.getPosition(measurementUnits, inUse.getGlucoseMeasurementUnitID())).getString();
            lowestRecorded.setText(Double.toString(inUse.getGlucoseMeasurement()) + " "+lowestUnits);
            highestRecordedDate.setText(inUse.getGlucoseMeasurementDate());
        }
        Map<String, ArrayList<Double>> correctiveMap = new HashMap<String, ArrayList<Double>>();
        for(BloodMeasurementModel bmm : measurements){
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date date = sdf.parse(bmm.getGlucoseMeasurementDate());
                if(correctiveMap.containsKey(date.toString())){
                    correctiveMap.get(date.toString()).add(bmm.getCorrectiveDoseAmount());
                }
                else
                {
                    correctiveMap.put(date.toString(), new ArrayList<Double>());
                    correctiveMap.get(date.toString()).add(bmm.getCorrectiveDoseAmount());
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        int count = 0;
        Double amount = 0.0;
        for(String key : correctiveMap.keySet()){
            count = 0;
            ArrayList<Double> doses = correctiveMap.get(key);
            for(Double dose : doses) {
                amount += dose;
                count++;
            }
        }
        double dailyAverage = (amount / (float) correctiveMap.keySet().size());
        averageCorrective.setText(String.format("%2.2f",dailyAverage) + " corrective drug units daily.");


        Map<String, Double> timeofday = new HashMap<String, Double>();
        for(BloodMeasurementModel bmm : measurements){
            if(bmm.isBreakfast()) {
                if(timeofday.containsKey("breakfast")){
                    timeofday.put("breakfast", timeofday.get("breakfast")+bmm.getGlucoseMeasurement());
                }
                else{
                    timeofday.put("breakfast",bmm.getGlucoseMeasurement());
                }
            }
            if(bmm.isLunch()) {
                if (timeofday.containsKey("lunch")) {
                    timeofday.put("lunch", timeofday.get("lunch") + bmm.getGlucoseMeasurement());
                } else {
                    timeofday.put("lunch", bmm.getGlucoseMeasurement());
                }
            }
            if(bmm.isDinner())
            {
                if (timeofday.containsKey("dinner")) {
                    timeofday.put("dinner", timeofday.get("dinner") + bmm.getGlucoseMeasurement());
                } else {
                    timeofday.put("dinner", bmm.getGlucoseMeasurement());
                }
            }
            if(bmm.isBedtime())
            {
                if (timeofday.containsKey("bedtime")) {
                    timeofday.put("bedtime", timeofday.get("bedtime") + bmm.getGlucoseMeasurement());
                } else {
                    timeofday.put("bedtime", bmm.getGlucoseMeasurement());
                }
            }
        }
        String highestKey =null;
        Double highestValue = 0.0;

        for(String key : timeofday.keySet()){
            if(timeofday.get(key) >= highestValue)
            {
                highestValue = timeofday.get(key);
                highestKey = key;
            }
        }
        highestTimeOfDay.setText(highestKey+".");


    }

    private void LoadMeasurements() {

        LoadMeasurements(_sharedPref.getBoolean("PREF_FILTER_HIGH_ONLY",false),
                _sharedPref.getBoolean("PREF_FILTER_SHOW_BREAKFAST",true),
                _sharedPref.getBoolean("PREF_FILTER_SHOW_LUNCH",true),
                _sharedPref.getBoolean("PREF_FILTER_SHOW_DINNER",true),
                _sharedPref.getBoolean("PREF_FILTER_SHOW_BEDTIME",true)
                );
    }

    private void LoadMeasurements(boolean showHigh, boolean showBreakfast, boolean showLunch, boolean showDinner, boolean showBedtime) {
        List<BloodMeasurementModel> measurements = getmDbHelper().getBloodMeasurements();

        List<BloodMeasurementModel> filteredMeasurements = new ArrayList<BloodMeasurementModel>();
        for (BloodMeasurementModel bmm : measurements) {
            if (showHigh) {
                if(bmm.isHigh())
                    filteredMeasurements.add(bmm);
            } else {
                if (bmm.isBreakfast() && showBreakfast) {
                    filteredMeasurements.add(bmm);
                }
                if (bmm.isLunch() && showLunch) {
                    filteredMeasurements.add(bmm);
                }
                if (bmm.isDinner() && showDinner) {
                    filteredMeasurements.add(bmm);
                }
                if (bmm.isBedtime() && showBedtime) {
                    filteredMeasurements.add(bmm);
                }
            }
        }


        mAdapter = new GlucoseMeasurementAdapter(filteredMeasurements);
        mAdapter.setmActivity(this);
        mMeasurementView.setAdapter(mAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        LoadMeasurements();
        boolean showSummary = _sharedPref.getBoolean("PREF_SHOW_SUMMARY_CARD",true);
        if(showSummary)
            LoadSummary();
        _summaryCard.setVisibility(showSummary?View.VISIBLE : View.GONE);
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
        if (id == R.id.menu_item_share) {
            String delimiter = _sharedPref.getString("PREF_DEFAULT_CSVDELIMITER", "||");
            String csv = mDbHelper.GetMeasurementsCSVText(delimiter);
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
                sendIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Blood glucose measurements.");
                sendIntent.setDataAndType(null, getContentResolver().getType(contentUri));

                startActivity(sendIntent);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
        if (id == R.id.menu_item_filter) {


            LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final View popupView = layoutInflater.inflate(R.layout.drop_down_filters, null);
            PopupWindow dropDownMenu = new PopupWindow(
                    popupView,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT);

            final Switch highOnly = popupView.findViewById(R.id.filter_switch_show_high_only);
            final Switch showBreakfast = popupView.findViewById(R.id.filter_switch_show_breakfast);
            final Switch showLunch = popupView.findViewById(R.id.filter_switch_show_lunch);
            final Switch showDinner = popupView.findViewById(R.id.filter_switch_show_dinner);
            final Switch showBedtime = popupView.findViewById(R.id.filter_switch_show_bedtime);
            final Switch showSummary = popupView.findViewById(R.id.filter_switch_show_summary);
            highOnly.setChecked(_sharedPref.getBoolean("PREF_FILTER_HIGH_ONLY",false));
            showSummary.setChecked(_sharedPref.getBoolean("PREF_SHOW_SUMMARY_CARD", true));
            setFiltersDefault(highOnly, showBreakfast, showLunch, showDinner, showBedtime);

            CompoundButton.OnCheckedChangeListener checkedChangeListener = new CompoundButton.OnCheckedChangeListener() {
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if(buttonView.getId() == highOnly.getId()) {
                        setFiltersDefault(highOnly, showBreakfast, showLunch, showDinner, showBedtime);
                    }
                    LoadMeasurements(highOnly.isChecked(), showBreakfast.isChecked(), showLunch.isChecked(), showDinner.isChecked(), showBedtime.isChecked());
                    if(showSummary.isChecked())
                        LoadSummary();
                    _summaryCard.setVisibility(showSummary.isChecked()?View.VISIBLE : View.GONE);

                }
            };
            highOnly.setOnCheckedChangeListener(checkedChangeListener);
            showBreakfast.setOnCheckedChangeListener(checkedChangeListener);
            showLunch.setOnCheckedChangeListener(checkedChangeListener);
            showDinner.setOnCheckedChangeListener(checkedChangeListener);
            showBedtime.setOnCheckedChangeListener(checkedChangeListener);
            showSummary.setOnCheckedChangeListener(checkedChangeListener);
            dropDownMenu.setOutsideTouchable(true);
            dropDownMenu.setOnDismissListener(new PopupWindow.OnDismissListener() {
                @Override
                public void onDismiss() {
                    //TODO do sth here on dismiss
                    SharedPreferences.Editor editor = _sharedPref.edit();
                    editor.putBoolean("PREF_FILTER_HIGH_ONLY", highOnly.isChecked());
                    editor.putBoolean("PREF_FILTER_SHOW_BREAKFAST", showBreakfast.isChecked());
                    editor.putBoolean("PREF_FILTER_SHOW_LUNCH", showLunch.isChecked());
                    editor.putBoolean("PREF_FILTER_SHOW_DINNER", showDinner.isChecked());
                    editor.putBoolean("PREF_FILTER_SHOW_BEDTIME", showBedtime.isChecked());
                    editor.putBoolean("PREF_SHOW_SUMMARY_CARD", showSummary.isChecked());
                    editor.apply();
                    editor.commit();
                }
            });

            dropDownMenu.showAsDropDown(findViewById(R.id.menu_item_filter));
        }
        return super.onOptionsItemSelected(item);
    }

    private void setFiltersDefault(Switch highOnly, Switch showBreakfast, Switch showLunch, Switch showDinner, Switch showBedtime) {
        showBreakfast.setChecked(!highOnly.isChecked());
        showLunch.setChecked(!highOnly.isChecked());
        showDinner.setChecked(!highOnly.isChecked());
        showBedtime.setChecked(!highOnly.isChecked());
        showBreakfast.setEnabled(!highOnly.isChecked());
        showLunch.setEnabled(!highOnly.isChecked());
        showDinner.setEnabled(!highOnly.isChecked());
        showBedtime.setEnabled(!highOnly.isChecked());
    }

    public DBHelper getmDbHelper() {
        return mDbHelper;
    }
}
