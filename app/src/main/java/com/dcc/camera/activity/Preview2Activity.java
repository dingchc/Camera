package com.dcc.camera.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.dcc.camera.R;
import com.dcc.camera.util.AppLogger;
import com.dcc.camera.util.Constant;
import com.dcc.camera.util.Utils;
import com.dcc.camera.widget.Camera2SurfaceView;
import com.dcc.camera.widget.CameraSurfaceView;

/**
 * @author ding
 *         Created by ding on 29/11/2017.
 */
public class Preview2Activity extends AppCompatActivity {

    private Camera2SurfaceView mCamera2SurfaceView;
    private Button mBtnCapture;

    private int mOperateType = Constant.KEY_OPERATE_CAPTURE;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppLogger.i("onCreate");

        // 读取intent
        readIntent(getIntent());

        setContentView(R.layout.activity_preview2);

        initViews();

    }

    /**
     * 读取intent参数
     */
    private void readIntent(Intent intent) {

        if (intent != null) {

            mOperateType = intent.getIntExtra(Constant.KEY_OPERATE, Constant.KEY_OPERATE_CAPTURE);
        }
    }

    /**
     * 初始化控件
     */
    private void initViews() {

        mCamera2SurfaceView = findViewById(R.id.surface_camera);
        mBtnCapture = findViewById(R.id.btn_capture);

        if (mOperateType == Constant.KEY_OPERATE_CAPTURE) {
//            mCamera2SurfaceView.setCaptureMode();
            updateBtnText(1);
        } else {
//            mCamera2SurfaceView.setRecordMode();
            updateBtnText(2);
        }

        mBtnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mOperateType == Constant.KEY_OPERATE_CAPTURE) {
                    captureImage();
                } else if (mOperateType == Constant.KEY_OPERATE_RECORD) {

//                    if (mCamera2SurfaceView.isRecording()) {
//                        updateBtnText(2);
//
//                        mCamera2SurfaceView.stopRecord();
//                    } else {
//                        updateBtnText(3);
//
//                        mCamera2SurfaceView.initMediaRecorder();
//                    }
                }

            }
        });
    }

    private void updateBtnText(int type) {

        String text = "";

        switch (type) {
            case 1:
                text = "拍照";
                break;
            case 2:
                text = "开始录制";
                break;
            case 3:
                text = "停止录制";
                break;
            default:
        }

        mBtnCapture.setText(text);

    }

    /**
     * 拍照
     */
    private void captureImage() {

        if (mCamera2SurfaceView != null) {
            mCamera2SurfaceView.takePictures(new CameraSurfaceView.CaptureListener() {
                @Override
                public void onShutter() {
                    AppLogger.i("shutter ...");
                }

                @Override
                public void onThumbTaken(byte[] data) {
                    AppLogger.i("onThumbTaken ... ");
                    Utils.printPictureDimens(data);
                    Utils.writeFile(data);
                }

                @Override
                public void onPictureTaken(byte[] data) {
                    AppLogger.i("onPictureTaken ... ");

                    Utils.printPictureDimens(data);
                }
            });
        }
    }

    /**
     * 录制视频
     */
    private void recordVideo() {

        if (mCamera2SurfaceView != null) {

//            mCamera2SurfaceView.initMediaRecorder();
        }
    }


}
