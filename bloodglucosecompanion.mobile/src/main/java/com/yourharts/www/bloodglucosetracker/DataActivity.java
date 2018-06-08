package com.yourharts.www.bloodglucosetracker;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.yourharts.www.bloodglucosetracker.Database.DBHelper;
import com.yourharts.www.bloodglucosetracker.Models.BloodMeasurementModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import static android.support.v4.content.FileProvider.getUriForFile;

public class DataActivity extends AppCompatActivity {
    private Button _importDataBtn;
    private Button _exportDataBtn;
    private Button _deleteDaaBtn;
    private Button _saveDatabaseBtn;
    private Button _saveRecordsButton;
    private TextView _databaseLocationTV;
    private TextView _databaseSizeTV;
    private TextView _databaseSummary;
    private DataActivityListener _listener;
    private SharedPreferences _sharedPref;
    private DBHelper _dbHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_data_management);
        _dbHelper = new DBHelper(getApplicationContext(), getFilesDir().getPath(), this);
        List<BloodMeasurementModel> allMeasurements = _dbHelper.getAllBloodMeasurements();
        _importDataBtn = findViewById(R.id.importDataBtn);
        _exportDataBtn = findViewById(R.id.exportDataBtn);
        _saveDatabaseBtn = findViewById(R.id.saveDBBtn);
        _saveRecordsButton = findViewById(R.id.saveRecordsBtn);
        _deleteDaaBtn = findViewById(R.id.deleteAllDataBtn);
        _databaseLocationTV = findViewById(R.id.databaseLocationTV);
        _databaseSizeTV = findViewById(R.id.databaseSizeTV);
        _databaseSummary = findViewById(R.id.databaseSummary);
        _sharedPref = getSharedPreferences(getString(R.string.pref_file_key), Context.MODE_PRIVATE);
        _listener = new DataActivityListener(this, _sharedPref , _dbHelper);
        _importDataBtn.setOnClickListener(_listener);
        _exportDataBtn.setOnClickListener(_listener);
        _deleteDaaBtn.setOnClickListener(_listener);
        _saveDatabaseBtn.setOnClickListener(_listener);
        _saveRecordsButton.setOnClickListener(_listener);
        setSummary();

        Toolbar toolbar = findViewById(R.id.toolbar_data);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        assert actionbar != null;
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_back_black_24dp);

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
    public void setSummary() {
        List<BloodMeasurementModel> allMeasurements = _dbHelper.getAllBloodMeasurements();
        _databaseLocationTV.setText(String.format("Your database is located at %s", _dbHelper.getDatabaseLocation()));
        _databaseSizeTV.setText(String.format("The size of your database is %d bytes", _dbHelper.getDatabaseSize()));
        _databaseSummary.setText(String.format("You currently have %d record(s) with the latest taken on %s and the earliest on %s.", allMeasurements.size(), allMeasurements.size()> 0? allMeasurements.get(0).getGlucoseMeasurementDate(): "never", allMeasurements.size()> 0? allMeasurements.get(allMeasurements.size() - 1).getGlucoseMeasurementDate() : "never"));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                new Thread(() -> {
                    try{
                        ImportDataAsync background = new ImportDataAsync();
                        background.execute(data);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }).start();
            }

        }


    }
    private class ImportDataAsync extends AsyncTask<Intent, Void, List<BloodMeasurementModel>> {
        @Override
        protected List<BloodMeasurementModel> doInBackground(Intent... intents) {

            return importData(intents[0]);
        }
        @Override
        protected void onPostExecute(List<BloodMeasurementModel> result) {
            AlertDialogHelper.showDialog(DataActivity.this, "Import Completed",String.format("We managed to import %d records from this file using the delimiter \'%s\'\nIf you were expecting a different result you can change the delimiter in settings and try again!",result.size(),_sharedPref.getString("PREF_DEFAULT_CSVDELIMITER","~")));
            setSummary();
        }
    }
    public List<BloodMeasurementModel> importData(Intent data){
        Uri uri = data.getData();
        List<BloodMeasurementModel> retval = new ArrayList<>();
        BufferedReader mBufferedReader;
        String line;
        try
        {
            int added = 0;
            assert uri != null;
            InputStream inputStream = getContentResolver().openInputStream(uri);
            assert inputStream != null;
            mBufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            List<BloodMeasurementModel> currentMeasurements = _dbHelper.getAllBloodMeasurements();
            boolean printedColumns = false;
            while ((line = mBufferedReader.readLine()) != null) {
                String[] columns = line.split(_sharedPref.getString("PREF_DEFAULT_CSVDELIMITER","~"));

                try {
                    double measurement = Double.parseDouble(columns[1]);
                    int measurementType = !columns[2].isEmpty() ?  Integer.parseInt(columns[2]) : 1;
                    String measurementDate = columns[3];
                    double correctiveAmount = !columns[4].isEmpty() ? Double.parseDouble(columns[4]): 0;
                    int correctiveType = !columns[5].isEmpty() ?  Integer.parseInt(columns[5]) : 1;
                    double baselineAmt = !columns[6].isEmpty() ?  Double.parseDouble(columns[6]) : 0;
                    int baselineType = !columns[7].isEmpty() ?  Integer.parseInt(columns[7]) : 1;
                    String notes = columns.length == 9 ? columns[8] : "";
                    BloodMeasurementModel newModel = new BloodMeasurementModel(0, measurement, measurementType, measurementDate, correctiveAmount, correctiveType, baselineAmt, baselineType, notes, _sharedPref, this);


                    boolean duplicateFound = false;
                    for(BloodMeasurementModel bmm : currentMeasurements)
                    {

                        if(measurementDate.substring(0,13).equals(bmm.getGlucoseMeasurementDate().substring(0,13)))
                        {
                            duplicateFound = true;
                            break;
                        }
                    }
                    if(!duplicateFound){
                        _dbHelper.addGlucoseMeasurement(newModel);
                        retval.add(newModel);
                        added++;
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            mBufferedReader.close();

        } catch (IOException e) {
            e.printStackTrace();

        }
        return retval;
    }

}

class DataActivityListener implements OnClickListener {
    private DataActivity _dataActivity;
    private SharedPreferences _sharedPreferences;
    private DBHelper _dbHelper;

    DataActivityListener(DataActivity dataActivity, SharedPreferences sharedPreferences, DBHelper dbHelper) {
        _dataActivity = dataActivity;
        _sharedPreferences = sharedPreferences;
        _dbHelper = dbHelper;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == _dataActivity.findViewById(R.id.importDataBtn).getId()) {
            Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
            intent.setType("text/csv");   //XML file only
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            try {

                _dataActivity.startActivityForResult(Intent.createChooser(intent, "Select"), 100);
            } catch (android.content.ActivityNotFoundException ex) {
                // Potentially direct the user to the Market with a Dialog
                Toast.makeText(_dataActivity, "Drive Not Found", Toast.LENGTH_LONG).show();
            }
        }
        if(v.getId() == _dataActivity.findViewById(R.id.exportDataBtn).getId()){
            String delimiter = _sharedPreferences.getString("PREF_DEFAULT_CSVDELIMITER", "~");
            String csv = _dbHelper.getMeasurementsCSVText(delimiter);
            File csvFilePath = new File(_dataActivity.getFilesDir().getPath(), "csv");
            csvFilePath.mkdirs();
            File newFile = new File(csvFilePath, UUID.randomUUID().toString() + ".csv");
            try {
                FileWriter writer = new FileWriter(newFile);
                writer.write(csv);
                writer.flush();
                writer.close();

                Uri contentUri = getUriForFile(_dataActivity, "com.yourharts.www.bloodglucosetracker.fileprovider", newFile);
                Intent sendIntent = new Intent();
                sendIntent.setAction(Intent.ACTION_SEND);
                sendIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                sendIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                SimpleDateFormat dbDateFormat = new SimpleDateFormat(_dataActivity.getString(R.string.database_date_time_format));
                sendIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Blood glucose measurements_"+ dbDateFormat.format(Calendar.getInstance().getTime()));
                sendIntent.setDataAndType(null, _dataActivity.getContentResolver().getType(contentUri));

                _dataActivity.startActivity(sendIntent);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if(v.getId() == _dataActivity.findViewById(R.id.saveRecordsBtn).getId()){
            String delimiter = _sharedPreferences.getString("PREF_DEFAULT_CSVDELIMITER", "~");
            String csv = _dbHelper.getMeasurementsCSVText(delimiter);
            String filename = "/Blood_glucose_measurements_"+ UUID.randomUUID().toString() + ".csv";
            File outputFilePath =  new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Blood Glucose Measurements/Records_Exports_CSV");
            outputFilePath.mkdirs();
            File output = new File(outputFilePath, filename);
            try {

                FileWriter writer = new FileWriter(output);
                writer.write(csv);
                writer.flush();
                writer.close();
                AlertDialogHelper.showDialog(_dataActivity,"Hurray", "Records successfully saved to downloads folder! ("+output.getPath()+")");
            } catch (IOException e) {
                AlertDialogHelper.showDialog(_dataActivity, "Booo", "Something went wrong and we failed.\nTry checking to see if this application has permissions to storage.\nIf it helps, this is what we know: "+e.getMessage());
                e.printStackTrace();
            }

        }
        if(v.getId() == _dataActivity.findViewById(R.id.saveDBBtn).getId()){
            _dbHelper.exportDatabase();
        }
        if(v.getId() == _dataActivity.findViewById(R.id.deleteAllDataBtn).getId()){
            AlertDialog.Builder alert = new AlertDialog.Builder(_dataActivity, R.style.MyAlertDialogStyle);

            alert.setTitle("Delete entry");
            alert.setMessage("Are you sure you want to delete all measurements? It cannot be undone! Well you can always export your data first, then import it back I suppose...");
            alert.setPositiveButton(R.string.deleteYesBtn, (dialog, which) -> {
                // continue with delete
                _dbHelper.deleteAllMeasurementRecords();
                _dataActivity.setSummary();
            });
            alert.setNegativeButton(android.R.string.no, (dialog, which) -> {
                // close dialog
                dialog.cancel();
            });
            alert.show();
        }
    }


}
