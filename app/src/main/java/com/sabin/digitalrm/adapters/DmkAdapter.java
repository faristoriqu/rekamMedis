package com.sabin.digitalrm.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sabin.digitalrm.R;
import com.sabin.digitalrm.models.DMK;

import java.util.List;

public abstract class DmkAdapter extends RecyclerView.Adapter<DmkAdapter.DmkHolder> {
        List<DMK> mDataset;

public DmkAdapter(List<DMK> dataset) {
        mDataset = dataset;
}

public class DmkHolder extends RecyclerView.ViewHolder{
    private TextView tvDmkCode, tvDmkName, tvDmkPages, tvDmkStatus;

    public DmkHolder(View itemView) {
        super(itemView);

        tvDmkCode = itemView.findViewById(R.id.code_dmk);
        tvDmkName = itemView.findViewById(R.id.dmk_name);
        tvDmkPages = itemView.findViewById(R.id.dmk_total_page);
        tvDmkStatus = itemView.findViewById(R.id.dmk_status);
    }

    public void bindInfo(DMK dmk){
        tvDmkCode.setText(dmk.getCode());
        tvDmkName.setText(dmk.getName());
        tvDmkPages.setText(dmk.getTotalPage().toString() + " Halaman");
        String status = "";
        switch (dmk.getStatus()){
            case DMK.STATUS_DMK_NEW:{
                status = "New";
                break;
            }

            case DMK.STATUS_DMK_GENERATED:{
                status = "Generated";
                break;
            }

            case DMK.STATUS_DMK_COORD:{
                status = "Coordinated";
                break;
            }

            case DMK.STATUS_DMK_ACTIVE:{
                status = "Activated";
                break;
            }
        }

        tvDmkStatus.setText(status);
    }
}

    @NonNull
    @Override
    public DmkHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        //TODO: MOVE TO PARENT, NOT HERE
        return new DmkHolder (inflater.inflate(R.layout.cardview_dmk, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull DmkHolder holder, int position) {
        onBindViewHolder(holder, position, mDataset.get(position));
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    protected abstract void onBindViewHolder(@NonNull DmkHolder hodler, int position, @NonNull DMK model);
}