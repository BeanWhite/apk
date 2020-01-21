package com.example.pjj50.xinjitai;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.WindowManager;

import com.example.pjj50.xinjitai.tool.CameraSettingInfo;
import com.example.pjj50.xinjitai.tool.SharedPreferenceUtil;
import com.example.pjj50.xinjitai.tool.TimerTaskUtil;

public class ShotService extends Service{
	private static ShotService service;
	private static boolean isRunning = false;
	private static CameraThread cameraThread;
	private CameraControl cameraControl;
	private static Handler cameraHandler;
	private final static int MSG_OPEN = 0;
	private final static int MSG_CLOSE = 1;
	private SensorManager mSensorManager = null;
	private Sensor mSensorAccelerometer = null;
	private Sensor mSensorMagnetic = null;
	private static boolean lock_face_detect = false;
	private static int pic_num = 1;
	//上传图片需要的参数
	private static String userid = "no";
	private static String testid = "0";
	private static int current_pic_num = 0;
	private static Context mContext;
	private static int camera_choose;
	private static ShotStopCallBack mShotStopListener;
	private static ServiceStartCallBack mServiceStartListener;
	private static WindowManager wm;
	public static final int TYPE_ACTIVE = 0;
	private static TimerTaskUtil taskUtil;

	interface ShotStopCallBack {
		public void doShotStop();
	}

	interface ServiceStartCallBack {
		public void doServiceStart();
	}

	private SensorEventListener accelerometerListener = new SensorEventListener() {
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			cameraControl.onAccelerometerSensorChanged(event);
		}
	};

	private SensorEventListener magneticListener = new SensorEventListener() {
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			cameraControl.onMagneticSensorChanged(event);
		}
	};

	@Override
	public void onCreate() {
		super.onCreate();
		mContext = this;
		camera_choose = CameraSettingInfo.camera_fornt;
		pic_num = SharedPreferenceUtil.getPreference(this, CameraSettingInfo.camera_pic_num,
				CameraSettingInfo.camera_pic_num_default);
		userid = SharedPreferenceUtil.getPreference(this, CameraSettingInfo.userid,
				userid);
		testid = SharedPreferenceUtil.getPreference(this, CameraSettingInfo.testid,
				testid);
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		if (mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
			mSensorAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		}
		if (mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
			mSensorMagnetic = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		}
		cameraThread = new CameraThread("handler_thread_autocamera", this);
		cameraThread.setPriority(Thread.MAX_PRIORITY);
		cameraThread.start();
		cameraControl = cameraThread.getCameraControl();
		wm = (WindowManager) this.getSystemService(Context.WINDOW_SERVICE);

		WindowManager.LayoutParams params = new WindowManager.LayoutParams(1, 1,
				WindowManager.LayoutParams.TYPE_SYSTEM_ALERT,
				WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
				PixelFormat.TRANSPARENT);
		if (Build.VERSION.SDK_INT >= 26){//6.0+
			params.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
		}else {
			params.type =  WindowManager.LayoutParams.TYPE_SYSTEM_ALERT;
		}
		params.gravity = Gravity.TOP | Gravity.RIGHT;
		params.alpha = PixelFormat.TRANSPARENT;
		params.x = params.y = this.getResources().getDimensionPixelOffset(R.dimen.preview_surface_offset);
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
			cameraControl.setAlpha(params.alpha);
		}
		wm.addView(cameraControl, params);
		cameraHandler = new Handler(cameraThread.getLooper()) {
			@Override
			public void handleMessage(Message msg) {
				super.handleMessage(msg);
				switch (msg.what) {
				case MSG_OPEN:
					cameraControl.openCamera(userid,testid);
					break;
				case MSG_CLOSE:
					cameraControl.closeCamera();
					break;
				}
			}
		};
		
		taskUtil = new TimerTaskUtil() {

			@Override
			protected void taskTimeOut() {
				isRunning = false;
				cameraHandler.sendEmptyMessage(MSG_CLOSE);
			}
		};
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		mSensorManager.registerListener(accelerometerListener, mSensorAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
		mSensorManager.registerListener(magneticListener, mSensorMagnetic, SensorManager.SENSOR_DELAY_NORMAL);
		if (mServiceStartListener != null)
			mServiceStartListener.doServiceStart();

	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		faceShotStop();
	}

	@Override
	public IBinder onBind(Intent arg0) {
		return new MyBinder();
	}

	public class MyBinder extends Binder {
		public ShotService getService() {
			if (service == null)
				service = new ShotService();
			return service;
		}
	}

	public static int getCameraChoose() {
		return camera_choose;
	}

	public static void switchCamera() {
		camera_choose = CameraSettingInfo.camera_fornt;
		SharedPreferenceUtil.savePreference(mContext, CameraSettingInfo.camera_choose_saver, camera_choose);
		if (isRunning) {
			current_pic_num = 0;
			cameraHandler.sendEmptyMessage(MSG_CLOSE);
			cameraHandler.sendEmptyMessageDelayed(MSG_OPEN, 3000);
		}
	}

	public static boolean getFaceShotStatus() {
		return isRunning;
	}

	public static void faceShotRun(String userid,String testid) {
		isRunning = true;
		current_pic_num = 0;
		SharedPreferenceUtil.savePreference(mContext, CameraSettingInfo.userid, userid);
		SharedPreferenceUtil.savePreference(mContext, CameraSettingInfo.testid, testid);
		cameraHandler.sendEmptyMessage(MSG_OPEN);
		//十分钟后关闭
		taskUtil.runNewTask(1000*600);
	}

	public static void faceShotStop() {
		isRunning = false;
		cameraHandler.sendEmptyMessage(MSG_CLOSE);
		if (mShotStopListener != null)
			mShotStopListener.doShotStop();
	}

	public static void setPicNum(int num) {
		SharedPreferenceUtil.savePreference(mContext, CameraSettingInfo.camera_pic_num, num);
		pic_num = num;
	}

	public static int getPicNum() {
		return pic_num;
	}

	public static void lockFaceDeteced() {
		lock_face_detect = true;
	}

	public static void unlockFaceDeteced() {
		lock_face_detect = false;
	}

	public static boolean askFaceDetecedPermission() {
		if (lock_face_detect)
			return false;
		return true;
//		if (current_pic_num < pic_num || pic_num == -1) {
//			return true;
//		} else {
//			faceShotStop();
//			return false;
//		}
	}

	public static void insertFaceItem() {
		current_pic_num = current_pic_num + 1;
	}

	public static WindowManager getWindowM() {
		return wm;
	}

	public static void addShotStopListener(ShotStopCallBack listener) {
		mShotStopListener = listener;
	}

	public static void removeShotStopListener() {
		mShotStopListener = null;
	}

	public static void addServiceStartListener(ServiceStartCallBack listener) {
		mServiceStartListener = listener;
	}

	public static void removeServiceStartListener() {
		mServiceStartListener = null;
	}
}