package com.yourharts.www.Models;

import java.util.Date;

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

    public BloodMeasurementModel(int id,
                                 double glucoseMeasurement,
                                 int glucoseMeasurementUnitID,
                                 String glucoseMeasurementDate,
                                 double correctiveDoseAmount,
                                 int correctiveDoseType,
                                 double baselineDoseAmount,
                                 int baselineDoseType,
                                 String notes){
        ID = id;
        GlucoseMeasurement = glucoseMeasurement;
        GlucoseMeasurementUnitID = glucoseMeasurementUnitID;
        BaselineDoseAmount = baselineDoseAmount;
        CorrectiveDoseAmount = correctiveDoseAmount;
        CorrectiveDoseType = correctiveDoseType;
        BaselineDoseType = baselineDoseType;
        GlucoseMeasurementDate = glucoseMeasurementDate;
        Notes = notes;
    }

    public int getID() {
        return ID;
    }

    @Override
    public String getString() {
        return null;
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

    public void setGlucoseMeasurementDate(String glucoseMeasurementDate) {
        GlucoseMeasurementDate = glucoseMeasurementDate;
    }

    public String getNotes() {
        return Notes;
    }

    public void setNotes(String notes) {
        this.Notes = notes;
    }
}
