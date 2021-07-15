package com.sabin.digitalrm.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.sabin.digitalrm.R;
import com.sabin.digitalrm.models.UnitsBlanko;
import com.sabin.digitalrm.models.VersionsBlanko;
import com.sabin.digitalrm.utils.Logger;

import java.util.List;

public abstract class DMKV3Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private Logger log = new Logger();
    List<VersionsBlanko> versionsDataset;
    TextView master;

    RecyclerView.ViewHolder lastHolder;

    int i = 0;
    int headerUnitPos = 0;
    int headerVersionPos = 0;
    EventListener listener;

    public interface EventListener {
        void onEvent(int data);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public DMKV3Adapter(Context ctx, List<VersionsBlanko> vDataset, EventListener listener) {
        this.mContext = ctx;
        this.listener = listener;
        this.versionsDataset = vDataset;
    }

    class VHItems extends RecyclerView.ViewHolder {
        public VHItems(View itemView) {
            super(itemView);

            master = itemView.findViewById(R.id.txtList);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        return new VHItems(inflater.inflate(R.layout.item_list_master_blanko, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        VersionsBlanko versionDataItem;
            if (position <= versionsDataset.size()) {
                versionDataItem = versionsDataset.get(position);

                master.setText(versionDataItem.getName());
                holder.itemView.setOnClickListener(view1 -> {
                    if (lastHolder != null)
                        lastHolder.itemView.setBackgroundColor(Color.TRANSPARENT);

                    lastHolder = holder;
                    holder.itemView.setBackgroundColor(mContext.getResources().getColor(R.color.colorPrimaryTransparant));
                    onDmkVersionClick(versionDataItem);
                });
                holder.itemView.setOnLongClickListener(view -> {
                    onItemLongClick(1, String.valueOf(versionDataItem.getId()), versionDataItem.getVersion(), versionDataItem.getName());
                    return true;
                });
            }
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return versionsDataset.size();
    }

    protected abstract void onAddDMKVClick();
    protected abstract void onDmkVersionClick(VersionsBlanko model);
    protected abstract void onItemLongClick(int mode, String... params);
}