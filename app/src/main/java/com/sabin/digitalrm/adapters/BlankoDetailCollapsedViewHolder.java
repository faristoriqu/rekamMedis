package com.sabin.digitalrm.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.LayoutRes;

import com.google.android.material.snackbar.Snackbar;
import com.sabin.digitalrm.R;
import com.sabin.digitalrm.fragments.prm.blanko.BlankoDetailFragment;
import com.sabin.digitalrm.models.DetailBlanko;
import com.sysdata.widget.accordion.CollapsedViewHolder;
import com.sysdata.widget.accordion.ExpandableItemHolder;
import com.sysdata.widget.accordion.ItemAdapter;

public final class BlankoDetailCollapsedViewHolder extends CollapsedViewHolder {
    private ImageView btnEdit;
    private ImageView btnDel;
    private TextView mTitleTextView;

    private Activity mContext;

    private BlankoDetailCollapsedViewHolder(Context ctx, View itemView) {
        super(itemView);

        mTitleTextView = itemView.findViewById(R.id.collapsed_title);
        btnEdit = itemView.findViewById(R.id.btnEditBlanko);
        btnDel = itemView.findViewById(R.id.btnDeleteBlanko);

        this.mContext = (Activity) ctx;
    }

    @Override
    protected void onBindItemView(ExpandableItemHolder itemHolder) {
        Log.e("[X-DEBUG]", "onBindItemView-BlankoDetilCollapsedViewHolder name:"+((DetailBlanko) itemHolder.item).getLName());
        mTitleTextView.setText(((DetailBlanko) itemHolder.item).getLName());

        btnEdit.setOnClickListener(v -> {
            BlankoDetailFragment.editBlankoDialog(this.mContext, ((DetailBlanko) itemHolder.item).getIdBl(), ((DetailBlanko) itemHolder.item).getLName());
        });

        btnDel.setOnClickListener(v -> {
            BlankoDetailFragment.deleteBlankoDialog(this.mContext, ((DetailBlanko) itemHolder.item).getIdBl(), ((DetailBlanko) itemHolder.item).getLName());
        });
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
        public static Factory create(Context ctx, @LayoutRes int itemViewLayoutId) {
            return new Factory(ctx, itemViewLayoutId);
        }

        @LayoutRes
        private final int mItemViewLayoutId;
        private Context context;

        Factory(Context ctx, @LayoutRes int itemViewLayoutId) {
            mItemViewLayoutId = itemViewLayoutId;
            context = ctx;
        }

        @Override
        public ItemAdapter.ItemViewHolder<?> createViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false /* attachToRoot */);

            return new BlankoDetailCollapsedViewHolder(context, itemView);
        }

        @Override
        public int getItemViewLayoutId() {
            return mItemViewLayoutId;
        }
    }
}
