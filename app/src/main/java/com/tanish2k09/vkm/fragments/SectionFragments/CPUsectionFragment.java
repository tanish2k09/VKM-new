package com.tanish2k09.vkm.fragments.SectionFragments;


import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.tanish2k09.vkm.R;
import com.tanish2k09.vkm.classes.db.Paths;
import com.tanish2k09.vkm.classes.db.fsDatabaseHelper;
import com.tanish2k09.vkm.fragments.FreqFragments.CoreFreqFragment;

import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class CPUsectionFragment extends Fragment {

    private boolean stopCoreFreqThread = false;
    private fsDatabaseHelper dbHelper;
    private FragmentManager fm;
    private int cpuCores, cnt_thread;
    private CoreFreqFragment coreFreqFragment;
    private boolean isFrozen = false;
    private int sleepMs = 500;

    public CPUsectionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_cpusection, container, false);
        TextView cpuGovName = v.findViewById(R.id.cpugovname);
        final TextView freezeText = v.findViewById(R.id.freezeText);
        dbHelper = fsDatabaseHelper.getDbHelper(v.getContext());
        cpuCores = dbHelper.getCpuCoreNum();
        final CardView freezeCard = v.findViewById(R.id.cpuFreqFreeze);

        cpuGovName.setText(dbHelper.readVal(Paths.cpufreq_folder_prime+"cpu0/cpufreq/","scaling_governor",fsDatabaseHelper.TABLE_CPU));

        fm = getChildFragmentManager();

        int cnt = 0;
        while(cnt < cpuCores) {
            FragmentTransaction ft = fm.beginTransaction();
            CoreFreqFragment coreFreqFragment = new CoreFreqFragment();
            ft.add(R.id.coreFreqLayout, coreFreqFragment, ("cpucore" + cnt));
            coreFreqFragment.setCoreNumText(cnt);
            ft.commitNow();
            ++cnt;
        }

        final Runnable freezeRunnable = new Runnable() {
            @Override
            public void run() {
                freezeCard.setEnabled(true);
            }
        };

        freezeCard.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(!isFrozen) {
                    stopCoreFreqThread = true;
                    freezeText.setText(R.string.start);
                    freezeCard.setCardBackgroundColor(getResources().getColor(R.color.colorWhiteBg, Objects.requireNonNull(getActivity()).getTheme()));
                    isFrozen = !isFrozen;
                }
                else
                {
                    isFrozen = false;
                    startCpuCoreFreqThread();
                    freezeText.setText(R.string.freeze);
                    freezeCard.setCardBackgroundColor(getResources().getColor(R.color.colorMaterialRed, Objects.requireNonNull(getActivity()).getTheme()));
                }
                freezeCard.setEnabled(false);
                Timer enableTimer = new Timer();
                enableTimer.schedule(new TimerTask() {
                    @Override
                    public void run()
                    {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(freezeRunnable);
                    }
                }, 100);
            }
        });

        startCpuCoreFreqThread();

        return v;
    }

    public void startCpuCoreFreqThread()
    {
        if(isFrozen)
            return;
        stopCoreFreqThread = false;
        final Handler handler = new Handler(Looper.getMainLooper());
        new Thread(new Runnable() {
            @Override
            public void run()
            {
                while(!stopCoreFreqThread)
                {
                    dbHelper.scanCPUCoreFreqs();

                    for(cnt_thread = 0; cnt_thread < cpuCores; cnt_thread++)
                    {
                        if(stopCoreFreqThread)
                            break;

                            Runnable runnable = new Runnable()
                            {

                                int thisCoreNum = cnt_thread;

                                @Override
                                public void run()
                                {
                                    coreFreqFragment = (CoreFreqFragment) fm.findFragmentByTag(("cpucore" + thisCoreNum));
                                    String val = dbHelper.readCoreFreqVal(thisCoreNum, fsDatabaseHelper.TABLE_CPUCOREFREQ);
                                    if (val.equals("0")) {
                                        coreFreqFragment.setFreqText(getString(R.string.offline));
                                        coreFreqFragment.setIsOn(false);
                                    } else {
                                        coreFreqFragment.setFreqText(val);
                                        coreFreqFragment.setIsOn(true);
                                    }
                                }
                            };

                        if(!stopCoreFreqThread)
                            handler.post(runnable);
                    }

                    if(stopCoreFreqThread)
                        break;

                    int sleepCnt = 0;
                    while((sleepCnt < (sleepMs / 5) ) && (!stopCoreFreqThread)) {
                        try {
                            Thread.sleep(5);
                            sleepCnt++;
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                    }
                }
                stopCoreFreqThread = false;
            }
        }).start();
    }

    public void setStopCoreFreqThread()
    {
        stopCoreFreqThread = true;
    }

}
