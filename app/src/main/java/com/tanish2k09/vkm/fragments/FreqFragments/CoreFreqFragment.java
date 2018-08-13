package com.tanish2k09.vkm.fragments.FreqFragments;


import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.tanish2k09.vkm.R;
import com.tanish2k09.vkm.classes.db.Paths;
import com.topjohnwu.superuser.Shell;

import java.lang.reflect.Type;

/**
 * A simple {@link Fragment} subclass.
 */
public class CoreFreqFragment extends Fragment {

    private TextView freqText;
    private Switch isOn;
    private int thisCoreNum = -1;
    private int onColor, onColorSemi;
    private Context ctx;

    public CoreFreqFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_core_freq, container, false);
        ctx = v.getContext();

        freqText = v.findViewById(R.id.coreFreq);
        isOn = v.findViewById(R.id.isCpuCoreOn);
        TextView coreNumText = v.findViewById(R.id.coreNumText);

        coreNumText.setTypeface(Typeface.createFromAsset(v.getContext().getApplicationContext().getAssets(),String.format("fonts/%s","Archive.otf")));

        String coreText = getResources().getString(R.string.core,thisCoreNum);
        coreNumText.setText(coreText);

        setDarkSwitchActivatedColor();

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

    public void setDarkSwitchActivatedColor()
    {
        int[][] states = new int[][] {
                new int[] {-android.R.attr.state_checked},
                new int[] {android.R.attr.state_checked},
        };

        int[] thumbColors = new int[] {
                getColorInt(R.color.secondBG),
                getColorInt(onColor)
        };

        int[] trackColors = new int[] {
                getColorInt(R.color.secondBGSemi),
                getColorInt(onColorSemi)
        };

        DrawableCompat.setTintList(DrawableCompat.wrap(isOn.getThumbDrawable()), new ColorStateList(states, thumbColors));
        DrawableCompat.setTintList(DrawableCompat.wrap(isOn.getTrackDrawable()), new ColorStateList(states, trackColors));
    }

    public void setDarkSwitchActivatedColor(int color, int colorSemi)
    {
        onColor = color;
        onColorSemi = colorSemi;
    }

    private int getColorInt(int colorID)
    {
        return ContextCompat.getColor(ctx,colorID);
    }

}
