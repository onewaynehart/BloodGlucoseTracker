package com.yourharts.www.bloodglucosetracker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
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
import java.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
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
    private Button _fab;
    private Switch _showHighOnlySW;
    private Switch _showBreakfastSW;
    private Switch _showLunchSW;
    private Switch _showDinnerSW;
    private Switch _showBedtimeSW;
    private Switch _showSummarySW;
    private View _popupFilterView;
    private Listener _listener;
    private MenuItem _filterMenuItem;
    PopupWindow _dropDownMenu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        LayoutInflater layoutInflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        _sharedPref = getSharedPreferences(getString(R.string.pref_file_key), Context.MODE_PRIVATE);
        _listener = new Listener(MainActivity.this, _sharedPref);
        _popupFilterView = layoutInflater.inflate(R.layout.drop_down_filters, null);
        _filterMenuItem = findViewById(R.id.menu_item_filter);
        _dropDownMenu = new PopupWindow(_popupFilterView, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);

        _dropDownMenu.setOutsideTouchable(true);
        setupListeners();
        _dbDateFormat = new SimpleDateFormat(getString(R.string.database_date_time_format));
        _dbHelper = new DBHelper(getApplicationContext(), getFilesDir().getPath());
        _measurementView = findViewById(R.id.bloodGlucoseMeasurementsRecyclerView);
        _summaryCard = findViewById(R.id.summary_card);
        _gettingStartedCard = findViewById(R.id.getting_started_card);
        _measurementView.setHasFixedSize(true);
        _layoutManager = new LinearLayoutManager(this);
        _measurementView.setLayoutManager(_layoutManager);

        _fab = findViewById(R.id.fab);
        _fab.setOnClickListener(_listener);

        try {
            _dbHelper.prepareDatabase();
        } catch (IOException e) {
            e.printStackTrace();
        }
        loadUserPreferences();


        loadMeasurements();
        loadSummary();
        if(_adapter.getItemCount() == 0) {

            _gettingStartedCard.setVisibility(View.VISIBLE);
            _filterMenuItem.setVisible(false);
        }
        else
        {
            _gettingStartedCard.setVisibility(View.GONE);
            _filterMenuItem.setVisible(true);
        }

    }

    private void setupListeners() {
        _dropDownMenu.setOnDismissListener(_listener);
        _showHighOnlySW = _popupFilterView.findViewById(R.id.filter_switch_show_high_only);
        _showHighOnlySW.setOnCheckedChangeListener(_listener);
        _showBreakfastSW = _popupFilterView.findViewById(R.id.filter_switch_show_breakfast);
        _showBreakfastSW.setOnCheckedChangeListener(_listener);
        _showLunchSW = _popupFilterView.findViewById(R.id.filter_switch_show_lunch);
        _showLunchSW.setOnCheckedChangeListener(_listener);
        _showDinnerSW = _popupFilterView.findViewById(R.id.filter_switch_show_dinner);
        _showDinnerSW.setOnCheckedChangeListener(_listener);
        _showBedtimeSW = _popupFilterView.findViewById(R.id.filter_switch_show_bedtime);
        _showBedtimeSW.setOnCheckedChangeListener(_listener);
        _showSummarySW = _popupFilterView.findViewById(R.id.filter_switch_show_summary);
        _showSummarySW.setOnCheckedChangeListener(_listener);
    }

    private void loadUserPreferences() {
        _showSummarySW.setChecked(_sharedPref.getBoolean("PREF_SHOW_SUMMARY_CARD", true));
        _showHighOnlySW.setChecked(_sharedPref.getBoolean("PREF_FILTER_HIGH_ONLY", false));
        _showBreakfastSW.setChecked(_sharedPref.getBoolean("PREF_FILTER_SHOW_BREAKFAST", true));
        _showLunchSW.setChecked(_sharedPref.getBoolean("PREF_FILTER_SHOW_LUNCH", true));
        _showDinnerSW.setChecked(_sharedPref.getBoolean("PREF_FILTER_SHOW_DINNER", true));
        _showBedtimeSW.setChecked(_sharedPref.getBoolean("PREF_FILTER_SHOW_BEDTIME", true));
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void loadSummary() {
        _summaryCard.setVisibility(_showSummarySW.isChecked() ? View.VISIBLE : View.GONE);
        if (_showSummarySW.isChecked() == false)
            return;

        List<BloodMeasurementModel> measurements = _dbHelper.getBloodMeasurements();
        if(measurements.size() == 0){
            _summaryCard.setVisibility(View.GONE);
            return;
        }

        List<DataModelInterface> measurementUnits = _dbHelper.getMeasurementUnits();
        TextView highestRecorded = findViewById(R.id.summary_highest_TV);
        TextView lowestRecorded = findViewById(R.id.summary_lowest_TV);
        TextView averageCorrective = findViewById(R.id.summary_average_corrective_TV);
        TextView highestRecordedDate = findViewById(R.id.summary_highest_date_TV);
        TextView highestTimeOfDay = findViewById(R.id.summary_highest_time_of_day_TV);
        BloodMeasurementModel inUse = measurements.get(0);
        for (BloodMeasurementModel bmm : measurements) {
            if (bmm.get_glucoseMeasurement() >= inUse.get_glucoseMeasurement())
                inUse = bmm;
        }
        if (inUse != null) {
            String highestUnits = measurementUnits.get(_dbHelper.getPosition(measurementUnits, inUse.get_glucoseMeasurementUnitID())).getString();
            highestRecorded.setText(Double.toString(inUse.get_glucoseMeasurement()) + " " + highestUnits);
            highestRecordedDate.setText(inUse.get_glucoseMeasurementDate());
        }
        inUse = measurements.get(0);
        for (BloodMeasurementModel bmm : measurements) {
            if (bmm.get_glucoseMeasurement() <= inUse.get_glucoseMeasurement())
                inUse = bmm;
        }
        if (inUse != null) {
            String lowestUnits = measurementUnits.get(_dbHelper.getPosition(measurementUnits, inUse.get_glucoseMeasurementUnitID())).getString();
            lowestRecorded.setText(Double.toString(inUse.get_glucoseMeasurement()) + " " + lowestUnits);
            highestRecordedDate.setText(inUse.get_glucoseMeasurementDate());
        }
        Map<String, ArrayList<Double>> correctiveMap = new HashMap<String, ArrayList<Double>>();
        for (BloodMeasurementModel bmm : measurements) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            try {
                Date date = sdf.parse(bmm.get_glucoseMeasurementDate());
                if (correctiveMap.containsKey(date.toString())) {
                    correctiveMap.get(date.toString()).add(bmm.get_correctiveDoseAmount());
                } else {
                    correctiveMap.put(date.toString(), new ArrayList<Double>());
                    correctiveMap.get(date.toString()).add(bmm.get_correctiveDoseAmount());
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
        averageCorrective.setText(String.format("%2.2f", dailyAverage) + " corrective drug units daily.");


        Map<String, Double> timeofday = new HashMap<>();
        for (BloodMeasurementModel bmm : measurements) {
            if (bmm.isBreakfast()) {
                if (timeofday.containsKey("breakfast")) {
                    timeofday.put("breakfast", timeofday.get("breakfast") + bmm.get_glucoseMeasurement());
                } else {
                    timeofday.put("breakfast", bmm.get_glucoseMeasurement());
                }
            }
            if (bmm.isLunch()) {
                if (timeofday.containsKey("lunch")) {
                    timeofday.put("lunch", timeofday.get("lunch") + bmm.get_glucoseMeasurement());
                } else {
                    timeofday.put("lunch", bmm.get_glucoseMeasurement());
                }
            }
            if (bmm.isDinner()) {
                if (timeofday.containsKey("dinner")) {
                    timeofday.put("dinner", timeofday.get("dinner") + bmm.get_glucoseMeasurement());
                } else {
                    timeofday.put("dinner", bmm.get_glucoseMeasurement());
                }
            }
            if (bmm.isBedtime()) {
                if (timeofday.containsKey("bedtime")) {
                    timeofday.put("bedtime", timeofday.get("bedtime") + bmm.get_glucoseMeasurement());
                } else {
                    timeofday.put("bedtime", bmm.get_glucoseMeasurement());
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
        highestTimeOfDay.setText(highestKey + ".");

        LineChart graph = findViewById(R.id.summary_card_graphView);
        graph.invalidate();
        graph.clear();
        List<Entry> breakfastEntries = new ArrayList<>();
        List<Entry> lunchEntries = new ArrayList<>();
        List<Entry> dinnerEntries = new ArrayList<>();
        List<Entry> bedtimeEntries = new ArrayList<>();



        measurements.sort(Comparator.comparing(BloodMeasurementModel::get_glucoseMeasurementDate));

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.database_date_format));


        for (BloodMeasurementModel bmm : measurements) {

            try {
                cal.setTime(sdf.parse(bmm.get_glucoseMeasurementDate()));// all done
            } catch (ParseException e) {
                e.printStackTrace();
            }
            if (bmm.isBreakfast()) {
                breakfastEntries.add(new Entry(cal.get(Calendar.DAY_OF_YEAR), (float) bmm.get_glucoseMeasurement()));
            }
            if (bmm.isLunch()) {
                lunchEntries.add(new Entry(cal.get(Calendar.DAY_OF_YEAR), (float) bmm.get_glucoseMeasurement()));
            }
            if (bmm.isDinner()) {
                dinnerEntries.add(new Entry(cal.get(Calendar.DAY_OF_YEAR), (float) bmm.get_glucoseMeasurement()));
            }
            if (bmm.isBedtime()) {
                bedtimeEntries.add(new Entry(cal.get(Calendar.DAY_OF_YEAR), (float) bmm.get_glucoseMeasurement()));
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


            LineData lineData = new LineData();


            if (_showBreakfastSW.isChecked())
                lineData.addDataSet(breakfastDataSet);
            if (_showLunchSW.isChecked())
                lineData.addDataSet(lunchDataSet);
            if (_showDinnerSW.isChecked())
                lineData.addDataSet(dinnerDataSet);
            if (_showBedtimeSW.isChecked())
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
        List<BloodMeasurementModel> measurements = getDBHelper().getBloodMeasurements();

        List<BloodMeasurementModel> filteredMeasurements = new ArrayList<>();
        for (BloodMeasurementModel bmm : measurements) {
            if (_showHighOnlySW.isChecked()) {
                if (bmm.isHigh())
                    filteredMeasurements.add(bmm);
            } else {
                if (bmm.isBreakfast() && _showBreakfastSW.isChecked()) {
                    filteredMeasurements.add(bmm);
                }
                if (bmm.isLunch() && _showLunchSW.isChecked()) {
                    filteredMeasurements.add(bmm);
                }
                if (bmm.isDinner() && _showDinnerSW.isChecked()) {
                    filteredMeasurements.add(bmm);
                }
                if (bmm.isBedtime() && _showBedtimeSW.isChecked()) {
                    filteredMeasurements.add(bmm);
                }
            }
        }


        _adapter = new GlucoseMeasurementAdapter(filteredMeasurements);
        _adapter.setmActivity(this);
        _measurementView.setAdapter(_adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadMeasurements();
        loadSummary();
        if(_adapter.getItemCount() == 0) {

            _gettingStartedCard.setVisibility(View.VISIBLE);
            _filterMenuItem.setVisible(false);
        }
        else
        {
            _gettingStartedCard.setVisibility(View.GONE);
            _filterMenuItem.setVisible(true);
        }
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
            String csv = _dbHelper.GetMeasurementsCSVText(delimiter);
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
            _dropDownMenu.showAsDropDown(findViewById(R.id.menu_item_filter));
        }
        return super.onOptionsItemSelected(item);
    }


    public DBHelper getDBHelper() {
        return _dbHelper;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void notifyPreferencesChanged() {

        loadSummary();
        loadMeasurements();
    }



    public void savePreferences() {
        SharedPreferences.Editor editor = _sharedPref.edit();
        editor.putBoolean("PREF_FILTER_HIGH_ONLY", _showHighOnlySW.isChecked());
        editor.putBoolean("PREF_FILTER_SHOW_BREAKFAST", _showBreakfastSW.isChecked());
        editor.putBoolean("PREF_FILTER_SHOW_LUNCH", _showLunchSW.isChecked());
        editor.putBoolean("PREF_FILTER_SHOW_DINNER", _showDinnerSW.isChecked());
        editor.putBoolean("PREF_FILTER_SHOW_BEDTIME", _showBedtimeSW.isChecked());
        editor.putBoolean("PREF_SHOW_SUMMARY_CARD",_showSummarySW.isChecked());
        editor.apply();
        editor.commit();
    }
}

class Listener implements View.OnClickListener, CompoundButton.OnCheckedChangeListener, PopupWindow.OnDismissListener {
    MainActivity _activity;
    SharedPreferences _sharedPreferences;

    public Listener(MainActivity activity, SharedPreferences preferences) {
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
