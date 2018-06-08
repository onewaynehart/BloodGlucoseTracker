package com.yourharts.www.bloodglucosetracker.Adapters;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;

import com.yourharts.www.bloodglucosetracker.Database.DBHelper;
import com.yourharts.www.bloodglucosetracker.Models.BloodMeasurementModel;
import com.yourharts.www.bloodglucosetracker.Models.DataModelInterface;
import com.yourharts.www.bloodglucosetracker.AddMeasurementActivity;
import com.yourharts.www.bloodglucosetracker.MainActivity;
import com.yourharts.www.bloodglucosetracker.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class GlucoseMeasurementAdapter extends RecyclerView.Adapter<GlucoseMeasurementAdapter.GlucoseMeasurementViewHolder>{
    @NonNull
    private List<BloodMeasurementModel> _dataset;
    private SharedPreferences _sharedPref;
    private MainActivity _activity;

    public GlucoseMeasurementAdapter(@NonNull List<BloodMeasurementModel> dataSet){
        _dataset = dataSet;
    }
    @Override
    public void onBindViewHolder(@NonNull GlucoseMeasurementViewHolder holder, int position) {

        BloodMeasurementModel model = _dataset.get(position);
        holder.getMeasurementDateTime().setText(model.getGlucoseMeasurementDate());
        holder.getMeasurementAmount().setText(String.format("%.1f",model.getGlucoseMeasurement()));
        holder.getCorrectiveDoseAmount().setText(String.format("%.1f",model.getCorrectiveDoseAmount()));
        holder.getBaselineDoseAmount().setText(String.format("%.1f",model.getBaselineDoseAmount()));
        holder.getNotes().setText(model.getNotes());

        DBHelper dbHelper = _activity.getDBHelper();
        _sharedPref = _activity.getSharedPreferences(_activity.getString(R.string.pref_file_key), Context.MODE_PRIVATE);

        List<DataModelInterface> measurementUnits = dbHelper.getMeasurementUnits();
        List<DataModelInterface> correctiveDrugTypes = dbHelper.getShortLastingDrugs();
        List<DataModelInterface> baselineDrugTypes = dbHelper.getLongLastingDrugs();

        for(DataModelInterface dmi : measurementUnits){
            if(dmi.getId() == model.getGlucoseMeasurementUnitID()){
                holder.getMeasurementUnits().setText(dmi.getString());
                break;
            }
        }
        for(DataModelInterface dmi : correctiveDrugTypes){
            if(dmi.getId() == model.getCorrectiveDoseTypeID()){
                holder.getCorrectiveDoseDrugName().setText(dmi.getString());
                break;
            }
        }
        for(DataModelInterface dmi : baselineDrugTypes){
            if(dmi.getId() == model.getBaselineDoseTypeID()){
                holder.getBaselineDoseDrugName().setText(dmi.getString());
                break;
            }
        }


        if(model.getGlucoseMeasurement()> _sharedPref.getInt("PREF_DEFAULT_THRESHOLD", 10))
        {
            holder.getWarningImage().setVisibility(View.VISIBLE);
        }
        else
        {
            holder.getWarningImage().setVisibility(View.INVISIBLE);
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date measurementTime = sdf.parse(model.getGlucoseMeasurementDate());
            int hour = measurementTime.getHours();
            if(hour < 10){
                holder.getTimeImageView().setImageResource(R.drawable.ic_coffee);
            }
            else if(hour < 16){
                holder.getTimeImageView().setImageResource(R.drawable.ic_baseline_fastfood_24px);
            }
            else if(hour < 21){
                holder.getTimeImageView().setImageResource(R.drawable.ic_baseline_local_bar_24px);
            }
            else {
                holder.getTimeImageView().setImageResource(R.drawable.ic_brightness_3_black_24dp);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        holder.getNotesRow().setVisibility((model.getNotes().isEmpty() || model.getNotes().trim().isEmpty()) ? View.GONE : View.VISIBLE);
        holder.getBaselineAmountRow().setVisibility((model.getBaselineDoseAmount() == 0 ? View.GONE : View.VISIBLE));
        holder.getCorrectiveAmountRow().setVisibility((model.getCorrectiveDoseAmount() == 0 ? View.GONE : View.VISIBLE));
    }

    @Override
    public int getItemCount() {
        return _dataset.size();
    }
    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public GlucoseMeasurementViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create a new view
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View v = inflater.inflate(R.layout.layout_blood_glucose_measurement_view, parent, false);
        return new GlucoseMeasurementViewHolder(v);
    }



    public void setActivity(MainActivity activity) {
        this._activity = activity;
    }


    public  class GlucoseMeasurementViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        private View mView;
        private TextView _measurementDateTime;
        private TextView _measurementAmount;
        private TextView _measurementUnits;
        private TextView _correctiveDoseAmount;
        private TextView _baselineDoseAmount;
        private TextView _correctiveDoseDrugName;
        private TextView _baselineDoseDrugName;
        private TextView _notes;
        private ImageView _warningImage;
        private ImageView _deleteImage;
        private ImageView _editImage;
        private ImageView _timeImageView;
        private TableRow _notesRow;
        private TableRow _correctiveAmountRow;
        private TableRow _baselineAmountRow;

        GlucoseMeasurementViewHolder(View view) {
            super(view);
            mView = view;
            _measurementDateTime = mView.findViewById(R.id.bloodGlucoseMeasurementdateLabel);
            _measurementAmount = mView.findViewById(R.id.bloodGlucoseMeasurementAmountLabel);

            _measurementUnits = mView.findViewById(R.id.bloodGlucoseMeasurementAmountUnitsLabel);
            _correctiveDoseAmount = mView.findViewById(R.id.bloodGlucoseMeasurementCorrectiveDoseAmount);
            _baselineDoseAmount = mView.findViewById(R.id.bloodGlucoseMeasurementBaselineDoseAmount);
            _baselineDoseDrugName = mView.findViewById(R.id.bloodGlucoseMeasurementBaselineDoseDrugName);
            _correctiveDoseDrugName = mView.findViewById(R.id.bloodGlucoseMeasurementCorrectiveDoseDrugName);
            _notes = mView.findViewById(R.id.bloodGlucoseMeasurementNotes);
            _warningImage = mView.findViewById(R.id.bloodMeasurementWarning);
            _deleteImage = mView.findViewById(R.id.bloodGlucoseMeasurementDeleteButton);
            _editImage = mView.findViewById(R.id.bloodGlucoseMeasurementEditButton);
            _timeImageView = mView.findViewById(R.id.timeImageView);
            _notesRow = mView.findViewById(R.id._notesRow);
            _correctiveAmountRow = mView.findViewById(R.id._correctiveAmountRow);
            _baselineAmountRow = mView.findViewById(R.id._baselineAmountRow);


            View.OnClickListener clickListener = v -> {
                if (v.equals(_deleteImage)) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(mView.getContext(), R.style.MyAlertDialogStyle);

                    alert.setTitle("Delete entry");
                    alert.setMessage("Are you sure you want to delete this measurement? It cannot be undone!");
                    alert.setPositiveButton(R.string.deleteYesBtn, (dialog, which) -> {
                        // continue with delete
                        BloodMeasurementModel model = _dataset.get(getAdapterPosition());
                        if(_activity.getDBHelper().deleteMeasurementRecord(model.getId()))
                            removeAt(getAdapterPosition());
                    });
                    alert.setNegativeButton(android.R.string.no, (dialog, which) -> {
                        // close dialog
                        dialog.cancel();
                    });
                    alert.show();
                }
                if (v.equals(_editImage)) {
                    BloodMeasurementModel model = _dataset.get(getAdapterPosition());
                    Intent intent = new Intent(_activity, AddMeasurementActivity.class);
                    intent.putExtra("ID", model.getId());
                    _activity.startActivity(intent);
                }
            };
            _editImage.setOnClickListener(clickListener);
            _deleteImage.setOnClickListener(clickListener);
            view.setOnClickListener(this);

        }

        TextView getMeasurementDateTime() {
            return _measurementDateTime;
        }

        TextView getMeasurementAmount() {
            return _measurementAmount;
        }

        TextView getMeasurementUnits() {
            return _measurementUnits;
        }

        TextView getCorrectiveDoseAmount() {
            return _correctiveDoseAmount;
        }

        TextView getBaselineDoseAmount() {
            return _baselineDoseAmount;
        }

        TextView getNotes() {
            return _notes;
        }

        TextView getCorrectiveDoseDrugName() {
            return _correctiveDoseDrugName;
        }

        TextView getBaselineDoseDrugName() {
            return _baselineDoseDrugName;
        }

        ImageView getWarningImage() {
            return _warningImage;
        }

        @Override
        public void onClick(View v) {
            if(v.equals(_deleteImage)) removeAt(getPosition());
        }

        void removeAt(int position) {
            _dataset.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, _dataset.size());
        }
        public void addAt(int position, BloodMeasurementModel model){
            _dataset.add(position,model);
            notifyDataSetChanged();
        }

        TableRow getNotesRow() {
            return _notesRow;
        }

        public ImageView getTimeImageView() {
            return _timeImageView;
        }

        TableRow getCorrectiveAmountRow() {
            return _correctiveAmountRow;
        }

        TableRow getBaselineAmountRow() {
            return _baselineAmountRow;
        }
    }
}
