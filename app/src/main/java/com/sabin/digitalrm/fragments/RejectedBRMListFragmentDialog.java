package com.sabin.digitalrm.fragments;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.sabin.digitalrm.DoctorMainActivity;
import com.sabin.digitalrm.R;
import com.sabin.digitalrm.interfaces.APIService;
import com.sabin.digitalrm.models.DetailVisitor;
import com.sabin.digitalrm.models.RejectedNote;
import com.sabin.digitalrm.utils.APIUtils;
import com.sabin.digitalrm.utils.ApiError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import retrofit2.Call;
import retrofit2.Response;

public class RejectedBRMListFragmentDialog extends DialogFragment {
    public static final String INTENT_EXTRA_KEY = "intent_extra_key";
    private static Context mContext;
    private APIService apiService;
    private TextView txtTitle;
    private Button btnPos, btnNeg;
    private ProgressBar pgLoading;
    private ListView lvRnotes;
    private LinearLayout rNotesContainer;
    ArrayList<RejectedNote> dataModels;
    private ListAdapter adapter;
    private static String uid, id_berkas;
    private List<RejectedNote> rNotes;
    private HashMap<Integer, Integer> resultData;
    private List<Integer> lstIdNotes, lstVal;
    private int lvCount = 0;
    private static int i = 0;
    private static HashMap<Integer, Integer> argHMap;

    public RejectedBRMListFragmentDialog() {}

    public static RejectedBRMListFragmentDialog newInstance(Context ctx, HashMap<Integer, Integer> hMap, String... args) {
        mContext = ctx;
        uid = args[0];
        id_berkas = args[1];
        argHMap = hMap;
        ArrayList<Integer> idNotes = new ArrayList<>();
        ArrayList<Integer> values = new ArrayList<>();

        int ii = 0;
        for ( Integer key : hMap.keySet() ) {
            idNotes.add(ii++, key);
        }

        int iii = 0;
        for ( Integer val : hMap.values() ) {
            values.add(iii++, val);
        }

        RejectedBRMListFragmentDialog frag = new RejectedBRMListFragmentDialog();
        Bundle bund = new Bundle();
        bund.putString("uid", args[0]);
        bund.putString("id_berkas", args[1]);
        bund.putIntegerArrayList("id_notes", idNotes);
        bund.putIntegerArrayList("values", values);
        frag.setArguments(bund);
        return frag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dialog_rejected_brm_list, container);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        txtTitle = view.findViewById(R.id.title);
        btnPos = view.findViewById(R.id.btnPositive);
        btnNeg = view.findViewById(R.id.btnNegative);
        pgLoading = view.findViewById(R.id.pbLoading);
        lvRnotes = view.findViewById(R.id.lvRnotes);
        rNotesContainer = view.findViewById(R.id.rNotesContainer);

        btnPos.setText("Simpan");
        btnNeg.setText("Batal");
        getDialog().setCancelable(false);
        getDialog().setCanceledOnTouchOutside(false);
        disablePosButton();
        contentLoading();

        lstIdNotes = new ArrayList<>();
        lstVal = new ArrayList<>();
        resultData = new HashMap<>();

        btnNeg.setOnClickListener(view12 -> getDialog().cancel());

        lstIdNotes = Objects.requireNonNull(getArguments()).getIntegerArrayList("id_notes");
        lstVal = Objects.requireNonNull(getArguments()).getIntegerArrayList("values");

        initDataset();

        btnPos.setOnClickListener(view1 -> {
            DoctorMainActivity.onFinishUserDialog(resultData);
            i = 0;
            getDialog().cancel();
        });
    }

    private void initDataset(){
        if(resultData != null)
            resultData.clear();

        uid = Objects.requireNonNull(getArguments()).getString("uid", null);
        id_berkas = Objects.requireNonNull(getArguments()).getString("id_berkas", "0");

        if(apiService == null) {
            apiService = APIUtils.getAPIService(mContext);
        }

        Call<List<RejectedNote>> updateDataCall = apiService.getRejectedNotes(uid, Integer.valueOf(id_berkas));

        updateDataCall.enqueue(new retrofit2.Callback<List<RejectedNote>>() {
            @Override
            public void onResponse(@NonNull Call<List<RejectedNote>> call, @NonNull Response<List<RejectedNote>> response) {
                if(response.isSuccessful()){
                    List<RejectedNote> lstNote = response.body();

                    if(Objects.requireNonNull(lstNote).size() > 0){
                        contentLoaded();
                        dataModels = new ArrayList<>();

                        dataModels.clear();
                        dataModels.addAll(lstNote);
                        adapter = new ListAdapter(mContext, dataModels);
                        adapter.notifyDataSetChanged();
                        lvRnotes.setAdapter(adapter);
                        lvRnotes.setDivider(getResources().getDrawable(R.drawable.list_divider));
                        lvRnotes.setDividerHeight(1);

                        lvCount = lstNote.size();
                        enablePosButton();

                        Log.e("[X-DEBUG]", "OK. Listcount:"+lstNote.size()+" Arraylistcount:"+dataModels.size());
                    }else{
                        getDialog().cancel();
                        Toast.makeText(mContext, "Tidak ada data ditemukan", Toast.LENGTH_LONG).show();
                        Log.e("[X-DEBUG]", "Tidak ada data ditemukan");
                    }
                }else {
                    getDialog().cancel();
                    String errorMsg = ApiError.parseError(response).getMessage();
                    Toast.makeText(mContext, "Terjadi kesalahan. Error: "+errorMsg, Toast.LENGTH_LONG).show();
                    Log.e("[X-DEBUG]", "Terjadi kesalahan. Error: "+errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<List<RejectedNote>> call, @NonNull Throwable t) {
                Log.e("[X-DEBUG]", "Failure " + t.getLocalizedMessage());
                Log.e("[X-DEBUG]", "[-] Request to get  onFailure thrown. Error: "+t.getLocalizedMessage());
            }
        });
    }

    private void enablePosButton(){
        btnPos.setTextColor(getResources().getColor(R.color.colorPrimary));
        btnPos.setEnabled(true);
    }

    private void disablePosButton(){
        btnPos.setTextColor(getResources().getColor(android.R.color.darker_gray));
        btnPos.setEnabled(false);
    }

    private void contentLoading(){
        rNotesContainer.setVisibility(View.GONE);
        pgLoading.setVisibility(View.VISIBLE);
    }

    private void contentLoaded(){
        rNotesContainer.setVisibility(View.VISIBLE);
        pgLoading.setVisibility(View.GONE);
    }

    public class ListAdapter extends ArrayAdapter<RejectedNote> {

        private Context mContext;
        private List<RejectedNote> itemList;

        public ListAdapter(@NonNull Context context, ArrayList<RejectedNote> list) {
            super(context, R.layout.rejected_note_item, list);
            mContext = context;
            itemList = list;
        }

        private class ViewHolder {
            TextView title, desc;
            CheckBox check;
        }

        private int lastPosition = -1;

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            // Get the data item for this position
            RejectedNote dataModel = getItem(position);
            // Check if an existing view is being reused, otherwise inflate the view
            ViewHolder viewHolder; // view lookup cache stored in tag

            if (convertView == null) {
                viewHolder = new ViewHolder();
                LayoutInflater inflater = LayoutInflater.from(getContext());
                convertView = inflater.inflate(R.layout.rejected_note_item, parent, false);

                viewHolder.title = convertView.findViewById(R.id.txtItemTitle);
                viewHolder.desc = convertView.findViewById(R.id.txtItemDesc);
                viewHolder.check = convertView.findViewById(R.id.checkbox);

                convertView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) convertView.getTag();
            }

            lastPosition = position;

            if(Objects.requireNonNull(dataModel).getStatus() == DetailVisitor.REJECTED_NOTE_OK){
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    viewHolder.title.setForeground(getResources().getDrawable(R.drawable.strikethrough_effect));
                }
                viewHolder.check.setChecked(true);
            }

            viewHolder.title.setText(Objects.requireNonNull(dataModel).getNote());
            viewHolder.desc.setText("Halaman: " + dataModel.getPage());
            viewHolder.check.setOnCheckedChangeListener((compoundButton, b) -> {
                if(compoundButton.isChecked()) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        int val = resultData.getOrDefault(dataModel.getId_note(), -1);

                        if(val != -1){
                            resultData.replace(dataModel.getId_note(), 1);
                        }else{
                            resultData.put(dataModel.getId_note(), 1);
                        }
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        viewHolder.title.setForeground(getResources().getDrawable(R.drawable.strikethrough_effect));
                    }
                }else{
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        int val = resultData.getOrDefault(dataModel.getId_note(), -1);

                        if(val != -1){
                            resultData.replace(dataModel.getId_note(), 0);
                        }else{
                            resultData.put(dataModel.getId_note(), 0);
                        }
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        viewHolder.title.setForeground(null);
                    }
                }
            });

            viewHolder.check.setTag(position);
            Log.e("[X-DEBUG]", "lstIdNotes Size:"+lstIdNotes.size()+" lstVal Size:"+lstVal.size());

            if(lstIdNotes.contains(dataModel.getId_note())){
                int index = lstIdNotes.indexOf(dataModel.getId_note());
                if(lstVal.get(index) == 1){
                    viewHolder.check.setChecked(true);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        viewHolder.title.setForeground(getResources().getDrawable(R.drawable.strikethrough_effect));
                    }
                }else{
                    viewHolder.check.setChecked(false);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        viewHolder.title.setForeground(null);
                    }
                }

                Log.e("[X-DEBUG]", "lstIdNotes Size:"+lstIdNotes.size()+" lstVal Size:"+lstVal.size());
            }else {
                int val = 0;

                if(viewHolder.check.isChecked()){
                    val = 1;
                }

                lstIdNotes.add(dataModel.getId_note());
                lstVal.add(val);
            }

            i = i+1;

            if(i == lvCount){
                for(int idx=0; idx<lvCount; idx++){
                    resultData.put(lstIdNotes.get(idx), lstVal.get(idx));
                }
            }

            return convertView;
        }
    }

}