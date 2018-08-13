package com.tanish2k09.vkm.fragments.SectionFragments;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.LayoutTransition;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import com.tanish2k09.vkm.R;
import com.tanish2k09.vkm.classes.db.Paths;
import com.tanish2k09.vkm.classes.db.fsDatabaseHelper;
import com.tanish2k09.vkm.fragments.FreqFragments.CoreFreqFragment;
import com.topjohnwu.superuser.Shell;
import java.util.Timer;
import java.util.TimerTask;

/**
 * A simple {@link Fragment} subclass.
 */
public class CPUsectionFragment extends Fragment{

    private boolean stopCoreFreqThread = false;
    private fsDatabaseHelper dbHelper;
    private FragmentManager fm;
    private int cpuCores, cnt_thread;
    private CoreFreqFragment coreFreqFragment;
    private boolean isFrozen = false;
    private int sleepMs = 1000;
    private int sleepMsPauseSegment = 20;
    private View v;

    public CPUsectionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_cpusection, container, false);
        final ImageButton toggleCoreInfo = v.findViewById(R.id.toggleCoreInfo);
        final FrameLayout viewContainer = v.findViewById(R.id.viewContainer);
        ConstraintLayout rootLayout = v.findViewById(R.id.rootCpuSectionLayout);
        dbHelper = fsDatabaseHelper.getDbHelper(v.getContext());
        cpuCores = dbHelper.getCpuCoreNum();

        initCoreInfo();

        rootLayout.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);

        fm = getChildFragmentManager();

        int cnt = 0;
        while(cnt < cpuCores) {
            FragmentTransaction ft = fm.beginTransaction();
            CoreFreqFragment coreFreqFragment = new CoreFreqFragment();
            ft.add(R.id.coreFreqLayout, coreFreqFragment, ("cpucore" + cnt));
            coreFreqFragment.setCoreNumText(cnt);
            coreFreqFragment.setDarkSwitchActivatedColor(R.color.colorMaterialYellowDark,R.color.colorMaterialYellowDarkSemi);
            ft.commitNow();
            ++cnt;
        }

        final Runnable freezeRunnable = new Runnable() {
            @Override
            public void run() {
                toggleCoreInfo.setClickable(true);
            }
        };

        final Animator rotateAnim = AnimatorInflater.loadAnimator(v.getContext(),R.animator.rotate);
        final Animator rotateAnimRev = AnimatorInflater.loadAnimator(v.getContext(),R.animator.rotate_rev);
        rotateAnim.setTarget(toggleCoreInfo);
        rotateAnimRev.setTarget(toggleCoreInfo);

        toggleCoreInfo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(!isFrozen) {
                    viewContainer.setVisibility(View.GONE);
                    stopCoreFreqThread = true;
                    rotateAnimRev.start();
                    isFrozen = !isFrozen;
                }
                else
                {
                    viewContainer.setVisibility(View.VISIBLE);
                    isFrozen = false;
                    startCpuCoreFreqThread();
                    rotateAnim.start();
                }
                toggleCoreInfo.setClickable(false);
                Timer enableTimer = new Timer();
                enableTimer.schedule(new TimerTask() {
                    @Override
                    public void run()
                    {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(freezeRunnable);
                    }
                }, 1000);
            }
        });

        startCpuCoreFreqThread();

        return v;
    }

    public void onResume()
    {
        dbHelper.scanCPU();
        initCoreInfoSpinners();
        super.onResume();
    }

    private void initCoreInfo()
    {
        initCoreInfoTypeface();
        initCoreInfoSpinners();
    }

    private void initCoreInfoTypeface()
    {
        TextView coreInfoHeader = v.findViewById(R.id.coreInfoHeading);
        TextView cpuGovText = v.findViewById(R.id.staticCpuGovText);
        TextView minFreqText = v.findViewById(R.id.minFreqText);
        TextView maxFreqText = v.findViewById(R.id.maxFreqText);

        Typeface archive = Typeface.createFromAsset(v.getContext().getApplicationContext().getAssets(), String.format("fonts/%s", "Archive.otf"));

        coreInfoHeader.setTypeface(archive);
        cpuGovText.setTypeface(archive);
        minFreqText.setTypeface(archive);
        maxFreqText.setTypeface(archive);
    }

    private void initCoreInfoSpinners()
    {
        String current;
        String[] list;

        /* CPU Governor Spinner */
        Spinner cpuGovSpinner = v.findViewById(R.id.cpuGovSpinner);
        ArrayAdapter<String> govAdapter;
        current = dbHelper.readVal(Paths.cpufreq_folder_prime+"cpu0/cpufreq/",Paths.governorFileName,fsDatabaseHelper.TABLE_CPU);
        list = dbHelper.readVal(Paths.cpufreq_folder_prime+"cpu0/cpufreq/",Paths.governorListFileName,fsDatabaseHelper.TABLE_CPU).split(" ");
        govAdapter = new ArrayAdapter<>(v.getContext(), R.layout.spinner_item, list);
        govAdapter.setDropDownViewResource(R.layout.spinner_dropdown);
        cpuGovSpinner.setAdapter(govAdapter);
        cpuGovSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            boolean govSpinnerInited = false;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(!govSpinnerInited) {
                    govSpinnerInited =  true;
                    return;
                }

                String selectedGov = parent.getSelectedItem().toString();
                int cnt;
                for(cnt = 0; cnt < dbHelper.getCpuCoreNum(); cnt++) {
                    Shell.Async.su("chmod 0644 "+Paths.cpufreq_folder_prime+"cpu"+cnt+"/cpufreq/"+Paths.governorFileName);
                    Shell.Async.su("echo '"+selectedGov+"' > "+Paths.cpufreq_folder_prime+"cpu"+cnt+"/cpufreq/"+Paths.governorFileName);
                    Shell.Async.su("chmod 0444 "+Paths.cpufreq_folder_prime+"cpu"+cnt+"/cpufreq/"+Paths.governorFileName);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        cpuGovSpinner.setSelection(govAdapter.getPosition(current));


        /* INIT FREQS */
        ArrayAdapter<String> freqAdapter;
        list = dbHelper.readVal(Paths.cpufreq_folder_prime+"cpu0/cpufreq/",Paths.cpufreq_possibleFreqsFile,fsDatabaseHelper.TABLE_CPU).split(" ");
        freqAdapter = new ArrayAdapter<>(v.getContext(), R.layout.spinner_item, list);
        freqAdapter.setDropDownViewResource(R.layout.spinner_dropdown);

        /* MAX Freq */
        Spinner maxFreqSpinner = v.findViewById(R.id.maxFreqSpinner);
        current = dbHelper.readVal(Paths.cpufreq_folder_prime+"cpu0/cpufreq/",Paths.cpufreq_curMaxFreqName,fsDatabaseHelper.TABLE_CPU);
        maxFreqSpinner.setAdapter(freqAdapter);
        maxFreqSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            boolean inited = false;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(!inited) {
                    inited =  true;
                    return;
                }

                String selectedFreq = parent.getSelectedItem().toString();
                int cnt;
                for(cnt = 0; cnt < dbHelper.getCpuCoreNum(); cnt++) {
                    dbHelper.execChmodWriteCpu("-1","0644",Paths.cpufreq_curMaxFreqName,cnt,selectedFreq);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        maxFreqSpinner.setSelection(freqAdapter.getPosition(current));

        /* MIN Freq */
        Spinner minFreqSpinner = v.findViewById(R.id.minFreqSpinner);
        current = dbHelper.readVal(Paths.cpufreq_folder_prime+"cpu0/cpufreq/",Paths.cpufreq_curMinFreqName,fsDatabaseHelper.TABLE_CPU);
        minFreqSpinner.setAdapter(freqAdapter);
        minFreqSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            boolean inited = false;

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if(!inited) {
                    inited =  true;
                    return;
                }

                String selectedFreq = parent.getSelectedItem().toString();
                int cnt;
                for(cnt = 0; cnt < dbHelper.getCpuCoreNum(); cnt++) {
                    dbHelper.execChmodWriteCpu("0444","0644",Paths.cpufreq_curMinFreqName,cnt,selectedFreq);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        minFreqSpinner.setSelection(freqAdapter.getPosition(current));
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
                                    String val = dbHelper.readCpuCoreFreqVal(thisCoreNum);
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
                    while((sleepCnt < (sleepMs / sleepMsPauseSegment) ) && (!stopCoreFreqThread)) {
                        try {
                            Thread.sleep(sleepMsPauseSegment);
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
