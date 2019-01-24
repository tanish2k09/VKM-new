package com.tanish2k09.vkm.classes.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

//import com.tanish2k09.vkm.classes.sysFs.SysFile;
import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.io.SuFile;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;


public class fsDatabaseHelper extends SQLiteOpenHelper {

    //private SQLiteDatabase db;
    private static final String DATABASE_NAME = "files.db";
    //public static final String TABLE_CPU = "cpu_table";
    //private static final String TABLE_CPUCOREFREQ = "cpucorefreq_table";
    //private Cursor cursor = null;
    private int cpuCoreNum = 0;

    //private boolean cpufreq_inflated = false;

    private static fsDatabaseHelper dbHelper;

    private fsDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, 1);
        //db = getWritableDatabase();
        //db.execSQL("drop table if exists "+TABLE_CPUCOREFREQ+";");
        //db.execSQL("create table "+TABLE_CPUCOREFREQ+" (CORENUM INTEGER,VAL VARCHAR)");

        //db.execSQL("drop table if exists "+TABLE_CPU+";");
        //db.execSQL("create table "+TABLE_CPU+" (PATH VARCHAR,FILENAME VARCHAR,VAL VARCHAR)");
    }

    public static fsDatabaseHelper getDbHelper(Context ctx)
    {
        if(dbHelper == null)
            dbHelper = new fsDatabaseHelper(ctx);
        return dbHelper;
    }

    /*private void writeWorkingCPUVal(String path, String filename, String val) {
        if(!cpufreq_inflated)
            db.execSQL("INSERT INTO "+TABLE_CPU+" (PATH,FILENAME,VAL) VALUES ('" + path + "','" + filename + "','" + val + "');");
        else
            db.execSQL("UPDATE "+TABLE_CPU+" SET VAL = '"+val+"' WHERE PATH = '"+path+"' AND FILENAME = '"+filename+"';");
    }
    */

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

/*    public String readVal(String path, String filename, String TableName)
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
    */

    public int getCpuCoreNum()
    {
        if(cpuCoreNum != 0)
            return cpuCoreNum;

        try {
            // Might throw IOException
            File file = new File(Paths.cpufreq_folder_prime+"possible");
            FileInputStream stream = new FileInputStream(file);
            BufferedReader br = new BufferedReader(new InputStreamReader(stream));
            // Might throw NumberFormatException
            String inputString = br.readLine();
            Shell.su("echo " + inputString + " > " + Paths.VKM_path + "debug").submit();
            //inputString = inputString.substring(2);

            return 8; //(Integer.parseInt(inputString) + 1);
        } catch (NumberFormatException e) {
            cpuCoreNum = 0;
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
            cpuCoreNum = 0;
        }
        return 0;
    }

    public void execChmodWrite(String prePerm, String writePerm, String folder, String file, String value)
    {
        if(!writePerm.equals("-1"))
            chmodCmd(writePerm, folder, file);
        Shell.su("echo '" + value + "' > " + folder + file).submit();
        if(!prePerm.equals("-1"))
            chmodCmd(prePerm,folder,file);
    }

    public void execChmodWriteCpuFreq(String prePerm, String writePerm, String file,int corenum, String value)
    {
        execChmodWrite(prePerm,writePerm,Paths.cpufreq_folder_prime + "cpu" + corenum + "/cpufreq/", file, value);
    }

    private void chmodCmd(String perm, String folder, String file)
    {
        Shell.su("chmod "+perm+" "+folder+file).submit();
    }

    public String getCurrentCpuGov(int corenum)
    {
        SuFile file = new SuFile(Paths.cpufreq_folder_prime+"cpu"+corenum+"/cpufreq/"+Paths.governorFileName);

        if(file.exists())
        {
            try {
                FileInputStream stream = new FileInputStream(file);
                BufferedReader br = new BufferedReader(new InputStreamReader(stream));
                return br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                return "0";
            }
        }
        return "<Error_NaF>";
    }

    public String getCurrentCpuFreqMaxMin(int corenum, boolean max)
    {
        File file;

        if(max)
            file = new File(Paths.cpufreq_folder_prime+"cpu"+corenum+"/cpufreq/"+Paths.cpufreq_curMaxFreqName);
        else
            file = new File(Paths.cpufreq_folder_prime+"cpu"+corenum+"/cpufreq/"+Paths.cpufreq_curMinFreqName);

        if(file.exists())
        {
            try {
                FileInputStream stream = new FileInputStream(file);
                BufferedReader br = new BufferedReader(new InputStreamReader(stream));
                return br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e("Exception","<IOExc at getCurrentCpuMaxMin with args "+corenum+" "+max);
                return "<Error_IOE>";
            }
        }
        return "<Error_NaF>";
    }
    
    public String getCpuGovList(int corenum)
    {
        SuFile file = new SuFile(Paths.cpufreq_folder_prime+"cpu"+corenum+"/cpufreq/"+Paths.governorListFileName);

        if(file.exists())
        {
            try {
                FileInputStream stream = new FileInputStream(file);
                BufferedReader br = new BufferedReader(new InputStreamReader(stream));
                return br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                return "0";
            }
        }
        return "<Error_NaF>";
    }

    public String[] getTunablesList()
    {
        // TODO : Remove policy0 and make it dynamic by checking the chipset.
        SuFile cpufreqFolder = new SuFile(Paths.cpufreq_folder_prime+"cpufreq/"+getCurrentCpuGov(0));
        //SuFile cpufreqFolder = new SuFile(Paths.cpufreq_folder_prime+"cpufreq/policy0/"+getCurrentCpuGov(0));
        SuFile[] files = cpufreqFolder.listFiles();
        String[] tunablesList = new String[files.length];

        for(int cnt = 0; cnt < files.length; cnt++)
        {
            tunablesList[cnt] = files[cnt].getName();
        }
        return tunablesList;
    }

    public String getCpuTunableValue(String tunable)
    {
        // TODO : Remove policy0 and make it dynamic by checking the chipset.
        //File tunableFile = new File(Paths.cpufreq_folder_prime+"cpufreq/policy0/"+getCurrentCpuGov(0)+"/"+tunable);
        File tunableFile = new File(Paths.cpufreq_folder_prime+"cpufreq/"+getCurrentCpuGov(0)+"/"+tunable);

        if(tunableFile.exists())
        {
            try {
                FileInputStream stream = new FileInputStream(tunableFile);
                BufferedReader br = new BufferedReader(new InputStreamReader(stream));
                return br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                return "0";
            }
        }
        return "<Error : NaF>";
    }
    
    public String getCpuFreqList(int corenum)
    {
        SuFile file = new SuFile(Paths.cpufreq_folder_prime+"cpu"+corenum+"/cpufreq/"+Paths.cpufreq_possibleFreqsFile);

        if(file.exists())
        {
            try {
                FileInputStream stream = new FileInputStream(file);
                BufferedReader br = new BufferedReader(new InputStreamReader(stream));
                return br.readLine();
            } catch (IOException e) {
                e.printStackTrace();
                return "0";
            }
        }
        return "<Error_NaF>";
    }

    public void setupPermissions()
    {
        // Enable read permission for some files in CPU section
        for(int core = 0; core < getCpuCoreNum(); ++core) {
            chmodCmd("o+r", Paths.cpufreq_folder_prime + "cpu" + core + "/cpufreq/", Paths.cpufreq_curMaxFreqName);
            chmodCmd("o+r", Paths.cpufreq_folder_prime + "cpu" + core + "/cpufreq/", Paths.cpufreq_curMinFreqName);
            chmodCmd("o+r", Paths.cpufreq_folder_prime + "cpu" + core + "/cpufreq/", Paths.governorFileName);
        }
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
