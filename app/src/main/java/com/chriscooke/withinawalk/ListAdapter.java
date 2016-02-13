package com.chriscooke.withinawalk;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by Chris_Home on 02/12/15.
 */
public class ListAdapter extends ArrayAdapter<loco> {

    public ListAdapter(Context context, int textViewResourceId) {
        super(context, textViewResourceId);
    }

    public ListAdapter(Context context, int resource, List<loco> locos) {
        super(context, resource, locos);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View v = convertView;

        if (v == null) {
            LayoutInflater vi;
            vi = LayoutInflater.from(getContext());
            v = vi.inflate(com.chriscooke.withinawalk.R.layout.locolist, null);
        }

        loco
                p = getItem(position);

        if (p != null) {
            TextView tt2 = (TextView) v.findViewById(com.chriscooke.withinawalk.R.id.Name);
            TextView tt3 = (TextView) v.findViewById(com.chriscooke.withinawalk.R.id.Address);


            if (tt2 != null) {
                tt2.setText(p.getName());
            }

            if (tt3 != null) {
                tt3.setText(p.getAddress());
            }
        }
        return v;
    }
}