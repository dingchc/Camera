package com.dcc.camera;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

/**
 * @author ding
 *         Created by ding on 29/11/2017.
 */
public class PreviewActivity extends AppCompatActivity {

    private CameraSurfaceView mCameraSurfaceView;
    private Button mBtnCapture;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        AppLogger.i("onCreate");

        setContentView(R.layout.activity_preview);

        initViews();

    }

    /**
     * 初始化控件
     */
    private void initViews() {

        mCameraSurfaceView = findViewById(R.id.surface_camera);
        mBtnCapture = findViewById(R.id.btn_capture);

        mBtnCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (mCameraSurfaceView != null) {
                    mCameraSurfaceView.takePictures(new CameraSurfaceView.CaptureListener() {
                        @Override
                        public void onShutter() {
                            AppLogger.i("shutter ...");
                        }

                        @Override
                        public void onPictureTaken(byte[] data) {
                            AppLogger.i("onPictureTaken ... ");

                            Utils.writeFile(data);
                        }
                    });
                }
            }
        });
    }




}
