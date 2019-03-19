package com.insakay.conductor;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class CustomListViewAdapter extends ArrayAdapter {

    private Activity activity;
    private String[] name, coverage;
    private Integer[] count;

    public CustomListViewAdapter(Activity activity, String[] landmarkName, String[] landmarkCoverage, Integer[] passCount) {
        super(activity, R.layout.listview_row, landmarkName);

        this.activity = activity;
        this.name = landmarkName;
        this.coverage = landmarkCoverage;
        this.count = passCount;

    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        LayoutInflater inflater = activity.getLayoutInflater();
        View rowView = inflater.inflate(R.layout.listview_row, null, true);

        TextView nameView = (TextView) rowView.findViewById(R.id.landmarkName);
        TextView coverageView = (TextView) rowView.findViewById(R.id.landmarkCoverage);
        TextView passengerCountView = (TextView) rowView.findViewById(R.id.passenger_count);

        nameView.setText(name[position]);
        coverageView.setText(coverage[position]);
        passengerCountView.setText(Integer.toString(count[position]));
        return rowView;
    }
}
