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
import com.sabin.digitalrm.models.DMKBerkas;
import com.sabin.digitalrm.models.DMKBlanko;

import java.util.List;


public abstract class DmkBerkasExportListAdapter extends ArrayAdapter<DMKBerkas> {
    private List<DMKBerkas> mDataset;
    private Context mContext;
    int resource;

    public DmkBerkasExportListAdapter(@NonNull Context context, int resource, @NonNull List<DMKBerkas> objects) {
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
        checkBox.setChecked(false);


        //getting the hero of the specified position
        DMKBerkas dmk = mDataset.get(position);

        //adding values to the list item
        checkBox.setOnCheckedChangeListener((compoundButton, b) -> onDmkCheck(b, dmk));
        title.setText(dmk.getName());
        version.setText("Halaman: " + dmk.getPage());
        code.setText(dmk.getCode());

        return convertView;
    }

    public abstract void onDmkCheck(boolean checked, DMKBerkas dmk);
}
