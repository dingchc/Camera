package com.dcc.camera;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;

/**
 * 相机View
 *
 * @author ding
 *         Created by ding on 29/11/2017.
 */
public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private Camera mCamera;
    private SurfaceHolder mSurfaceHolder;
    private Context mContext;
    private int cameraId = 0;

    public CameraSurfaceView(Context context) {
        this(context, null);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CameraSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        AppLogger.i("CameraSurfaceView");

        this.mContext = context;

        initCamera();

        mSurfaceHolder = this.getHolder();

        mSurfaceHolder.addCallback(this);

    }

    /**
     * 初始化相机
     */
    private void initCamera() {

        AppLogger.e("init camera");

        int cameraCnt = Camera.getNumberOfCameras();

        if (cameraCnt > 0) {

            cameraId = 0;
            this.mCamera = openCamera(cameraId);
        }

        if (this.mCamera == null) {

            AppLogger.e("camera cannot create");
            return;
        }

        // 配置相机
        configCamera();

        // 设置相机方向
        setCameraDisplayOrientation((Activity) mContext, cameraId, mCamera);

    }

    /**
     * 打开相机
     *
     * @param cameraId 相机id
     */
    private Camera openCamera(int cameraId) {

        Camera camera = null;
        try {
            camera = Camera.open(cameraId);
        } catch (Exception e) {
            AppLogger.i("open mCamera failed " + e.getMessage());
        }

        return camera;
    }

    /**
     * 配置相机
     */
    private void configCamera() {

        final Camera.Parameters parameters = mCamera.getParameters();

        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        AppLogger.i("surfaceCreated");

        startPreview();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        AppLogger.i("format=" + format + ", width =" + width + ", height=" + height);

        // 设置相机方向
        setCameraDisplayOrientation((Activity) mContext, cameraId, mCamera);

        stopPreview();

        startPreview();

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        AppLogger.i("surfaceDestroyed");
        stopPreview();

        // 释放相机
        releaseCamera();

    }

    /**
     * 释放相机
     */
    private void releaseCamera() {
        if (mCamera != null) {
            mCamera.release();
        }
    }

    /**
     * 开始预览
     */
    private void startPreview() {

        if (mCamera != null) {

            AppLogger.i("start preview ...");
            try {
                mCamera.setPreviewDisplay(mSurfaceHolder);
                mCamera.startPreview();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 停止预览
     */
    private void stopPreview() {

        if (mCamera != null) {

            AppLogger.i("stop preview ...");

            mCamera.stopPreview();
        }
    }

    /**
     * 设置相机方向
     * @param activity 活动
     * @param cameraId 相机id
     * @param camera 相机
     */
    public static void setCameraDisplayOrientation(Activity activity, int cameraId, android.hardware.Camera camera) {

        if (camera == null) {
            AppLogger.i("camera is not initialize");
            return;
        }

        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);


        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();

        AppLogger.i("rotation="+rotation);

        int degrees = 0;

        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
            default:
        }

        int result;

        // front
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            // compensate the mirror
            result = (360 - result) % 360;
        }
        // back-facing
        else {
            result = (info.orientation - degrees + 360) % 360;
        }

        AppLogger.i("result="+result);

        camera.setDisplayOrientation(result);
    }

    /**
     * 拍照
     */
    public void takePictures(final CaptureListener listener) {

        if (mCamera != null) {
            mCamera.takePicture(new Camera.ShutterCallback() {
                @Override
                public void onShutter() {

                    if (listener != null) {
                        listener.onShutter();
                    }
                }
            }, null, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    if (listener != null) {
                        listener.onPictureTaken(data);
                    }
                }
            });
        }
    }

    /**
     * 回调
     */
    static interface CaptureListener {

        /**
         * 咔嚓
         */
        void onShutter();

        /**
         * 拍照返回
         * @param data 数据
         */
        void onPictureTaken(byte[] data);

    }
}
