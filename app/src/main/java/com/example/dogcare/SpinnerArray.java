package com.example.dogcare;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

public class SpinnerArray extends ArrayAdapter<String> {
    private final Context context;

    public SpinnerArray(Context context, String[] items) {
        super(context, android.R.layout.simple_spinner_item, items);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        View view = super.getView(position, convertView, parent);
        TextView textView = (TextView) view;
        textView.setTextColor(context.getResources().getColor(R.color.input_text_color));
        return view;
    }

    @Override
    public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.spinner_item, parent, false);
        }
        TextView textView = (TextView) convertView.findViewById(R.id.text_view);
        textView.setText(getItem(position));
        return convertView;
    }
}