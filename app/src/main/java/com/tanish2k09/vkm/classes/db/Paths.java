package com.tanish2k09.vkm.classes.db;

public class Paths {

    /* GENERAL */
    public static final String VKM_path = "/storage/emulated/0/.VKM/fs/";

    /* CPUFREQ */
    public static final String cpufreq_folder_prime = "/sys/devices/system/cpu/";
    //public static final String cpufreq_freqFile = "/scaling_cur_freq";
    public static final String cpufreq_freqFileName = "scaling_cur_freq";
    public static final String cpufreq_coreOnlineFile = "/online";
    public static final String cpufreq_coreOnlineFileName = "online";
    public static final String governorListFileName = "scaling_available_governors";

}
