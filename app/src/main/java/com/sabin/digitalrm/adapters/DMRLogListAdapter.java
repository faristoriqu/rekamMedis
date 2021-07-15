package com.sabin.digitalrm.adapters;

import android.app.Activity;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.sabin.digitalrm.R;
import com.sabin.digitalrm.models.DMRLog;
import com.sabin.digitalrm.utils.TimeFormat;

import java.util.List;


public class DMRLogListAdapter extends ArrayAdapter<DMRLog> {
    private List<DMRLog> mDataset;
    private Context mContext;
    int resource;

    public DMRLogListAdapter(@NonNull Context context, int resource, @NonNull List<DMRLog> objects) {
        super(context, resource, objects);
        mDataset = objects;
        mContext = context;
        this.resource = resource;
        Log.d("X-DEBUG", "BRMListAdapter: ");
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        //we need to get the view of the xml for our list item
        //And for this we need a layoutinflater
        if (convertView == null){
            LayoutInflater layoutInflater = (LayoutInflater) getContext()
                    .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
            convertView = layoutInflater.inflate(R.layout.listview_dmr_log, null, true);

        }

        Log.d("X-DEBUG", "getView: ");

        TextView action = convertView.findViewById(R.id.action);
        TextView user = convertView.findViewById(R.id.user);
        TextView date = convertView.findViewById(R.id.date);
        TextView desc = convertView.findViewById(R.id.desc);
        TextView index = convertView.findViewById(R.id.index);

        //getting the hero of the specified position
        DMRLog mLog = mDataset.get(position);

        //adding values to the list item
        action.setText(mLog.getAction());
        user.setText(mLog.getPersonName());
        date.setText(TimeFormat.dateTimeFormat(mLog.getDateTime()));
        if(!mLog.getDesc().isEmpty())
            desc.setText(mLog.getDesc());
        else
            desc.setText("Tidak ada deskripsi");

        index.setText(String.valueOf(position + 1));

        return convertView;
    }
}
