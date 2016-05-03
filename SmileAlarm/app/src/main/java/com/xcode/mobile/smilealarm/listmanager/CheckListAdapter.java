package com.xcode.mobile.smilealarm.listmanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

import com.xcode.mobile.smilealarm.R;

/**
 * Created by an.nguyen on 11/17/2015.
 */
public class CheckListAdapter extends ArrayAdapter<String> {
    private Context ctx;
    private ArrayList<String> items;
    private int checkedCount = 0;

    public CheckListAdapter(Context context, int textViewResourceId, ArrayList<String> objects) {
        super(context, textViewResourceId, objects);
        ctx = context;
        items = new ArrayList<>();
        items.addAll(objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;

        if (convertView == null) {
            LayoutInflater vi = LayoutInflater.from(ctx);
            convertView = vi.inflate(R.layout.check_list, null);

            holder = new ViewHolder();
            holder.advice = (TextView) convertView.findViewById(R.id.advice_text);
            holder.checkbox = (CheckBox) convertView.findViewById(R.id.checkbox);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        String advice = items.get(position);
        holder.advice.setText(advice);
        holder.checkbox.setTag(advice);
        holder.checkbox.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                CheckBox cb = (CheckBox) v;
                if (cb.isChecked()) {
                    checkedCount++;
                } else {
                    checkedCount--;
                }
                if (checkedCount == items.size()) {
                    ((OnAllItemsCheckedListener) ctx).onAllItemsChecked();
                }
            }
        });

        return convertView;
    }

    public interface OnAllItemsCheckedListener {
        void onAllItemsChecked();
    }

    public class ViewHolder {
        TextView advice;
        CheckBox checkbox;
    }
}
