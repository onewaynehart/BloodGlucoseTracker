package com.yourharts.www.Models;

import android.content.Context;
import android.content.SharedPreferences;

import com.yourharts.www.bloodglucosetracker.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.app.PendingIntent.getActivity;

public class BloodMeasurementModel extends DataModelInterface {
    private int ID;
    private double GlucoseMeasurement;
    private int GlucoseMeasurementUnitID;
    private double BaselineDoseAmount;
    private double CorrectiveDoseAmount;
    private int CorrectiveDoseType;
    private int BaselineDoseType;
    private String GlucoseMeasurementDate;
    private String Notes;
    private final String DELIMITER = "||";
    private Date _measurementDate;
    private SharedPreferences _sharedPreferences;
    public BloodMeasurementModel(int id,
                                 double glucoseMeasurement,
                                 int glucoseMeasurementUnitID,
                                 String glucoseMeasurementDate,
                                 double correctiveDoseAmount,
                                 int correctiveDoseType,
                                 double baselineDoseAmount,
                                 int baselineDoseType,
                                 String notes,
                                 SharedPreferences sharedPreferences){
        ID = id;
        GlucoseMeasurement = glucoseMeasurement;
        GlucoseMeasurementUnitID = glucoseMeasurementUnitID;
        BaselineDoseAmount = baselineDoseAmount;
        CorrectiveDoseAmount = correctiveDoseAmount;
        CorrectiveDoseType = correctiveDoseType;
        BaselineDoseType = baselineDoseType;
        GlucoseMeasurementDate = glucoseMeasurementDate;
        Notes = notes;
        _sharedPreferences =  sharedPreferences;
        SimpleDateFormat parser = new SimpleDateFormat("yyyy-MM-dd HH:mm");
        try {
            _measurementDate = parser.parse(glucoseMeasurementDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }

    public int getID() {
        return ID;
    }

    @Override
    public String getString() {
        StringBuilder buffer = new StringBuilder();
        buffer.append(ID);
        buffer.append(DELIMITER);
        buffer.append(GlucoseMeasurement);
        buffer.append(DELIMITER);
        buffer.append(GlucoseMeasurementUnitID);
        buffer.append(DELIMITER);
        buffer.append(GlucoseMeasurementDate);
        buffer.append(DELIMITER);
        buffer.append(CorrectiveDoseAmount);
        buffer.append(DELIMITER);
        buffer.append(CorrectiveDoseType);
        buffer.append(DELIMITER);
        buffer.append(BaselineDoseAmount);
        buffer.append(DELIMITER);
        buffer.append(BaselineDoseType);
        buffer.append(DELIMITER);
        buffer.append(Notes);
        buffer.append("\n");
        return buffer.toString();
    }
    public String getString(String delimiter){
        StringBuilder buffer = new StringBuilder();
        buffer.append(ID);
        buffer.append(delimiter);
        buffer.append(GlucoseMeasurement);
        buffer.append(delimiter);
        buffer.append(GlucoseMeasurementUnitID);
        buffer.append(delimiter);
        buffer.append(GlucoseMeasurementDate);
        buffer.append(delimiter);
        buffer.append(CorrectiveDoseAmount);
        buffer.append(delimiter);
        buffer.append(CorrectiveDoseType);
        buffer.append(delimiter);
        buffer.append(BaselineDoseAmount);
        buffer.append(delimiter);
        buffer.append(BaselineDoseType);
        buffer.append(delimiter);
        buffer.append(Notes);
        buffer.append("\n");
        return buffer.toString();
    }
    public double getGlucoseMeasurement() {
        return GlucoseMeasurement;
    }

    public void setGlucoseMeasurement(double glucoseMeasurement) {
        GlucoseMeasurement = glucoseMeasurement;
    }

    public int getGlucoseMeasurementUnitID() {
        return GlucoseMeasurementUnitID;
    }

    public void setGlucoseMeasurementUnitID(int glucoseMeasurementUnitID) {
        GlucoseMeasurementUnitID = glucoseMeasurementUnitID;
    }

    public double getBaselineDoseAmount() {
        return BaselineDoseAmount;
    }

    public void setBaselineDoseAmount(double baselineDoseAmount) {
        BaselineDoseAmount = baselineDoseAmount;
    }

    public double getCorrectiveDoseAmount() {
        return CorrectiveDoseAmount;
    }

    public void setCorrectiveDoseAmount(double correctiveDoseAmount) {
        CorrectiveDoseAmount = correctiveDoseAmount;
    }

    public int getCorrectiveDoseType() {
        return CorrectiveDoseType;
    }

    public void setCorrectiveDoseType(int correctiveDoseType) {
        CorrectiveDoseType = correctiveDoseType;
    }

    public int getBaselineDoseType() {
        return BaselineDoseType;
    }

    public void setBaselineDoseType(int baselineDoseType) {
        BaselineDoseType = baselineDoseType;
    }

    public String getGlucoseMeasurementDate() {
        return GlucoseMeasurementDate;
    }



    public String getNotes() {
        return Notes;
    }

    public void setNotes(String notes) {
        this.Notes = notes;
    }

    public boolean isHigh() {
        boolean retval = false;
        if(_sharedPreferences != null)
        {
            int defaultThreshold = _sharedPreferences.getInt("PREF_DEFAULT_THRESHOLD", 10);
            retval = this.GlucoseMeasurement > defaultThreshold;
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
