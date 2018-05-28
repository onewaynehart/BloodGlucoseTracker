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
import android.widget.CompoundButton;
import android.widget.PopupWindow;
import android.widget.Switch;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;

import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.yourharts.www.Adapters.GlucoseMeasurementAdapter;
import com.yourharts.www.Database.DBHelper;
import com.yourharts.www.Models.BloodMeasurementModel;
import com.yourharts.www.Models.DataModelInterface;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.UUID;

import static android.support.v4.content.FileProvider.getUriForFile;

public class MainActivity extends Activity {
    private RecyclerView _measurementView;
    private GlucoseMeasurementAdapter _adapter;
    private RecyclerView.LayoutManager _layoutManager;
    private DBHelper _dbHelper;
    private DateFormat _dbDateFormat;
    private SharedPreferences _sharedPref;
    private CardView _summaryCard;
    private CardView _gettingStartedCard;
    private FloatingActionButton _fab;
    private Switch _showHighOnlySW;
    private Switch _showBreakfastSW;
    private Switch _showLunchSW;
    private Switch _showDinnerSW;
    private Switch _showBedtimeSW;
    private Switch _showSummarySW;
    private View _popupFilterView;
    private TextView _summaryTextView;
    private MainActivityListener _mainActivitylistener;
    private MenuItem _filterMenuItem;
    PopupWindow _dropDownMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        _sharedPref = getSharedPreferences(getString(R.string.pref_file_key), Context.MODE_PRIVATE);
        _mainActivitylistener = new MainActivityListener(MainActivity.this, _sharedPref);
        _popupFilterView = layoutInflater.inflate(R.layout.drop_down_filters, null);
        _summaryTextView = findViewById(R.id._summaryTV);
        _dropDownMenu = new PopupWindow(_popupFilterView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        _dropDownMenu.setOutsideTouchable(true);
        setupListeners();
        _dbDateFormat = new SimpleDateFormat(getString(R.string.database_date_time_format));
        _dbHelper = new DBHelper(getApplicationContext(), getFilesDir().getPath(), this);
        _measurementView = findViewById(R.id.bloodGlucoseMeasurementsRecyclerView);
        _summaryCard = findViewById(R.id.summary_card);
        _gettingStartedCard = findViewById(R.id.getting_started_card);
        _measurementView.setHasFixedSize(true);
        _layoutManager = new LinearLayoutManager(this);
        _measurementView.setLayoutManager(_layoutManager);


        try {
            _dbHelper.prepareDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        loadUserPreferences();


        loadMeasurements();
        loadSummary();


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
        _showSummarySW = _popupFilterView.findViewById(R.id.filter_switch_show_summary);
        _showSummarySW.setOnCheckedChangeListener(_mainActivitylistener);
        _fab = findViewById(R.id.fab);
        _fab.setOnClickListener(_mainActivitylistener);
    }

    private void loadUserPreferences() {
        _showSummarySW.setChecked(_sharedPref.getBoolean("PREF_SHOW_SUMMARY_CARD", true));
        _showHighOnlySW.setChecked(_sharedPref.getBoolean("PREF_FILTER_HIGH_ONLY", false));
        _showBreakfastSW.setChecked(_sharedPref.getBoolean("PREF_FILTER_SHOW_BREAKFAST", true));
        _showLunchSW.setChecked(_sharedPref.getBoolean("PREF_FILTER_SHOW_LUNCH", true));
        _showDinnerSW.setChecked(_sharedPref.getBoolean("PREF_FILTER_SHOW_DINNER", true));
        _showBedtimeSW.setChecked(_sharedPref.getBoolean("PREF_FILTER_SHOW_BEDTIME", true));
    }


    private void loadSummary() {
        _summaryCard.setVisibility(_showSummarySW.isChecked() ? View.VISIBLE : View.GONE);
        if (_showSummarySW.isChecked() == false)
            return;

        List<BloodMeasurementModel> measurements = _dbHelper.getAllBloodMeasurements();
        if (measurements.size() == 0) {
            _summaryCard.setVisibility(View.GONE);
            return;
        }
        List<DataModelInterface> measurementUnits = _dbHelper.getMeasurementUnits();
        double highestRecorded = 0.0;
        double lowestRecorded = 0.0;
        String lowestUnits = "";
        double averageCorrective = 0.0;
        String highestUnits = "";
        String highestRecordedDate = "";
        String highestTimeOfDay = "";
        String lowestRecordedDate = "";
        if (measurements.size() == 0)
            return;
        BloodMeasurementModel inUse = measurements.get(0);
        for (BloodMeasurementModel bmm : measurements) {
            if (bmm.getGlucoseMeasurement() >= inUse.getGlucoseMeasurement())
                inUse = bmm;
        }
        if (inUse != null) {
            highestUnits = measurementUnits.get(_dbHelper.getPosition(measurementUnits, inUse.getGlucoseMeasurementUnitID())).getString();
            highestRecorded = inUse.getGlucoseMeasurement();
            highestRecordedDate = inUse.get_glucoseMeasurementDate();
        }
        inUse = measurements.get(0);
        for (BloodMeasurementModel bmm : measurements) {
            if (bmm.getGlucoseMeasurement() <= inUse.getGlucoseMeasurement())
                inUse = bmm;
        }
        if (inUse != null) {
            lowestUnits = measurementUnits.get(_dbHelper.getPosition(measurementUnits, inUse.getGlucoseMeasurementUnitID())).getString();
            lowestRecorded = inUse.getGlucoseMeasurement();
            lowestRecordedDate = inUse.get_glucoseMeasurementDate();
        }
        Map<String, ArrayList<Double>> correctiveMap = new HashMap<String, ArrayList<Double>>();
        for (BloodMeasurementModel bmm : measurements) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date date = sdf.parse(bmm.get_glucoseMeasurementDate());
                if (correctiveMap.containsKey(date.toString())) {
                    correctiveMap.get(date.toString()).add(bmm.getCorrectiveDoseAmount());
                } else {
                    correctiveMap.put(date.toString(), new ArrayList<Double>());
                    correctiveMap.get(date.toString()).add(bmm.getCorrectiveDoseAmount());
                }
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }
        int count = 0;
        Double amount = 0.0;
        for (String key : correctiveMap.keySet()) {
            count = 0;
            ArrayList<Double> doses = correctiveMap.get(key);
            for (Double dose : doses) {
                amount += dose;
                count++;
            }
        }
        double dailyAverage = (amount / (float) correctiveMap.keySet().size());
        averageCorrective = dailyAverage;


        Map<String, Double> timeofday = new HashMap<>();
        for (BloodMeasurementModel bmm : measurements) {
            if (bmm.isBreakfast()) {
                if (timeofday.containsKey("breakfast")) {
                    timeofday.put("breakfast", timeofday.get("breakfast") + bmm.getGlucoseMeasurement());
                } else {
                    timeofday.put("breakfast", bmm.getGlucoseMeasurement());
                }
            }
            if (bmm.isLunch()) {
                if (timeofday.containsKey("lunch")) {
                    timeofday.put("lunch", timeofday.get("lunch") + bmm.getGlucoseMeasurement());
                } else {
                    timeofday.put("lunch", bmm.getGlucoseMeasurement());
                }
            }
            if (bmm.isDinner()) {
                if (timeofday.containsKey("dinner")) {
                    timeofday.put("dinner", timeofday.get("dinner") + bmm.getGlucoseMeasurement());
                } else {
                    timeofday.put("dinner", bmm.getGlucoseMeasurement());
                }
            }
            if (bmm.isBedtime()) {
                if (timeofday.containsKey("bedtime")) {
                    timeofday.put("bedtime", timeofday.get("bedtime") + bmm.getGlucoseMeasurement());
                } else {
                    timeofday.put("bedtime", bmm.getGlucoseMeasurement());
                }
            }
        }
        String highestKey = null;
        Double highestValue = 0.0;

        for (String key : timeofday.keySet()) {
            if (timeofday.get(key) >= highestValue) {
                highestValue = timeofday.get(key);
                highestKey = key;
            }
        }
        highestTimeOfDay = highestKey;
        List<DataModelInterface> correctiveDrugs = _dbHelper.getShortLastingDrugs();

        int defaultCorrectiveDrugID = _sharedPref.getInt(getString(R.string.pref_defaultCorrectiveDrugID), 1);
        int correctiveDrugPos = (_dbHelper.getPosition(correctiveDrugs, defaultCorrectiveDrugID));
        String correctiveDrugName = correctiveDrugs.get(correctiveDrugPos).getString();

        String summary = String.format("Your highest ever blood glucose reading was %.2f %s taken on %s.\nYour lowest ever blood glucose reading was %.2f %s taken on %s.\nOn average you take %.2f units of %s a day to correct your levels.\nYour highest readings are usually around %s.",
                highestRecorded,
                highestUnits,
                highestRecordedDate,
                lowestRecorded,
                lowestUnits,
                lowestRecordedDate,
                averageCorrective,
                correctiveDrugName,
                highestTimeOfDay);
        _summaryTextView.setText(summary);


        LineChart graph = findViewById(R.id.summary_card_graphView);
        graph.invalidate();
        graph.clear();
        List<Entry> breakfastEntries = new ArrayList<>();
        List<Entry> lunchEntries = new ArrayList<>();
        List<Entry> dinnerEntries = new ArrayList<>();
        List<Entry> bedtimeEntries = new ArrayList<>();


        Collections.reverse(measurements);

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.database_date_format));

        System.out.println("----------------------Start Analyzing ---------------------");
        for (BloodMeasurementModel bmm : measurements) {
            long index = 0;
            try {
                cal.setTime(sdf.parse(bmm.get_glucoseMeasurementDate()));
                index = cal.getTimeInMillis();

                System.out.println(bmm.get_glucoseMeasurementDate() + " = " + index);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (bmm.isBreakfast()) {
                breakfastEntries.add(new Entry(index, (float) bmm.getGlucoseMeasurement()));
            }
            if (bmm.isLunch()) {
                lunchEntries.add(new Entry(index, (float) bmm.getGlucoseMeasurement()));
            }
            if (bmm.isDinner()) {
                dinnerEntries.add(new Entry(index, (float) bmm.getGlucoseMeasurement()));
            }
            if (bmm.isBedtime()) {
                bedtimeEntries.add(new Entry(index, (float) bmm.getGlucoseMeasurement()));
            }
        }
        try {
            LineDataSet breakfastDataSet = new LineDataSet(breakfastEntries, "breakfast");
            LineDataSet lunchDataSet = new LineDataSet(lunchEntries, "lunch");
            LineDataSet dinnerDataSet = new LineDataSet(dinnerEntries, "dinner");
            LineDataSet bedtimeDataSet = new LineDataSet(bedtimeEntries, "bedtime");
            breakfastDataSet.setColor(getResources().getColor(R.color.colorPrimaryVariant));
            lunchDataSet.setColor(getResources().getColor(R.color.colorPrimaryVariant2));
            dinnerDataSet.setColor(getResources().getColor(R.color.colorSecondary));
            bedtimeDataSet.setColor(getResources().getColor(R.color.colorSecondaryVariant));
            breakfastDataSet.setDrawCircles(false);
            lunchDataSet.setDrawCircles(false);
            dinnerDataSet.setDrawCircles(false);
            bedtimeDataSet.setDrawCircles(false);
            LineData lineData = new LineData();


            if (_showBreakfastSW.isChecked() && breakfastEntries.size() > 0)
                lineData.addDataSet(breakfastDataSet);
            if (_showLunchSW.isChecked() && lunchEntries.size() > 0)
                lineData.addDataSet(lunchDataSet);
            if (_showDinnerSW.isChecked() && dinnerEntries.size() > 0)
                lineData.addDataSet(dinnerDataSet);
            if (_showBedtimeSW.isChecked() && bedtimeEntries.size() > 0)
                lineData.addDataSet(bedtimeDataSet);
            graph.getXAxis().setGranularity(1.0f);
            graph.getXAxis().setTextColor(getResources().getColor(R.color.colorSurface));
            graph.getDescription().setText("Measurements over time.");
            graph.setData(lineData);
            graph.invalidate();


        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void loadMeasurements() {
        List<BloodMeasurementModel> measurements = getDBHelper().getAllBloodMeasurements();

        List<BloodMeasurementModel> filteredMeasurements = new ArrayList<>();
        for (BloodMeasurementModel bmm : measurements) {
            if (_showHighOnlySW.isChecked()) {
                if (bmm.isHigh()) {
                    if(_dbHelper.getMeasurementPositionInList(filteredMeasurements, bmm.getId())== -1)
                        filteredMeasurements.add(bmm);
                }
            }
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

        _adapter = new GlucoseMeasurementAdapter(filteredMeasurements);
        _adapter.setmActivity(this);
        _measurementView.setAdapter(_adapter);
        if (_adapter.getItemCount() == 0) {

            _gettingStartedCard.setVisibility(View.VISIBLE);

        } else {
            _gettingStartedCard.setVisibility(View.GONE);

        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadMeasurements();
        loadSummary();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        _filterMenuItem = menu.findItem(R.id.menu_item_filter);
        _filterMenuItem.setVisible(_adapter != null ? _adapter.getItemCount() > 0 : false);
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
        if (id == R.id.menu_item_filter) {
            _dropDownMenu.showAsDropDown(findViewById(R.id.menu_item_filter));
        }
        if (id == R.id.menu_item_data) {
            Intent intent = new Intent(MainActivity.this, DataActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    public DBHelper getDBHelper() {
        return _dbHelper;
    }


    public void notifyPreferencesChanged() {


        loadMeasurements();
        loadSummary();


    }


    public void savePreferences() {
        SharedPreferences.Editor editor = _sharedPref.edit();
        editor.putBoolean("PREF_FILTER_HIGH_ONLY", _showHighOnlySW.isChecked());
        editor.putBoolean("PREF_FILTER_SHOW_BREAKFAST", _showBreakfastSW.isChecked());
        editor.putBoolean("PREF_FILTER_SHOW_LUNCH", _showLunchSW.isChecked());
        editor.putBoolean("PREF_FILTER_SHOW_DINNER", _showDinnerSW.isChecked());
        editor.putBoolean("PREF_FILTER_SHOW_BEDTIME", _showBedtimeSW.isChecked());
        editor.putBoolean("PREF_SHOW_SUMMARY_CARD", _showSummarySW.isChecked());
        editor.apply();
        editor.commit();
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
        _activity.savePreferences();
    }
}
