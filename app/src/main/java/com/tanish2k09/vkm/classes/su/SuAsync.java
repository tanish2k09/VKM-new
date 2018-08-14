package com.tanish2k09.vkm.classes.su;

import android.os.AsyncTask;

import com.topjohnwu.superuser.Shell;

/**
 * Created by Tanish on 2018-05-16.
 */

public class SuAsync extends AsyncTask<String, Void, Void> {

    @Override
    protected Void doInBackground(String... cmds) {
        int i = cmds.length;
        int count;
        for(count = 0; count < i; count++)
            Shell.Async.su(cmds[count]);
        return null;
    }
}
