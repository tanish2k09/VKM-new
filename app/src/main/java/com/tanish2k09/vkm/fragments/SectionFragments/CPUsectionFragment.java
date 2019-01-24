package com.tanish2k09.vkm.fragments.SectionFragments;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.LayoutTransition;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.constraint.ConstraintLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.tanish2k09.vkm.R;
import com.tanish2k09.vkm.classes.db.Paths;
import com.tanish2k09.vkm.classes.db.fsDatabaseHelper;
import com.tanish2k09.vkm.fragments.FreqFragments.CoreFreqFragment;
import com.tanish2k09.vkm.fragments.modals.freqSelectorModal;
import com.tanish2k09.vkm.fragments.modals.governorListModal;
import com.tanish2k09.vkm.fragments.modals.tunablesListModal;

import java.util.Timer;
import java.util.TimerTask;

import static android.content.Context.MODE_PRIVATE;

/**
 * A simple {@link Fragment} subclass.
 */
public class CPUsectionFragment extends Fragment implements governorListModal.Listener, freqSelectorModal.Listener, tunablesListModal.Listener {

    private boolean stopCoreFreqThread = false;
    private fsDatabaseHelper dbHelper;
    private FragmentManager fm;
    private int cpuCores, cnt_thread;
    private CoreFreqFragment coreFreqFragment;
    private boolean isFrozen = false;
    private int sleepMs = 1000;
    private int sleepMsPauseSegment = 200;
    private View v;
    private boolean isMinSelected = false;

    public CPUsectionFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_cpusection, container, false);
        final ImageButton toggleCoreInfo = v.findViewById(R.id.toggleCoreInfo);
        final CardView viewContainer = v.findViewById(R.id.coreinfoCard);
        ConstraintLayout rootLayout = v.findViewById(R.id.rootCpuSectionLayout);
        dbHelper = fsDatabaseHelper.getDbHelper(v.getContext());
        cpuCores = dbHelper.getCpuCoreNum();

        initCoreInfo();

        rootLayout.getLayoutTransition().enableTransitionType(LayoutTransition.CHANGING);

        fm = getChildFragmentManager();

        int cnt = 0;
        while (cnt < cpuCores) {
            FragmentTransaction ft = fm.beginTransaction();
            CoreFreqFragment coreFreqFragment = new CoreFreqFragment();
            ft.add(R.id.coreFreqLayout, coreFreqFragment, ("cpucore" + cnt));
            coreFreqFragment.setCoreNumText(cnt);
            //coreFreqFragment.setDarkSwitchActivatedColor(getCpuColors(false), getCpuColors(true));
            ft.commitNow();
            ++cnt;
        }

        final Runnable freezeRunnable = new Runnable() {
            @Override
            public void run() {
                toggleCoreInfo.setClickable(true);
            }
        };

        final Animator rotateAnim = AnimatorInflater.loadAnimator(v.getContext(), R.animator.rotate);
        final Animator rotateAnimRev = AnimatorInflater.loadAnimator(v.getContext(), R.animator.rotate_rev);
        rotateAnim.setTarget(toggleCoreInfo);
        rotateAnimRev.setTarget(toggleCoreInfo);

        toggleCoreInfo.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!isFrozen) {
                    viewContainer.setVisibility(View.GONE);
                    stopCoreFreqThread = true;
                    rotateAnimRev.start();
                    isFrozen = !isFrozen;
                } else {
                    viewContainer.setVisibility(View.VISIBLE);
                    isFrozen = false;
                    startCpuCoreFreqThread();
                    rotateAnim.start();
                }
                toggleCoreInfo.setClickable(false);
                Timer enableTimer = new Timer();
                enableTimer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(freezeRunnable);
                    }
                }, 500);
            }
        });

        return v;
    }

    public void onResume() {
        initCoreInfoSelectors();
        startCpuCoreFreqThread();
        super.onResume();
    }

    private void initCoreInfo() {
        initCoreInfoTypeface();
        initCoreInfoSelectors();
    }

    private void initCoreInfoTypeface() {
        TextView coreInfoHeader = v.findViewById(R.id.coreInfoHeading);
        TextView cpuGovText = v.findViewById(R.id.staticCpuGovText);
        TextView minFreqText = v.findViewById(R.id.minFreqText);
        TextView maxFreqText = v.findViewById(R.id.maxFreqText);

        Typeface archive = Typeface.createFromAsset(v.getContext().getApplicationContext().getAssets(), String.format("fonts/%s", "Archive.otf"));

        //coreInfoHeader.setTypeface(archive);
        //cpuGovText.setTypeface(archive);
        //minFreqText.setTypeface(archive);
        //maxFreqText.setTypeface(archive);
    }

    private void initCoreInfoSelectors() {
        initCoreInfoSelectorsListeners();
        initCoreInfoSelectorsTexts();
    }

    private void initCoreInfoSelectorsTexts()
    {
        /* CPU Governor Button */
        TextView govText = v.findViewById(R.id.govTextView);
        govText.setText(dbHelper.getCurrentCpuGov(0));

        /* MAX Freq */
        TextView maxFreqText = v.findViewById(R.id.maxFreqTextView);
        String freq = dbHelper.getCurrentCpuFreqMaxMin(0,true);
        maxFreqText.setText(freq);

        /* MIN Freq */
        TextView minFreqText = v.findViewById(R.id.minFreqTextView);
        String freq2 = dbHelper.getCurrentCpuFreqMaxMin(0,false);
        minFreqText.setText(freq2);
    }

    private void initCoreInfoSelectorsListeners()
    {
        /* CPU Governor Button */
        CardView govCard = v.findViewById(R.id.govTextCard);
        govCard.setOnClickListener(new View.OnClickListener() {

            FragmentManager fm = getChildFragmentManager();
            String[] list = dbHelper.getCpuGovList(0).split(" ");

            @Override
            public void onClick(View v) {
                governorListModal.newInstance(list.length, list).show(fm, "govSelectorSheet");
            }
        });

        /* MAX Freq */
        CardView maxFreqCard = v.findViewById(R.id.maxFreqTextCard);
        maxFreqCard.setOnClickListener(new View.OnClickListener() {

            FragmentManager fm = getChildFragmentManager();
            String[] list = dbHelper.getCpuFreqList(0).split(" ");

            @Override
            public void onClick(View v) {
                isMinSelected = false;
                freqSelectorModal.newInstance(list.length, list).show(fm, "freqSelectorSheet");
            }
        });

        /* MIN Freq */
        CardView minFreqCard = v.findViewById(R.id.minFreqTextCard);
        minFreqCard.setOnClickListener(new View.OnClickListener() {

            FragmentManager fm = getChildFragmentManager();
            String[] list = dbHelper.getCpuFreqList(0).split(" ");

            @Override
            public void onClick(View v) {
                isMinSelected = true;
                freqSelectorModal.newInstance(list.length, list).show(fm, "freqSelectorSheet");
            }
        });

        /* Governor Tunables selector listener */
        CardView tunableCard = v.findViewById(R.id.govTunablesCard);
        tunableCard.setOnClickListener(new View.OnClickListener() {

            FragmentManager fm = getChildFragmentManager();
            String[] list = dbHelper.getTunablesList();

            @Override
            public void onClick(View v) {
                tunablesListModal.newInstance(list.length,list).show(fm,"tunablesSheet");
            }
        });
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
                                    //coreFreqFragment.setIsOn(false);
                                } else {
                                    coreFreqFragment.setFreqText(val);
                                    //coreFreqFragment.setIsOn(true);
                                }
                            }
                        };

                        if(!stopCoreFreqThread) {
                            handler.postAtFrontOfQueue(runnable);
                        }
                    }

                    if(stopCoreFreqThread)
                        break;

                    for(int sleepCnt = 0; sleepCnt < sleepMs/sleepMsPauseSegment; ++sleepCnt)
                        if(!stopCoreFreqThread) {
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

    private int getCpuColors(boolean semi)
    {
        SharedPreferences sp = v.getContext().getSharedPreferences("colors",MODE_PRIVATE);
        SharedPreferences.Editor sp_editor = sp.edit();

        if (!semi)
        {
            int color = sp.getInt("cpu",0);
            if(color == 0) {
                sp_editor.putInt("cpu",R.color.colorCPU);
                sp_editor.apply();
                return R.color.colorCPU;
            }
            return color;
        }
        else
        {
            int color = sp.getInt("cpusemi",0);
            if(color == 0) {
                sp_editor.putInt("cpusemi",R.color.colorCPUsec);
                sp_editor.commit();
                return R.color.colorCPUsec;
            }
            return color;
        }
    }

    @Override
    public void onStop()
    {
        setStopCoreFreqThread();
        super.onStop();
    }

    @Override
    public void onItemClickedGov(int position) {
        int cnt;
        String[] list = dbHelper.getCpuGovList(0).split(" ");
        for(cnt = 0 ; cnt < dbHelper.getCpuCoreNum() ; cnt++)
            dbHelper.execChmodWriteCpuFreq("0444","0644",Paths.governorFileName,cnt,list[position]);
        TextView govText = v.findViewById(R.id.govTextView);
        govText.setText(list[position]);
    }

    @Override
    public void onItemClickedFreq(int position) {
        int cnt;
        String filename;
        TextView text;
        String[] list = dbHelper.getCpuFreqList(0).split(" ");
        if(isMinSelected) {
            text = v.findViewById(R.id.minFreqTextView);
            filename = Paths.cpufreq_curMinFreqName;
        }
        else {
            text = v.findViewById(R.id.maxFreqTextView);
            filename = Paths.cpufreq_curMaxFreqName;
        }

        text.setText(list[position]);
        for(cnt = 0 ; cnt < dbHelper.getCpuCoreNum() ; cnt++)
            dbHelper.execChmodWriteCpuFreq("0444","0644",filename,cnt,list[position]);
    }

    @Override
    public void onItemClickedTunable(int position) {

    }
}
