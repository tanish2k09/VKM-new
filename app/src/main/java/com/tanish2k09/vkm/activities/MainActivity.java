package com.tanish2k09.vkm.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.tanish2k09.vkm.R;
import com.tanish2k09.vkm.fragments.SectionFragments.CPUsectionFragment;

import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    private FragmentManager fm;
    private FragmentTransaction ft;
    private Toolbar toolbar;
    private TextView title;

    /* Current Fragment codes :
     * 0 - Home
     * 1 - CPU
     */
    private int currentFragment = 0;

    private void init_bottom_sheet()
    {
        LinearLayout ll = findViewById(R.id.bottom_parent);
        final BottomSheetBehavior<LinearLayout> bsb = BottomSheetBehavior.from(ll);
        ImageButton navButton = findViewById(R.id.NavButton);
        final Animation animFadeOut = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_out);
        final Animation FadeIn = AnimationUtils.loadAnimation(getApplicationContext(), android.R.anim.fade_in);

        animFadeOut.setInterpolator(getApplicationContext(),android.R.interpolator.linear);
        FadeIn.setInterpolator(getApplicationContext(),android.R.interpolator.linear);
        animFadeOut.setDuration(200);
        FadeIn.setDuration(200);
        final Button ultimate = findViewById(R.id.button_ultimate_overlay);

        ultimate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                bsb.setState(BottomSheetBehavior.STATE_COLLAPSED);
            }
        });

        navButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (bsb.getState() == BottomSheetBehavior.STATE_EXPANDED)
                    bsb.setState(BottomSheetBehavior.STATE_COLLAPSED);
                else if (bsb.getState() == BottomSheetBehavior.STATE_COLLAPSED)
                    bsb.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        animFadeOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ultimate.setVisibility(View.GONE);
                ultimate.setEnabled(false);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        FadeIn.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                ultimate.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {}
        });

        bsb.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            int prevState = BottomSheetBehavior.STATE_COLLAPSED;

            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if(newState != prevState)
                {
                    if(newState == BottomSheetBehavior.STATE_COLLAPSED) {
                        if (ultimate.isEnabled())
                            ultimate.startAnimation(animFadeOut);
                        prevState = newState;
                    }
                    else if (newState == BottomSheetBehavior.STATE_EXPANDED)
                    {
                        prevState = newState;
                    }
                    else if (prevState == BottomSheetBehavior.STATE_COLLAPSED)
                    {
                        prevState = BottomSheetBehavior.STATE_DRAGGING;
                        ultimate.setEnabled(true);
                        ultimate.startAnimation(FadeIn);
                    }
                }

            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });
    }

    private void loadCPUfragment()
    {
        Objects.requireNonNull(toolbar).setBackgroundColor(getColor(R.color.colorMaterialYellow));
        title.setText(R.string.cpu);
        ft = fm.beginTransaction();
        CPUsectionFragment cpuSectionFragment = new CPUsectionFragment();
        ft.replace(R.id.main_layout_display,cpuSectionFragment,"cpu");
        ft.commit();
        currentFragment = 1;
    }


    /* onCreate Overview :
     *
     * Set toolbar options
     * Declare necessary vars
     * Prepare fragment actions from intent
     * Initialize our bottom "nav-drawer"
     * Check the parent intent for which fragment to load
     * Load the proper fragment
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        title = findViewById(R.id.toolbar_title);
        Objects.requireNonNull(getSupportActionBar()).setTitle(null);
        Window window = getWindow();
        window.setStatusBarColor(getResources().getColor(android.R.color.black,getTheme()));

        Intent intent = getIntent();
        String nextFragment = intent.getStringExtra("fragment");

        fm = getSupportFragmentManager();

        init_bottom_sheet();

        if (nextFragment.equals("cpu"))
        {
            loadCPUfragment();
        }
    }

    @Override
    protected void onDestroy()
    {
        if(currentFragment == 1)
        {
            CPUsectionFragment CpuSectionFragment = (CPUsectionFragment) fm.findFragmentByTag("cpu");
            CpuSectionFragment.setStopCoreFreqThread();
        }
        super.onDestroy();
    }
}
