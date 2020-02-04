package com.test.pixman.utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;

public class GenericFileProvider extends FileProvider {


    public static void store(Bitmap bitmap, String fileName, Activity activity) {
//        final String dirPath = ;
//        File dir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES));
//        if (!dir.exists())
//            dir.mkdirs();
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), fileName);
        try {
            FileOutputStream fOut = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, fOut);
            fOut.flush();
            fOut.close();

            Toast.makeText(activity, "Image Saved", Toast.LENGTH_SHORT).show();

            Uri uri = FileProvider.getUriForFile(activity,
                    activity.getApplicationContext().getPackageName() + ".com.test.pixman.provider", file);
//            showNotification(activity, uri, bitmap);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
