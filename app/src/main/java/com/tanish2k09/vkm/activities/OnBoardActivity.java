package com.tanish2k09.vkm.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.StatFs;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.CardView;
import android.view.View;
import android.view.Window;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.tanish2k09.vkm.R;
import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.io.SuFile;
import java.lang.ref.WeakReference;
import com.tanish2k09.vkm.classes.db.fsDatabaseHelper;

public class OnBoardActivity extends AppCompatActivity {

    private TextView root_check;
    private TextView venom_check;
    private TextView busybox_text;
    private CardView placeholder_card;
    private ProgressBar bar;
    private TextView mkdir;

    private int color_success;

    private checkVenomAsync cVA;
    private sucheckAsync scA;
    private dbDataInflater dataInflater;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_on_board);

        Window window = getWindow();
        window.setNavigationBarColor(getColor(R.color.colorblack));
        window.setStatusBarColor(getColor(R.color.colorblack));

        StatFs memChecker = new StatFs("/storage/emulated/0/");
        long mbLeft = (memChecker.getAvailableBytes()/1024)/1024;
        if (mbLeft < 1) {
            Toast.makeText(this, "Minimum 1MB free space required. Available : " + memChecker.getAvailableBytes() / 1024 + "KB", Toast.LENGTH_LONG).show();
            finish();
        }

        root_check = findViewById(R.id.root_check);
        venom_check = findViewById(R.id.venom_check);
        busybox_text = findViewById(R.id.busybox_onboard);
        placeholder_card = findViewById(R.id.card_placeholder);
        bar = findViewById(R.id.onboard_progress);
        mkdir = findViewById(R.id.mkdir_text);

        color_success = R.color.colorpass;

        //Asynced sucheck
        scA = new sucheckAsync(this);
        cVA = new checkVenomAsync(this);
        dataInflater = new dbDataInflater(this);

        scA.execute();
    }

    /* Launch() :
     *
     * 0 - Start kernel checking
     * 1 - Inflate database with file infos
     * 2 - Launch next activity (main) providing it with next fragment to prepare and display
     */
    private void launch(int code) {
        if (code == 0)
        {
            cVA.execute();
        }
        else if(code == 1) {
            fsDatabaseHelper dbHelper = fsDatabaseHelper.getDbHelper(this);
            root_check.setTextColor(getResources().getColor(color_success,getTheme()));
            dataInflater.execute(dbHelper);
        }
        else if (code == 2) {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("fragment","cpu");
            this.finish();
            startActivity(intent);
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class sucheckAsync extends AsyncTask<Void, Void, Void> {

        private WeakReference<OnBoardActivity> mRefActivity;
        private boolean got_root = false;

        private sucheckAsync(OnBoardActivity activity) {
            mRefActivity = new WeakReference<>(activity);
        }

        @Override
        protected Void doInBackground(Void... params) {
            Shell.Async.su("cd /sys/..");
            got_root = Shell.rootAccess();
            return null;
        }

        @Override
        protected void onPostExecute(Void params) {
            OnBoardActivity activity = mRefActivity.get();
            if (got_root) {
                activity.root_check.setText(R.string.root_detect_true);
                activity.root_check.setTextColor(getResources().getColor(color_success,getTheme()));
                activity.busybox_text.setTextColor(getResources().getColor(color_success,getTheme()));
                activity.launch(0);
            }
            else {
                activity.root_check.setText(R.string.root_detect_false);
                activity.root_check.setTextColor(getResources().getColor(R.color.colorRed, getTheme()));
                activity.bar.setVisibility(View.GONE);
                activity.placeholder_card.setBackgroundColor(getResources().getColor(R.color.applicationBG, getTheme()));
            }
        }

    }

    @SuppressLint("StaticFieldLeak")
    private class checkVenomAsync extends AsyncTask<Void,Void,Boolean>{

        private WeakReference<OnBoardActivity> mRefActivity;

        private checkVenomAsync(OnBoardActivity activity) {
            mRefActivity = new WeakReference<>(activity);
        }

        @Override
        protected void onPreExecute()
        {
            venom_check.setText(R.string.venom_check);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            SuFile gpu_oc_file = new SuFile("/proc/gpufreq/allow_gpu_oc_700");
            return gpu_oc_file.exists();
        }

        @Override
        protected void onPostExecute(Boolean check) {
            OnBoardActivity activity = mRefActivity.get();
            if(check) {
                activity.venom_check.setTextColor(getResources().getColor(color_success, getTheme()));
                activity.launch(1);
            }
            else{
                activity.venom_check.setText(R.string.non_venom);
                activity.venom_check.setTextColor(getResources().getColor(R.color.colorfail, getTheme()));
            }
        }
    }

    @SuppressLint("StaticFieldLeak")
    private class dbDataInflater extends AsyncTask<fsDatabaseHelper,Void,Void>
    {
        private WeakReference<OnBoardActivity> mRefActivity;

        private dbDataInflater(OnBoardActivity activity)
        {
            mRefActivity = new WeakReference<>(activity);
        }

        @Override
        protected Void doInBackground(fsDatabaseHelper... dbHelperList) {
            dbHelperList[0].getCpuCoreNum();
            return null;
        }

        @Override
        protected void onPostExecute(Void v)
        {
            OnBoardActivity activity = mRefActivity.get();
            activity.mkdir.setTextColor(getResources().getColor(color_success,getTheme()));
            activity.launch(2);
        }
    }

    @Override
    protected void onDestroy()
    {
        scA.cancel(true);
        cVA.cancel(true);
        dataInflater.cancel(true);
        super.onDestroy();
    }
}
