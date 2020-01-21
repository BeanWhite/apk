package com.example.pjj50.xinjitai;

import android.annotation.TargetApi;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Environment;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;

/**
 * Created by pjj50 on 2017/12/8.
 */

public class MyMediaRecorder {
    private MediaRecorder mediaRecorder;
    private String savePath;
    private String currentFilePath;
    public String fileName;
    public String ErrString;
    public int state;//0初始态，1录音中，2暂停
    public MyMediaRecorder() {
        try{
            savePath =  Environment.getExternalStorageDirectory().getCanonicalPath()+"/xinjitai";
        }catch (IOException e)
        {
            //不能访问当前路径
            ErrString="不能访问当前路径";
        }
        File file = new File(savePath);
        if (!file.exists())
            file.mkdirs();
    }
    public String startRecord() {
        try {
            if(state==0)
            {
                state=1;
                mediaRecorder = new MediaRecorder();
                File file = new File(savePath, generateFileName());
                currentFilePath = file.getAbsolutePath();
                mediaRecorder.setOutputFile(currentFilePath);
                mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
                mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
                mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
                mediaRecorder.prepare();
                mediaRecorder.start();
            }
            else if(state==2){
                resume();
            }
            else {
                return null;
            }


        } catch (Exception e) {
            state=0;
            return e.getMessage();
        }
        return null;
    }

    @TargetApi(24)
    public String pause(){
        if(state!=1)
            return null;
        try {
            if(Build.VERSION.SDK_INT>=22){
            //调用高版本的相关接口处理
                mediaRecorder.pause();
                state=2;
            }else{
            //调用低版本的相关接口处理

                return "版本过低";
            }

        }catch (Exception e){
            return e.getMessage();
        }
        return null;
    }
    @TargetApi(24)
    public String resume(){
        if(state!=2)
            return null;
        try {
            if(Build.VERSION.SDK_INT>=22){
                //调用高版本的相关接口处理
                mediaRecorder.resume();
                state=2;
            }else{
                //调用低版本的相关接口处理

                return "版本过低";
            }

        }catch (Exception e){
            return e.getMessage();
        }
        return null;
    }

    public boolean stopAndRelease() {
        if (mediaRecorder == null||state==0)
            return false;
        mediaRecorder.setOnErrorListener(null);
        try{
            mediaRecorder.stop();
        } catch(RuntimeException stopException){
        }
        mediaRecorder.reset();
        mediaRecorder.release();
        mediaRecorder = null;
        state=0;
        return true;
    }

    public void cancel() {
        this.stopAndRelease();
        mediaRecorder=null;
        if (currentFilePath != null) {
            File file = new File(currentFilePath);
            file.delete();
            currentFilePath = null;
        }
        state=0;
    }

    private String generateFileName() {
        SimpleDateFormat sDateFormat    =   new    SimpleDateFormat("yyyyMMddhhmmss");
        String    date    =    sDateFormat.format(new    java.util.Date());
        fileName="访谈"+date + ".mp4";
        return fileName;
    }

    public String getCurrentFilePath() {
        return currentFilePath;
    }
}
