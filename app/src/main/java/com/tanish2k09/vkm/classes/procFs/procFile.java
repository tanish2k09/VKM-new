package com.tanish2k09.vkm.classes.procFs;

import com.topjohnwu.superuser.Shell;
import com.topjohnwu.superuser.io.SuFile;
import com.topjohnwu.superuser.io.SuFileInputStream;

import java.io.FileNotFoundException;
import java.io.IOException;

import static java.lang.Integer.parseInt;

/**
 * Created by Tanish on 2018-05-15.
 */

public class procFile {

    private SuFileInputStream inputStream;

    private byte[] byteRead = new byte[0];

    private boolean isPrepared = false;

    private int lastReadChars = 0;

    private static final String VKM_path = "/data/media/VKM/fs";
    private String path, vkm_file_path;

    public procFile(String procPath)
    {
        path = procPath;
        vkm_file_path = VKM_path+path;
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

    public int prepareInput()
    {
        if(existsFile())
        {
            String command = "cp -f "+path+" "+vkm_file_path;
            Shell.Sync.su(command);

            if(existsVkmFile())
            {
                try {
                    inputStream = new SuFileInputStream(vkm_file_path);
                    isPrepared = true;
                    inputStream.close();
                    return 0;
                } catch (IOException e) {
                    e.printStackTrace(); // We should never reach here.
                }
            }
            return -1; //Copied file doesn't exist
        }
        return -2; //Proc File doesn't exist
    }

    private void refreshInput()
    {
        String command = "rm -f " + vkm_file_path;
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

    private String byteToInt(int chars, int off)
    {
        int i;
        StringBuilder num = new StringBuilder("");
        for(i=0;i<chars;i++)
        {
            if ((byteRead[off + i] < 58) && (byteRead[off + i] > 47))
                num.append (((int)byteRead[off+i])-48);
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

    public String getPath()
    {
        return path;
    }

    public boolean isPrepared()
    {
        return isPrepared;
    }

}
