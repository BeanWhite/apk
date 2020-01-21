package com.example.pjj50.xinjitai;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.ClipData;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.Parcelable;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.JavascriptInterface;
import android.webkit.URLUtil;
import android.widget.Toast;

import com.example.pjj50.xinjitai.zxing.android.CaptureActivity;
import com.example.pjj50.xinjitai.zxing.common.Constant;
import com.tencent.smtt.sdk.DownloadListener;
import com.tencent.smtt.sdk.ValueCallback;
import com.tencent.smtt.sdk.WebChromeClient;
import com.tencent.smtt.sdk.WebSettings;
import com.tencent.smtt.sdk.WebView;
import com.tencent.smtt.sdk.WebViewClient;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity  implements ShotService.ShotStopCallBack {

    private static boolean isExit = false;//是否退出的标志
    private static WebView webView;

    private static String loadUrlM="http://www.cloud.aipsy.net/";
    private static String loadUrl="http://192.168.62.61:8848/Customer/login.html";

    private static String loadUrP="http://192.168.62.61:8848/Customer/login.html";
    private static String interviewUrl="https://xjt.cloud.aipsy.net/Interview-start.html";
    private static String uploadUrl="https://xjt.cloud.aipsy.net/Api-uploadaudio";
    private static String checkUpdateUrl="http://www.cloud.aipsy.net/getVersion.html";
    private static String apkUrl="https://aixl-1256268449.cos.ap-chengdu.myqcloud.com/aixjt.apk";

    private static Double thisVersion=1.7;
    private static String lastUrl="";
    private static final String TAG = "uploadFile";
    private static final int TIME_OUT = 10*1000;   //超时时间
    private static final String CHARSET = "utf-8"; //设置编码
    MyMediaRecorder recorder;
    private final int REQUEST_CODE_START=111;
    private final int REQUEST_CODE_OK=112;
    private DownloadUtils downloadUtils;
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static final int MY_PERMISSIONS_REQUEST_CALL_PHONE = 2;
    private static final int REQUEST_CODE_SCAN = 808;
    private static final int MY_PERMISSIONS_REQUEST_READ_PHONE_STATE = 4;
    private static String[] PERMISSIONS_STORAGE = {
            "android.permission.READ_EXTERNAL_STORAGE",
            "android.permission.WRITE_EXTERNAL_STORAGE" };
    private String[] permissions = new String[]{Manifest.permission.CALL_PHONE,Manifest.permission.SEND_SMS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION,Manifest.permission.READ_PHONE_STATE};
    private String[] permissions1 = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.CAMERA,Manifest.permission.RECORD_AUDIO};
    private List<String> mPermissionList = new ArrayList<>();
    private final int mRequestCode = 100;
    private final int mRequestCodes = 101;
    private ValueCallback<Uri> mUploadMessage;// 表单的数据信息
    private ValueCallback<Uri[]> mUploadCallbackAboveL;
    private final static int FILECHOOSER_RESULTCODE = 1;// 表单的结果回调
    private Uri imageUri;
    private LocationManager locationManager;
    private String provider;
    private static String _userid="";
    private static String _testid="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //判断app版本号
        HttpUtil.requestData(checkUpdateUrl, new HttpCallBackListener() {
            @Override
            public void onSuccess(String respose) {
                //处理请求
                double version=Double.valueOf(respose);
                if(thisVersion<version){
                    //提示升级版本号
                    final AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    //    设置Title的内容
                    builder.setTitle("更新");
                    builder.setCancelable(false);//不允许点击其他地方关闭
                    //    设置Content来显示一个信息
                    builder.setMessage("有新的app发布，请下载新版本安装");
                    //    设置一个PositiveButton
                    builder.setPositiveButton("确定", null);
                    //    设置一个NegativeButton
                    builder.setNegativeButton("退出", new DialogInterface.OnClickListener()
                    {
                        @Override
                        public void onClick(DialogInterface dialog, int which)
                        {
                            System.exit(0);
                        }
                    });
                    Looper.prepare();
                    final AlertDialog alertDialog=builder.create();
                    alertDialog.show();
                    alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View view)
                        {
                            if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                                    != PackageManager.PERMISSION_GRANTED){
                                if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)){
                                    //已经禁止提示了
                                    Toast.makeText(MainActivity.this, "您已禁止程序所需权限，请在设置中开启。", Toast.LENGTH_SHORT).show();
                                }else{
                                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},REQUEST_CODE_START);

                                }
                            }else {
                                downloadUtils =   new DownloadUtils(MainActivity.this);
                                downloadUtils.downloadAPK(apkUrl, "aixjt.apk");
                                alertDialog.dismiss();
                            }
                        }
                    });
                    //    显示出该对话框
                    Looper.loop();
                }
            }
            @Override
            public void onError(Exception e) {
                Looper.prepare();
                //处理异常
                String str=e.toString();
               // Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
                Looper.loop();
            }
        });
        hideBottomUIMenu();
        initWebView();

    }

    public void initWebView(){
        //设置浏览器
        webView=findViewById(R.id.webview);
        webView.setWebViewClient(new WebViewClient(){
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                hideBottomUIMenu();
                if (url.startsWith("weixin://wap/pay?")) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                    if(Build.VERSION.SDK_INT<26) {
                        view.loadUrl(url);
                        return true;
                    }
                    return false;
                }
                if (url.startsWith("alipays://platformapi/startApp?")) {
                    Intent intent = new Intent();
                    intent.setAction(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(url));
                    startActivity(intent);
                    if(Build.VERSION.SDK_INT<26) {
                        view.loadUrl(url);
                        return true;
                    }
                    return false;
                }
                if(url.contains("Microclass-package.html?tcid")){
                    return false;
                }
                if(Build.VERSION.SDK_INT<26) {
                    view.loadUrl(url);
                    return true;
                }
                return false;
            }

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon){
                if(lastUrl.equals(interviewUrl))
                {
                    if(recorder!=null){
                        recorder.cancel();
                    }
                }
                lastUrl=url;
            }
        });
        WebSettings settings=webView.getSettings();
        settings.setDomStorageEnabled(true);
        settings.setAppCacheMaxSize(1024*1024*8);
        String appCachePath = getApplicationContext().getCacheDir().getAbsolutePath();
        settings.setAppCachePath(appCachePath);
        settings.setAllowFileAccess(true);
        settings.setAppCacheEnabled(true);
        settings.setJavaScriptEnabled(true);
        settings.setSupportZoom(true);
        settings.setUseWideViewPort(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        settings.setLoadWithOverviewMode(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        webView.addJavascriptInterface(MainActivity.this,"xinjitai");
        AndroidBug5497Workaround.assistActivity(MainActivity.this);
        verifyStoragePermissions(MainActivity.this);
        //避免视频闪屏和透明
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        webView.setDownloadListener(new DownloadListener() {
            @Override
            public void onDownloadStart(String url, String userAgent, String contentDisposition, String mimeType, long contentLength) {
                DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
                // 允许媒体扫描，根据下载的文件类型被加入相册、音乐等媒体库
                request.allowScanningByMediaScanner();
                // 设置通知的显示类型，下载进行时和完成后显示通知
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                // 允许在计费流量下下载
                request.setAllowedOverMetered(true);
                // 允许该记录在下载管理界面可见
                request.setVisibleInDownloadsUi(true);
                // 允许漫游时下载
                request.setAllowedOverRoaming(true);
                // 设置下载文件保存的路径和文件名
                String fileName  = URLUtil.guessFileName(url, contentDisposition, mimeType);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, fileName);
                final DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
                // 添加一个下载任务
                long downloadId = downloadManager.enqueue(request);
            }

        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onShowFileChooser(WebView webView, ValueCallback<Uri[]> filePathCallback, FileChooserParams fileChooserParams) {
                mUploadCallbackAboveL = filePathCallback;
                take();
                return true;
            }
            //<3.0
            public void openFileChooser(ValueCallback<Uri> uploadMsg) {
                mUploadMessage = uploadMsg;
                take();
            }
            //>3.0+
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType) {
                mUploadMessage = uploadMsg;
                take();
            }
            //>4.1.1
            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
                mUploadMessage = uploadMsg;
                take();
            }
        });
        webView.loadUrl(loadUrl);
    }
    public  void verifyStoragePermissions(Activity activity) {
        try {
            //逐个判断你要的权限是否已经通过
            for (int i = 0; i < permissions1.length; i++) {
                if (ContextCompat.checkSelfPermission(this, permissions1[i]) != PackageManager.PERMISSION_GRANTED) {
                    mPermissionList.add(permissions1[i]);//添加还未授予的权限
                }
            }
            //申请权限
            if (mPermissionList.size() > 0) {//有权限没有通过，需要申请
                ActivityCompat.requestPermissions(this, permissions1, mRequestCodes);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
//        if(_userid!=""&&_testid!=""){
//            ShotService.faceShotRun(_userid,_testid);
//        }
//        ShotService.addShotStopListener(this);
    }
    @Override
    protected void onPause() {
        super.onPause();
        ShotService.faceShotStop();
        ShotService.removeShotStopListener();
    }
    @Override
    protected void onStop() {
        super.onStop();
        ShotService.faceShotStop();
        ShotService.removeShotStopListener();
    }
    @Override
    public void doShotStop() {
        mHandler.sendEmptyMessage(0);
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == FILECHOOSER_RESULTCODE) {
            if (null == mUploadMessage && null == mUploadCallbackAboveL) return;
            Uri result = data == null || resultCode != RESULT_OK ? null : data.getData();
            if (mUploadCallbackAboveL != null) {
                onActivityResultAboveL(requestCode, resultCode, data);
            } else if (mUploadMessage != null) {
                Log.e("result", result + "");
                if (result == null) {
                    mUploadMessage.onReceiveValue(imageUri);
                    mUploadMessage = null;
                    Log.e("imageUri", imageUri + "");
                } else {
                    mUploadMessage.onReceiveValue(result);
                    mUploadMessage = null;
                }
            }
        }
        if (requestCode == REQUEST_CODE_SCAN && resultCode == RESULT_OK) {
            if (data != null) {
                final String content = data.getStringExtra(Constant.CODED_CONTENT);
                Log.i("content",content);
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        //调用js方法
                        webView.loadUrl("javascript:gotoContent(\""+content+"\")");
                    }
                });

            }
        }
    }


    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void onActivityResultAboveL(int requestCode, int resultCode, Intent data) {
        if (requestCode != FILECHOOSER_RESULTCODE || mUploadCallbackAboveL == null) {
            return;
        }

        Uri[] results = null;
        if (resultCode == Activity.RESULT_OK) {
            if (data == null) {
                results = new Uri[]{imageUri};
            } else {
                String dataString = data.getDataString();
                ClipData clipData = data.getClipData();
                if (clipData != null) {
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        results[i] = item.getUri();
                    }
                }
                if (dataString != null)
                    results = new Uri[]{Uri.parse(dataString)};
            }
        }
        if (results != null) {
            mUploadCallbackAboveL.onReceiveValue(results);
            mUploadCallbackAboveL = null;
        } else {
            results = new Uri[]{imageUri};
            mUploadCallbackAboveL.onReceiveValue(results);
            mUploadCallbackAboveL = null;
        }
        return;
    }
    /**
     * 隐藏虚拟按键，并且全屏
     */
    protected void hideBottomUIMenu() {
        //隐藏虚拟按键，并且全屏
        if (Build.VERSION.SDK_INT > 11 && Build.VERSION.SDK_INT < 19) { // lower api
            View v = this.getWindow().getDecorView();
            v.setSystemUiVisibility(View.GONE);
        } else if (Build.VERSION.SDK_INT >= 19) {
            //for new api versions.
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }
    }

    private void take() {
        File imageStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "MyApp");
        if (!imageStorageDir.exists()) {
            imageStorageDir.mkdirs();
        }
        File file = new File(imageStorageDir + File.separator + "IMG_" + String.valueOf(System.currentTimeMillis()) + ".jpg");
        imageUri = Uri.fromFile(file);
        final List<Intent> cameraIntents = new ArrayList<Intent>();
        final Intent captureIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        final PackageManager packageManager = getPackageManager();
        final List<ResolveInfo> listCam = packageManager.queryIntentActivities(captureIntent, 0);
        for (ResolveInfo res : listCam) {
            final String packageName = res.activityInfo.packageName;
            final Intent i = new Intent(captureIntent);
            i.setComponent(new ComponentName(res.activityInfo.packageName, res.activityInfo.name));
            i.setPackage(packageName);
            i.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            cameraIntents.add(i);
        }
        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
        i.addCategory(Intent.CATEGORY_OPENABLE);
        i.setType("image/*");
        Intent chooserIntent = Intent.createChooser(i, "Image Chooser");
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, cameraIntents.toArray(new Parcelable[]{}));
        startActivityForResult(chooserIntent, FILECHOOSER_RESULTCODE);
    }

    @JavascriptInterface
    public void recordAction(){
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                ||ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    ||ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.RECORD_AUDIO)){
                //已经禁止提示了
                Toast.makeText(MainActivity.this, "您已禁止程序所需权限，请在设置中开启。", Toast.LENGTH_SHORT).show();
            }else{
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ,Manifest.permission.RECORD_AUDIO},REQUEST_CODE_START);
            }
        }
        else {
            if(recorder==null){
                recorder=new MyMediaRecorder();
            }
            try{
                if(recorder.state!=1){
                    String errStr= recorder.startRecord();
                    if(errStr!=null)
                    {
                        Toast.makeText(getApplicationContext(), "异常:"+errStr,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //调用js的开始方法
                            webView.loadUrl("javascript:start()");
                        }
                    });

                }
                else {
                    String errStr=recorder.pause();
                    if(errStr!=null)
                    {
                        Toast.makeText(getApplicationContext(), "异常:"+errStr,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //调用js的暂停方法
                            webView.loadUrl("javascript:pause()");
                        }
                    });

                }
            }catch (Exception e){
                Toast.makeText(MainActivity.this, "出现异常:"+e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }

    @JavascriptInterface
    public void complete(){
        if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                ||ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO)
                != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    ||ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.RECORD_AUDIO)){
                //已经禁止提示了
                Toast.makeText(MainActivity.this, "您已禁止程序所需权限，请在设置中开启。", Toast.LENGTH_SHORT).show();
            }else{
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE
                        ,Manifest.permission.RECORD_AUDIO},REQUEST_CODE_OK);
            }
        }
        else {
            boolean result= recorder.stopAndRelease();
            //上传文件并调用js方法返回文件名
            if(result){
                final String path= uploadFile(new File(recorder.getCurrentFilePath()),uploadUrl);
                if(!path.equals("0")){
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            //调用js的暂停方法
                            webView.loadUrl("javascript:pause()");
                            webView.loadUrl("javascript:filepath(\""+path+"\")");
                            webView.loadUrl("javascript:ok()");
                        }
                    });
                }
            }
        }
    }

    @JavascriptInterface
    public void checkVideo(){
        if (Build.VERSION.SDK_INT>22){
            if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED
                    ||ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED){
                if(ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.CAMERA)
                        ||ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,Manifest.permission.RECORD_AUDIO)){
                    //已经禁止提示了
                    Toast.makeText(MainActivity.this, "您已禁止程序所需权限，请在设置中开启。", Toast.LENGTH_SHORT).show();
                }else{
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA
                            ,Manifest.permission.RECORD_AUDIO},REQUEST_CODE_OK);
                }
            }
        }
    }
    @JavascriptInterface
    public void heartbeat(String userid,String testid){
        _userid=userid;
        _testid=testid;
        ShotService.faceShotRun(userid,testid);
    }
    @JavascriptInterface
    public void closeheartbeat(){
        ShotService.faceShotStop();
    }
    @JavascriptInterface
    public void openReadphone() {
        mPermissionList.clear();//清空没有通过的权限
        //逐个判断你要的权限是否已经通过
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);//添加还未授予的权限
            }
        }
        //申请权限
        if (mPermissionList.size() > 0) {//有权限没有通过，需要申请
            ActivityCompat.requestPermissions(this, permissions, mRequestCode);
        }else{
           // Toast.makeText(MainActivity.this, "权限已经开启。", Toast.LENGTH_SHORT).show();
        };
    }

    @JavascriptInterface
    public void getweizhi() {
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        List<String> list = locationManager.getProviders(true);
    }

    @JavascriptInterface
    public String getxy() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // 没有获得授权，申请授权
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.CALL_PHONE)) {
                Toast.makeText(MainActivity.this, "您已禁止程序所需权限，请在设置中开启。", Toast.LENGTH_SHORT).show();
            } else {
                // 不需要解释为何需要该权限，直接请求授权
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE},
                        MY_PERMISSIONS_REQUEST_CALL_PHONE);
            }
            return "0";
        }else{
            provider = LocationManager.NETWORK_PROVIDER;
            Location location = locationManager.getLastKnownLocation(provider);
            if(location!=null)
                return location.getLatitude()+","+location.getLongitude();//经纬度
            else
                return "0";
        }

    }




    @JavascriptInterface
    public void saoyisao() {
        mPermissionList.clear();//清空没有通过的权限
        //逐个判断你要的权限是否已经通过
        for (int i = 0; i < permissions1.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissions1[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions1[i]);//添加还未授予的权限
            }
        }
        //申请权限
        if (mPermissionList.size() > 0) {//有权限没有通过，需要申请
            ActivityCompat.requestPermissions(this, permissions1, mRequestCodes);
        }else{
            Intent intent =new Intent(MainActivity.this, CaptureActivity.class);
            startActivityForResult(intent,REQUEST_CODE_SCAN);
        };
    }


    @JavascriptInterface
    public String callphone(String phonenumber,String msg) {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            // 没有获得授权，申请授权
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this, Manifest.permission.CALL_PHONE)) {
                Toast.makeText(MainActivity.this, "您已禁止程序所需权限，请在设置中开启。", Toast.LENGTH_SHORT).show();
            } else {
                // 不需要解释为何需要该权限，直接请求授权
                ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE},
                        MY_PERMISSIONS_REQUEST_CALL_PHONE);
            }
        }else{
            //     已经获得授权，可以打电话
            Intent intent = new Intent(Intent.ACTION_CALL);
            Uri data = Uri.parse("tel:"+phonenumber);
            intent.setData(data);
            startActivity(intent);
            provider = LocationManager.NETWORK_PROVIDER;
            Location location = locationManager.getLastKnownLocation(provider);
            if(location!=null)
                return location.getLatitude()+","+location.getLongitude();//经纬度
            else
                return "0";
        }
        return "0";

       //else {
//           SmsManager smsManager = SmsManager.getDefault();
//            //拆分短信内容（手机短信长度限制）
//            List<String> divideContents = smsManager.divideMessage(msg);
//            for (String text : divideContents) {
//               smsManager.sendTextMessage(phonenumber, null, text, null, null);
//            }
//            if(PhoneNumberUtils.isGlobalPhoneNumber(phonenumber)){
//                Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:"+phonenumber));
//                intent.putExtra("sms_body", msg);
//                startActivity(intent);
//            }
// else {
//      }
    }
    //请求权限后回调的方法
    //参数： requestCode  是我们自己定义的权限请求码
    //参数： permissions  是我们请求的权限名称数组
    //参数： grantResults 是我们在弹出页面后是否允许权限的标识数组，数组的长度对应的是权限名称数组的长度，数组的数据0表示允许权限，-1表示我们点击了禁止权限
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean hasPermissionDismiss = false;//有权限没有通过
        if (mRequestCode == requestCode) {
            for (int i = 0; i < grantResults.length; i++) {
                if (grantResults[i] == -1) {
                    hasPermissionDismiss = true;
                }
            }
            //如果有权限没有被允许
            if (hasPermissionDismiss) {
                Toast.makeText(MainActivity.this, "您已禁止程序所需权限，请在设置中开启。", Toast.LENGTH_SHORT).show();
            }else{
                //全部权限通过，可以进行下一步操作。。。

            }
        }

    }
    @JavascriptInterface
    public void  opensms(){
        mPermissionList.clear();//清空没有通过的权限
        //逐个判断你要的权限是否已经通过
        for (int i = 0; i < permissions.length; i++) {
            if (ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                mPermissionList.add(permissions[i]);//添加还未授予的权限
            }
        }
        //申请权限
        if (mPermissionList.size() > 0) {//有权限没有通过，需要申请
            ActivityCompat.requestPermissions(this, permissions, mRequestCode);
        }else{
           // Toast.makeText(MainActivity.this, "权限已经开启。", Toast.LENGTH_SHORT).show();
        };
    }


    public static String uploadFile(File file, String RequestURL)
    {
        String result = null;
        String  BOUNDARY =  "*********";  //边界标识   随机生成
        String PREFIX = "--" , LINE_END = "\r\n";
        String CONTENT_TYPE = "multipart/form-data";   //内容类型
        try {
            URL url = new URL(RequestURL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setReadTimeout(TIME_OUT);
            conn.setConnectTimeout(TIME_OUT);
            conn.setDoInput(true);  //允许输入流
            conn.setDoOutput(true); //允许输出流
            conn.setUseCaches(false);  //不允许使用缓存
            conn.setRequestMethod("POST");  //请求方式
            conn.setRequestProperty("Charset", CHARSET);  //设置编码
            conn.setRequestProperty("connection", "keep-alive");
            conn.setRequestProperty("Content-Type", CONTENT_TYPE + ";boundary=" + BOUNDARY);

            if(file!=null)
            {
                DataOutputStream dos = new DataOutputStream( conn.getOutputStream());
                StringBuffer sb = new StringBuffer();
                sb.append(PREFIX);
                sb.append(BOUNDARY);
                sb.append(LINE_END);

                sb.append("Content-Disposition: form-data; name=\"mp4\"; filename=\""+file.getName()+"\""+LINE_END);
                sb.append("Content-Type: application/octet-stream; charset="+CHARSET+LINE_END);
                sb.append(LINE_END);
                dos.write(sb.toString().getBytes());
                InputStream is = new FileInputStream(file);
                byte[] bytes = new byte[1024];
                int len = 0;
                while((len=is.read(bytes))!=-1)
                {
                    dos.write(bytes, 0, len);
                }
                is.close();
                dos.write(LINE_END.getBytes());
                byte[] end_data = (PREFIX+BOUNDARY+PREFIX+LINE_END).getBytes();
                dos.write(end_data);
                dos.flush();
                int res = conn.getResponseCode();
                InputStream input =  conn.getInputStream();
                StringBuffer sb1= new StringBuffer();
                int ss ;
                while((ss=input.read())!=-1)
                {
                    sb1.append((char)ss);
                }
                result = sb1.toString();
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e("test",e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("test",e.getMessage());
        }
        return result.substring(result.indexOf("/")) ;

}
    ///返回键控制程序退出
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        String nowurl=webView.getUrl();

        if(keyCode==KeyEvent.KEYCODE_BACK&&(nowurl.equals(loadUrl)||nowurl.equals(loadUrl+"/#")||nowurl.equals(loadUrlM)||nowurl.contains(loadUrP)))
        {
            exit();
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_BACK&&webView.canGoBack()) {
            webView.goBack();
            return false;
        }
        return super.onKeyDown(keyCode, event);
    }

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            isExit = false;
        }
    };
    private void exit() {
        if (!isExit) {
            isExit = true;
            Toast.makeText(getApplicationContext(), "再按一次退出爱心理",
                    Toast.LENGTH_SHORT).show();
            // 利用handler延迟发送更改状态信息
            mHandler.sendEmptyMessageDelayed(0, 2000);
        } else {
            finish();
            System.exit(0);
        }
    }
}
