package com.yourharts.www.bloodglucosetracker;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.text.Html;
import android.text.Spanned;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Switch;
import android.widget.TextView;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.MPPointF;
import com.yourharts.www.Database.DBHelper;
import com.yourharts.www.Models.BloodMeasurementModel;
import com.yourharts.www.Models.DataModelInterface;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChartsActivity extends AppCompatActivity {
    private TextView _summaryTextView;
    private DBHelper _dbHelper;
    private DateFormat _dbDateFormat;
    private SharedPreferences _sharedPref;
    private CardView _summaryCard;
    private Switch _breakfastSw;
    private Switch _lunchSw;
    private Switch _dinnerSw;
    private Switch _bedtimeSw;
    private Switch _summaryCardSw;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_charts);

        _dbDateFormat = new SimpleDateFormat(getString(R.string.database_date_time_format));
        _dbHelper = new DBHelper(getApplicationContext(), getFilesDir().getPath(), this);
        _sharedPref = getSharedPreferences(getString(R.string.pref_file_key), Context.MODE_PRIVATE);
        _summaryCard = findViewById(R.id.summary_card);
        _summaryTextView = findViewById(R.id._summaryTV);
        _breakfastSw = findViewById(R.id.charts_breakfastSw);
        _lunchSw = findViewById(R.id.charts_lunchSw);
        _dinnerSw = findViewById(R.id.charts_dinnerSw);
        _bedtimeSw = findViewById(R.id.charts_bedtimeSw);


        Toolbar toolbar = findViewById(R.id.toolbar_charts);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_back_black_24dp);
        _summaryCardSw = findViewById(R.id.showSummaySw);

        setupCheckListeners();

        loadEVERYTHING();


    }

    private void setupCheckListeners() {
        _breakfastSw.setOnCheckedChangeListener((buttonView, isChecked) -> {
            loadGlucoseMeasurementChart();
        });
        _lunchSw.setOnCheckedChangeListener((buttonView, isChecked) -> {
            loadGlucoseMeasurementChart();
        });
        _dinnerSw.setOnCheckedChangeListener((buttonView, isChecked) -> {
            loadGlucoseMeasurementChart();
        });
        _bedtimeSw.setOnCheckedChangeListener((buttonView, isChecked) -> {
            loadGlucoseMeasurementChart();
        });
        _summaryCardSw.setOnCheckedChangeListener((buttonView, isChecked) -> {
            _summaryCard.setVisibility(isChecked ? View.VISIBLE : View.GONE);
            if(isChecked)
                loadSummaryCard();
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_charts, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private class LoadSummaryAsync extends AsyncTask<TextView, Void, String> {


        @Override
        protected String doInBackground(TextView... textViews) {

            return getSummaryCardText();
        }
        @Override
        protected void onPostExecute(String result) {
            Spanned output = Html.fromHtml(result);
            _summaryTextView.setText(output);


        }
    }


    private void loadEVERYTHING() {
        if(_summaryCardSw.isChecked()) {
            loadSummaryCard();
        }


        loadGlucoseMeasurementChart();
        new Thread(() -> {
            try{
                drawInsulinChart();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
        new Thread(() -> {
            try{
                drawHighReadingChart();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();

    }

    private void loadGlucoseMeasurementChart() {
        new Thread(() -> {
            try{
                drawGlucoseChart();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void loadSummaryCard() {
        LoadSummaryAsync backgroundWorker = new LoadSummaryAsync();
        _summaryTextView.setText("Loading...");
        backgroundWorker.execute(_summaryTextView);
    }

    private String getSummaryCardText() {
        List<DataModelInterface> measurementUnits = _dbHelper.getMeasurementUnits();
        List<BloodMeasurementModel> allBloodGlucoseMeasurements = _dbHelper.getAllBloodMeasurements();
        double highestRecorded = 0.0;
        double lowestRecorded = 0.0;
        String lowestUnits = "";
        double averageCorrective = 0.0;
        String highestUnits = "";
        String highestRecordedDate = "";
        String highestTimeOfDay = "";
        String lowestRecordedDate = "";

        if(allBloodGlucoseMeasurements.size() > 0){
            BloodMeasurementModel inUse = allBloodGlucoseMeasurements.get(0);
            for (BloodMeasurementModel bmm : allBloodGlucoseMeasurements) {
                if (bmm.getGlucoseMeasurement() >=  inUse.getGlucoseMeasurement())
                    inUse = bmm;
            }
            if (inUse != null) {
                highestUnits = measurementUnits.get(_dbHelper.getPosition(measurementUnits, inUse.getGlucoseMeasurementUnitID())).getString();
                highestRecorded = inUse.getGlucoseMeasurement();
                highestRecordedDate = inUse.getGlucoseMeasurementDate();
            }
            inUse = allBloodGlucoseMeasurements.get(0);
            for (BloodMeasurementModel bmm : allBloodGlucoseMeasurements) {
                if (bmm.getGlucoseMeasurement() <= inUse.getGlucoseMeasurement())
                    inUse = bmm;
            }
            if (inUse != null) {
                lowestUnits = measurementUnits.get(_dbHelper.getPosition(measurementUnits, inUse.getGlucoseMeasurementUnitID())).getString();
                lowestRecorded = inUse.getGlucoseMeasurement();
                lowestRecordedDate = inUse.getGlucoseMeasurementDate();
            }
            Map<String, ArrayList<Double>> correctiveMap = new HashMap<>();
            for (BloodMeasurementModel bmm : allBloodGlucoseMeasurements) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                try {
                    Date date = sdf.parse(bmm.getGlucoseMeasurementDate());
                    if (correctiveMap.containsKey(date.toString())) {
                        correctiveMap.get(date.toString()).add(bmm.getCorrectiveDoseAmount());
                    } else {
                        correctiveMap.put(date.toString(), new ArrayList<>());
                        correctiveMap.get(date.toString()).add(bmm.getCorrectiveDoseAmount());
                    }
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }

            Double amount = 0.0;
            for (String key : correctiveMap.keySet()) {

                ArrayList<Double> doses = correctiveMap.get(key);
                for (Double dose : doses) {
                    amount += dose;

                }
            }
            double dailyAverage = (amount / (float) correctiveMap.keySet().size());
            averageCorrective = dailyAverage;


            Map<String, Double> timeofday = new HashMap<>();
            for (BloodMeasurementModel bmm : allBloodGlucoseMeasurements) {
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

            String summary = String.format(getString(R.string.summary_text),
                    highestRecorded,
                    highestUnits,
                    highestRecordedDate,
                    lowestRecorded,
                    lowestUnits,
                    lowestRecordedDate,
                    averageCorrective,
                    correctiveDrugName,
                    highestTimeOfDay);
            return summary;
        }
        else{
            return "You have no recorded measurements to analyze.";
        }




    }

    private void drawHighReadingChart(){
        PieChart pieChart = findViewById(R.id.high_reading_pie_chart);
        pieChart.clear();
        pieChart.invalidate();


        List<PieEntry> entries = new ArrayList<>();
        
        float breakfastTotal = 0;
        float lunchTotal= 0;
        float dinnerTotal = 0;
        float bedtimeTotal = 0;

        for(BloodMeasurementModel bmm : _dbHelper.getAllBloodMeasurements()){
            if(bmm.isBreakfast())
                breakfastTotal +=bmm.getGlucoseMeasurement();
            if(bmm.isLunch())
                lunchTotal +=bmm.getGlucoseMeasurement();
            if(bmm.isDinner())
                dinnerTotal +=bmm.getGlucoseMeasurement();
            if(bmm.isBedtime())
                bedtimeTotal +=bmm.getGlucoseMeasurement();
        }
        float totalPoints = breakfastTotal + lunchTotal + dinnerTotal + bedtimeTotal;

        float breakfast = (breakfastTotal / totalPoints) * 100;
        float lunch = (lunchTotal / totalPoints) * 100;
        float dinner = (dinnerTotal / totalPoints) * 100;
        float bedtime = (bedtimeTotal / totalPoints) * 100;

        entries.add(new PieEntry(breakfast, "breakfast"));
        entries.add(new PieEntry(lunch, "lunch"));
        entries.add(new PieEntry(dinner, "dinner"));
        entries.add(new PieEntry(bedtime, "bedtime"));

        PieDataSet dataSet = new PieDataSet(entries,"");
        dataSet.setSliceSpace(3f);
        dataSet.setIconsOffset(new MPPointF(0, 40));
        dataSet.setSelectionShift(5f);

        // add a lot of colors

        ArrayList<Integer> colors = new ArrayList<Integer>();

        for (int c : ColorTemplate.VORDIPLOM_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.JOYFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.COLORFUL_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.LIBERTY_COLORS)
            colors.add(c);

        for (int c : ColorTemplate.PASTEL_COLORS)
            colors.add(c);

        colors.add(ColorTemplate.getHoloBlue());

        dataSet.setColors(colors);


        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        pieChart.setData(data);
        pieChart.setUsePercentValues(false);
        Description description = new Description();
        description.setText("High glucose readings");
        pieChart.setDescription(description);
        pieChart.invalidate(); // refresh
    }
    private void drawGlucoseChart() {

        LineChart graph = findViewById(R.id.chart_glucose_over_time);
        graph.invalidate();
        graph.clear();
        List<Entry> breakfastEntries = new ArrayList<>();
        List<Entry> lunchEntries = new ArrayList<>();
        List<Entry> dinnerEntries = new ArrayList<>();
        List<Entry> bedtimeEntries = new ArrayList<>();
        List<BloodMeasurementModel> measurements = _dbHelper.getAllBloodMeasurements();
        Collections.reverse(measurements);

        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.database_date_format));


        for (BloodMeasurementModel bmm : measurements) {
            long index = 0;
            try {
                cal.setTime(sdf.parse(bmm.getGlucoseMeasurementDate()));
                index = cal.getTimeInMillis();

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


        if (breakfastEntries.size() > 0 && _breakfastSw.isChecked())
            lineData.addDataSet(breakfastDataSet);
        if (lunchEntries.size() > 0 &&_lunchSw.isChecked())
            lineData.addDataSet(lunchDataSet );
        if (dinnerEntries.size() > 0 && _dinnerSw.isChecked())
            lineData.addDataSet(dinnerDataSet);
        if (bedtimeEntries.size() > 0 && _bedtimeSw.isChecked())
            lineData.addDataSet(bedtimeDataSet);

        lineData.setDrawValues(false);
        graph.getXAxis().setGranularity(1.0f);
        graph.getXAxis().setTextColor(getResources().getColor(R.color.colorSurface));
        graph.getDescription().setText("Blood glucose measurements over time.");
        graph.setData(lineData);
        graph.invalidate();
    }
    public void drawInsulinChart(){
        LineChart graph = findViewById(R.id.chart_insulin_over_time);
        graph.invalidate();
        graph.clear();
        List<Entry> shortActing = new ArrayList<>();
        List<Entry> longActing = new ArrayList<>();
        List<BloodMeasurementModel> measurements = _dbHelper.getAllBloodMeasurements();
        Collections.reverse(measurements);
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat(getString(R.string.database_date_format));


        for (BloodMeasurementModel bmm : measurements) {
            long index = 0;
            try {
                cal.setTime(sdf.parse(bmm.getGlucoseMeasurementDate()));
                index = cal.getTimeInMillis();

            } catch (ParseException e) {
                e.printStackTrace();
            }

            //if(bmm.getCorrectiveDoseAmount()>0){
                boolean existing = false;
                for(Entry entry : shortActing){
                    if(entry.getX() == index)
                    {
                        entry.setY(entry.getY() +(float) bmm.getCorrectiveDoseAmount());
                        existing = true;
                        break;
                    }
                }
                if(!existing){
                    Entry newEntry = new Entry(index, (float) bmm.getCorrectiveDoseAmount());
                    shortActing.add(newEntry);
                }
            //}

            //if(bmm.getBaselineDoseAmount()>0) {
                existing = false;
                for(Entry entry : longActing){
                    if(entry.getX() == index)
                    {
                        entry.setY(entry.getY() +(float) bmm.getBaselineDoseAmount());
                        existing = true;
                        break;
                    }
                }
                if(!existing){
                    Entry newEntry = new Entry(index, (float) bmm.getBaselineDoseAmount());
                    longActing.add(newEntry);
                }
            //}
        }



        LineDataSet shortLastingDataSet = new LineDataSet(shortActing, String.format("corrective (%s)", _dbHelper.getShortLastingDrugs().get(_dbHelper.getPosition(_dbHelper.getShortLastingDrugs(), _sharedPref.getInt(getString(R.string.pref_defaultCorrectiveDrugID),1))).getString()));
        LineDataSet baselineDataSet = new LineDataSet(longActing, String.format("baseline (%s)",_dbHelper.getLongLastingDrugs().get(_dbHelper.getPosition(_dbHelper.getLongLastingDrugs(), _sharedPref.getInt(getString(R.string.pref_defaultBaselineDrugID),1))).getString()));

        shortLastingDataSet.setColor(getResources().getColor(R.color.colorSecondary));
        baselineDataSet.setColor(getResources().getColor(R.color.colorPrimaryVariant2));

        shortLastingDataSet.setDrawCircles(false);
        baselineDataSet.setDrawCircles(false);


        LineData lineData = new LineData();


        if (shortActing.size() > 0)
            lineData.addDataSet(shortLastingDataSet);
        if (longActing.size() > 0)
            lineData.addDataSet(baselineDataSet);


        lineData.setDrawValues(false);
        graph.getXAxis().setGranularity(1.0f);
        graph.getXAxis().setTextColor(getResources().getColor(R.color.colorSurface));
        graph.getDescription().setText("Daily insulin intake over time");
        graph.setData(lineData);
        graph.invalidate();
    }
}
