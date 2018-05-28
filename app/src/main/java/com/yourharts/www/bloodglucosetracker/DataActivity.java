package com.yourharts.www.bloodglucosetracker;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.yourharts.www.Database.DBHelper;
import com.yourharts.www.Models.BloodMeasurementModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.xml.transform.Result;

import static android.support.v4.content.FileProvider.getUriForFile;
import static android.widget.Toast.LENGTH_SHORT;

public class DataActivity extends Activity {
    private Button _importDataBtn;
    private Button _exportDataBtn;
    private Button _deleteDaaBtn;
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
        _dbHelper = new DBHelper(getApplicationContext(), getFilesDir().getPath());
        List<BloodMeasurementModel> allMeasurements = _dbHelper.getBloodMeasurements();
        _importDataBtn = findViewById(R.id.importDataBtn);
        _exportDataBtn = findViewById(R.id.exportDataBtn);
        _deleteDaaBtn = findViewById(R.id.deleteAllDataBtn);
        _databaseLocationTV = findViewById(R.id.databaseLocationTV);
        _databaseSizeTV = findViewById(R.id.databaseSizeTV);
        _databaseSummary = findViewById(R.id.databaseSummary);
        _sharedPref = getSharedPreferences(getString(R.string.pref_file_key), Context.MODE_PRIVATE);
        _listener = new DataActivityListener(this, _sharedPref , _dbHelper);
        _importDataBtn.setOnClickListener(_listener);
        _exportDataBtn.setOnClickListener(_listener);
        _deleteDaaBtn.setOnClickListener(_listener);
        setSummary();
    }

    public void setSummary() {
        List<BloodMeasurementModel> allMeasurements = _dbHelper.getBloodMeasurements();
        _databaseLocationTV.setText(String.format("Your database is located at %s", _dbHelper.getDatabaseLocation()));
        _databaseSizeTV.setText(String.format("The size of your database is %d bytes", _dbHelper.getDatabaseSize()));
        _databaseSummary.setText(String.format("You currently have %d record(s) with the latest taken on %s and the earliest on %s.", allMeasurements.size(), allMeasurements.size()> 0? allMeasurements.get(0).get_glucoseMeasurementDate(): "never", allMeasurements.size()> 0? allMeasurements.get(allMeasurements.size() - 1).get_glucoseMeasurementDate() : "never"));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 100) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                BufferedReader mBufferedReader = null;
                String line;
                try
                {
                    int added = 0;
                    InputStream inputStream = getContentResolver().openInputStream(uri);
                    mBufferedReader = new BufferedReader(new InputStreamReader(inputStream));
                    List<BloodMeasurementModel> currentMeasurements = _dbHelper.getBloodMeasurements();
                    boolean printedColumns = false;
                    while ((line = mBufferedReader.readLine()) != null) {
                        String[] columns = line.split(_sharedPref.getString("PREF_DEFAULT_CSVDELIMITER","~"));

                        try {
                            double measurement = Double.parseDouble(columns[1]);
                            int measurementType = columns[2].isEmpty() == false ?  Integer.parseInt(columns[2]) : 1;
                            String measurementDate = columns[3];
                            double correctiveAmount = columns[4].isEmpty() == false ? Double.parseDouble(columns[4]): 0;
                            int correctiveType =columns[5].isEmpty() == false ?  Integer.parseInt(columns[5]) : 1;
                            double baselineAmt =columns[6].isEmpty() == false ?  Double.parseDouble(columns[6]) : 0;
                            int baselineType =columns[7].isEmpty() == false ?  Integer.parseInt(columns[7]) : 1;
                            String notes = columns.length == 9 ? columns[8] : "";
                            BloodMeasurementModel newModel = new BloodMeasurementModel(0, measurement, measurementType, measurementDate, correctiveAmount, correctiveType, baselineAmt, baselineType, notes, _sharedPref);


                            boolean duplicateFound = false;
                            for(BloodMeasurementModel bmm : currentMeasurements)
                            {

                                if(measurementDate.substring(0,13).equals(bmm.get_glucoseMeasurementDate().substring(0,13)))
                                {
                                    duplicateFound = true;
                                    break;
                                }
                            }
                            if(duplicateFound == false){
                                _dbHelper.addGlucoseMeasurement(newModel);
                                added++;
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                    Toast.makeText(this, "Imported "+added+ " records", Toast.LENGTH_SHORT).show();

                    mBufferedReader.close();
                    setSummary();
                } catch (IOException e) {
                    e.printStackTrace();

                }
            }

        }


    }


}

class DataActivityListener implements OnClickListener {
    private DataActivity _dataActivity;
    private SharedPreferences _sharedPreferences;
    private DBHelper _dbHelper;

    public DataActivityListener(DataActivity dataActivity, SharedPreferences sharedPreferences, DBHelper dbHelper) {
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
                Toast.makeText(_dataActivity, "Drive Not Found", LENGTH_SHORT).show();
            }
        }
        if(v.getId() == _dataActivity.findViewById(R.id.exportDataBtn).getId()){
            String delimiter = _sharedPreferences.getString("PREF_DEFAULT_CSVDELIMITER", "~");
            String csv = _dbHelper.GetMeasurementsCSVText(delimiter);
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
