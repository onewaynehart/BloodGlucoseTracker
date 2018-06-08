package com.yourharts.www.bloodglucosetracker.Database;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Environment;
import android.util.Log;

import com.yourharts.www.bloodglucosetracker.Models.BloodMeasurementModel;
import com.yourharts.www.bloodglucosetracker.Models.DataModelInterface;
import com.yourharts.www.bloodglucosetracker.Models.LongLastingDrugModel;
import com.yourharts.www.bloodglucosetracker.Models.MeasurementUnitModel;
import com.yourharts.www.bloodglucosetracker.Models.ShortLastingDrugModel;
import com.yourharts.www.bloodglucosetracker.R;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.yourharts.www.bloodglucosetracker.AlertDialogHelper;

public class DBHelper  extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "glucose_tracker_v2.db";
    private String DATABASE_LOCATION;
    private final static String TAG = "DatabaseHelper";
    private Context _context;
    private SharedPreferences _sharedPreferences;
    Activity _activity;
    public DBHelper(Context context, String filePath, Activity activity) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        _context = context;
        _activity = activity;
        DATABASE_LOCATION = new StringBuffer(filePath).append("/").append(DATABASE_NAME).toString();
        _sharedPreferences  = context.getSharedPreferences(context.getString(R.string.pref_file_key), Context.MODE_PRIVATE);

    }
    public void prepareDatabase() throws IOException {

            if(!checkDataBase())
                copyDataBase();
    }
    private boolean checkDataBase() {
        boolean checkDB = false;
        try {
            File file = new File(DATABASE_LOCATION);
            checkDB = file.exists();
        } catch(SQLiteException e) {
            Log.d(TAG, e.getMessage());
        }
        return checkDB;
    }
    private void copyDataBase() throws IOException {
        OutputStream os = new FileOutputStream(DATABASE_LOCATION);
        InputStream is = _context.getAssets().open(DATABASE_NAME);
        byte[] buffer = new byte[1024];
        int length;
        while ((length = is.read(buffer)) > 0) {
            os.write(buffer, 0, length);
        }
        is.close();
        os.flush();
        os.close();
    }
    public void deleteDb() {
        File file = new File(DATABASE_LOCATION);
        if(file.exists()) {
            file.delete();
            Log.d(TAG, "Database deleted.");
        }
    }
    public String getDatabaseLocation()
    {
        return DATABASE_LOCATION;
    }
    public long getDatabaseSize(){
        File file = new File(DATABASE_LOCATION);
        return file.length();
    }
    @Override
    public void onCreate(SQLiteDatabase db) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

    public int getPosition(List<DataModelInterface>items,String itemString){
        int retval = -1;
        for(int count = 0 ; count <items.size(); count++){
            if(items.get(count).getString().equals(itemString)){
                retval= count;
                break;
            }
        }
        return retval;
    }
    public int getPosition(List<DataModelInterface>items,int itemID){
        int retval = -1;
        for(int count = 0 ; count <items.size(); count++){
            if(items.get(count).getId() == itemID){
                retval= count;
                break;
            }
        }
        return retval;
    }
    public int getMeasurementPositionInList(List<BloodMeasurementModel>items,int itemID){
        int retval = -1;
        for(int count = 0 ; count <items.size(); count++){
            if(items.get(count).getId() == itemID){
                retval= count;
                break;
            }
        }
        return retval;
    }
    public List<DataModelInterface> getMeasurementUnits()
    {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(DATABASE_LOCATION, null, SQLiteDatabase.OPEN_READONLY);
        String query = "SELECT id, UnitName FROM GlucoseUnitTypes" + " Order By UnitName ASC";
        Cursor cursor = db.rawQuery(query, null);
        List<DataModelInterface> retval = new ArrayList<DataModelInterface>();
        while(cursor.moveToNext()) {
            MeasurementUnitModel drug = new MeasurementUnitModel(cursor.getInt(0),cursor.getString(1));

            retval.add(drug);
        }
        cursor.close();
        db.close();
        return retval;
    }
    public List<DataModelInterface> getShortLastingDrugs()
    {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(DATABASE_LOCATION, null, SQLiteDatabase.OPEN_READONLY);
        String query = "SELECT id, drugname FROM CorrectiveDoseDrugs Order By drugname ASC";
        Cursor cursor = db.rawQuery(query, null);
        List<DataModelInterface> retval = new ArrayList<DataModelInterface>();
        while(cursor.moveToNext()) {
            ShortLastingDrugModel drug = new ShortLastingDrugModel(cursor.getInt(0),cursor.getString(1));

            retval.add(drug);
        }
        cursor.close();
        db.close();
        return retval;
    }
    public List<DataModelInterface> getLongLastingDrugs()
    {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(DATABASE_LOCATION, null, SQLiteDatabase.OPEN_READONLY);
        String query = "SELECT id, drugname FROM BaselineDoseDrugs Order By drugname ASC";
        Cursor cursor = db.rawQuery(query, null);
        List<DataModelInterface> retval = new ArrayList<DataModelInterface>();
        while(cursor.moveToNext()) {
            LongLastingDrugModel drug = new LongLastingDrugModel(cursor.getInt(0),cursor.getString(1));

            retval.add(drug);
        }
        cursor.close();
        db.close();
        return retval;
    }
    public List<BloodMeasurementModel> getAllBloodMeasurements()
    {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(DATABASE_LOCATION, null, SQLiteDatabase.OPEN_READONLY);
        List<BloodMeasurementModel> retval = new ArrayList<BloodMeasurementModel>();
        String query = "SELECT * FROM GlucoseMeasurement Order By GlucoseMeasurementDate DESC";
        Cursor cursor = db.rawQuery(query, null);
        while(cursor.moveToNext()) {
            BloodMeasurementModel measurement = new BloodMeasurementModel(
                    cursor.getInt(0),
                    cursor.getDouble(1),
                    cursor.getInt(2),
                    cursor.getString(3),
                    cursor.getDouble(4),
                    cursor.getInt(5),
                    cursor.getDouble(6),
                    cursor.getInt(7),
                    cursor.getString(8), _sharedPreferences , _activity);

            retval.add(measurement);
        }
        cursor.close();
        db.close();
        return retval;
    }
    public String getMeasurementsCSVText(String delimiter){
        StringBuffer buffer = new StringBuffer();
        buffer.append("ID");
        buffer.append(delimiter);
        buffer.append("GlucoseMeasurement");
        buffer.append(delimiter);
        buffer.append("GlucoseMeasurementUnitID");
        buffer.append(delimiter);
        buffer.append("GlucoseMeasurementDate");
        buffer.append(delimiter);
        buffer.append("CorrectiveDoseAmount");
        buffer.append(delimiter);
        buffer.append("CorrectiveDoseTypeID");
        buffer.append(delimiter);
        buffer.append("BaselineDoseAmount");
        buffer.append(delimiter);
        buffer.append("BaselineDoseTypeID");
        buffer.append(delimiter);
        buffer.append("Notes");
        buffer.append("\n");

        List<BloodMeasurementModel> measurements = getAllBloodMeasurements();

        for(BloodMeasurementModel model : measurements)
        {
            buffer.append(model.getString(delimiter));
        }


        return buffer.toString();
    }
    public boolean deleteMeasurementRecord(int ID)
    {
        boolean retval = false;
        SQLiteDatabase db = SQLiteDatabase.openDatabase(DATABASE_LOCATION, null, SQLiteDatabase.OPEN_READWRITE);
        try{
            db.delete("GlucoseMeasurement", "ID = "+ID, null);
            db.close();

            retval = true;
        }
        catch (Exception e){

        }
        return retval;
    }
    public void deleteAllMeasurementRecords()
    {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(DATABASE_LOCATION, null, SQLiteDatabase.OPEN_READWRITE);

        try{
            db.delete("GlucoseMeasurement", null, null);
            db.close();
        }
        catch (Exception e){
        }
    }
    public boolean addGlucoseMeasurement(BloodMeasurementModel model){
        boolean retval = false;
        String tableName = "GlucoseMeasurement";
        String col_1 = "GlucoseMeasurement";
        String col_2 = "GlucoseMeasurementUnitID";
        String col_3 = "GlucoseMeasurementDate";
        String col_4 = "CorrectiveDoseAmount";
        String col_5 = "CorrectiveDoseTypeID";
        String col_6 = "BaselineDoseAmount";
        String col_7 = "BaselineDoseTypeID";
        String col_8 = "Notes";
        SQLiteDatabase db = SQLiteDatabase.openDatabase(DATABASE_LOCATION, null, SQLiteDatabase.OPEN_READWRITE);

        try{
            ContentValues cv = new ContentValues();
            cv.put(col_1,model.getGlucoseMeasurement());
            cv.put(col_2,model.getGlucoseMeasurementUnitID());
            cv.put(col_3,model.getGlucoseMeasurementDate());
            cv.put(col_4,model.getCorrectiveDoseAmount());
            cv.put(col_5,model.getCorrectiveDoseTypeID());
            cv.put(col_6,model.getBaselineDoseAmount());
            cv.put(col_7,model.getBaselineDoseTypeID());
            cv.put(col_8,model.getNotes());
            if(model.getId()==0) {
                db.insert(tableName, null, cv);
            }
            else{
                db.update(tableName, cv, "ID = "+model.getId(), null);
            }
            db.close();
            retval = true;
        }
        catch (Exception e)
        {
            retval = false;
        }
        return retval;
    }

    public BloodMeasurementModel getBloodMeasurement(int modelID) {
        SQLiteDatabase db = SQLiteDatabase.openDatabase(DATABASE_LOCATION, null, SQLiteDatabase.OPEN_READONLY);
        List<BloodMeasurementModel> retval = new ArrayList<BloodMeasurementModel>();
        String query = "SELECT * FROM GlucoseMeasurement WHERE ID = "+ modelID;
        Cursor cursor = db.rawQuery(query, null);
        while(cursor.moveToNext()) {
            BloodMeasurementModel measurement = new BloodMeasurementModel(
                    cursor.getInt(0),
                    cursor.getDouble(1),
                    cursor.getInt(2),
                    cursor.getString(3),
                    cursor.getDouble(4),
                    cursor.getInt(5),
                    cursor.getDouble(6),
                    cursor.getInt(7),
                    cursor.getString(8) , _sharedPreferences, _activity);

            retval.add(measurement);
        }
        cursor.close();
        db.close();
        return retval.get(0);
    }

    public void exportDatabase() {

        try {
            String filename = "/Blood_glucose_measurements_"+ UUID.randomUUID().toString() + ".db";
            File outputFilePath =  new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "Blood Glucose Measurements/Database_Exports");
            outputFilePath.mkdirs();
            InputStream is = new FileInputStream(DATABASE_LOCATION);
            File outputfile = new File(outputFilePath.getPath(),filename);

            outputfile.createNewFile();
            OutputStream os = new FileOutputStream(outputfile);

            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) > 0) {
                os.write(buffer, 0, length);
            }
            is.close();
            os.flush();
            os.close();
            AlertDialogHelper.showDialog(_activity, "Hurray", "Records successfully saved to downloads folder! ("+outputFilePath+")");

        } catch (Exception e) {
            AlertDialogHelper.showDialog(_activity, "Booo", "Something went wrong and we failed.\nTry checking to see if this application has permissions to storage.\nIf it helps, this is what we know: "+e.getMessage());
            e.printStackTrace();
        }
    }
}
