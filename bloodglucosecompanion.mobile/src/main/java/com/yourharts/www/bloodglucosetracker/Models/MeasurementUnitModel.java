package com.yourharts.www.bloodglucosetracker.Models;

public class MeasurementUnitModel extends DataModelInterface{
    private int ID;
    private String _unitName;

    public MeasurementUnitModel(int id, String unitname)
    {
        ID = id;
        _unitName = unitname;
    }

    public int getId() {
        return ID;
    }

    @Override
    public String getString() {
        return _unitName;
    }

    @Override
    public String toString() {
        return _unitName;
    }
}
