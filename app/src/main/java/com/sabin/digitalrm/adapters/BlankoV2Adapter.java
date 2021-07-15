package com.sabin.digitalrm.adapters;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.sabin.digitalrm.R;
import com.sabin.digitalrm.models.UnitsBlanko;
import com.sabin.digitalrm.models.VersionsBlanko;
import com.sabin.digitalrm.utils.Logger;

import java.util.List;

public abstract class BlankoV2Adapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    private static final int TYPE_HEADER_UNITS = 0;
    private static final int TYPE_HEADER_VERSIONS = 1;
    private static final int TYPE_ITEMS = 2;
    private Context mContext;
    private Logger log = new Logger();
    List<UnitsBlanko> unitsDataset;
    List<VersionsBlanko> versionsDataset;
//    List<List> data;
    TextView title1, title2, master;

    RecyclerView.ViewHolder lastHolder;

    int i = 0;
    int headerUnitPos = 0;
    int headerVersionPos = 0;
    EventListener listener;

    public interface EventListener {
        void onEvent(int data);
    }

    public UnitsBlanko getItemU (int position) {
        return unitsDataset.get(position);
    }

    public VersionsBlanko getItemV (int position) {
        return versionsDataset.get(position);
    }

    public BlankoV2Adapter(Context ctx, List<UnitsBlanko> uDataset, List<VersionsBlanko> vDataset, EventListener listener) {
        this.mContext = ctx;
        this.listener = listener;
        this.unitsDataset = uDataset;
        this.versionsDataset = vDataset;

//        this.data = new ArrayList<>();
//        this.data.add(0, uDataset);
//        this.data.add(1, vDataset);
    }

    class VHItems extends RecyclerView.ViewHolder {
        public VHItems(View itemView) {
            super(itemView);

            master = itemView.findViewById(R.id.txtList);
        }
    }

    class VHHeaderUnits extends RecyclerView.ViewHolder {
        public VHHeaderUnits(View itemView) {
            super(itemView);

            title1 = itemView.findViewById(R.id.txtHeader);
        }
    }

    class VHHeaderVersions extends RecyclerView.ViewHolder {
        public VHHeaderVersions(View itemView) {
            super(itemView);

            title2 = itemView.findViewById(R.id.txtHeader);
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_ITEMS) {
            //inflate your layout and pass it to view holder
            return new VHItems(inflater.inflate(R.layout.item_list_master_blanko, parent, false));
        } else if (viewType == TYPE_HEADER_UNITS) {
            //inflate your layout and pass it to view holder
            return new VHHeaderUnits(inflater.inflate(R.layout.item_header_master_blanko, parent, false));
        } else if (viewType == TYPE_HEADER_VERSIONS) {
            //inflate your layout and pass it to view holder
            return new VHHeaderVersions(inflater.inflate(R.layout.item_header_master_blanko, parent, false));
        }

        throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        UnitsBlanko unitDataItem;
        VersionsBlanko versionDataItem;

        if (holder instanceof VHItems) {
            if (position <= unitsDataset.size()){
                unitDataItem = unitsDataset.get(position-1);

                master.setText(unitDataItem.getName());
                holder.itemView.setOnClickListener(view1 -> {
                    if(lastHolder != null)
                        lastHolder.itemView.setBackgroundColor(Color.TRANSPARENT);

                    lastHolder = holder;
                    holder.itemView.setBackgroundColor(mContext.getResources().getColor(R.color.colorPrimaryTransparant));
                    onBlankoClick(unitDataItem);
                });
                holder.itemView.setOnLongClickListener(view -> {
                    onItemLongClick(1, String.valueOf(unitDataItem.getId()), unitDataItem.getName());
                    return true;
                });

                log.x("[ On Create View holder ] == "+unitDataItem.getName());

                //BUG: i++ will hit index out of range when on last item of array!
            }else{
                if (i < versionsDataset.size()){
                    versionDataItem = versionsDataset.get(i);
                    i++;

                    master.setText(versionDataItem.getName());
                    holder.itemView.setOnClickListener(view1 -> {
                        if(lastHolder != null)
                            lastHolder.itemView.setBackgroundColor(Color.TRANSPARENT);

                        lastHolder = holder;
                        holder.itemView.setBackgroundColor(mContext.getResources().getColor(R.color.colorPrimaryTransparant));

                        onDmkVersionClick(versionDataItem);
                    });
                    holder.itemView.setOnLongClickListener(view -> {
                        onItemLongClick(2, String.valueOf(versionDataItem.getId()), versionDataItem.getVersion(), versionDataItem.getName());
                        return true;
                    });

                    log.x("[ On Create View holder ] == "+versionDataItem.getName());
                }
            }

            //cast holder to VHItem and set data
        } else if (holder instanceof VHHeaderUnits) {
            //cast holder to VHHeader and set data for header.
            title1.setText("KATEGORI BLANKO");
            holder.itemView.setOnClickListener(view1 -> onAddBlankoUCClick());
        } else if (holder instanceof VHHeaderVersions) {
            //cast holder to VHHeader and set data for header.
            headerVersionPos = position;
            title2.setText("VERSI DMK");
            holder.itemView.setOnClickListener(view1 -> onAddDMKVClick());
        }
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        RecyclerView.LayoutManager manager = recyclerView.getLayoutManager();
        if(manager instanceof LinearLayoutManager && getItemCount() > 0) {
            LinearLayoutManager llm = (LinearLayoutManager) manager;
            recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                    super.onScrollStateChanged(recyclerView, newState);
                }

                @Override
                public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    int visiblePosition = llm.findFirstCompletelyVisibleItemPosition();
                    if(visiblePosition > -1) {
                        View v = llm.findViewByPosition(visiblePosition);
                        //do something
                        v.setBackgroundColor(Color.parseColor("#777777"));
                    }
                }
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
        return unitsDataset.size() + versionsDataset.size() + 2;
    }

    @Override
    public int getItemViewType(int position) {
        if (isPositionHeaderUnit(position))
            return TYPE_HEADER_UNITS;
        else if (isPositionHeaderVersion(position))
            return TYPE_HEADER_VERSIONS;

        return TYPE_ITEMS;
    }

    private boolean isPositionHeaderUnit(int position) {
        return position == 0;
    }

    private boolean isPositionHeaderVersion(int position) {
        return position == unitsDataset.size()+1;
    }

    protected abstract void onAddBlankoUCClick();
    protected abstract void onAddDMKVClick();
    protected abstract void onBlankoClick(UnitsBlanko model);
    protected abstract void onDmkVersionClick(VersionsBlanko model);
    protected abstract void onItemLongClick(int mode, String... params);
}