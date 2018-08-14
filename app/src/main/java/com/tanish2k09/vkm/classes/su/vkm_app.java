package com.tanish2k09.vkm.classes.su;

import com.topjohnwu.superuser.BuildConfig;
import com.topjohnwu.superuser.BusyBox;
import com.topjohnwu.superuser.Shell;

/**
 * Created by Tanish on 2018-05-15.
 */

public class vkm_app extends Shell.ContainerApp{
    @Override
    public void onCreate() {
        super.onCreate();
        // Set flags
        Shell.setFlags(Shell.FLAG_REDIRECT_STDERR);
        Shell.verboseLogging(BuildConfig.DEBUG);
        // Use internal busybox
        BusyBox.setup(this);
    }
}
