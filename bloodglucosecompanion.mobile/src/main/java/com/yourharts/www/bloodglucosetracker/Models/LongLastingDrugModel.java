package com.yourharts.www.bloodglucosetracker.Models;
public class LongLastingDrugModel extends DataModelInterface {
    private int mID;
    private String mName;

    public LongLastingDrugModel(int ID, String Name)
    {
        mID = ID;
        mName = Name;
    }
    public int getId() {
        return mID;
    }

    @Override
    public String getString() {
        return mName;
    }



    public String getName() {
        return mName;
    }

    public void setName(String Name) {
        this.mName = Name;
    }
}
