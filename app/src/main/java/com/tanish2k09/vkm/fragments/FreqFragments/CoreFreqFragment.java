package com.tanish2k09.vkm.fragments.FreqFragments;


import android.graphics.Typeface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.tanish2k09.vkm.R;
import com.tanish2k09.vkm.classes.db.Paths;
import com.topjohnwu.superuser.Shell;

/**
 * A simple {@link Fragment} subclass.
 */
public class CoreFreqFragment extends Fragment {

    private TextView freqText;
    private Switch isOn;
    private int thisCoreNum = -1;

    public CoreFreqFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_core_freq, container, false);

        freqText = v.findViewById(R.id.coreFreq);
        isOn = v.findViewById(R.id.isCpuCoreOn);
        TextView coreNumText = v.findViewById(R.id.coreNumText);

        String coreText = getResources().getString(R.string.core,thisCoreNum);
        coreNumText.setText(coreText);

        isOn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isOn.isChecked())
                    Shell.Async.su("echo 0 > "+ Paths.cpufreq_folder_prime + "cpu" + thisCoreNum + Paths.cpufreq_coreOnlineFile );
                else
                    Shell.Async.su("echo 1 > "+ Paths.cpufreq_folder_prime + "cpu" + thisCoreNum + Paths.cpufreq_coreOnlineFile );
            }
        });

        return v;
    }

    public void setFreqText(String freq)
    {
        if(freq.equals("Offline"))
        {
            freqText.setText(freq);
            freqText.setTypeface(freqText.getTypeface(), Typeface.ITALIC);
            return;
        }
        freqText.setText(getString(R.string.freq,freq));
        freqText.setTypeface(freqText.getTypeface(), Typeface.BOLD);
    }

    public void setIsOn(boolean on)
    {
        isOn.setChecked(on);
    }

    public void setCoreNumText(int core)
    {
        thisCoreNum = core;
    }

}
