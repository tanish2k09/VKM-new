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

/**
 * <p>A fragment that shows a list of items as a modal bottom sheet.</p>
 * <p>You can show this modal bottom sheet from your activity like this:</p>
 * <pre>
 *     freqSelectorModal.newInstance(30).show(getSupportFragmentManager(), "dialog");
 * </pre>
 * <p>You activity (or fragment) needs to implement {@link freqSelectorModal.Listener}.</p>
 */
public class freqSelectorModal extends BottomSheetDialogFragment {

    private static int numItems;
    private static String[] list;
    private Listener mListener;
    private int currentNum = 1;

    public static freqSelectorModal newInstance(int itemCount, String[] listArg) {
        final freqSelectorModal fragment = new freqSelectorModal();
        numItems = itemCount;
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
        View v = inflater.inflate(R.layout.fragment_freq_list_dialog, container, false);

        TextView  totalFreqText = v.findViewById(R.id.totalFreqText);
        Typeface archive = Typeface.createFromAsset(v.getContext().getApplicationContext().getAssets(), String.format("fonts/%s", "Archive.otf"));
        totalFreqText.setTypeface(archive,Typeface.BOLD);

        totalFreqText.setText(getString(R.string.totalFreqs,numItems));

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
        void onItemClickedFreq(int position);
    }

    private class ViewHolder extends RecyclerView.ViewHolder {

        final TextView text, serial;
        final View tapView;

        ViewHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.fragment_list_item_text, parent, false));
            text = itemView.findViewById(R.id.text);
            serial = itemView.findViewById(R.id.serial);
            tapView = itemView.findViewById(R.id.tapView);
            tapView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mListener != null) {
                        mListener.onItemClickedFreq(getAdapterPosition());
                        dismiss();
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
            String freq = list[currentNum - 1];
            holder.text.setText(getString(R.string.freqkHz,freq));
            holder.serial.setText(getString(R.string.numSerial,position+1));
            currentNum++;
        }

        @Override
        public int getItemCount() {
            return mItemCount;
        }

    }

}
