package com.sabin.digitalrm.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.LayoutRes;

import com.sabin.digitalrm.R;
import com.sabin.digitalrm.models.DetailBlanko;
import com.sysdata.widget.accordion.CollapsedViewHolder;
import com.sysdata.widget.accordion.ExpandableItemHolder;
import com.sysdata.widget.accordion.ExpandedViewHolder;
import com.sysdata.widget.accordion.ItemAdapter;

public abstract class BlankoDetailViewHolder extends CollapsedViewHolder {

    private TextView mTitleTextView;

    private BlankoDetailViewHolder(View itemView) {
        super(itemView);

        mTitleTextView = itemView.findViewById(R.id.collapsed_title);
    }

    @Override
    protected void onBindItemView(ExpandableItemHolder itemHolder) {
        mTitleTextView.setText(((DetailBlanko) itemHolder.item).getName());
    }

    @Override
    protected void onRecycleItemView() {
        // do nothing
    }

    @Override
    protected ItemAdapter.ItemViewHolder.Factory getViewHolderFactory() {
        return null;
    }

    public static class Factory implements ItemAdapter.ItemViewHolder.Factory {

        public static Factory create(@LayoutRes int itemViewLayoutId) {
            return new Factory(itemViewLayoutId);
        }

        @LayoutRes
        private final int mItemViewLayoutId;

        Factory(@LayoutRes int itemViewLayoutId) {
            mItemViewLayoutId = itemViewLayoutId;
        }

        @Override
        public ItemAdapter.ItemViewHolder<?> createViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false /* attachToRoot */);
            return new BlankoDetailViewHolder(itemView) {
                @Override
                protected void onBindItemView(ExpandableItemHolder expandableItemHolder) {

                }
            };
        }

        @Override
        public int getItemViewLayoutId() {
            return mItemViewLayoutId;
        }
    }

}
