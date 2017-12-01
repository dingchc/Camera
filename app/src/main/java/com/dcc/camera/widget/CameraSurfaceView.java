package com.dcc.camera.widget;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.media.MediaRecorder;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.dcc.camera.util.AppLogger;
import com.dcc.camera.util.Utils;

import java.util.List;

/**
 * 相机View
 *
 * @author ding
 *         Created by ding on 29/11/2017.
 */

public class CameraSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    /**
     * 默认的预览宽
     */
    private final int DEFAULT_PREVIEW_WIDTH = 480;

    /**
     * 默认的预览高
     */
    private final int DEFAULT_PREVIEW_HEIGHT = 640;

    /**
     * 拍照
     */
    private final int MODE_CAPTURE = 1;

    /**
     * 录像
     */
    private final int MODE_RECORD = 2;

    /**
     * 状态：空闲
     */
    private final int STATE_IDLE = 0;

    /**
     * 状态：正在录制
     */
    private final int STATE_RECORDING = 1;

    /**
     * 状态：出错
     */
    private final int STATE_ERROR = 2;

    private Context mContext;

    /**
     * 相机
     */
    private Camera mCamera;

    private SurfaceHolder mSurfaceHolder;
    private int cameraId = 0;

    /**
     * 预览模式
     * {@link #MODE_CAPTURE}、{@link #MODE_RECORD}
     */
    private int mMode = MODE_CAPTURE;

    /**
     * 录像
     */
    private MediaRecorder mMediaRecorder;

    /**
     * 视频文件输出路径
     */
    private String mVideoOutputPath;

    /**
     * 相册旋转角度
     */
    private int mCameraOrientation = 0;


    /**
     * 状态
     * {@link #STATE_IDLE}、{@link #STATE_RECORDING}
     */
    private int mState = STATE_IDLE;

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

        parameters.set("orientation", "portrait");
        parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);

        Camera.Size sizeInfo = getOptimalSize(DEFAULT_PREVIEW_WIDTH, DEFAULT_PREVIEW_HEIGHT);

        if (sizeInfo != null) {
            AppLogger.i("sizeInfo.width=" + sizeInfo.width + ", sizeInfo.height=" + sizeInfo.height);
            parameters.setPreviewSize(sizeInfo.width, sizeInfo.height);
        }

    }

    /**
     * 设置Surface预览的宽高
     */
    private void setSurfaceViewDimen() {

        int width = getWidth();
        int height = getHeight();

        float rate = (DEFAULT_PREVIEW_WIDTH * 1.0f) / (DEFAULT_PREVIEW_HEIGHT * 1.0f);

        if (height > width) {
            height = (int) (width/ rate);
        } else {
            width = (int) (height / rate);
        }

        this.getLayoutParams().width = width;
        this.getLayoutParams().height = height;

        AppLogger.i("width="+width+", height="+height);
    }

    /**
     * 计算最佳预览尺寸
     * @param w 宽
     * @param h 高
     * @return size
     */
    public Camera.Size getOptimalSize(int w, int h) {

        List<Camera.Size> sizeList = mCamera.getParameters().getSupportedPreviewSizes();

        final double ASPECT_TOLERANCE = 0.1;
        double targetRatio = (double) w / h;

        if (sizeList == null) {
            return null;
        }

        Camera.Size optimalSize = null;

        double minDiff = Double.MAX_VALUE;

        int targetHeight = h;

        for (Camera.Size size : sizeList) {

            if (size.width == w && size.height == h) {
                return size;
            }

            double ratio = (double) size.width / size.height;
            if (Math.abs(ratio - targetRatio) > ASPECT_TOLERANCE)
                continue;
            if (Math.abs(size.height - targetHeight) < minDiff) {
                optimalSize = size;
                minDiff = Math.abs(size.height - targetHeight);
            }
        }

        // Cannot find preview size that matches the aspect ratio, ignore the requirement
        if (optimalSize == null) {
            minDiff = Double.MAX_VALUE;
            for (Camera.Size size : sizeList) {
                if (Math.abs(size.height - targetHeight) < minDiff) {
                    optimalSize = size;
                    minDiff = Math.abs(size.height - targetHeight);
                }
            }
        }
        return optimalSize;
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        AppLogger.i("surfaceCreated");

        // 设置Surface预览的宽高
        setSurfaceViewDimen();

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
     *
     * @param activity 活动
     * @param cameraId 相机id
     * @param camera   相机
     */
    public void setCameraDisplayOrientation(Activity activity, int cameraId, android.hardware.Camera camera) {

        if (camera == null) {
            AppLogger.i("camera is not initialize");
            return;
        }

        android.hardware.Camera.CameraInfo info =
                new android.hardware.Camera.CameraInfo();
        android.hardware.Camera.getCameraInfo(cameraId, info);


        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();

        AppLogger.i("rotation=" + rotation);

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

        AppLogger.i("result=" + result);

        mCameraOrientation = result;

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
            }, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    if (listener != null) {
                        listener.onPictureTaken(data);
                    }
                }
            }, new Camera.PictureCallback() {
                @Override
                public void onPictureTaken(byte[] data, Camera camera) {
                    if (listener != null) {
                        listener.onThumbTaken(data);
                    }
                }
            });
        }
    }

    /**
     * 初始化MediaRecorder
     */
    public void initMediaRecorder() {

        if (this.mMode != MODE_RECORD && mCamera == null) {
            return;
        }

        if (mMediaRecorder == null) {

            mMediaRecorder = new MediaRecorder();

            mCamera.unlock();

            mMediaRecorder.setCamera(mCamera);

//            getSupportSize();

            mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
            mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

            mMediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);

            mMediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mMediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);

            mVideoOutputPath = Utils.getVideoPath();

            mMediaRecorder.setOrientationHint(mCameraOrientation);

            mMediaRecorder.setOutputFile(mVideoOutputPath);

            mMediaRecorder.setVideoFrameRate(30);
            mMediaRecorder.setVideoEncodingBitRate(DEFAULT_PREVIEW_WIDTH * DEFAULT_PREVIEW_HEIGHT * 2);
            mMediaRecorder.setVideoSize(DEFAULT_PREVIEW_WIDTH, DEFAULT_PREVIEW_HEIGHT);

            mMediaRecorder.setPreviewDisplay(mSurfaceHolder.getSurface());

            try {
                mMediaRecorder.prepare();

                mMediaRecorder.start();
                mState = STATE_RECORDING;

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void getSupportSize() {

        if (mCamera != null) {
            List<Camera.Size> sizeList = mCamera.getParameters().getSupportedPreviewSizes();
            for (Camera.Size size : sizeList) {
                AppLogger.i("width=" + size.width + ", height=" + size.height);
            }

        }
    }

    /**
     * 初始化MediaRecorder
     */
    public void stopRecord() {

        if (mMediaRecorder != null) {
            mMediaRecorder.stop();
            mMediaRecorder.release();
        }
    }

    /**
     * 获取视频输出路径
     *
     * @return 输出路径
     */
    public String getVideoOutputPath() {
        return mVideoOutputPath;
    }

    /**
     * 设置模式
     *
     * @param mMode 模式
     */
    private void setMode(int mMode) {
        this.mMode = mMode;
    }

    /**
     * 拍摄模式
     */
    public void setCaptureMode() {
        setMode(MODE_CAPTURE);
    }

    /**
     * 录制模式
     */
    public void setRecordMode() {
        setMode(MODE_RECORD);
    }

    /**
     * 是否是正在录制
     *
     * @return true 是、false 否
     */
    public boolean isRecording() {

        return mMode == MODE_RECORD && mState == STATE_RECORDING;
    }

    /**
     * 回调
     */
    public interface CaptureListener {

        /**
         * 咔嚓
         */
        void onShutter();

        /**
         * 拍照返回
         *
         * @param data 数据
         */
        void onPictureTaken(byte[] data);

        /**
         * 拍照缩略图返回
         *
         * @param data 数据
         */
        void onThumbTaken(byte[] data);

    }
}
