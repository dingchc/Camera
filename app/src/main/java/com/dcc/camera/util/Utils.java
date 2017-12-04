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
        String filePath = getPicturePath();
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

    /**
     * 获取文件输出路径
     * @param type 类型
     * @return 路径
     */
    private static String getOutputFilePath(int type) {
        String filePath = "";

        if (type == 1) {
            filePath = createOutputDir() + System.currentTimeMillis() + ".jpg";
        } else if (type == 2) {
            filePath = createOutputDir() + System.currentTimeMillis() + ".mp4";
        }

        return filePath;
    }

    /**
     * 创建输出路径
     * @return 路径
     */
    private static String createOutputDir() {

        String dirPath = Environment.getExternalStorageDirectory() + File.separator + "my_camera" + File.separator;

        File file = new File(dirPath);

        if (!file.exists()) {
            file.mkdir();
        }

        return dirPath;
    }

    /**
     * 获取图片文件输出路径
     * @return 路径
     */
    public static String getPicturePath() {
        return getOutputFilePath(1);
    }

    /**
     * 获取视频文件输出路径
     * @return 路径
     */
    public static String getVideoPath() {
        return getOutputFilePath(2);
    }
}
