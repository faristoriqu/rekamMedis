package com.sabin.digitalrm.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.sabin.digitalrm.R;
import com.sabin.digitalrm.models.Bookmark;
import com.sabin.digitalrm.models.DMKBookmark;

import java.util.List;

public abstract class BookmarkAdapter extends RecyclerView.Adapter<BookmarkAdapter.BookmarkHolder> {
    private List<DMKBookmark> mDatasets;


    public class BookmarkHolder extends RecyclerView.ViewHolder{
        TextView title, desc, timestamp, pageNo;

        private BookmarkHolder(View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.page_title);
            desc = itemView.findViewById(R.id.page_desc);
            timestamp = itemView.findViewById(R.id.page_timestapmp);
            pageNo = itemView.findViewById(R.id.page_no);
        }

        public void bindInfo(DMKBookmark bookmark){
            title.setText(bookmark.getName());
            desc.setText("DMK "+bookmark.getDmk());
            timestamp.setText("Total halaman: "+bookmark.getTotalPages());
            pageNo.setText(String.valueOf(bookmark.getPage()));
        }
    }

    public BookmarkAdapter(List<DMKBookmark> _mDataset){
        mDatasets = _mDataset;
    }

    /**
     * Returns the total number of items in the data set held by the adapter.
     *
     * @return The total number of items in this adapter.
     */
    @Override
    public int getItemCount() {
        return mDatasets.size();
    }


    @NonNull
    @Override
    public BookmarkHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        return new BookmarkHolder(inflater.inflate(R.layout.cardview_bookmark, parent, false));
    }


    @Override
    public void onBindViewHolder(@NonNull BookmarkHolder holder, int position) {
        onBindViewHolder(holder, position, mDatasets.get(mDatasets.size() - position - 1));
    }

    protected abstract void onBindViewHolder(@NonNull BookmarkHolder holder, int position, @NonNull DMKBookmark model);
}
