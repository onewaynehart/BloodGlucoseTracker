package com.yourharts.www.Models;

import android.app.Activity;
import android.content.SharedPreferences;

import com.yourharts.www.bloodglucosetracker.R;

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
        StringBuilder buffer = new StringBuilder();
        buffer.append(_id);
        buffer.append(" ");
        buffer.append(_glucoseMeasurement);
        buffer.append(" ");
        buffer.append(_glucoseMeasurementUnitID);
        buffer.append(" ");
        buffer.append(_glucoseMeasurementDate);
        buffer.append(" ");
        buffer.append(_correctiveDoseAmount);
        buffer.append(" ");
        buffer.append(_correctiveDoseTypeID);
        buffer.append(" ");
        buffer.append(_baselineDoseAmount);
        buffer.append(" ");
        buffer.append(_baselineDoseTypeID);
        buffer.append(" ");
        buffer.append(_notes);
        buffer.append("\n");
        return buffer.toString();
    }
    public String getString(String delimiter){
        StringBuilder buffer = new StringBuilder();
        buffer.append(_id);
        buffer.append(delimiter);
        buffer.append(_glucoseMeasurement);
        buffer.append(delimiter);
        buffer.append(_glucoseMeasurementUnitID);
        buffer.append(delimiter);
        buffer.append(_glucoseMeasurementDate);
        buffer.append(delimiter);
        buffer.append(_correctiveDoseAmount);
        buffer.append(delimiter);
        buffer.append(_correctiveDoseTypeID);
        buffer.append(delimiter);
        buffer.append(_baselineDoseAmount);
        buffer.append(delimiter);
        buffer.append(_baselineDoseTypeID);
        buffer.append(delimiter);
        buffer.append(_notes);
        buffer.append("\n");
        return buffer.toString();
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
