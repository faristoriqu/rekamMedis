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
import android.widget.CheckBox;
import android.widget.TextView;

import com.sabin.digitalrm.R;
import com.sabin.digitalrm.models.DMKBlanko;
import com.sabin.digitalrm.models.DetailBlanko;

import java.util.List;


public abstract class DmkBlankoVisitListAdapter extends ArrayAdapter<DMKBlanko> {
    private List<DMKBlanko> mDataset;
    private Context mContext;
    int resource;

    public DmkBlankoVisitListAdapter(@NonNull Context context, int resource, @NonNull List<DMKBlanko> objects) {
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
            convertView = layoutInflater.inflate(R.layout.listview_dmk_blanko_visit, null, true);

        }

        Log.d("X-DEBUG", "getView: ");

        TextView title = convertView.findViewById(R.id.dmk_title);
        TextView version = convertView.findViewById(R.id.dmk_version);
        TextView code = convertView.findViewById(R.id.dmk_code);
        CheckBox checkBox = convertView.findViewById(R.id.dmk_checked);


        //getting the hero of the specified position
        DMKBlanko dmk = mDataset.get(position);

        //adding values to the list item
        checkBox.setOnCheckedChangeListener((compoundButton, b) -> onDmkCheck(b, dmk));
        title.setText(dmk.getName());
        version.setText("Versi DMK: " + dmk.getVersionName());
        code.setText(dmk.getCode());

        return convertView;
    }

    public abstract void onDmkCheck(boolean checked, DMKBlanko dmk);
}
