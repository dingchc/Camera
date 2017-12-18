package com.dcc.camera.util;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;


import com.dcc.camera.MSApplication;

import java.lang.reflect.Method;

/**
 * 权限检查
 * Created by ding on 1/12/17.
 */

public class MPermissionUtil {

    /**
     * 权限及请求Code对应表
     */
    public enum PermissionRequest {

        // 相机
        CAMERA(1001, Manifest.permission.CAMERA),
        // 录音
        AUDIO_RECORD(1002, Manifest.permission.RECORD_AUDIO),
        // 打电话及获取电话状态
        PHONE(1003, Manifest.permission.READ_PHONE_STATE, Manifest.permission.CALL_PHONE),
        // 读取设备编号
        READ_PHONE_STATE(1004, Manifest.permission.READ_PHONE_STATE),
        // 读写存储卡
        READ_WRITE_STORAGE(1005, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
        // 读短信
        READ_SMS(1006, Manifest.permission.READ_SMS),
        // 视频
        VIDEO(1007, Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA),
        // 位置
        LOCATION(1008, Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
        // 保存图片
        SAVE_IMAGE(1009, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        private int requestCode;
        private String[] permissions;

        PermissionRequest(int requestCode, String... permissions) {

            this.permissions = permissions;
            this.requestCode = requestCode;
        }

        public int getRequestCode() {
            return requestCode;
        }

        public String[] getPermissions() {
            return permissions;
        }
    }

    /**
     * 判断是否权限已授权
     *
     * @param permissionRequest 权限
     * @param context           活动
     * @return true 已授权、 false 未授权
     */
    public static boolean checkPermission(Context context, PermissionRequest permissionRequest) {

        boolean ret;

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || permissionRequest == null || context == null) {

            AppLogger.i(AppLogger.TAG, "permission don't check");
            return true;
        }

        // 判断是否已经获取全部授权
        if (!isAllPermissionsGranted(context, permissionRequest.getPermissions())) {

            requestPermission(context, permissionRequest);

            ret = false;
        } else {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                ret = isOpsGranted(permissionRequest);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                return isOpsGranted(permissionRequest);
            } else {
                ret = true;
                AppLogger.i(AppLogger.TAG, "permission granted");
            }
        }

        return ret;

    }

    /**
     * 判断是否已经获得权限的授权
     *
     * @param context     活动
     * @param permissions 权限
     * @return true 已授权、 false 未授权
     */
    private static boolean isAllPermissionsGranted(Context context, String[] permissions) {

        if (permissions == null || permissions.length <= 0) {
            return true;
        }

        for (String permission : permissions) {

            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {

                return false;
            }
        }

        return true;

    }

    /**
     * 申请权限授权
     *
     * @param context           上下文（这里特指Activity）
     * @param permissionRequest 权限请求
     */
    protected static void requestPermission(Context context, PermissionRequest permissionRequest) {

        AppLogger.i(AppLogger.TAG, "requestPermission");

        if (context instanceof Activity) {
            ActivityCompat.requestPermissions((Activity) context, permissionRequest.getPermissions(), permissionRequest.getRequestCode());
        }

    }

    /**
     * 判断是否全部权限已获取
     *
     * @param grantResults 权限结果
     * @return true 已授权、false 未授权
     */
    public static boolean hasAllPermissionsGranted(int[] grantResults) {

        if (grantResults != null && grantResults.length > 0) {
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    return false;
                }
            }
        } else {
            return false;
        }

        return true;
    }

    /**
     * 跳转到系统应用详情页面，方便用户设定权限
     *
     * @param context 上下文
     */
    public static void goSettingAppDetail(Context context) {

        if (context != null) {
            Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
            intent.setData(Uri.parse("package:" + Utils.getPackageName(context)));
            context.startActivity(intent);
        }

    }

    /**
     * 是否系统的OPS权限允许
     *
     * @param permissionRequest 权限
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean isOpsGranted(PermissionRequest permissionRequest) {

        if (permissionRequest != null) {
            String[] permissionRequestArray = permissionRequest.getPermissions();

            if (permissionRequestArray != null) {

                for (String permission : permissionRequestArray) {

                    boolean ret = checkOpsPermission(permission);

                    if (!ret) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    /**
     * 是否系统的OPS权限允许
     *
     * @param permission 权限名称
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static boolean checkOpsPermission(String permission) {

        Context context = MSApplication.getMSApp();

        if (context != null) {
            AppOpsManager opsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);

            if (opsManager != null) {

                AppLogger.i("** permission=" + permission);

                String permissionCode = null;

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    permissionCode = getOpsCode(permission);
                } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M && Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    permissionCode = getOpsCodeKit(permission);
                }

                int result = 0;

                if (permissionCode != null) {
                    result = opsManager.checkOp(permissionCode, Binder.getCallingUid(), Utils.getPackageName(context));
                }

                AppLogger.i("** result=" + result);
                return AppOpsManager.MODE_ALLOWED == result;
            }
        }

        return true;
    }

    /**
     * 获取AppOps的code
     *
     * @param permission 权限名称
     * @return AppOps的code
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static String getOpsCode(String permission) {

        try {

            if (TextUtils.isEmpty(permission)) {
                return null;
            }

            Context context = MSApplication.getMSApp();

            AppOpsManager opsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);

            if (opsManager != null) {
                Method method = opsManager.getClass().getMethod("permissionToOp", String.class);
                return method != null ? (String) method.invoke(opsManager, permission) : null;
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * 获取AppOps的code
     *
     * @param permission 权限名称
     * @return AppOps的code
     */
    @TargetApi(Build.VERSION_CODES.KITKAT)
    private static String getOpsCodeKit(String permission) {

        try {

            if (TextUtils.isEmpty(permission)) {
                return null;
            }

            Context context = MSApplication.getMSApp();

            AppOpsManager opsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);

            if (opsManager != null) {

                Method method = opsManager.getClass().getMethod("permissionToOpCode", String.class);

                if (method != null) {
                    Integer codeInt = (Integer) method.invoke(opsManager, permission);

                    if (codeInt != null) {
                        Method method1 = opsManager.getClass().getMethod("opToName", Integer.TYPE);
                        return method1 != null ? (String) method1.invoke(opsManager, codeInt) : null;
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
