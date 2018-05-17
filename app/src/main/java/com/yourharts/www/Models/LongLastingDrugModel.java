package com.yourharts.www.Models;
public class LongLastingDrugModel extends DataModelInterface {
    private int mID;
    private String mName;

    public LongLastingDrugModel(int ID, String Name)
    {
        mID = ID;
        mName = Name;
    }
    public int getID() {
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
