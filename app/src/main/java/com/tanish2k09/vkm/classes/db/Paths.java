package com.tanish2k09.vkm.classes.db;

public class Paths {

    /* GENERAL */
    public static final String VKM_path = "/storage/emulated/0/.VKM/fs/";

    /* CPUFREQ */
    public static final String cpufreq_folder_prime = "/sys/devices/system/cpu/";
    public static final String cpufreq_curfreqFileName = "scaling_cur_freq";
    public static final String cpufreq_coreOnlineFile = "/online";
    public static final String cpufreq_possibleFreqsFile = "scaling_available_frequencies";
    public static final String governorListFileName = "scaling_available_governors";
    public static final String governorFileName = "scaling_governor";
    public static final String cpufreq_curMaxFreqName = "scaling_max_freq";
    public static final String cpufreq_curMinFreqName = "scaling_min_freq";

}
