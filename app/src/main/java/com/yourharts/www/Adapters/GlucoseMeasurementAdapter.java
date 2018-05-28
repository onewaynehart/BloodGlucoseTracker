package com.yourharts.www.Adapters;

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

import com.yourharts.www.Database.DBHelper;
import com.yourharts.www.Models.BloodMeasurementModel;
import com.yourharts.www.Models.DataModelInterface;
import com.yourharts.www.bloodglucosetracker.AddMeasurementActivity;
import com.yourharts.www.bloodglucosetracker.MainActivity;
import com.yourharts.www.bloodglucosetracker.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class GlucoseMeasurementAdapter extends RecyclerView.Adapter<GlucoseMeasurementAdapter.GlucosemeasurementViewHolder>{
    @NonNull
    private List<BloodMeasurementModel> mDataset;
    private SharedPreferences _sharedPref;
    private MainActivity mActivity;

    public GlucoseMeasurementAdapter(List<BloodMeasurementModel> dataSet){
        mDataset = dataSet;
    }
    @Override
    public void onBindViewHolder(@NonNull GlucosemeasurementViewHolder holder, int position) {

        BloodMeasurementModel model = mDataset.get(position);
        holder.getmMeasurementDateTime().setText(model.get_glucoseMeasurementDate());
        holder.getmMeasurementAmount().setText(String.format("%.1f",model.getGlucoseMeasurement()));
        holder.getmCorrectiveDoseAmount().setText(String.format("%.1f",model.getCorrectiveDoseAmount()));
        holder.getmBaselineDoseAmount().setText(String.format("%.1f",model.getBaselineDoseAmount()));
        holder.getmNotes().setText(model.getNotes());

        DBHelper dbHelper = mActivity.getDBHelper();
        _sharedPref = mActivity.getSharedPreferences(mActivity.getString(R.string.pref_file_key), Context.MODE_PRIVATE);

        List<DataModelInterface> measurementUnits = dbHelper.getMeasurementUnits();
        List<DataModelInterface> correctiveDrugTypes = dbHelper.getShortLastingDrugs();
        List<DataModelInterface> baselineDrugTypes = dbHelper.getLongLastingDrugs();

        for(DataModelInterface dmi : measurementUnits){
            if(dmi.getId() == model.getGlucoseMeasurementUnitID()){
                holder.getmMeasurementUnits().setText(dmi.getString());
                break;
            }
        }
        for(DataModelInterface dmi : correctiveDrugTypes){
            if(dmi.getId() == model.getCorrectiveDoseTypeID()){
                holder.getmCorrectiveDoseDrugName().setText(dmi.getString());
                break;
            }
        }
        for(DataModelInterface dmi : baselineDrugTypes){
            if(dmi.getId() == model.getBaselineDoseTypeID()){
                holder.getmBaselineDoseDrugName().setText(dmi.getString());
                break;
            }
        }


        if(model.getGlucoseMeasurement()> _sharedPref.getInt("PREF_DEFAULT_THRESHOLD", 10))
        {
            holder.getmWarningImage().setVisibility(View.VISIBLE);
        }
        else
        {
            holder.getmWarningImage().setVisibility(View.INVISIBLE);
        }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            Date measurementTime = sdf.parse(model.get_glucoseMeasurementDate());
            int hour = measurementTime.getHours();
            if(hour < 10){
                holder.getTimeImageView().setImageResource(R.drawable.ic_coffee);
            }
            else if(hour < 16 && hour >= 10){
                holder.getTimeImageView().setImageResource(R.drawable.ic_baseline_fastfood_24px);
            }
            else if(hour < 21 && hour >= 16){
                holder.getTimeImageView().setImageResource(R.drawable.ic_baseline_local_bar_24px);
            }
            else if(hour >= 21 ){
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
        return mDataset.size();
    }
    // Create new views (invoked by the layout manager)
    @Override
    public GlucoseMeasurementAdapter.GlucosemeasurementViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        // create a new view
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        View v = inflater.inflate(R.layout.blood_glucose_measurement_item_view, parent, false);
        GlucosemeasurementViewHolder vh = new GlucosemeasurementViewHolder(v);
        return vh;
    }

    public MainActivity getmActivity() {
        return mActivity;
    }

    public void setmActivity(MainActivity activity) {
        this.mActivity = activity;
    }


    public  class GlucosemeasurementViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        // each data item is just a string in this case
        private View mView;
        private TextView mMeasurementDateTime;
        private TextView mMeasurementAmount;
        private TextView mMeasurementUnits;
        private TextView mCorrectiveDoseAmount;
        private TextView mBaselineDoseAmount;
        private TextView mCorrectiveDoseDrugName;
        private TextView mBaselineDoseDrugName;
        private TextView mNotes;
        private ImageView mWarningImage;
        private ImageView mdeleteImage;
        private ImageView mEditImage;
        private ImageView mTimeImageView;
        private TableRow _notesRow;
        private TableRow _correctiveAmountRow;
        private TableRow _baselineAmountRow;
        private DBHelper mDBHelper;
        public GlucosemeasurementViewHolder(View view) {
            super(view);
            mView = view;
            mMeasurementDateTime = mView.findViewById(R.id.bloodGlucoseMeasurementdateLabel);
            mMeasurementAmount = mView.findViewById(R.id.bloodGlucoseMeasurementAmountLabel);

            mMeasurementUnits = mView.findViewById(R.id.bloodGlucoseMeasurementAmountUnitsLabel);
            mCorrectiveDoseAmount = mView.findViewById(R.id.bloodGlucoseMeasurementCorrectiveDoseAmount);
            mBaselineDoseAmount = mView.findViewById(R.id.bloodGlucoseMeasurementBaselineDoseAmount);
            mBaselineDoseDrugName = mView.findViewById(R.id.bloodGlucoseMeasurementBaselineDoseDrugName);
            mCorrectiveDoseDrugName = mView.findViewById(R.id.bloodGlucoseMeasurementCorrectiveDoseDrugName);
            mNotes = mView.findViewById(R.id.bloodGlucoseMeasurementNotes);
            mWarningImage = mView.findViewById(R.id.bloodMeasurementWarning);
            mdeleteImage = mView.findViewById(R.id.bloodGlucoseMeasurementDeleteButton);
            mEditImage = mView.findViewById(R.id.bloodGlucoseMeasurementEditButton);
            mTimeImageView = mView.findViewById(R.id.timeImageView);
            _notesRow = mView.findViewById(R.id._notesRow);
            _correctiveAmountRow = mView.findViewById(R.id._correctiveAmountRow);
            _baselineAmountRow = mView.findViewById(R.id._baselineAmountRow);


            View.OnClickListener clickListener = v -> {
                if (v.equals(mdeleteImage)) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(mView.getContext(), R.style.MyAlertDialogStyle);

                    alert.setTitle("Delete entry");
                    alert.setMessage("Are you sure you want to delete this measurement? It cannot be undone!");
                    alert.setPositiveButton(R.string.deleteYesBtn, (dialog, which) -> {
                        // continue with delete
                        BloodMeasurementModel model = mDataset.get(getAdapterPosition());
                        if(mActivity.getDBHelper().deleteMeasurementRecord(model.getId())==true)
                            removeAt(getAdapterPosition());
                    });
                    alert.setNegativeButton(android.R.string.no, (dialog, which) -> {
                        // close dialog
                        dialog.cancel();
                    });
                    alert.show();
                }
                if (v.equals(mEditImage)) {
                    BloodMeasurementModel model = mDataset.get(getAdapterPosition());
                    Intent intent = new Intent(mActivity, AddMeasurementActivity.class);
                    intent.putExtra("ID", model.getId());
                    mActivity.startActivity(intent);
                }
            };
            mEditImage.setOnClickListener(clickListener);
            mdeleteImage.setOnClickListener(clickListener);
            view.setOnClickListener(this);

        }

        public TextView getmMeasurementDateTime() {
            return mMeasurementDateTime;
        }

        public TextView getmMeasurementAmount() {
            return mMeasurementAmount;
        }

        public TextView getmMeasurementUnits() {
            return mMeasurementUnits;
        }

        public TextView getmCorrectiveDoseAmount() {
            return mCorrectiveDoseAmount;
        }

        public TextView getmBaselineDoseAmount() {
            return mBaselineDoseAmount;
        }

        public TextView getmNotes() {
            return mNotes;
        }

        public TextView getmCorrectiveDoseDrugName() {
            return mCorrectiveDoseDrugName;
        }

        public TextView getmBaselineDoseDrugName() {
            return mBaselineDoseDrugName;
        }

        public ImageView getmWarningImage() {
            return mWarningImage;
        }

        public ImageView getMdeleteImage() {
            return mdeleteImage;
        }

        @Override
        public void onClick(View v) {
            if(v.equals(mdeleteImage)) removeAt(getPosition());
        }

        public void removeAt(int position) {
            mDataset.remove(position);
            notifyItemRemoved(position);
            notifyItemRangeChanged(position, mDataset.size());
        }
        public void addAt(int position, BloodMeasurementModel model){
            mDataset.add(position,model);
            notifyDataSetChanged();
        }

        public TableRow getNotesRow() {
            return _notesRow;
        }

        public ImageView getTimeImageView() {
            return mTimeImageView;
        }

        public TableRow getCorrectiveAmountRow() {
            return _correctiveAmountRow;
        }

        public TableRow getBaselineAmountRow() {
            return _baselineAmountRow;
        }
    }
}
