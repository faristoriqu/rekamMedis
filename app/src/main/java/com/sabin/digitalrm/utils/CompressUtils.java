package com.sabin.digitalrm.utils;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class CompressUtils {
    private static final int BUFFER = 80000;
    private static final Logger log = new Logger();

    public boolean zip(Context context, ArrayList<String> _files, String zipFilePath, String zipFileName) {
        log.x("-> zip");
        try {
            if(dirChecker(context, zipFilePath)) {
                BufferedInputStream origin;
                FileOutputStream dest = new FileOutputStream(zipFilePath + "/" + zipFileName);
                ZipOutputStream out = new ZipOutputStream(new BufferedOutputStream(dest));
                byte data[] = new byte[BUFFER];

                for (String file : _files) {
                    File fileObj = new File(file);
                    if(fileObj.isFile()) {
                        log.x("Compressing: " + file);
                        FileInputStream fi = new FileInputStream(file);
                        origin = new BufferedInputStream(fi, BUFFER);

                        ZipEntry entry = new ZipEntry(file.substring(file.lastIndexOf("/") + 1));
                        out.putNextEntry(entry);
                        int count;

                        while ((count = origin.read(data, 0, BUFFER)) != -1) {
                            out.write(data, 0, count);
                        }
                        origin.close();
                    }
                }

                out.close();

                return true;
            }else{
                return false;
            }
        } catch (Exception e) {
            Log.e("[X-DEBUG]", "Error: "+e.getLocalizedMessage());

            return false;
        }
    }

    public boolean unzip(Context context, String _zipFile, String _targetLocation) {

        //create target location folder if not exist
        if(dirChecker(context, _targetLocation)) {
            try {
                FileInputStream fin = new FileInputStream(_zipFile);
                ZipInputStream zin = new ZipInputStream(fin);
                ZipEntry ze = null;
                while ((ze = zin.getNextEntry()) != null) {

                    //create dir if required while unzipping
                    if (ze.isDirectory()) {
                        dirChecker(context, ze.getName());
                    } else {
                        FileOutputStream fout = new FileOutputStream(_targetLocation + ze.getName());
                        for (int c = zin.read(); c != -1; c = zin.read()) {
                            fout.write(c);
                        }

                        zin.closeEntry();
                        fout.close();
                    }

                }
                zin.close();

                return true;
            } catch (Exception e) {
                Log.e("[X-DEBUG]", "Error: " + e.getLocalizedMessage());

                return false;
            }
        }else{
            return false;
        }
    }

    private boolean dirChecker(Context context, String location){
        File filePath = new File(location);
        if (!filePath.exists()) {
            if (!filePath.mkdirs()) {
                Toast.makeText(context, "Save Path Creation Error", Toast.LENGTH_SHORT).show();

                return false;
            }
        }

        return true;
    }

}
