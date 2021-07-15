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

public abstract class BlankoV3Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private Context mContext;
    private Logger log = new Logger();
    List<UnitsBlanko> unitsDataset;
    TextView master;

    RecyclerView.ViewHolder lastHolder;

    EventListener listener;

    public interface EventListener {
        void onEvent(int data);
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public BlankoV3Adapter(Context ctx, List<UnitsBlanko> uDataset, EventListener listener) {
        this.mContext = ctx;
        this.listener = listener;
        this.unitsDataset = uDataset;
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
        UnitsBlanko unitDataItem;

        if (position <= unitsDataset.size()) {
            unitDataItem = unitsDataset.get(position);

            master.setText(unitDataItem.getName());
            holder.itemView.setOnClickListener(view1 -> {
                if (lastHolder != null)
                    lastHolder.itemView.setBackgroundColor(Color.TRANSPARENT);

                lastHolder = holder;
                holder.itemView.setBackgroundColor(mContext.getResources().getColor(R.color.colorPrimaryTransparant));
                onBlankoClick(unitDataItem);
            });
            holder.itemView.setOnLongClickListener(view -> {
                onItemLongClick(1, String.valueOf(unitDataItem.getId()), unitDataItem.getName());
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
        return unitsDataset.size();
    }

    protected abstract void onAddBlankoUCClick();
    protected abstract void onBlankoClick(UnitsBlanko model);
    protected abstract void onItemLongClick(int mode, String... params);
}