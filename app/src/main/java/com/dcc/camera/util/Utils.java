package com.dcc.camera.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import com.dcc.camera.util.AppLogger;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by ding on 29/11/2017.
 */

public class Utils {

    /**
     * 数据写文件
     *
     * @param data 数据
     */
    public static void writeFile(byte[] data) {

        if (data == null) {
            AppLogger.e("data array is null");
            return;
        }
        String filePath = Environment.getExternalStorageDirectory() + File.separator + System.currentTimeMillis() + ".jpg";
        File file = new File(filePath);

        FileOutputStream fos = null;

        try {
            fos = new FileOutputStream(file);
            fos.write(data);
            fos.flush();

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (fos != null) {
                    fos.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 打印图片的高宽
     * @param data 数据
     */
    public static void printPictureDimens(byte[] data) {

        if (data == null) {

            return;
        }

        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

        if (bitmap != null) {
            AppLogger.i("onPictureTaken ... " + bitmap.getWidth() + ", " + bitmap.getHeight());

            bitmap.recycle();
        }
    }
}
