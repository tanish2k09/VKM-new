package com.tanish2k09.vkm.classes.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.tanish2k09.vkm.classes.sysFs.SysFile;
import com.topjohnwu.superuser.Shell;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;


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

    public void scanCPU()
    {
        scanCPUfreq();
    }

    private void scanCPUfreq()
    {
        int retVal;

        SysFile sysFile;

        /* CPU GOVERNOR */
        sysFile= new SysFile(Paths.cpufreq_folder_prime+"cpu0/cpufreq/","scaling_governor");
        retVal = sysFile.prepareInput();
        if(retVal == 0) {
            writeWorkingCPUVal(sysFile.getFolderPath(), sysFile.getFileName(), sysFile.readLine(1));
        }
        else {
            writeWorkingCPUVal(sysFile.getFolderPath(), sysFile.getFileName(), ""+retVal);
        }

        /* LIST OF CPU GOVS */
        sysFile = new SysFile(Paths.cpufreq_folder_prime + "cpu0/cpufreq/",Paths.governorListFileName);
        retVal = sysFile.prepareInput();
        if(retVal == 0) {
            writeWorkingCPUVal(sysFile.getFolderPath(), sysFile.getFileName(), sysFile.readLine(1));
        }
        else {
            writeWorkingCPUVal(sysFile.getFolderPath(), sysFile.getFileName(), ""+retVal);
        }

        /* LIST OF FREQS */
        sysFile = new SysFile(Paths.cpufreq_folder_prime + "cpu0/cpufreq/",Paths.cpufreq_possibleFreqsFile);
        retVal = sysFile.prepareInput();
        if(retVal == 0) {
            writeWorkingCPUVal(sysFile.getFolderPath(), sysFile.getFileName(), sysFile.readLine(1));
        }
        else {
            writeWorkingCPUVal(sysFile.getFolderPath(), sysFile.getFileName(), ""+retVal);
        }

        /* CURRENT MAX FREQ */
        sysFile = new SysFile(Paths.cpufreq_folder_prime + "cpu0/cpufreq/",Paths.cpufreq_curMaxFreqName);
        retVal = sysFile.prepareInput();
        if(retVal == 0) {
            writeWorkingCPUVal(sysFile.getFolderPath(), sysFile.getFileName(), sysFile.readLine(1));
        }
        else {
            writeWorkingCPUVal(sysFile.getFolderPath(), sysFile.getFileName(), ""+retVal);
        }

        /* CURRENT MIN FREQ */
        sysFile = new SysFile(Paths.cpufreq_folder_prime + "cpu0/cpufreq/",Paths.cpufreq_curMinFreqName);
        retVal = sysFile.prepareInput();
        if(retVal == 0) {
            writeWorkingCPUVal(sysFile.getFolderPath(), sysFile.getFileName(), sysFile.readLine(1));
        }
        else {
            writeWorkingCPUVal(sysFile.getFolderPath(), sysFile.getFileName(), ""+retVal);
        }


        /* This MUST remain at the end */
        if(!cpufreq_inflated)
            cpufreq_inflated = true;
    }

    private void writeWorkingCPUVal(String path, String filename, String val) {
        if(!cpufreq_inflated)
            db.execSQL("INSERT INTO "+TABLE_CPU+" (PATH,FILENAME,VAL) VALUES ('" + path + "','" + filename + "','" + val + "');");
        else
            db.execSQL("UPDATE "+TABLE_CPU+" SET VAL = '"+val+"' WHERE PATH = '"+path+"' AND FILENAME = '"+filename+"';");
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

    public String readCpuCoreFreqVal(int corenum)
    {
        File coreFile = new File(Paths.cpufreq_folder_prime+"cpu"+corenum+"/cpufreq/"+Paths.cpufreq_curfreqFileName);
        if(coreFile.exists())
        {
            try {
                FileInputStream stream = new FileInputStream(coreFile);
                BufferedReader br = new BufferedReader(new InputStreamReader(stream));
                return br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                return "0";
            }
        }
        return "0";
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

        SysFile cpuCoreNumFile = new SysFile(Paths.cpufreq_folder_prime,"possible");
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

    public void execChmodWrite(String prePerm, String writePerm, String folder, String file, String value)
    {
        if(!writePerm.equals("-1"))
            chmodCmd(writePerm, folder, file);
        Shell.Sync.su("echo '" + value + "' > " + folder + file);
        if(!prePerm.equals("-1"))
            chmodCmd(prePerm,folder,file);
    }

    public void execChmodWriteCpu(String prePerm, String writePerm, String file,int corenum, String value)
    {
        execChmodWrite(prePerm,writePerm,Paths.cpufreq_folder_prime + "cpu" + corenum + "/cpufreq/", file, value);
    }

    private void chmodCmd(String perm, String folder, String file)
    {
        Shell.Sync.su("chmod "+perm+" "+folder+file);
    }











    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
