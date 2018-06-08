package com.yourharts.www.bloodglucosetracker.Models;

import android.app.Activity;
import android.content.SharedPreferences;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.PendingIntent.getActivity;

public class BloodMeasurementModel extends DataModelInterface {
    private int _id;
    private double _glucoseMeasurement;
    private int _glucoseMeasurementUnitID;
    private double _baselineDoseAmount;
    private double _correctiveDoseAmount;
    private int _correctiveDoseTypeID;
    private int _baselineDoseTypeID;
    private String _glucoseMeasurementDate;
    private String _notes;

    private Date _measurementDate;
    private SharedPreferences _sharedPreferences;
    private Activity _activity;
    public BloodMeasurementModel(int id,
                                 double glucoseMeasurement,
                                 int glucoseMeasurementUnitID,
                                 String glucoseMeasurementDate,
                                 double correctiveDoseAmount,
                                 int correctiveDoseTypeID,
                                 double baselineDoseAmount,
                                 int baselineDoseTypeID,
                                 String notes,
                                 SharedPreferences sharedPreferences,
                                 Activity activity){
        _activity = activity;
        _id = id;
        _glucoseMeasurement = glucoseMeasurement;
        _glucoseMeasurementUnitID = glucoseMeasurementUnitID;
        _baselineDoseAmount = baselineDoseAmount;
        _correctiveDoseAmount = correctiveDoseAmount;
        _correctiveDoseTypeID = correctiveDoseTypeID;
        _baselineDoseTypeID = baselineDoseTypeID;
        _glucoseMeasurementDate = glucoseMeasurementDate;
        _notes = notes;
        _sharedPreferences =  sharedPreferences;
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            _measurementDate = parser.parse(glucoseMeasurementDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public int getId() {
        return _id;
    }

    @Override
    public String getString() {
        String buffer = String.valueOf(_id) +
                " " +
                _glucoseMeasurement +
                " " +
                _glucoseMeasurementUnitID +
                " " +
                _glucoseMeasurementDate +
                " " +
                _correctiveDoseAmount +
                " " +
                _correctiveDoseTypeID +
                " " +
                _baselineDoseAmount +
                " " +
                _baselineDoseTypeID +
                " " +
                _notes +
                "\n";
        return buffer;
    }
    public String getString(String delimiter){
        String buffer = String.valueOf(_id) +
                delimiter +
                _glucoseMeasurement +
                delimiter +
                _glucoseMeasurementUnitID +
                delimiter +
                _glucoseMeasurementDate +
                delimiter +
                _correctiveDoseAmount +
                delimiter +
                _correctiveDoseTypeID +
                delimiter +
                _baselineDoseAmount +
                delimiter +
                _baselineDoseTypeID +
                delimiter +
                _notes +
                "\n";
        return buffer;
    }
    public double getGlucoseMeasurement() {
        return _glucoseMeasurement;
    }



    public int getGlucoseMeasurementUnitID() {
        return _glucoseMeasurementUnitID;
    }



    public double getBaselineDoseAmount() {
        return _baselineDoseAmount;
    }



    public double getCorrectiveDoseAmount() {
        return _correctiveDoseAmount;
    }



    public int getCorrectiveDoseTypeID() {
        return _correctiveDoseTypeID;
    }



    public int getBaselineDoseTypeID() {
        return _baselineDoseTypeID;
    }



    public String getGlucoseMeasurementDate() {
        return _glucoseMeasurementDate;
    }



    public String getNotes() {
        return _notes;
    }



    public boolean isHigh() {
        boolean retval = false;
        if(_sharedPreferences != null)
        {
            int defaultThreshold = _sharedPreferences.getInt("PREF_DEFAULT_THRESHOLD", 10);
            retval = this._glucoseMeasurement >= defaultThreshold;
        }
        return retval;
    }

    public boolean isBreakfast() {
        boolean retval = false;
        if(_measurementDate != null)
        {
            retval = _measurementDate.getHours() < 10;
        }
        return retval;
    }
    public boolean isLunch() {
        boolean retval = false;
        if(_measurementDate != null)
        {
            retval = _measurementDate.getHours() >= 10 && _measurementDate.getHours() < 16;
        }
        return retval;
    }
    public boolean isDinner() {
        boolean retval = false;
        if(_measurementDate != null)
        {
            retval = _measurementDate.getHours() < 21 && _measurementDate.getHours() >= 16;
        }
        return retval;
    }
    public boolean isBedtime() {
        boolean retval = false;
        if(_measurementDate != null)
        {
            retval = _measurementDate.getHours() >= 21;
        }
        return retval;
    }

}
