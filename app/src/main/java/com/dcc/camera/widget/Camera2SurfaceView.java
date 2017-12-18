package com.dcc.camera.widget;

import android.annotation.TargetApi;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;

import com.dcc.camera.util.AppLogger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author ding
 *         Created by ding on 18/12/2017.
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class Camera2SurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    /**
     * 相机
     */
    private CameraDevice mCamera;

    /**
     * 默认的预览宽
     */
    private final int DEFAULT_PREVIEW_WIDTH = 640;

    /**
     * 默认的预览高
     */
    private final int DEFAULT_PREVIEW_HEIGHT = 480;

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

    private CameraCaptureSession mSession;

    private CaptureRequest mCaptureRequest;


    public Camera2SurfaceView(Context context) {
        this(context, null);
    }

    public Camera2SurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Camera2SurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.mContext = context;

        this.mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);

    }

    /**
     * 初始化
     */
    private void initCamera() {

        CameraManager cameraManager = (CameraManager) getContext().getSystemService(Context.CAMERA_SERVICE);
        try {
            String[] cameraIds = cameraManager.getCameraIdList();

            String backId = "";

            for (String id : cameraIds) {

                CameraCharacteristics characteristics = cameraManager.getCameraCharacteristics(id);

                Integer facing = characteristics.get(CameraCharacteristics.LENS_FACING);

                if (facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    continue;
                }

                StreamConfigurationMap configurationMap = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);

                if (configurationMap == null) {
                    continue;
                }

                AppLogger.i("id= " + id);

                Boolean isFlashAvailable = characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE);

                AppLogger.i("isFlashAvailable= " + isFlashAvailable);

                backId = id;
            }

            cameraManager.openCamera(backId, new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull CameraDevice camera) {
                    AppLogger.i("onOpened");
                    mCamera = camera;

                    startPreview();
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice camera) {
                    AppLogger.i("onDisconnected");
                    mCamera.close();
                    mCamera = null;
                }

                @Override
                public void onError(@NonNull CameraDevice camera, int error) {
                    AppLogger.i("error");
                    mCamera.close();
                    mCamera = null;

                }
            }, mHandler);

        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Handler mHandler = new Handler(Looper.myLooper()) {

        @Override
        public void handleMessage(Message msg) {

            AppLogger.i("msg.what=" + msg.what);
        }
    };

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        AppLogger.i("surfaceCreated");

        // 设置Surface预览的宽高
        setSurfaceViewDimen();

        initCamera();

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

    }

    /**
     * 设置Surface预览的宽高
     */
    private void setSurfaceViewDimen() {

        int width = getWidth();
        int height = getHeight();

        float rate = (DEFAULT_PREVIEW_WIDTH * 1.0f) / (DEFAULT_PREVIEW_HEIGHT * 1.0f);

        if (height > width) {
            height = (int) (width * rate);
        } else {
            width = (int) (height / rate);
        }

        ViewGroup.LayoutParams lp = this.getLayoutParams();
        lp.width = width;
        lp.height = height;

        this.setLayoutParams(lp);

        AppLogger.i("width=" + width + ", height=" + height);
    }

    /**
     * 开始预览
     */
    private void startPreview() {

        if (mCamera != null) {

            AppLogger.i("start preview ...");
            try {

                final CaptureRequest.Builder builder = mCamera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                builder.addTarget(mSurfaceHolder.getSurface());

                mCamera.createCaptureSession(Collections.singletonList(mSurfaceHolder.getSurface()), new CameraCaptureSession.StateCallback() {
                    @Override
                    public void onConfigured(@NonNull CameraCaptureSession session) {

                        AppLogger.i("onConfigured " + session);
                        try {

                            builder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                            mCaptureRequest = builder.build();
                            mSession = session;

                            mSession.setRepeatingRequest(mCaptureRequest, null, null);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onConfigureFailed(@NonNull CameraCaptureSession session) {
                        AppLogger.i("onConfigureFailed " + session);
                    }
                }, null);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 拍照
     */
    public void takePictures(final CameraSurfaceView.CaptureListener listener) {

        if (mSession != null && mCaptureRequest != null) {

            try {
                mSession.capture(mCaptureRequest, new CameraCaptureSession.CaptureCallback() {
                    @Override
                    public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                        super.onCaptureCompleted(session, request, result);

                        AppLogger.i("size=" + result.getPartialResults().size());
                    }
                }, mHandler);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
