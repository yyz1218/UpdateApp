package com.zzdj.esports.android.updateapp.utils;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import java.util.ArrayList;

/**
 * author yaoyaozhong // FIXME: 2018/8/30
 * permissionUtils工具类
 */
public class MyPermissionUtil {

    public static PermissionObject with(AppCompatActivity activity) {
        return new PermissionObject(activity);
    }

    public static PermissionObject with(Fragment fragment) {
        return new PermissionObject(fragment);
    }

    public static class PermissionObject {

        private AppCompatActivity mActivity;
        private Fragment mFragment;

        PermissionObject(AppCompatActivity activity) {
            mActivity = activity;
        }

        PermissionObject(Fragment fragment) {
            mFragment = fragment;
        }

        public int has(String permissionName) {
            int permissionCheck;//0同意 1拒绝后不再显示 -1拒绝
            if (mActivity != null) {
                permissionCheck = ContextCompat.checkSelfPermission(mActivity, permissionName);
                if(permissionCheck==-1){
                    boolean hasrefuse = ActivityCompat.shouldShowRequestPermissionRationale(mActivity, permissionName);
                    if(hasrefuse){
                        permissionCheck=-1;
                    }else{
                        permissionCheck=1;
                    }
                }
            } else {
                permissionCheck = ContextCompat.checkSelfPermission(mFragment.getContext(), permissionName);
                if(permissionCheck==-1){
                    boolean hasrefuse = ActivityCompat.shouldShowRequestPermissionRationale((Activity) mFragment.getContext(), permissionName);
                    if(hasrefuse){
                        permissionCheck=-1;
                    }else{
                        permissionCheck=1;
                    }
                }
            }

            return permissionCheck;
        }

        public PermissionRequestObject request(String permissionName) {
            if (mActivity != null) {
                return new PermissionRequestObject(mActivity, new String[]{permissionName});
            } else {
                return new PermissionRequestObject(mFragment, new String[]{permissionName});
            }
        }

        public PermissionRequestObject request(String... permissionNames) {
            if (mActivity != null) {
                return new PermissionRequestObject(mActivity, permissionNames);
            } else {
                return new PermissionRequestObject(mFragment, permissionNames);
            }
        }
    }

    static public class PermissionRequestObject {

        private static final String TAG = PermissionObject.class.getSimpleName();
        private AppCompatActivity mActivity;
        private Func mDenyFunc;
        private Fragment mFragment;
        private Func mGrantFunc;
        private String[] mPermissionNames;
        private ArrayList<SinglePermission> mPermissionsWeDontHave;
        private FuncSingle mRationalFunc;
        private int mRequestCode;
        private FuncMore mResultFunc;

        public PermissionRequestObject(AppCompatActivity activity, String[] permissionNames) {
            mActivity = activity;
            mPermissionNames = permissionNames;
        }

        public PermissionRequestObject(Fragment fragment, String[] permissionNames) {
            mFragment = fragment;
            mPermissionNames = permissionNames;
        }

        /**
         * 请求权限时候 需要传一个请求码
         */
        public PermissionRequestObject ask(int reqCode) {
            mRequestCode = reqCode;
            int length = mPermissionNames.length;
            mPermissionsWeDontHave = new ArrayList<>(length);
            for (String mPermissionName : mPermissionNames) {
                mPermissionsWeDontHave.add(new SinglePermission(mPermissionName));
            }

            if (needToAsk()) {
                Log.i(TAG, "需要请求权限");
                if (mActivity != null) {
                    ActivityCompat.requestPermissions(mActivity, mPermissionNames, reqCode);
                } else {
                    mFragment.requestPermissions(mPermissionNames, reqCode);
                }
            } else {
                Log.i(TAG, "不需要请求权限");
                if (mGrantFunc != null) {
                    mGrantFunc.call();
                }
            }
            return this;
        }

        private boolean needToAsk() {
            ArrayList<SinglePermission> neededPermissions = new ArrayList<>(mPermissionsWeDontHave);
            for (int i = 0; i < mPermissionsWeDontHave.size(); i++) {
                SinglePermission perm = mPermissionsWeDontHave.get(i);
                int checkRes;
                if (mActivity != null) {
                    checkRes = ContextCompat.checkSelfPermission(mActivity, perm.getPermissionName());
                } else {
                    checkRes = ContextCompat.checkSelfPermission(mFragment.getContext(), perm.getPermissionName());
                }
                if (checkRes == PackageManager.PERMISSION_GRANTED) {
                    neededPermissions.remove(perm);
                } else {
                    boolean shouldShowRequestPermissionRationale;
                    if (mActivity != null) {
                        shouldShowRequestPermissionRationale =
                                ActivityCompat.shouldShowRequestPermissionRationale(mActivity, perm.getPermissionName());
                    } else {
                        shouldShowRequestPermissionRationale = mFragment.shouldShowRequestPermissionRationale(perm.getPermissionName());
                    }
                    if (shouldShowRequestPermissionRationale) {
                        perm.setRationalNeeded(true);
                    }
                }
            }
            mPermissionsWeDontHave = neededPermissions;
            mPermissionNames = new String[mPermissionsWeDontHave.size()];
            for (int i = 0; i < mPermissionsWeDontHave.size(); i++) {
                mPermissionNames[i] = mPermissionsWeDontHave.get(i).getPermissionName();
            }
            return mPermissionsWeDontHave.size() != 0;
        }

        /**
         * 要求第一次被拒绝的许可，如果有必要展示理性
         */
        public PermissionRequestObject onRational(FuncSingle rationalFunc) {
            mRationalFunc = rationalFunc;
            return this;
        }

        /**
         * Called if all the permissions were granted
         */
        public PermissionRequestObject onAllGranted(Func grantFunc) {
            mGrantFunc = grantFunc;
            return this;
        }

        /**
         * Called if there is at least one denied permission
         */
        public PermissionRequestObject onAnyDenied(Func denyFunc) {
            mDenyFunc = denyFunc;
            return this;
        }

        /**
         * Called with the original operands from {@link AppCompatActivity#onRequestPermissionsResult(int, String[], int[])
         * onRequestPermissionsResult} for any result
         */
        public PermissionRequestObject onResult(FuncMore resultFunc) {
            mResultFunc = resultFunc;
            return this;
        }

        /**
         * This Method should be called from {@link AppCompatActivity#onRequestPermissionsResult(int, String[], int[])
         * onRequestPermissionsResult} with all the same incoming operands
         * <pre>
         * {@code
         *
         * public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
         *      if (mStoragePermissionRequest != null)
         *          mStoragePermissionRequest.onRequestPermissionsResult(requestCode, permissions,grantResults);
         * }
         * }
         * </pre>
         */
        public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
            if (mRequestCode == requestCode) {
                if (mResultFunc != null) {
                    Log.i(TAG, "Calling Results Func");
                    mResultFunc.call(requestCode, permissions, grantResults);
                    return;
                }

                for (int i = 0; i < permissions.length; i++) {
                    if (grantResults[i] == PackageManager.PERMISSION_DENIED) {
                        if (mPermissionsWeDontHave.get(i).isRationalNeeded() && mRationalFunc != null) {
                            Log.i(TAG, "Calling Rational Func");
                            mRationalFunc.call(mPermissionsWeDontHave.get(i).getPermissionName());
                        } else if (mDenyFunc != null) {
                            Log.i(TAG, "Calling Deny Func");
                            mDenyFunc.call();
                        } else {
                            Log.e(TAG, "NUll DENY FUNCTIONS");
                        }

                        // terminate if there is at least one deny
                        return;
                    }
                }

                // there has not been any deny
                if (mGrantFunc != null) {
                    Log.i(TAG, "Calling Grant Func");
                    mGrantFunc.call();
                } else {
                    Log.e(TAG, "NUll GRANT FUNCTIONS");
                }
            }
        }
    }
}
