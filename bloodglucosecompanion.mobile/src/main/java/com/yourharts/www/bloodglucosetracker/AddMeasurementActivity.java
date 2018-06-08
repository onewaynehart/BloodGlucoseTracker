package com.yourharts.www.bloodglucosetracker;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

import com.yourharts.www.bloodglucosetracker.Adapters.GenericSpinnerAdapter;
import com.yourharts.www.bloodglucosetracker.Database.DBHelper;
import com.yourharts.www.bloodglucosetracker.Models.BloodMeasurementModel;
import com.yourharts.www.bloodglucosetracker.Models.DataModelInterface;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AddMeasurementActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener, TimePickerDialog.OnTimeSetListener {
    private DBHelper _dbHelper;
    private DateFormat _dbDateFormat;
    private Spinner _measurementUnitsDropdown;
    private Spinner _correctiveDoseDrugDropdown;
    private Spinner _baselineDoseDrugDropdown;
    private EditText _glucoseAmountTB;
    private EditText _correctiveDrugAmountTB;
    private EditText _baselineDrugAmountTB;
    private EditText _notesTB;
    private TextView _dateLbl;
    private boolean _isEditMode;
    private BloodMeasurementModel _dataModel;
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
        setContentView(R.layout.layout_add_glucose_measurement);

        _measurementUnitsDropdown = findViewById(R.id.measurementUnitsDropDown);
        _correctiveDoseDrugDropdown = findViewById(R.id.correctiveDoseDrugDropdown);
        _baselineDoseDrugDropdown = findViewById(R.id.baselineDoseDrugDropdown);
        _glucoseAmountTB = findViewById(R.id.addMeasurementGlucoseAmountTB);
        _correctiveDrugAmountTB = findViewById(R.id.addMeasurementCorrectiveDoseAmountTB);
        _baselineDrugAmountTB = findViewById(R.id.addMeasurementBaselineDrugAmountTB);
        _notesTB = findViewById(R.id.addMeasurementNotesTB);
        _dateLbl = findViewById(R.id.addMeasurementTimeLabel);


        _dbDateFormat = new SimpleDateFormat(getString(R.string.database_date_time_format));
        _dbHelper = new DBHelper(getApplicationContext(), getFilesDir().getPath(),  this);


        List<DataModelInterface> units = _dbHelper.getMeasurementUnits();
        List<DataModelInterface> correctiveDrugs = _dbHelper.getShortLastingDrugs();
        List<DataModelInterface> baselineDrugs = _dbHelper.getLongLastingDrugs();
        GenericSpinnerAdapter unitsAdapter = new GenericSpinnerAdapter(getApplicationContext(), units, this);
        GenericSpinnerAdapter correctiveDrugsAdapter = new GenericSpinnerAdapter(getApplicationContext(), correctiveDrugs, this);
        GenericSpinnerAdapter baselineDrugsAdapter = new GenericSpinnerAdapter(getApplicationContext(), baselineDrugs, this);
        _correctiveDoseDrugDropdown.setAdapter(correctiveDrugsAdapter);
        _baselineDoseDrugDropdown.setAdapter(baselineDrugsAdapter);
        _measurementUnitsDropdown.setAdapter(unitsAdapter);
        int modelID = getIntent().getIntExtra("ID", 0);
        if (modelID > 0) {
            _isEditMode = true;
            _dataModel = _dbHelper.getBloodMeasurement(modelID);

        }
        _sharedPref = getSharedPreferences(getString(R.string.pref_file_key), Context.MODE_PRIVATE);
        Toolbar toolbar = findViewById(R.id.toolbar_add_measurement);
        setSupportActionBar(toolbar);
        ActionBar actionbar = getSupportActionBar();
        assert actionbar != null;
        actionbar.setDisplayHomeAsUpEnabled(true);
        actionbar.setHomeAsUpIndicator(R.drawable.ic_back_black_24dp);
        SetInitialValues();
        setupOnClickListeners();


    }
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void SetInitialValues() {

        int defaultBaselineDrugID = _sharedPref.getInt(getString(R.string.pref_defaultBaselineDrugID), 1);
        int defaultCorrectiveDrugID = _sharedPref.getInt(getString(R.string.pref_defaultCorrectiveDrugID), 1);
        int defaultMeasurementUnitID = _sharedPref.getInt(getString(R.string.pref_defaultMeasurementUnitID), 1);

        //Use defaults
        if (!_isEditMode) {
            _dateLbl.setText(_dbDateFormat.format(Calendar.getInstance().getTime()));
            _measurementUnitsDropdown.setSelection(((GenericSpinnerAdapter) _measurementUnitsDropdown.getAdapter()).getPosition(defaultMeasurementUnitID));
            _correctiveDoseDrugDropdown.setSelection(((GenericSpinnerAdapter) _correctiveDoseDrugDropdown.getAdapter()).getPosition(defaultCorrectiveDrugID));
            _baselineDoseDrugDropdown.setSelection(((GenericSpinnerAdapter) _baselineDoseDrugDropdown.getAdapter()).getPosition(defaultBaselineDrugID));
        }
        //Use existing model
        else
        {
            _glucoseAmountTB.setText(String.format(getString(R.string.decimal_format), _dataModel.getGlucoseMeasurement()));
            _correctiveDrugAmountTB.setText(String.format("%.1f", _dataModel.getCorrectiveDoseAmount()));
            _baselineDrugAmountTB.setText(String.format("%.1f", _dataModel.getBaselineDoseAmount()));
            _dateLbl.setText(_dataModel.getGlucoseMeasurementDate());
            _notesTB.setText(_dataModel.getNotes());
            _measurementUnitsDropdown.setSelection(((GenericSpinnerAdapter) _measurementUnitsDropdown.getAdapter()).getPosition(_dataModel.getGlucoseMeasurementUnitID()));
            _correctiveDoseDrugDropdown.setSelection(((GenericSpinnerAdapter) _correctiveDoseDrugDropdown.getAdapter()).getPosition(getDataModel().getCorrectiveDoseTypeID()));
            _baselineDoseDrugDropdown.setSelection(((GenericSpinnerAdapter) _baselineDoseDrugDropdown.getAdapter()).getPosition(_dataModel.getBaselineDoseTypeID()));
        }

    }

    private void setupOnClickListeners() {
        //Button clicks
        Button cancelBtn = findViewById(R.id.addMeasuremeanCancelBtn);
        Button saveBtn = findViewById(R.id.addMeasurementsaveBtn);

        cancelBtn.setOnClickListener(v -> finish());

        saveBtn.setOnClickListener(v -> {
            if (saveData()) {
                Intent intent = new Intent(AddMeasurementActivity.this, MainActivity.class);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
        _dateLbl.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            _year = cal.get(Calendar.YEAR);
            _month = cal.get(Calendar.MONTH) ;
            _dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);

            if (_isEditMode && _dataModel != null) {
                try {
                    Date modelDate = _dbDateFormat.parse(_dataModel.getGlucoseMeasurementDate());
                    cal.setTime(modelDate);
                    _year = cal.get(Calendar.YEAR);
                    _month = cal.get(Calendar.MONTH) ;
                    _dayOfMonth = cal.get(Calendar.DAY_OF_MONTH);
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
            DatePickerDialog dpd = new DatePickerDialog(AddMeasurementActivity.this, AddMeasurementActivity.this, _year, _month, _dayOfMonth);
            dpd.show();
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
        boolean retval;
        try {
            double glucoseMeasurement = _glucoseAmountTB.getText().toString().isEmpty() ? 0 : Double.parseDouble(_glucoseAmountTB.getText().toString());
            double correctiveDoseAmount = _correctiveDrugAmountTB.getText().toString().isEmpty() ? 0 : Double.parseDouble(_correctiveDrugAmountTB.getText().toString());
            double baselineDoseAmount = _baselineDrugAmountTB.getText().toString().isEmpty() ? 0 : Double.parseDouble(_baselineDrugAmountTB.getText().toString());


            if (glucoseMeasurement == 0) {
                return false;
            }

            BloodMeasurementModel dataModel = new BloodMeasurementModel(
                    _isEditMode ? _dataModel.getId() : 0,
                    glucoseMeasurement,
                    ((DataModelInterface) _measurementUnitsDropdown.getSelectedItem()).getId(),
                    _dateLbl.getText().toString(),
                    correctiveDoseAmount,
                    ((DataModelInterface) _correctiveDoseDrugDropdown.getSelectedItem()).getId(),
                    baselineDoseAmount,
                    ((DataModelInterface) _baselineDoseDrugDropdown.getSelectedItem()).getId(),
                    _notesTB.getText().toString(), _sharedPref, this);


            retval = _dbHelper.addGlucoseMeasurement(dataModel);

            boolean useLastAsDefault = _sharedPref.getBoolean(getString(R.string.pref_use_last_as_default), false);
            if(useLastAsDefault) {
                SharedPreferences.Editor editor = _sharedPref.edit();
                editor.putInt(getString(R.string.pref_defaultMeasurementUnitID), dataModel.getGlucoseMeasurementUnitID());
                editor.putInt(getString(R.string.pref_defaultBaselineDrugID), dataModel.getBaselineDoseTypeID());
                editor.putInt(getString(R.string.pref_defaultCorrectiveDrugID), dataModel.getCorrectiveDoseTypeID());
                editor.apply();
                editor.commit();
            }

        } catch (Exception e) {
            retval = false;
        }
        return retval;
    }
    public BloodMeasurementModel getDataModel() {
        return _dataModel;
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

        if (_isEditMode && _dataModel != null) {
            try {
                Date modelDate = _dbDateFormat.parse(_dataModel.getGlucoseMeasurementDate());
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
        _dateLbl.setText(output);

    }
}


