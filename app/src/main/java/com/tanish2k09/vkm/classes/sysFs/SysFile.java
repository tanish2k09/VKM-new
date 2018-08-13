package com.tanish2k09.vkm.classes.sysFs;

import android.support.annotation.NonNull;

import com.tanish2k09.vkm.classes.db.Paths;
import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.io.SuFile;
import com.topjohnwu.superuser.io.SuFileInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by Tanish on 2018-05-16.
 *
 * Usage : Create new object, call constructor (provide folder path and filename), call prepareInput and check return value
 * Usage : Then proceed to reading the vkm file using the provided functions
 */

public class SysFile {

    private SuFileInputStream inputStream;

    private byte[] byteRead = new byte[0];

    private int lastReadChars = 0;

    private boolean isPrepared = false;

    private String path, vkm_file_path, fileName, folderPath;
    private static final String VKM_path = Paths.VKM_path;

    public SysFile(String sysPath, String sysFileName )
    {
        fileName = sysFileName;
        folderPath = sysPath;
        path = sysPath + sysFileName;
        vkm_file_path = VKM_path+sysPath.substring(1)+sysFileName;
    }

    public String getVkm_file_path() { return vkm_file_path;}

    public String getFolderPath() { return folderPath; }

    public String getFileName()
    {
        return fileName;
    }

    public String getPath() {
        return path;
    }

    private boolean existsFile()
    {
        if (path.length() > 0)
        {
            SuFile file = new SuFile(path);
            return (file.exists() && file.isFile());
        }
        return false;
    }

    private boolean existsVkmFile()
    {
        if (path.length() > 0)
        {
            SuFile file = new SuFile(vkm_file_path);
            return (file.exists() && file.isFile());
        }
        return false;
    }


    /* PrepareInput :
     * Return -2 on path not found, -1 on copy not found, 0 on success;
     */
    public int prepareInput()
    {
        if(existsFile())
        {
            String command = "mkdir -p " + VKM_path + folderPath.substring(1);
            Shell.Sync.su(command);
            command = "cp -f "+path+" "+vkm_file_path;
            Shell.Sync.su(command);

            if (existsVkmFile()) {
                try {
                    inputStream = new SuFileInputStream(vkm_file_path);
                    isPrepared = true;
                    inputStream.close();
                    return 0;
                } catch (IOException e) {
                    e.printStackTrace(); // We should never reach here.
                }
            }
            return -1; //Sys File exists but copied file doesn't
        }
        return -2; //Sys File doesn't exist
    }

    private void refreshInput()
    {
        String command = "mkdir -p " + VKM_path;
        Shell.Sync.su(command);
        command = "rm -f " + vkm_file_path;
        Shell.Sync.su(command);
        command = "cp -f " + path + " " + vkm_file_path;
        Shell.Sync.su(command);
    }

    public int readInput(int chars, int off)
    {
        if(!isPrepared || (chars == 0))
            return -1; //Input stream not prepared or chars are none

        refreshInput();

        byteRead = new byte[chars];
        try {
            inputStream = new SuFileInputStream(vkm_file_path);
            lastReadChars = inputStream.read(byteRead,off,chars);
            inputStream.close();
            return lastReadChars; //Just to be safe with the exception.
        } catch (IOException e) {
            e.printStackTrace();
        }
        lastReadChars = 0;
        return 0;
    }

    @NonNull
    private String byteToInt(int chars, int off)
    {
        int i;
        StringBuilder num = new StringBuilder("");
        for(i=0;i<chars;i++)
        {
            if ((byteRead[off + i] < 58) && (byteRead[off + i] > 47))
                num.append (((int)byteRead[off+i])-48);
            else
                break;
        }
        return num.toString();
    }

    public String getInputInt(int chars, int off)
    {
        if (chars < 1)
            return "";
        if (off < 0)
            off = 0;
        if((chars + off) > lastReadChars)
        {
            chars = lastReadChars;
            off = 0;
        }
        return byteToInt(chars,off);
    }

    // Line_num : The counter of line to read as per normal counting, not next-gen programming starting-from-zero counting
    public String readLine(int line_num)
    {
        if(!isPrepared || (line_num < 1))
            return ""; //Input stream not prepared or chars are none

        refreshInput();

        String buffer;

        try {
            inputStream = new SuFileInputStream(vkm_file_path);
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            for(int cnt = 0; cnt < line_num - 1; cnt++)
            {
                br.readLine();
            }
            buffer = br.readLine();
            br.close();
            inputStream.close();
            if(buffer != null)
                return buffer;
            else
                return "";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";
    }

    public String readFreqLine(int line_num)
    {
        if(!existsFile())
            return "0";

        if(!isPrepared || (line_num < 1))
            return "-1"; //Input stream not prepared or chars are none

        refreshInput();

        String buffer;

        try {
            inputStream = new SuFileInputStream(vkm_file_path);
            BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
            for(int cnt = 0; cnt < line_num - 1; cnt++)
            {
                br.readLine();
            }
            buffer = br.readLine();
            br.close();
            inputStream.close();
            if(buffer != null)
                return buffer;
            else
                return "0";
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "0";
    }
}
