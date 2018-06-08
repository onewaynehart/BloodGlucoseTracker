package com.yourharts.www.bloodglucosetracker.Adapters;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.yourharts.www.bloodglucosetracker.Database.DBHelper;
import com.yourharts.www.bloodglucosetracker.Models.DataModelInterface;
import com.yourharts.www.bloodglucosetracker.R;

import java.util.List;

public class GenericSpinnerAdapter  extends BaseAdapter {
    private final Context mContext;
    private LayoutInflater layoutInflater;
    private List<DataModelInterface> mDataset;
    private DBHelper _dbHelper;
    private Activity _activity;
    public GenericSpinnerAdapter(Context context, List<DataModelInterface> datatset, Activity activity){
        mDataset = datatset;
        _activity = activity;
        _dbHelper = new DBHelper(context, context.getFilesDir().getPath(), _activity );
        this.mContext = context;
        layoutInflater =(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }



    @Override
    public int getCount() {
        return mDataset.size();
    }

    @Override
    public Object getItem(int position) {
        return mDataset.get(position);
    }

    @Override
    public long getItemId(int position) {
        return mDataset.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder spinnerHolder;
        if(convertView == null){
            spinnerHolder = new ViewHolder();
            convertView = layoutInflater.inflate(R.layout.layout_generic_spinner_list, parent, false);
            spinnerHolder.spinnerItemList = convertView.findViewById(R.id.spinner_list_item);
            convertView.setTag(spinnerHolder);
        }else{
            spinnerHolder = (ViewHolder)convertView.getTag();
        }
        spinnerHolder.spinnerItemList.setText(mDataset.get(position).getString());
        return convertView;
    }
    public int getPosition(String itemString){
        return _dbHelper.getPosition(mDataset, itemString);
    }
    public int getPosition(int itemID){
        return _dbHelper.getPosition(mDataset, itemID);
    }
    public List<DataModelInterface> getDataset() {
        return mDataset;
    }

    class ViewHolder{
        TextView spinnerItemList;
    }
}
