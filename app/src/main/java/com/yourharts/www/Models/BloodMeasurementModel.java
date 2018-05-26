package com.yourharts.www.Models;

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
    public BloodMeasurementModel(int id,
                                 double glucoseMeasurement,
                                 int glucoseMeasurementUnitID,
                                 String glucoseMeasurementDate,
                                 double correctiveDoseAmount,
                                 int correctiveDoseTypeID,
                                 double baselineDoseAmount,
                                 int baselineDoseTypeID,
                                 String notes,
                                 SharedPreferences sharedPreferences){
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

    public int get_id() {
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
    public double get_glucoseMeasurement() {
        return _glucoseMeasurement;
    }

    public void set_glucoseMeasurement(double _glucoseMeasurement) {
        this._glucoseMeasurement = _glucoseMeasurement;
    }

    public int get_glucoseMeasurementUnitID() {
        return _glucoseMeasurementUnitID;
    }

    public void set_glucoseMeasurementUnitID(int _glucoseMeasurementUnitID) {
        this._glucoseMeasurementUnitID = _glucoseMeasurementUnitID;
    }

    public double get_baselineDoseAmount() {
        return _baselineDoseAmount;
    }

    public void set_baselineDoseAmount(double _baselineDoseAmount) {
        this._baselineDoseAmount = _baselineDoseAmount;
    }

    public double get_correctiveDoseAmount() {
        return _correctiveDoseAmount;
    }

    public void set_correctiveDoseAmount(double _correctiveDoseAmount) {
        this._correctiveDoseAmount = _correctiveDoseAmount;
    }

    public int get_correctiveDoseTypeID() {
        return _correctiveDoseTypeID;
    }

    public void set_correctiveDoseTypeID(int _correctiveDoseTypeID) {
        this._correctiveDoseTypeID = _correctiveDoseTypeID;
    }

    public int get_baselineDoseTypeID() {
        return _baselineDoseTypeID;
    }

    public void set_baselineDoseTypeID(int _baselineDoseTypeID) {
        this._baselineDoseTypeID = _baselineDoseTypeID;
    }

    public String get_glucoseMeasurementDate() {
        return _glucoseMeasurementDate;
    }



    public String get_notes() {
        return _notes;
    }

    public void set_notes(String _notes) {
        this._notes = _notes;
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
