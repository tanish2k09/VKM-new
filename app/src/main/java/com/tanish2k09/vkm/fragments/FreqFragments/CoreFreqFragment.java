package com.tanish2k09.vkm.fragments.FreqFragments;


import android.annotation.SuppressLint;
import android.content.SharedPreferences;
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
import android.widget.Toast;

import com.hanks.htextview.base.HTextView;
import com.tanish2k09.vkm.R;
import com.tanish2k09.vkm.classes.db.Paths;
import com.tanish2k09.vkm.classes.db.fsDatabaseHelper;

import java.util.Objects;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class CoreFreqFragment extends Fragment {

    private HTextView freqText;
    //private Switch isOn;
    private int thisCoreNum = -1;
    private int onColor, onColorSemi;
    private View v;
    private Typeface textTfCache;

    public CoreFreqFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_core_freq, container, false);

        freqText = v.findViewById(R.id.coreFreq);
        //isOn = v.findViewById(R.id.isCpuCoreOn);
        TextView coreNumText = v.findViewById(R.id.coreNumText);

        //coreNumText.setTypeface(Typeface.createFromAsset(v.getContext().getApplicationContext().getAssets(),String.format("fonts/%s","Archive.otf")));
        //textTfCache = freqText.getTypeface();

        String coreText = getResources().getString(R.string.core,thisCoreNum);
        coreNumText.setText(coreText);

        //setDarkSwitchActivatedColor();

        /*
        isOn.setOnClickListener(new View.OnClickListener() {

            fsDatabaseHelper dbHelper = fsDatabaseHelper.getDbHelper(v.getContext());

            @Override
            public void onClick(View v) {
                if(isOn.isChecked())
                    dbHelper.execChmodWrite("0444","0644",Paths.cpufreq_folder_prime+thisCoreNum+"/","online","0");
                else
                    dbHelper.execChmodWrite("0444","0644",Paths.cpufreq_folder_prime+thisCoreNum+"/","online","1");
            }
        });
        */

        return v;
    }

    public void setFreqText(String freq)
    {
        if (freqText.getText().toString().equals(freq))
            return;

        if(freq.equals("Offline"))
        {
            freqText.animateText(freq);
            //freqText.setTypeface(textTfCache, Typeface.NORMAL);
        }
        else {
            freqText.animateText(getString(R.string.freq, freq));
            //freqText.setTypeface(textTfCache, Typeface.BOLD);
        }
    }

    //public void setIsOn(boolean on) { isOn.setChecked(on); }

    public void setCoreNumText(int core)
    {
        thisCoreNum = core;
    }

    /*
    public void setDarkSwitchActivatedColor()
    {
        int[][] states = new int[][] {
                new int[] {-android.R.attr.state_checked},
                new int[] {android.R.attr.state_checked},
        };

        int[] thumbColors = new int[] {
                getColorInt("R.color.secondBG",R.color.secondBG),
                getColorInt("onColor", onColor)
        };

        int[] trackColors = new int[] {
                getColorInt("R.color.secondBGSemi",R.color.secondBGSemi),
                getColorInt("onColorSemi",onColorSemi)
        };

        DrawableCompat.setTintList(DrawableCompat.wrap(isOn.getThumbDrawable()), new ColorStateList(states, thumbColors));
        DrawableCompat.setTintList(DrawableCompat.wrap(isOn.getTrackDrawable()), new ColorStateList(states, trackColors));
    }

    public void setDarkSwitchActivatedColor(int color, int colorSemi)
    {
        onColor = color;
        onColorSemi = colorSemi;
    }

    @SuppressLint("ApplySharedPref")
    private int getColorInt(String colorID, int colorIDint)
    {
        SharedPreferences sp = Objects.requireNonNull(getActivity()).getSharedPreferences("colorsCoreFreq",MODE_PRIVATE);
        int val = sp.getInt(colorID , -1);
        if (val == -1)
        {
            int color = ContextCompat.getColor(v.getContext(),colorIDint);
            SharedPreferences.Editor sp_editor = sp.edit();
            sp_editor.putInt(colorID, color);
            sp_editor.commit();
            return color;
        }

        return val;
    }
    */

}
