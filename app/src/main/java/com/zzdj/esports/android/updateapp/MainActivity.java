package com.zzdj.esports.android.updateapp;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.zzdj.esports.android.updateapp.utils.Func;
import com.zzdj.esports.android.updateapp.utils.MyPermissionUtil;

import java.io.File;

public class MainActivity extends AppCompatActivity {
    public static final String WRITE_EXTERNAL_STORAGE = Manifest.permission.WRITE_EXTERNAL_STORAGE;
    public static final String READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE;
    private MyPermissionUtil.PermissionRequestObject mStoragePermissionRequest;
    private static final int REQUEST_CODE_STORAGE = 2;
    @BindView(id = R.id.btn1)
    private Button btn1;

    @BindView(id = R.id.zz_progreebar_ll)
    private LinearLayout zz_progreebar_ll;

    @BindView(id =R.id.pb_loading)
    private ProgressBar pb_loading;

    @BindView(id = R.id.tv_progress)
    private TextView tv_progress;

    private boolean isOK=false;
    private Activity activity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        InjectUtils.inject(MainActivity.this);
        activity = this;
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onAskForStoragePermissionClick();
            }
        });
    }
    //存储权限
    public void onAskForStoragePermissionClick() {
        mStoragePermissionRequest = MyPermissionUtil.with(this).request(WRITE_EXTERNAL_STORAGE).onAllGranted(new Func() {
            @Override protected void call() {
                upLoadFile("http://wcsqd.wanmeiyueyu.com/hh/android/20180509/zhizhudj.apk");
            }
        }).onAnyDenied(new Func() {
            @Override protected void call() {
            }
        }).ask(REQUEST_CODE_STORAGE);

    }

    private void upLoadFile(String url) {
        zz_progreebar_ll.setVisibility(View.VISIBLE);
        //新建文件夹 先选好路径 再调用mkdir函数 现在是根目录下面的Ask文件夹
        File nf = new File(Environment.getExternalStorageDirectory() + "/zhizhudianjing");
        if (!nf.exists()) {
            nf.mkdir();
        }
        //在根目录下面的蜘蛛电竞文件夹下 创建zz_team_head.jpg文件
        final String path = Environment.getExternalStorageDirectory() + "/zhizhudianjing/";
        DownloadUtil.get().download(url, path, "zhizhudj.apk", new DownloadUtil.OnDownloadListener() {
            @Override
            public void onDownloadSuccess(File file) {
                //下载完成进行相关逻辑操作
                if (isOK == true) {
                    openAPK(file);
                }
            }

            @Override
            public void onDownloading(int progress) {
                if (progress == 100) {
                    isOK = true;
                }
                Message message = handler.obtainMessage();
                message.what = 1;
                message.arg1 = progress;
                message.sendToTarget();
            }

            @Override
            public void onDownloadFailed(Exception e) {
                //下载异常进行相关提示操作
                Log.i("LOG",e.toString());
                Message message1 = handler.obtainMessage();
                handler.sendEmptyMessage(2);
            }
        });
    }

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1) {
                pb_loading.setProgress(msg.arg1);
                tv_progress.setText("已经下载" + msg.arg1 + "%");
            } else {
                Toast.makeText(MainActivity.this,"由于网络原因，下载失败！",Toast.LENGTH_SHORT).show();
            }
            super.handleMessage(msg);
        }
    };

    /**
     * 下载完成安装apk
     *
     * @param
     */
    private void openAPK(File apkFile) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        if (Build.VERSION.SDK_INT >= 24) {//判断版本大于等于7.0
            //参数1 上下文, 参数2 Provider主机地址 和配置文件中保持一致   参数3  共享的文件
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            Uri contentUri = FileProvider.getUriForFile(this, "com.zzdj.esports.android.updateapp.fileProvider", apkFile);
            intent.setDataAndType(contentUri, "application/vnd.android.package-archive");
        } else {
            intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }
        if (this.getPackageManager().queryIntentActivities(intent, 0).size() > 0) {
            this.startActivity(intent);
            finish();
        }
    }

        @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        if (mStoragePermissionRequest != null) {
            mStoragePermissionRequest.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

}
