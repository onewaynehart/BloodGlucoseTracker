package com.yourharts.www.bloodglucosetracker;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.yourharts.www.Database.DBHelper;
import com.yourharts.www.Models.BloodMeasurementModel;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.xml.transform.Result;

import static android.widget.Toast.LENGTH_SHORT;

public class DataActivity extends Activity {
    private ImageButton _importDataBtn;
    private DataActivityListener _listener;
    private SharedPreferences _sharedPref;
    private DBHelper _dbHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_data_management);
        _importDataBtn = findViewById(R.id.importDataBtn);
        _sharedPref = getSharedPreferences(getString(R.string.pref_file_key), Context.MODE_PRIVATE);
        _listener = new DataActivityListener(this, _sharedPref);
        _importDataBtn.setOnClickListener(_listener);
        _dbHelper = new DBHelper(getApplicationContext(), getFilesDir().getPath());
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
                        String[] columns = line.split("~");

                        try {
                            double measurement = Double.parseDouble(columns[1]);
                            int measurementType = Integer.parseInt(columns[2]);
                            String measurementDate = columns[3];
                            double correctiveAmount = Double.parseDouble(columns[4]);
                            int correctiveType = Integer.parseInt(columns[5]);
                            double baselineAmt = Double.parseDouble(columns[6]);
                            int baselineType = Integer.parseInt(columns[7]);
                            String notes = columns.length == 9 ? columns[8] : "";
                            BloodMeasurementModel newModel = new BloodMeasurementModel(0, measurement, measurementType, measurementDate, correctiveAmount, correctiveType, baselineAmt, baselineType, notes, _sharedPref);


                            boolean duplicateFound = false;
                            for(BloodMeasurementModel bmm : currentMeasurements)
                            {

                                if(measurementDate.equals(bmm.get_glucoseMeasurementDate()))
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

    public DataActivityListener(DataActivity dataActivity, SharedPreferences sharedPreferences) {
        _dataActivity = dataActivity;
        _sharedPreferences = sharedPreferences;
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
    }
}
