package com.chriscooke.withinawalk;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;

/**
 * Created by Chris_Home on 23/11/15.
 */
public class AutoCompleteAdapter extends ArrayAdapter<AutoPlace> {

    private GoogleApiClient client;

    public AutoCompleteAdapter(Context context) {
        super(context, 0);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            holder = new ViewHolder();
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            holder.text = (TextView) convertView.findViewById(android.R.id.text1);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        holder.text.setText(getItem(position).getDescription());

        return convertView;
    }

    public void setGoogleApiClient(GoogleApiClient googleApiClient) {
        this.client = googleApiClient;
    }

    private class ViewHolder {
        TextView text;
    }
}




