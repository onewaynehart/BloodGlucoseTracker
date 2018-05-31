package com.yourharts.www.bloodglucosetracker;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.design.widget.NavigationView;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;

import com.yourharts.www.Database.DBHelper;
import com.yourharts.www.Models.DataModelInterface;

import java.util.List;


public class SettingsActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getFragmentManager().beginTransaction().replace(android.R.id.content, new MainPreferenceFragment()).commit();
        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.getMenu().getItem(3).setChecked(true);
    }
    public static  class MainPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        private ListPreference _glucoseUnitsLP;
        private ListPreference _correctiveDrugTypeLP;
        private ListPreference _baselineDrugTypeLP;
        private ListPreference _defaultCSVLP;
        private EditTextPreference _defaultThresholdETP;
        private SwitchPreference _useLastAsDefaultSw;
        private SwitchPreference _showSummaryCard;


        private DBHelper _dbHelper;
        private SharedPreferences _sharedPref;
        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_main);
            _sharedPref = getActivity().getSharedPreferences(getString(R.string.pref_file_key), Context.MODE_PRIVATE);
            _dbHelper = new DBHelper(getActivity(), getActivity().getFilesDir().getPath(), this.getActivity());

            _glucoseUnitsLP = (ListPreference) findPreference(getString(R.string.pref_defaultMeasurementUnitID));
            _correctiveDrugTypeLP = (ListPreference) findPreference(getString(R.string.pref_defaultCorrectiveDrugID));
            _baselineDrugTypeLP = (ListPreference) findPreference(getString(R.string.pref_defaultBaselineDrugID)) ;
            _useLastAsDefaultSw = (SwitchPreference)findPreference(getString(R.string.pref_use_last_as_default));
            _defaultThresholdETP = (EditTextPreference)findPreference("PREF_DEFAULT_THRESHOLD");
            _defaultCSVLP = (ListPreference) findPreference("PREF_DEFAULT_CSVDELIMITER");
            _showSummaryCard = (SwitchPreference) findPreference("PREF_SHOW_SUMMARY_CARD");
            setDefaults();


        }
        private void setDefaults() {
            if(_dbHelper == null){
                _dbHelper = new DBHelper(getActivity(), getActivity().getFilesDir().getPath(), this.getActivity());
            }
            List<DataModelInterface> glucoseMeasurementTypes  = _dbHelper.getMeasurementUnits();
            CharSequence[] glucoseMeasurementNames = new CharSequence[glucoseMeasurementTypes.size()];
            CharSequence[] glucoseMeasurementIDs = new CharSequence[glucoseMeasurementTypes.size()];

            List<DataModelInterface> correctiveDrugs = _dbHelper.getShortLastingDrugs();
            CharSequence[] correctiveDrugNames = new CharSequence[correctiveDrugs.size()];
            CharSequence[] correctiveDrugIDs = new CharSequence[correctiveDrugs.size()];

            List<DataModelInterface> baselineDrugs = _dbHelper.getLongLastingDrugs();
            CharSequence[] baselineDrugNames = new CharSequence[baselineDrugs.size()];
            CharSequence[] baselineDrugIDs = new CharSequence[baselineDrugs.size()];

            int defaultMeasurementUnitID = _sharedPref.getInt(getString(R.string.pref_defaultMeasurementUnitID), 1);
            int defaultCorrectiveDrugID = _sharedPref.getInt(getString(R.string.pref_defaultCorrectiveDrugID), 1);
            int defaultBaselineDrugID = _sharedPref.getInt(getString(R.string.pref_defaultBaselineDrugID), 1);
            int defaultThreshold = _sharedPref.getInt("PREF_DEFAULT_THRESHOLD", 10);
            String csvDelimiter = _sharedPref.getString("PREF_DEFAULT_CSVDELIMITER","~");
            int count = 0;
            for(DataModelInterface dmi : glucoseMeasurementTypes)
            {
                glucoseMeasurementNames[count] = dmi.getString();
                glucoseMeasurementIDs[count] = Integer.toString(dmi.getId());
                count++;
            }
            _glucoseUnitsLP.setEntries(glucoseMeasurementNames);
            _glucoseUnitsLP.setEntryValues(glucoseMeasurementIDs);
            _glucoseUnitsLP.setValueIndex(_dbHelper.getPosition(glucoseMeasurementTypes, defaultMeasurementUnitID));

            count = 0;

            for(DataModelInterface dmi : correctiveDrugs)
            {
                correctiveDrugNames[count] = dmi.getString();
                correctiveDrugIDs[count] = Integer.toString(dmi.getId());
                count++;
            }
            _correctiveDrugTypeLP.setEntries(correctiveDrugNames);
            _correctiveDrugTypeLP.setEntryValues(correctiveDrugIDs);
            _correctiveDrugTypeLP.setValueIndex(_dbHelper.getPosition(correctiveDrugs, defaultCorrectiveDrugID));

            count = 0;

            for(DataModelInterface dmi : baselineDrugs)
            {
                baselineDrugNames[count] = dmi.getString();
                baselineDrugIDs[count] = Integer.toString(dmi.getId());
                count++;
            }
            _baselineDrugTypeLP.setEntries(baselineDrugNames);
            _baselineDrugTypeLP.setEntryValues(baselineDrugIDs);
            _baselineDrugTypeLP.setValueIndex(_dbHelper.getPosition(baselineDrugs, defaultBaselineDrugID));
            _defaultThresholdETP.setText(Integer.toString(defaultThreshold));
            _defaultCSVLP.setValue(csvDelimiter);
            _defaultThresholdETP.setSummary("Current value: "+defaultThreshold);

            updateListPrefSummary_PREF_LIST(_defaultCSVLP);
            updateListPrefSummary_PREF_LIST(_correctiveDrugTypeLP);
            updateListPrefSummary_PREF_LIST(_glucoseUnitsLP);
            updateListPrefSummary_PREF_LIST(_baselineDrugTypeLP);
        }

        @Override
        public void onResume() {
            super.onResume();
            getPreferenceScreen().getSharedPreferences()
                    .registerOnSharedPreferenceChangeListener(this);
            setDefaults();
        }

        @Override
        public void onPause() {
            super.onPause();
            getPreferenceScreen().getSharedPreferences()
                    .unregisterOnSharedPreferenceChangeListener(this);
        }

        private  void updateListPrefSummary_PREF_LIST(ListPreference preference){
            CharSequence entry = preference.getEntry();
            preference.setSummary("Current selection: " + entry);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            if(key.equals("PREF_DEFAULT_CSVDELIMITER")){
                SharedPreferences.Editor editor = _sharedPref.edit();
                String defaultcsvDelimiter = "~";
                try {
                    defaultcsvDelimiter = _defaultCSVLP.getValue();
                    editor.putString("PREF_DEFAULT_CSVDELIMITER", defaultcsvDelimiter);
                    editor.apply();
                    editor.commit();
                    updateListPrefSummary_PREF_LIST(_defaultCSVLP);
                }
                catch(Exception e){

                }

            }
            if(key.equals("PREF_DEFAULT_THRESHOLD")){
                SharedPreferences.Editor editor = _sharedPref.edit();
                int defaultThreshold = 10;
                try {
                    defaultThreshold = Integer.parseInt(_defaultThresholdETP.getText());
                    editor.putInt("PREF_DEFAULT_THRESHOLD", defaultThreshold);
                    editor.apply();
                    editor.commit();
                    _defaultThresholdETP.setSummary("Current value: "+defaultThreshold);
                }
                catch(Exception e){

                }
            }
            if(key.equals(getString(R.string.pref_defaultMeasurementUnitID))) {
                SharedPreferences.Editor editor = _sharedPref.edit();
                int measurementTypeID = 0;
                try {
                    measurementTypeID = Integer.parseInt(_glucoseUnitsLP.getValue());
                    editor.putInt(getString(R.string.pref_defaultMeasurementUnitID), measurementTypeID);
                    editor.apply();
                    editor.commit();
                    updateListPrefSummary_PREF_LIST(_glucoseUnitsLP);
                }
                catch(Exception e){

                }
            }
            if(key.equals(getString(R.string.pref_defaultCorrectiveDrugID))) {
                SharedPreferences.Editor editor = _sharedPref.edit();
                int correctiveDrugID = 0;
                try {
                    correctiveDrugID = Integer.parseInt(_correctiveDrugTypeLP.getValue());
                    editor.putInt(getString(R.string.pref_defaultCorrectiveDrugID), correctiveDrugID);
                    editor.apply();
                    editor.commit();
                    updateListPrefSummary_PREF_LIST(_correctiveDrugTypeLP);
                }
                catch(Exception e){

                }
            }
            if(key.equals(getString(R.string.pref_defaultBaselineDrugID))) {
                SharedPreferences.Editor editor = _sharedPref.edit();
                int baselineDrugID = 0;
                try {
                    baselineDrugID = Integer.parseInt(_baselineDrugTypeLP.getValue());
                    editor.putInt(getString(R.string.pref_defaultBaselineDrugID), baselineDrugID);
                    editor.apply();
                    editor.commit();
                    updateListPrefSummary_PREF_LIST(_baselineDrugTypeLP);
                }
                catch(Exception e){

                }
            }
            if(key.equals(getString(R.string.pref_use_last_as_default))) {
                SharedPreferences.Editor editor = _sharedPref.edit();
                boolean useLastAsDefault = false;
                try {
                    useLastAsDefault = _useLastAsDefaultSw.isChecked();
                    editor.putBoolean(getString(R.string.pref_use_last_as_default), useLastAsDefault);
                    editor.apply();
                    editor.commit();
                }
                catch(Exception e){

                }
            }
            if(key.equals(("PREF_SHOW_SUMMARY_CARD"))){
                SharedPreferences.Editor editor = _sharedPref.edit();
                boolean showSummary = false;
                try {
                    showSummary = _showSummaryCard.isChecked();
                    editor.putBoolean("PREF_SHOW_SUMMARY_CARD", showSummary);
                    editor.apply();
                    editor.commit();
                }
                catch(Exception e){

                }
            }
        }
    }



}
