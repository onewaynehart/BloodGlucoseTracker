package com.yourharts.www.bloodglucosetracker;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.yourharts.www.Adapters.GenericSpinnerAdapter;
import com.yourharts.www.Adapters.GlucoseMeasurementAdapter;
import com.yourharts.www.Database.DBHelper;
import com.yourharts.www.Models.BloodMeasurementModel;
import com.yourharts.www.Models.DataModelInterface;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AddMeasurementActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
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
    private SharedPreferences _sharedPref;
    private int _year;
    private int _month;
    private int _dayOfMonth;
    private int _hour;
    private int _minute;
    private int _seconds;

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
        if (modelID > 0) {
            mIsEditMode = true;
            mDataModel = mDbHelper.getBloodMeasurement(modelID);

        }
        _sharedPref = getSharedPreferences(getString(R.string.pref_file_key), Context.MODE_PRIVATE);

        SetInitialValues();
        setupOnClickListeners();


    }

    private void SetInitialValues() {

        int defaultBaselineDrugID = _sharedPref.getInt(getString(R.string.defaultBaselineDrugID), 1);
        int defaultCorrectiveDrugID = _sharedPref.getInt(getString(R.string.defaultCorrectiveDrugID), 1);
        int defaultMeasurementUnitID = _sharedPref.getInt(getString(R.string.defaultMeasuremntUnitID), 1);

        //Use defaults
        if (!mIsEditMode) {
            mDateLbl.setText(dbDateFormat.format(Calendar.getInstance().getTime()));
            mMeasurementUnitsDropdown.setSelection(((GenericSpinnerAdapter)mMeasurementUnitsDropdown.getAdapter()).getPosition(defaultMeasurementUnitID));
            mCorrectiveDoseDrugDropdown.setSelection(((GenericSpinnerAdapter)mCorrectiveDoseDrugDropdown.getAdapter()).getPosition(defaultCorrectiveDrugID));
            mbaselineDoseDrugDropdown.setSelection(((GenericSpinnerAdapter)mbaselineDoseDrugDropdown.getAdapter()).getPosition(defaultBaselineDrugID));
        }
        //Use existing model
        else
        {
            mGlucoseAmountTB.setText(String.format(getString(R.string.decimal_format), mDataModel.getGlucoseMeasurement()));
            mCorrectiveDrugAmountTB.setText(String.format("%.1f", mDataModel.getCorrectiveDoseAmount()));
            mbaselineDrugAmountTB.setText(String.format("%.1f", mDataModel.getBaselineDoseAmount()));
            mDateLbl.setText(mDataModel.getGlucoseMeasurementDate());
            mNotesTB.setText(mDataModel.getNotes());
            mMeasurementUnitsDropdown.setSelection(((GenericSpinnerAdapter)mMeasurementUnitsDropdown.getAdapter()).getPosition(mDataModel.getGlucoseMeasurementUnitID()));
            mCorrectiveDoseDrugDropdown.setSelection(((GenericSpinnerAdapter)mCorrectiveDoseDrugDropdown.getAdapter()).getPosition(getDataModel().getCorrectiveDoseType()));
            mbaselineDoseDrugDropdown.setSelection(((GenericSpinnerAdapter)mbaselineDoseDrugDropdown.getAdapter()).getPosition(mDataModel.getBaselineDoseType()));
        }

    }

    private void setupOnClickListeners() {
        //Button clicks
        Button cancelBtn = findViewById(R.id.addMeasuremeanCancelBtn);
        Button saveBtn = findViewById(R.id.addMeasurementsaveBtn);

        cancelBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });

        saveBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (saveData()) {
                    Intent intent = new Intent(AddMeasurementActivity.this, MainActivity.class);
                    setResult(RESULT_OK, intent);
                    finish();
                }
            }
        });
        mDateLbl.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Calendar cal = Calendar.getInstance();
                _year = cal.get(Calendar.YEAR);
                _month = cal.get(Calendar.MONTH) + 1;
                _dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);

                if (mIsEditMode && mDataModel != null) {
                    try {
                        Date modelDate = dbDateFormat.parse(mDataModel.getGlucoseMeasurementDate());
                        cal.setTime(modelDate);
                        _year = cal.get(Calendar.YEAR);
                        _month = cal.get(Calendar.MONTH) + 1;
                        _dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
                DatePickerDialog dpd = new DatePickerDialog(AddMeasurementActivity.this, AddMeasurementActivity.this, _year, _month, _dayOfMonth);
                dpd.show();
            }
        });
    }

    @Override
    public void onStop() {
        super.onStop();
        this.setResult(RESULT_OK);
    }
    @Override
    public void onResume(){
        super.onResume();
        SetInitialValues();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.setResult(RESULT_OK);
    }

    private boolean saveData() {
        boolean retval = false;
        try {
            double glucoseMeasurement = mGlucoseAmountTB.getText().toString().isEmpty() ? 0 : Double.parseDouble(mGlucoseAmountTB.getText().toString());
            double correctiveDoseAmount = mCorrectiveDrugAmountTB.getText().toString().isEmpty() ? 0 : Double.parseDouble(mCorrectiveDrugAmountTB.getText().toString());
            double baselineDoseAmount = mbaselineDrugAmountTB.getText().toString().isEmpty() ? 0 : Double.parseDouble(mbaselineDrugAmountTB.getText().toString());


            if (glucoseMeasurement == 0) {
                return false;
            }

            BloodMeasurementModel dataModel = new BloodMeasurementModel(
                    mIsEditMode ? mDataModel.getID() : 0,
                    glucoseMeasurement,
                    ((DataModelInterface) mMeasurementUnitsDropdown.getSelectedItem()).getID(),
                    mDateLbl.getText().toString(),
                    correctiveDoseAmount,
                    ((DataModelInterface) mCorrectiveDoseDrugDropdown.getSelectedItem()).getID(),
                    baselineDoseAmount,
                    ((DataModelInterface) mbaselineDoseDrugDropdown.getSelectedItem()).getID(),
                    mNotesTB.getText().toString());


            retval = mDbHelper.AddGlucoseMeasurement(dataModel);

            SharedPreferences.Editor editor = _sharedPref.edit();
            editor.putInt(getString(R.string.defaultMeasuremntUnitID), dataModel.getGlucoseMeasurementUnitID());
            editor.putInt(getString(R.string.defaultBaselineDrugID), dataModel.getBaselineDoseType());
            editor.putInt(getString(R.string.defaultCorrectiveDrugID), dataModel.getCorrectiveDoseType());
            editor.apply();
            editor.commit();

        } catch (Exception e) {
            retval = false;
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

    @Override
    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
        _year = year;
        _month = month;
        _dayOfMonth = dayOfMonth;


        Calendar cal = Calendar.getInstance();
        _minute = cal.get(Calendar.MINUTE);
        _hour = cal.get(Calendar.HOUR);
        _seconds = cal.get(Calendar.SECOND);

        if (mIsEditMode && mDataModel != null) {
            try {
                Date modelDate = dbDateFormat.parse(mDataModel.getGlucoseMeasurementDate());
                cal.setTime(modelDate);
                _minute = cal.get(Calendar.MINUTE);
                _hour = cal.get(Calendar.HOUR);
                _seconds = cal.get(Calendar.SECOND);
            } catch (ParseException e) {
                e.printStackTrace();
            }
        }

        TimePickerDialog tpd = new TimePickerDialog(AddMeasurementActivity.this, AddMeasurementActivity.this, _hour, _minute, true);
        tpd.show();
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        _hour = hourOfDay;
        _minute = minute;
        String output = String.format("%d-%02d-%02d %02d:%02d", _year, _month + 1, _dayOfMonth, _hour, _minute);
        mDateLbl.setText(output);

    }
}


