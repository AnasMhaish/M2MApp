package com.m2mapp.utilities;

import android.content.Context;
import android.os.Environment;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class LoggerService {
    public static void appendLog(String text, String filePath)
    {
        boolean success = false;
        File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/M2M");
        if (!dir.exists()){
            success = dir.mkdir();
        }
        File logFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/M2M"+ filePath);
        if (!logFile.exists())
        {
            try
            {
                logFile.createNewFile();
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        try
        {
            //BufferedWriter for performance, true to set append to file flag
            BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true));
            buf.append(text);
            buf.newLine();
            buf.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
