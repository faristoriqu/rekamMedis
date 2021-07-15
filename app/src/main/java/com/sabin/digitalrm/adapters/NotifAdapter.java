package com.sabin.digitalrm.adapters;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.sabin.digitalrm.R;
import com.sabin.digitalrm.models.InfoBRMV2;
import com.sabin.digitalrm.utils.TimeFormat;
import com.sabin.digitalrm.models.Notif;

import java.util.List;

public abstract class NotifAdapter extends RecyclerView.Adapter<NotifAdapter.NotifHolder> {
    List<InfoBRMV2> mDataset;

    public NotifAdapter(List<InfoBRMV2> dataset){
        mDataset = dataset;
    }

    public class NotifHolder extends RecyclerView.ViewHolder{
        ImageView icon;
        TextView title, body, time, count;

        public NotifHolder(View itemView) {
            super(itemView);

            icon = itemView.findViewById(R.id.notif_icon);
            title = itemView.findViewById(R.id.notif_title);
            body = itemView.findViewById(R.id.notif_body);
            time = itemView.findViewById(R.id.notif_time);
            count = itemView.findViewById(R.id.notif_count);
        }

        public void bindInfo(Context ctx, InfoBRMV2 notif){
            boolean isRead = false;
            final String defaultTitle = "BRM No."+ notif.getNo_brm() +" yang telah anda tangani, ditolak oleh petugas";


            title.setText(defaultTitle);
            body.setText("Sentuh untuk melihat detail");
            time.setText(TimeFormat.timeAgoFormat(notif.getRejected_at()));
            count.setText(String.valueOf(notif.getRcount()));

//            if(notif.getRead().equals("t")){
//                isRead = true;
//            }
//
//            if(isRead){
//                icon.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_bell_dark));
//            }else{
//                icon.setImageDrawable(ctx.getResources().getDrawable(R.drawable.ic_bell_primary));
//            }
        }
    }

    @NonNull
    @Override
    public NotifHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        return new NotifHolder(inflater.inflate(R.layout.cardview_notif, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull NotifHolder holder, int position) {
        onBindViewHolder(holder, position, mDataset.get(position));
    }

    @Override
    public int getItemCount() {
        return mDataset.size();
    }

    protected abstract void onBindViewHolder(@NonNull NotifAdapter.NotifHolder holder, int pos, @NonNull InfoBRMV2 model);
}
