package com.dcc.camera.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.dcc.camera.util.AppLogger;
import com.dcc.camera.util.Constant;
import com.dcc.camera.widget.CameraSurfaceView;
import com.dcc.camera.R;
import com.dcc.camera.util.Utils;

/**
 * @author ding
 *         Created by ding on 29/11/2017.
 */
public class PreviewActivity extends AppCompatActivity {

    private CameraSurfaceView mCameraSurfaceView;
    private Button mBtnCapture;

    private int mOperateType = Constant.KEY_OPERATE_CAPTURE;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppLogger.i("onCreate");

        // 读取intent
        readIntent(getIntent());

        setContentView(R.layout.activity_preview);

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

        mCameraSurfaceView = findViewById(R.id.surface_camera);
        mBtnCapture = findViewById(R.id.btn_capture);

        if (mOperateType == Constant.KEY_OPERATE_CAPTURE) {
            mCameraSurfaceView.setCaptureMode();
            updateBtnText(1);
        } else {
            mCameraSurfaceView.setRecordMode();
            updateBtnText(2);
        }

        mBtnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mOperateType == Constant.KEY_OPERATE_CAPTURE) {
                    captureImage();
                } else if (mOperateType == Constant.KEY_OPERATE_RECORD) {

                    if (mCameraSurfaceView.isRecording()) {
                        updateBtnText(2);

                        mCameraSurfaceView.stopRecord();
                    } else {
                        updateBtnText(3);

                        mCameraSurfaceView.initMediaRecorder();
                    }
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

        if (mCameraSurfaceView != null) {
            mCameraSurfaceView.takePictures(new CameraSurfaceView.CaptureListener() {
                @Override
                public void onShutter() {
                    AppLogger.i("shutter ...");
                }

                @Override
                public void onThumbTaken(byte[] data) {
                    AppLogger.i("onThumbTaken ... ");
                    Utils.printPictureDimens(data);
                }

                @Override
                public void onPictureTaken(byte[] data) {
                    AppLogger.i("onPictureTaken ... ");

                    Utils.printPictureDimens(data);
                    Utils.writeFile(data);
                }
            });
        }
    }

    /**
     * 录制视频
     */
    private void recordVideo() {

        if (mCameraSurfaceView != null) {

            mCameraSurfaceView.initMediaRecorder();
        }
    }


}
