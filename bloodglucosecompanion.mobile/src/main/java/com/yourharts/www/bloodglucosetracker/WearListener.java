package com.yourharts.www.bloodglucosetracker;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;
import com.google.android.gms.wearable.WearableListenerService;
import com.yourharts.www.bloodglucosetracker.Database.DBHelper;
import com.yourharts.www.bloodglucosetracker.Models.BloodMeasurementModel;

public class WearListener extends WearableListenerService {

    private GoogleApiClient _googleApiClient;
    private DBHelper _dbHelper;
    private static final String MESSAGE_PATH = "/glucose";
    private  static final String CHANNEL_ID= "88";
    private static final int NOTIFICATION_ID = 888;
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
        if (messageEvent.getPath().equals(MESSAGE_PATH)) {
            try {
                String newMeasurement = new String(messageEvent.getData());
                System.out.println("I got something: " + newMeasurement);
                String[] data = newMeasurement.split("~");
                double measurement = Double.parseDouble(data[0]);
                String date = data[1];
                Double shortLasting = !data[2].isEmpty() ? Double.parseDouble(data[2]) : 0;
                Double longLasting = !data[3].isEmpty() ? Double.parseDouble(data[3]) : 0;
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
                        data.length == 5 ? data[4] : "",
                        _sharedPref,
                        null);
                _dbHelper.addGlucoseMeasurement(model);
                Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
                // Vibrate for 500 milliseconds
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    assert v != null;
                    v.vibrate(VibrationEffect.createOneShot(1000, VibrationEffect.DEFAULT_AMPLITUDE));


                } else {
                    //deprecated in API 26
                    assert v != null;
                    v.vibrate(1000);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


            Intent startIntent = new Intent(this, MainActivity.class);
            //showForegroundNotification("New glucose measurement data received from watch.");
            startActivity(startIntent);


        }
        super.onMessageReceived(messageEvent);
    }
    private void showForegroundNotification(String contentText) {
        // Create intent that will bring our app to the front, as if it was tapped in the app
        // launcher
        Intent showTaskIntent = new Intent(getApplicationContext(), MainActivity.class);
        showTaskIntent.setAction(Intent.ACTION_MAIN);
        showTaskIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        showTaskIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        PendingIntent contentIntent = PendingIntent.getActivity(
                getApplicationContext(),
                0,
                showTaskIntent,
                PendingIntent.FLAG_UPDATE_CURRENT);

        Notification notification = new Notification.Builder(getApplicationContext())
                .setContentTitle(getString(R.string.app_name))
                .setContentText(contentText)
                .setSmallIcon(R.drawable.ic_hospital)
                .setWhen(System.currentTimeMillis())
                .setContentIntent(contentIntent)
                .build();
        startForeground(NOTIFICATION_ID, notification);
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        System.out.println("I have been destroyed");
        _googleApiClient.disconnect();
    }

}


