package com.dcc.camera.activity;

import android.app.AppOpsManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;

import com.dcc.camera.R;
import com.dcc.camera.util.AppLogger;
import com.dcc.camera.util.Constant;
import com.dcc.camera.util.MPermissionUtil;

public class MainActivity extends AppCompatActivity {

    private Button mBtnCameraCapture, mBtnCameraRecord;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mBtnCameraCapture = findViewById(R.id.btn_camera_capture);
        mBtnCameraRecord = findViewById(R.id.btn_camera_record);

        initEvent();
    }

    /**
     * 初始化事件
     */
    private void initEvent() {

        mBtnCameraCapture.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!MPermissionUtil.checkPermission(MainActivity.this, MPermissionUtil.PermissionRequest.CAMERA)) {
                    return;
                }

                gotoCapture();

            }
        });

        mBtnCameraRecord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!MPermissionUtil.checkPermission(MainActivity.this, MPermissionUtil.PermissionRequest.VIDEO)) {
                    return;
                }

                gotoRecord();

            }
        });

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();

                MPermissionUtil.checkPermission(MainActivity.this, MPermissionUtil.PermissionRequest.CAMERA);

            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        onPermissionsResult(requestCode, permissions, grantResults);
    }

    /**
     * 处理权限请求的返回
     *
     * @param requestCode  请求code
     * @param permissions  权限（暂时保留）
     * @param grantResults 权限结果
     */
    public void onPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {

        AppLogger.i(AppLogger.TAG, "requestCode=" + requestCode);

        // 拍照
        if (requestCode == MPermissionUtil.PermissionRequest.CAMERA.getRequestCode()) {

            // 授权失败
            if (!MPermissionUtil.hasAllPermissionsGranted(grantResults)) {

                return;
            }
        }
        // 录音
        else if (requestCode == MPermissionUtil.PermissionRequest.AUDIO_RECORD.getRequestCode()) {

            // 授权失败
            if (!MPermissionUtil.hasAllPermissionsGranted(grantResults)) {

                return;
            }
        }
        // 电话
        else if (requestCode == MPermissionUtil.PermissionRequest.PHONE.getRequestCode()) {
            // 授权失败
            if (!MPermissionUtil.hasAllPermissionsGranted(grantResults)) {

                return;
            }
        }
        // 读写存储设备
        else if (requestCode == MPermissionUtil.PermissionRequest.READ_WRITE_STORAGE.getRequestCode()) {
            // 授权失败
            if (!MPermissionUtil.hasAllPermissionsGranted(grantResults)) {
                return;
            }
        }
        // 读短信
        else if (requestCode == MPermissionUtil.PermissionRequest.READ_SMS.getRequestCode()) {
            // 授权失败
            if (!MPermissionUtil.hasAllPermissionsGranted(grantResults)) {
                return;
            }
        }
        // 视频
        else if (requestCode == MPermissionUtil.PermissionRequest.VIDEO.getRequestCode()) {
            // 授权失败
            if (!MPermissionUtil.hasAllPermissionsGranted(grantResults)) {

                return;
            }
        }
        // 位置
        else if (requestCode == MPermissionUtil.PermissionRequest.LOCATION.getRequestCode()) {
            // 授权失败
            if (!MPermissionUtil.hasAllPermissionsGranted(grantResults)) {

                return;
            }
        }
        // 保存图片
        else if (requestCode == MPermissionUtil.PermissionRequest.SAVE_IMAGE.getRequestCode()) {

            // 授权失败
            if (!MPermissionUtil.hasAllPermissionsGranted(grantResults)) {
                return;
            }
        }

        // 到最后，代表权限已获得
        onPermissionGranted(requestCode);
    }

    /**
     * 权限获得
     *
     * @param requestCode 请求code
     */
    protected void onPermissionGranted(int requestCode) {

        // 拍照
        if (requestCode == MPermissionUtil.PermissionRequest.CAMERA.getRequestCode()) {
            AppLogger.i("CAMERA");

            gotoCapture();
        }
        // 相册
        else if (requestCode == MPermissionUtil.PermissionRequest.READ_WRITE_STORAGE.getRequestCode()) {
            AppLogger.i("READ_WRITE_STORAGE");
        }
        // 保存图片-读写权限
        else if (requestCode == MPermissionUtil.PermissionRequest.SAVE_IMAGE.getRequestCode()) {

            AppLogger.i("SAVE_IMAGE");
        }

    }

    private void gotoCapture() {

        Intent intent;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            intent = new Intent(MainActivity.this, PreviewActivity.class);
        } else {
            intent = new Intent(MainActivity.this, Preview2Activity.class);
        }
        intent.putExtra(Constant.KEY_OPERATE, Constant.KEY_OPERATE_CAPTURE);
        startActivity(intent);
    }

    private void gotoRecord() {

        Intent intent = new Intent(MainActivity.this, PreviewActivity.class);
        intent.putExtra(Constant.KEY_OPERATE, Constant.KEY_OPERATE_RECORD);
        startActivity(intent);
    }
}
