package com.example.pjj50.xinjitai;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class TestActivity extends AppCompatActivity {
    Button btnStart;
    Button btnOk;
    TextView textView;
    MyMediaRecorder recorder;
    private final int REQUEST_CODE_START=111;
    private final int REQUEST_CODE_OK=112;
    private AppCompatActivity thisActivity;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);
        btnStart=(Button)findViewById(R.id.btn);
        btnOk=(Button)findViewById(R.id.btnOk);
        textView=(TextView)findViewById(R.id.textview);
        recorder=new MyMediaRecorder();
        thisActivity=this;
        btnStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(thisActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED
                        ||ContextCompat.checkSelfPermission(thisActivity, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED){
                    if(ActivityCompat.shouldShowRequestPermissionRationale(thisActivity,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    ||ActivityCompat.shouldShowRequestPermissionRationale(thisActivity,Manifest.permission.RECORD_AUDIO)){
                        //已经禁止提示了
                        Toast.makeText(thisActivity, "您已禁止程序所需权限，请在设置中开启。", Toast.LENGTH_SHORT).show();
                    }else{
                        ActivityCompat.requestPermissions(thisActivity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE
                                ,Manifest.permission.RECORD_AUDIO},REQUEST_CODE_START);
                    }

                }
                else {
                    if(btnStart.getText().equals("开始")||btnStart.getText().equals("继续")){

                        String errStr= recorder.startRecord();
                        if(errStr!=null)
                        {
                            Toast.makeText(getApplicationContext(), "异常:"+errStr,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        btnStart.setText("暂停");
                        textView.setText("路径为："+recorder.getCurrentFilePath());
                    }else {
                        btnStart.setText("继续");
                        recorder.pause();
                    }
                }

            }
        });
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(ContextCompat.checkSelfPermission(thisActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED
                        ||ContextCompat.checkSelfPermission(thisActivity, Manifest.permission.RECORD_AUDIO)
                        != PackageManager.PERMISSION_GRANTED){
                    if(ActivityCompat.shouldShowRequestPermissionRationale(thisActivity,Manifest.permission.WRITE_EXTERNAL_STORAGE)
                            ||ActivityCompat.shouldShowRequestPermissionRationale(thisActivity,Manifest.permission.RECORD_AUDIO)){
                        //已经禁止提示了
                        Toast.makeText(thisActivity, "您已禁止程序所需权限，请在设置中开启。", Toast.LENGTH_SHORT).show();
                    }else{
                        ActivityCompat.requestPermissions(thisActivity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE
                                ,Manifest.permission.RECORD_AUDIO},REQUEST_CODE_OK);
                    }
                }
                else {
                    recorder.stopAndRelease();
                    btnStart.setText("开始");
                }

            }
        });
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode){
            case REQUEST_CODE_START:
                if(grantResults.length >1 &&grantResults[0]==PackageManager.PERMISSION_GRANTED
                        &&grantResults[1]==PackageManager.PERMISSION_GRANTED){
                    //用户同意授权
                    if(btnStart.getText().equals("开始")||btnStart.getText().equals("继续")){

                        String errStr= recorder.startRecord();
                        if(errStr!=null)
                        {
                            Toast.makeText(getApplicationContext(), "异常:"+errStr,
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                        btnStart.setText("暂停");
                        textView.setText("路径为："+recorder.getCurrentFilePath());
                    }else {
                        btnStart.setText("继续");
                        recorder.pause();
                    }
                }else{
                    //用户拒绝授权
                    Toast.makeText(getApplicationContext(), "授权失败，无法进行下一步操作",
                            Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_CODE_OK:
                if(grantResults.length >1 &&grantResults[0]==PackageManager.PERMISSION_GRANTED
                        &&grantResults[1]==PackageManager.PERMISSION_GRANTED){
                    //用户同意授权
                    recorder.stopAndRelease();
                    btnStart.setText("开始");
                }else{
                    //用户拒绝授权
                    Toast.makeText(getApplicationContext(), "授权失败，无法进行下一步操作",
                            Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
