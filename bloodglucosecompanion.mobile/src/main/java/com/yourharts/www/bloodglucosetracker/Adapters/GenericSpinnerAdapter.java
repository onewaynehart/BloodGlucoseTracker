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
    private LayoutInflater _layoutInflater;
    private List<DataModelInterface> _dataset;
    private DBHelper _dbHelper;
    private Activity _activity;
    public GenericSpinnerAdapter(Context context, List<DataModelInterface> datatset, Activity activity){
        _dataset = datatset;
        _activity = activity;
        _dbHelper = new DBHelper(context, context.getFilesDir().getPath(), _activity );
        this.mContext = context;
        _layoutInflater =(LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }



    @Override
    public int getCount() {
        return _dataset.size();
    }

    @Override
    public Object getItem(int position) {
        return _dataset.get(position);
    }

    @Override
    public long getItemId(int position) {
        return _dataset.get(position).getId();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder spinnerHolder;
        if(convertView == null){
            spinnerHolder = new ViewHolder();
            convertView = _layoutInflater.inflate(R.layout.layout_generic_spinner_list, parent, false);
            spinnerHolder.spinnerItemList = convertView.findViewById(R.id.spinner_list_item);
            convertView.setTag(spinnerHolder);
        }else{
            spinnerHolder = (ViewHolder)convertView.getTag();
        }
        spinnerHolder.spinnerItemList.setText(_dataset.get(position).getString());
        return convertView;
    }
    public int getPosition(String itemString){
        return _dbHelper.getPosition(_dataset, itemString);
    }
    public int getPosition(int itemID){
        return _dbHelper.getPosition(_dataset, itemID);
    }
    public List<DataModelInterface> getDataset() {
        return _dataset;
    }

    class ViewHolder{
        TextView spinnerItemList;
    }
}
