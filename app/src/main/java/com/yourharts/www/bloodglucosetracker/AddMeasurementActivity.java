package com.yourharts.www.bloodglucosetracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.yourharts.www.Adapters.GenericSpinnerAdapter;
import com.yourharts.www.Adapters.GlucoseMeasurementAdapter;
import com.yourharts.www.Database.DBHelper;
import com.yourharts.www.Models.BloodMeasurementModel;
import com.yourharts.www.Models.DataModelInterface;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

public class AddMeasurementActivity extends AppCompatActivity {
    private DBHelper mDbHelper;
    private DateFormat dbDateFormat;
    private Spinner mMeasurementUnitsDropdown;
    private Spinner mCorrectiveDoseDrugDropdown;
    private Spinner mbaselineDoseDrugDropdown;
    private EditText mGlucoseAmountTB;
    private EditText mCorrectiveDrugAmountTB;
    private EditText mbaselineDrugAmountTB;
    private EditText mNotesTB;
    private TextView mDateLbl;
    private boolean mIsEditMode;
    private BloodMeasurementModel mDataModel;
    private GlucoseMeasurementAdapter mGlucoseMEasurementAdapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_glucose_measurement);
        Toolbar toolbar = findViewById(R.id.toolbar);
        mMeasurementUnitsDropdown = findViewById(R.id.measurementUnitsDropDown);
        mCorrectiveDoseDrugDropdown = findViewById(R.id.correctiveDoseDrugDropdown);
        mbaselineDoseDrugDropdown = findViewById(R.id.baselineDoseDrugDropdown);
        mGlucoseAmountTB = findViewById(R.id.addMeasurementGlucoseAmountTB);
        mCorrectiveDrugAmountTB = findViewById(R.id.addMeasurementCorrectiveDoseAmountTB);
        mbaselineDrugAmountTB = findViewById(R.id.addMeasurementBaselineDrugAmountTB);
        mNotesTB = findViewById(R.id.addMeasurementNotesTB);
        mDateLbl = findViewById(R.id.addMeasurementTimeLabel);
        setSupportActionBar(toolbar);

        dbDateFormat = new SimpleDateFormat(getString(R.string.database_date_format));
        mDbHelper = new DBHelper(getApplicationContext(), getFilesDir().getPath());


        List<DataModelInterface> units = mDbHelper.getMeasurementUnits();
        List<DataModelInterface> correctiveDrugs = mDbHelper.getShortLastingDrugs();
        List<DataModelInterface> baselineDrugs = mDbHelper.getLongLastingDrugs();
        GenericSpinnerAdapter unitsAdapter = new GenericSpinnerAdapter(getApplicationContext(), units);
        GenericSpinnerAdapter correctiveDrugsAdapter = new GenericSpinnerAdapter(getApplicationContext(), correctiveDrugs);
        GenericSpinnerAdapter baselineDrugsAdapter = new GenericSpinnerAdapter(getApplicationContext(), baselineDrugs);
        mCorrectiveDoseDrugDropdown.setAdapter(correctiveDrugsAdapter);
        mbaselineDoseDrugDropdown.setAdapter(baselineDrugsAdapter);
        mMeasurementUnitsDropdown.setAdapter(unitsAdapter);
        int modelID = getIntent().getIntExtra("ID", 0);
        if(modelID >0){
            mIsEditMode = true;
            mDataModel = mDbHelper.getBloodMeasurement(modelID);

        }

        //Get defaults
        int currentCount = 0;
        int defaultMeasurementUnitID = 1;
        int defaultCorrectiveDrugID = 1;
        int defaultBaselineDrugID=1;
        if(mDataModel != null)
        {
            mGlucoseAmountTB.setText(String.format(getString(R.string.decimal_format),mDataModel.getGlucoseMeasurement()));
            mCorrectiveDrugAmountTB.setText(String.format("%.1f",mDataModel.getCorrectiveDoseAmount()));
            mbaselineDrugAmountTB.setText(String.format("%.1f",mDataModel.getBaselineDoseAmount()));
            mDateLbl.setText(mDataModel.getGlucoseMeasurementDate());
            mNotesTB.setText(mDataModel.getNotes());
        }

        //Set defaults
        if(!mIsEditMode)
            mDateLbl.setText(dbDateFormat.format( Calendar.getInstance().getTime()));

        for(DataModelInterface dmi : ((GenericSpinnerAdapter)mMeasurementUnitsDropdown.getAdapter()).getDataset()) {
            if (!mIsEditMode) {
                    if (dmi.getID() == defaultMeasurementUnitID) {
                        mMeasurementUnitsDropdown.setSelection(currentCount);
                        break;
                    }
                }
                if (dmi.getID() == mDataModel.getGlucoseMeasurementUnitID()) {
                    mMeasurementUnitsDropdown.setSelection(currentCount);
                    break;
                }

                currentCount++;

        }
        currentCount = 0;
        for(DataModelInterface dmi : ((GenericSpinnerAdapter)mCorrectiveDoseDrugDropdown.getAdapter()).getDataset())
        {
            if(!mIsEditMode) {
                if (dmi.getID() == defaultCorrectiveDrugID) {
                    mCorrectiveDoseDrugDropdown.setSelection(currentCount);
                    break;
                }
            }

            if(dmi.getID() == mDataModel.getCorrectiveDoseType()){
                mCorrectiveDoseDrugDropdown.setSelection(currentCount);
                break;
            }
            currentCount++;
        }
        currentCount = 0;
        for(DataModelInterface dmi : ((GenericSpinnerAdapter)mbaselineDoseDrugDropdown.getAdapter()).getDataset())
        {
            if(!mIsEditMode){
                    if (dmi.getID() == defaultBaselineDrugID){
                    mbaselineDoseDrugDropdown.setSelection(currentCount);
                    break;
                }
            }

            if(dmi.getID() == mDataModel.getBaselineDoseType()){
                mbaselineDoseDrugDropdown.setSelection(currentCount);
                break;
            }

            currentCount++;
        }



        //Button clicks
        Button cancelBtn = findViewById(R.id.addMeasuremeanCancelBtn);
        Button saveBtn = findViewById(R.id.addMeasurementsaveBtn);

        cancelBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                finish();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                if(saveData()){
                    Intent intent = new Intent(AddMeasurementActivity.this, MainActivity.class);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });

    }
    @Override
    public void onStop()
    {
        super.onStop();
        this.setResult(RESULT_OK);
    }


    @Override
    public void onDestroy()
    {
        super.onDestroy();
        this.setResult(RESULT_OK);
    }
    private boolean saveData()
    {
            boolean retval = false;
            try {
                double glucoseMeasurement = mGlucoseAmountTB.getText().toString().isEmpty() ? 0 : Double.parseDouble(mGlucoseAmountTB.getText().toString());
                double correctiveDoseAmount = mCorrectiveDrugAmountTB.getText().toString().isEmpty() ? 0 : Double.parseDouble(mCorrectiveDrugAmountTB.getText().toString());
                double baselineDoseAmount = mbaselineDrugAmountTB.getText().toString().isEmpty() ? 0 : Double.parseDouble(mbaselineDrugAmountTB.getText().toString());


                if(glucoseMeasurement == 0)
                {
                    return false;
                }
                BloodMeasurementModel dataModel = new BloodMeasurementModel(
                        mIsEditMode ? mDataModel.getID() : 0,
                        glucoseMeasurement,
                        ((DataModelInterface)mMeasurementUnitsDropdown.getSelectedItem()).getID(),
                        mDateLbl.getText().toString(),
                        correctiveDoseAmount,
                        ((DataModelInterface)mCorrectiveDoseDrugDropdown.getSelectedItem()).getID(),
                        baselineDoseAmount,
                        ((DataModelInterface)mbaselineDoseDrugDropdown.getSelectedItem()).getID(),
                        mNotesTB.getText().toString());

                    retval = mDbHelper.AddGlucoseMeasurement(dataModel);

            }
            catch (Exception e){
                retval  =false;
            }
        return retval;
    }


    public void setGlucoseMEasurementAdapter(GlucoseMeasurementAdapter glucoseMEasurementAdapter) {
        this.mGlucoseMEasurementAdapter = glucoseMEasurementAdapter;
    }

    public boolean getIsEditMode() {
        return mIsEditMode;
    }

    public void setIsEditMode(boolean mIsEditMode) {
        this.mIsEditMode = mIsEditMode;
    }

    public BloodMeasurementModel getDataModel() {
        return mDataModel;
    }

    public void setDataModel(BloodMeasurementModel mDataModel) {
        this.mDataModel = mDataModel;
    }
}


