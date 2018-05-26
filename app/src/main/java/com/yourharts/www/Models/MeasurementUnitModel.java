package com.yourharts.www.Models;

public class MeasurementUnitModel extends DataModelInterface{
    private int ID;
    private String  mUnitName;

    public MeasurementUnitModel(int id, String unitname)
    {
        ID = id;
        mUnitName = unitname;
    }

    public int get_id() {
        return ID;
    }

    @Override
    public String getString() {
        return mUnitName;
    }

    public String getmUnitName() {
        return mUnitName;
    }

    @Override
    public String toString() {
        return mUnitName;
    }
}
