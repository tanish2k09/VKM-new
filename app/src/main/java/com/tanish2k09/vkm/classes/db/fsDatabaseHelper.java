package com.tanish2k09.vkm.classes.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tanish2k09.vkm.classes.sysFs.sysFile;


public class fsDatabaseHelper extends SQLiteOpenHelper {

    private SQLiteDatabase db;
    private static final String DATABASE_NAME = "files.db";
    public static final String TABLE_CPU = "cpu_table";
    public static final String TABLE_CPUCOREFREQ = "cpucorefreq_table";
    private Cursor cursor = null;
    private int cpuCoreNum = 0;

    private boolean cpufreq_inflated = false;
    private boolean cpuCoreFreq_inflated = false;

    private static fsDatabaseHelper dbHelper;

    private fsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        db = getWritableDatabase();
        db.execSQL("drop table if exists "+TABLE_CPUCOREFREQ+";");
        db.execSQL("create table "+TABLE_CPUCOREFREQ+" (CORENUM INTEGER,VAL VARCHAR)");

        db.execSQL("drop table if exists "+TABLE_CPU+";");
        db.execSQL("create table "+TABLE_CPU+" (PATH VARCHAR,FILENAME VARCHAR,VAL VARCHAR)");
    }

    public static fsDatabaseHelper getDbHelper(Context ctx)
    {
        if(dbHelper == null)
            dbHelper = new fsDatabaseHelper(ctx);
        return dbHelper;
    }

    public void scanAll()
    {
        scanCPU();
    }

    public void scanCPU()
    {
        scanCPUfreq();
    }

    private void scanCPUfreq()
    {
        int retVal;

        sysFile cpuGovernor = new sysFile(Paths.cpufreq_folder_prime+"cpu0/cpufreq/","scaling_governor");
        retVal = cpuGovernor.prepareInput();
        if(retVal == 0) {
            writeWorkingCPUVal(cpuGovernor.getFolderPath(), cpuGovernor.getFileName(), cpuGovernor.readLine(1));
        }
        else {
            writeWorkingCPUVal(cpuGovernor.getFolderPath(), cpuGovernor.getFileName(), ""+retVal);
        }

        sysFile cpuGovListFile = new sysFile(Paths.cpufreq_folder_prime + "cpu0/",Paths.governorListFileName);
        retVal = cpuGovListFile.prepareInput();
        if(retVal == 0) {
            writeWorkingCPUVal(cpuGovListFile.getFolderPath(),cpuGovListFile.getFileName(),cpuGovListFile.readLine(1));
        }
        else {
            writeWorkingCPUVal(cpuGovListFile.getFolderPath(),cpuGovListFile.getFileName(),""+retVal);
        }

        if(!cpufreq_inflated)
            cpufreq_inflated = true;
    }

    public void scanCPUCoreFreqs()
    {
        int cnt;
        int retval;
        for (cnt = 0; cnt < getCpuCoreNum(); cnt++) {
            int isCurrentCoreOn;
            sysFile coreOn = new sysFile(Paths.cpufreq_folder_prime + "cpu" + cnt + "/", Paths.cpufreq_coreOnlineFileName);
            retval = coreOn.prepareInput();
            if (retval == 0) {
                coreOn.readInput(1, 0);
                isCurrentCoreOn = Integer.parseInt(coreOn.getInputInt(1, 0));
                if (isCurrentCoreOn == 1) {
                    sysFile coreFreq = new sysFile(Paths.cpufreq_folder_prime + "cpu" + cnt + "/cpufreq/", Paths.cpufreq_freqFileName);
                    retval = coreFreq.prepareInput();
                    if (retval == 0) {
                        writeWorkingCPUCoreFreqVal(cnt, coreFreq.readFreqLine(1));
                    }
                } else {
                    writeWorkingCPUCoreFreqVal(cnt, "0");
                }
            }
        }

        if(!cpuCoreFreq_inflated)
            cpuCoreFreq_inflated = true;
    }

    private void writeWorkingCPUVal(String path, String filename, String val) {
        if(!cpufreq_inflated)
            db.execSQL("INSERT INTO "+TABLE_CPU+" (PATH,FILENAME,VAL) VALUES ('" + path + "','" + filename + "','" + val + "');");
        else
            db.execSQL("UPDATE "+TABLE_CPU+" SET VAL = '"+val+"' WHERE PATH = '"+path+"' AND FILENAME = '"+filename+"';");
    }

    private void writeWorkingCPUCoreFreqVal(int corenum, String val)
    {
        if(!cpuCoreFreq_inflated)
            db.execSQL("INSERT INTO "+TABLE_CPUCOREFREQ+" (CORENUM,VAL) VALUES ("+corenum+",'"+val+"');");
        else
            db.execSQL("UPDATE "+TABLE_CPUCOREFREQ+" SET VAL = '"+val+"' WHERE CORENUM = "+corenum+";");
    }

    public String readCoreFreqVal(int corenum,String Tablename)
    {
        String val = "";
        cursor = db.rawQuery("select VAL from "+Tablename+" where CORENUM = " + corenum +";", null);
        if (cursor != null)
        {
            if (cursor.moveToFirst())
            {
                val = cursor.getString(cursor.getColumnIndex("VAL"));
            }
            cursor.close();
        }
        return val;
    }

    public String readVal(String path, String filename, String TableName)
    {
        String val = "";
        cursor = db.rawQuery("select VAL from "+TableName+" where PATH = '" + path + "' AND FILENAME = '"+filename+"';", null);
        if (cursor != null)
        {
            if (cursor.moveToFirst())
            {
                val = cursor.getString(cursor.getColumnIndex("VAL"));
            }
            cursor.close();
        }
        return val;
    }

    public int getCpuCoreNum()
    {
        if(cpuCoreNum != 0)
            return cpuCoreNum;

        sysFile cpuCoreNumFile = new sysFile(Paths.cpufreq_folder_prime,"possible");
        int retVal = cpuCoreNumFile.prepareInput();
        if(retVal == 0)
        {
            String inputString = cpuCoreNumFile.readLine(1);
            inputString = inputString.substring(2);
            try {
                return (Integer.parseInt(inputString) + 1);
            } catch (NumberFormatException e)
            {
                cpuCoreNum = 1;
                e.printStackTrace();
            }
        }
        return 1;
    }











    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
