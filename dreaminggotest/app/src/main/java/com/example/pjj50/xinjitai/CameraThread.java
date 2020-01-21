package com.example.pjj50.xinjitai;

import android.content.Context;
import android.os.HandlerThread;

public class CameraThread extends HandlerThread{
	private CameraControl cameraControl;
	private Context mContext;
	public CameraThread(String name, Context context) {
		super(name);
		mContext = context;
	}

	@Override
	public synchronized void start() {
		super.start();
		cameraControl = new CameraControl(mContext);
	}
	
	public CameraControl getCameraControl(){
		return cameraControl;
	}
}
