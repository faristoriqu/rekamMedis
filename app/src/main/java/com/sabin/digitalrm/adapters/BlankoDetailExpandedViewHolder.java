package com.sabin.digitalrm.adapters;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.media.Image;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import com.google.android.material.floatingactionbutton.FloatingActionButton;import com.google.android.material.snackbar.Snackbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.sabin.digitalrm.R;
import com.sabin.digitalrm.fragments.prm.blanko.BlankoDetailFragment;
import com.sabin.digitalrm.models.DMKBlanko;
import com.sabin.digitalrm.models.DetailBlanko;
import com.sabin.digitalrm.models.VersionsBlanko;
import com.sysdata.widget.accordion.ExpandableItemHolder;
import com.sysdata.widget.accordion.ExpandedViewHolder;
import com.sysdata.widget.accordion.ItemAdapter;

import java.util.ArrayList;
import java.util.List;

public final class BlankoDetailExpandedViewHolder extends ExpandedViewHolder {
    private ImageView btnEdit;
    private ImageView btnDel;
    private ImageView btnAdd;
    private TextView mTitleTextView;
    private ListView expandedLst;
    private Activity mContext;
    private List<VersionsBlanko> vLst;

    private BlankoDetailExpandedViewHolder(Context ctx, View itemView, List<VersionsBlanko> lst) {
        super(itemView);

        mTitleTextView = itemView.findViewById(R.id.expanded_title);
        expandedLst = itemView.findViewById(R.id.expanded_list);
        btnAdd = itemView.findViewById(R.id.btnAddDmkInBlanko);
        btnEdit = itemView.findViewById(R.id.btnEditBlanko);
        btnDel = itemView.findViewById(R.id.btnDeleteBlanko);

        this.mContext = (Activity) ctx;
        this.vLst = lst;
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onBindItemView(ExpandableItemHolder itemHolder) {
        int idBl = ((DetailBlanko) itemHolder.item).getIdBl();
        String name = ((DetailBlanko) itemHolder.item).getLName();
        List<DMKBlanko> lstDMKs = ((DetailBlanko) itemHolder.item).getLDMK();
        ArrayList<String> dmkName = new ArrayList<>();
        ArrayList<Integer> dmkIds = new ArrayList<>();

        mTitleTextView.setText(name);

        for(int i = 0; i < lstDMKs.size(); i++){
            dmkName.add(i, "[DMK "+lstDMKs.get(i).getCode()+"] "+lstDMKs.get(i).getName());
            dmkIds.add(i, lstDMKs.get(i).getId());
        }

        ArrayAdapter<String> spAdapter = new DMKListAdapter(mContext, dmkName, dmkIds, idBl);
        //Insert Adapter into List
        expandedLst.setAdapter(spAdapter);

        //set click functionality for each list item
        // Setting on Touch Listener for handling the touch inside ScrollView
        expandedLst.setOnTouchListener((v, event) -> {
            v.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });

        expandedLst.setOnItemClickListener((parent, view, position, id) -> Log.i("User clicked ", dmkName.get(position)));

        btnAdd.setOnClickListener(v -> {
            BlankoDetailFragment.dlgAddDMK(mContext, v, vLst, idBl);
        });

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

        public static BlankoDetailExpandedViewHolder.Factory create(Context ctx, @LayoutRes int itemViewLayoutId, List<VersionsBlanko> lst) {
            return new Factory(ctx, itemViewLayoutId, lst);
        }

        @LayoutRes
        private final int mItemViewLayoutId;
        private Context context;
        private List<VersionsBlanko> vlst;

        Factory(Context ctx, @LayoutRes int itemViewLayoutId, List<VersionsBlanko> lst) {
            mItemViewLayoutId = itemViewLayoutId;
            context = ctx;
            vlst = lst;
        }

        @Override
        public ItemAdapter.ItemViewHolder<?> createViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(viewType, parent, false /* attachToRoot */);
            return new BlankoDetailExpandedViewHolder(context, itemView, vlst);
        }

        @Override
        public int getItemViewLayoutId() {
            return mItemViewLayoutId;
        }
    }

    private static class DMKListAdapter extends ArrayAdapter<String> {

        private List<String> iMod;
        private List<Integer> ids;
        private int blankoID;
        private Context ctx;

        public DMKListAdapter(@NonNull Context context, @NonNull List<String> objects, List<Integer> ids, int idBl) {
            super(context, R.layout.dmk_itemlist_layout, objects);

            this.ctx = context;
            this.iMod = objects;
            this.ids = ids;
            this.blankoID = idBl;
        }

        private class VwHolder{
            TextView txtName;
            RelativeLayout btnRemove;
        }

        @NonNull
        @Override
        public View getView(int position, View convertView, ViewGroup parent){
            String name = iMod.get(position);
            VwHolder vh;
            final View reslt;

            if(convertView==null){
                vh = new VwHolder();
                LayoutInflater inf = LayoutInflater.from(getContext());
                convertView = inf.inflate(R.layout.dmk_itemlist_layout, parent, false);
                vh.txtName = convertView.findViewById(R.id.txtItem);
                vh.btnRemove = convertView.findViewById(R.id.btnRemove);

                vh.btnRemove.setOnClickListener(view -> {
                    TextView txtTitle = parent.getRootView().findViewById(R.id.expanded_title);
                    BlankoDetailFragment.deleteConfirm(ctx, ids.get(position), blankoID, name, txtTitle.getText().toString());

                    Log.e("[X-DEBUG]", "idDMK:"+ids.get(position)+" blName:"+txtTitle.getText().toString());
                });

                reslt = convertView;

                reslt.setTag(vh);
            }else{
                vh = (VwHolder) convertView.getTag();
                reslt = convertView;
            }

            vh.txtName.setText(name);

            return reslt;
        }
    }
}
