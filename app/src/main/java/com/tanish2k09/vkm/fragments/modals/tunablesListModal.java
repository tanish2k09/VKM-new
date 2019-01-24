package com.tanish2k09.vkm.fragments.modals;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tanish2k09.vkm.R;
import com.tanish2k09.vkm.classes.db.fsDatabaseHelper;

import java.lang.reflect.Type;

/**
 * <p>A fragment that shows a list of items as a modal bottom sheet.</p>
 * <p>You can show this modal bottom sheet from your activity like this:</p>
 * <pre>
 *     tunablesListModal.newInstance(30).show(getSupportFragmentManager(), "dialog");
 * </pre>
 * <p>You activity (or fragment) needs to implement {@link tunablesListModal.Listener}.</p>
 */
public class tunablesListModal extends BottomSheetDialogFragment {

    // TODO: Customize parameter argument names
    private static int numItems = 0;
    private static String[] list, vals;
    private Listener mListener;

    public static tunablesListModal newInstance(int length, String[] listArg) {
        final tunablesListModal fragment = new tunablesListModal();
        numItems = length;
        list = listArg;
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(BottomSheetDialogFragment.STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_tunables_list_dialog, container, false);

        vals = new String[list.length];
        for(int cnt = 0 ; cnt < list.length ; cnt++)
            vals[cnt] = fsDatabaseHelper.getDbHelper(v.getContext()).getCpuTunableValue(list[cnt]);

        TextView totalTunablesText = v.findViewById(R.id.totalTunableText);
        Typeface archive = Typeface.createFromAsset(v.getContext().getApplicationContext().getAssets(), String.format("fonts/%s", "Archive.otf"));
        totalTunablesText.setTypeface(archive, Typeface.BOLD);
        totalTunablesText.setText(getString(R.string.totalTunables, list.length));

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        final RecyclerView recyclerView = view.findViewById(R.id.list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(new ItemAdapter(numItems));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        final Fragment parent = getParentFragment();
        if (parent != null) {
            mListener = (Listener) parent;
        } else {
            mListener = (Listener) context;
        }
    }

    @Override
    public void onDetach() {
        mListener = null;
        super.onDetach();
    }

    public interface Listener {
        void onItemClickedTunable(int position);
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        final TextView text, serialNum, value;

        ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            // TODO: Customize the item layout
            super(inflater.inflate(R.layout.fragment_tunables_list_item, parent, false));
            text = itemView.findViewById(R.id.text);
            serialNum = itemView.findViewById(R.id.serialNum);
            value = itemView.findViewById(R.id.valText);

            text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onItemClickedTunable(getAdapterPosition());
                        //dismiss();
                    }
                }
            });
        }

    }

    private class ItemAdapter extends RecyclerView.Adapter<ViewHolder> {

        private final int mItemCount;

        ItemAdapter(int itemCount) {
            mItemCount = itemCount;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            return new ViewHolder(LayoutInflater.from(parent.getContext()), parent);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            holder.serialNum.setText(getString(R.string.numSerial, position+1));
            holder.text.setText(list[position]);
            holder.value.setText(vals[position]);
        }

        @Override
        public int getItemCount() {
            return mItemCount;
        }

    }

}
