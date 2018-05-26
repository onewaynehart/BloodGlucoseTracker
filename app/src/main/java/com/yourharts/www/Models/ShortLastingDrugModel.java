package com.yourharts.www.Models;
public class ShortLastingDrugModel extends DataModelInterface{
    private int mID;
    private String mName;

    public ShortLastingDrugModel(int ID, String Name)
    {
        mID = ID;
        mName = Name;
    }
    public int get_id() {
        return mID;
    }

    @Override
    public String getString() {
        return mName;
    }

    public void setID(int ID) {
        this.mID = ID;
    }

    public String getName() {
        return mName;
    }

    public void setName(String Name) {
        this.mName = Name;
    }
}
