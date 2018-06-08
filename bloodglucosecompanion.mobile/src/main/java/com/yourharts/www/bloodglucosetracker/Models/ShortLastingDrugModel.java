package com.yourharts.www.bloodglucosetracker.Models;
public class ShortLastingDrugModel extends DataModelInterface{
    private int _id;
    private String _name;

    public ShortLastingDrugModel(int id, String Name)
    {
        _id = id;
        _name = Name;
    }
    public int getId() {
        return _id;
    }

    @Override
    public String getString() {
        return _name;
    }

    public String getName() {
        return _name;
    }

    public void setName(String Name) {
        this._name = Name;
    }
}
