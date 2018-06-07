package com.yourharts.www.bloodglucosetracker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.DataEvent;
import com.google.android.gms.wearable.DataEventBuffer;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.yourharts.www.Database.DBHelper;
import com.yourharts.www.Models.BloodMeasurementModel;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class WearListener extends WearableListenerService {

    private GoogleApiClient _googleApiClient;
    private DBHelper _dbHelper;
    private static final String MESSAGE_PATH = "/glucose";
    private SharedPreferences _sharedPref;

    @Override
    public void onCreate() {

        System.out.println("Im here! I'm listening!");
        _googleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();
        _googleApiClient.connect();
        _dbHelper = new DBHelper(getApplicationContext(), getFilesDir().getPath(), null);
        _sharedPref = getSharedPreferences(getString(R.string.pref_file_key), Context.MODE_PRIVATE);
        super.onCreate();
    }

    @Override
    public void onMessageReceived(MessageEvent messageEvent) {

        try {
            String newMeasurement = new String(messageEvent.getData()).toString();
            System.out.println("I got something: " + newMeasurement);
            String[] data = newMeasurement.split("~");
            double measurement = Double.parseDouble(data[0]);
            String date = data[1];
            Double shortLasting = data[2].isEmpty() == false ? Double.parseDouble(data[2]): 0;
            Double longLasting = data[3].isEmpty() == false ? Double.parseDouble(data[3]) : 0;
            int defaultBaselineDrugID = _sharedPref.getInt(getString(R.string.pref_defaultBaselineDrugID), 1);
            int defaultCorrectiveDrugID = _sharedPref.getInt(getString(R.string.pref_defaultCorrectiveDrugID), 1);
            int defaultMeasurementUnitID = _sharedPref.getInt(getString(R.string.pref_defaultMeasurementUnitID), 1);
            BloodMeasurementModel model = new BloodMeasurementModel(0,
                                                                    measurement,
                                                                    defaultMeasurementUnitID,
                                                                    date,
                                                                    shortLasting,
                                                                    defaultCorrectiveDrugID,
                                                                    longLasting,
                                                                    defaultBaselineDrugID,
                                                                    data.length == 5? data[4]: "",
                                                                    _sharedPref,
                                                                    null);
            _dbHelper.addGlucoseMeasurement(model);

        } catch (Exception e) {
            e.printStackTrace();
        }
        if (messageEvent.getPath().equals(MESSAGE_PATH)) {

            Intent startIntent = new Intent(this, MainActivity.class);
            startIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startIntent);
        }
        super.onMessageReceived(messageEvent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("I have been destroyed");
        _googleApiClient.disconnect();
    }

}


