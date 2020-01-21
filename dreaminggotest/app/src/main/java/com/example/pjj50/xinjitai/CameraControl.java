package com.example.pjj50.xinjitai;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Camera;
import android.hardware.Camera.ErrorCallback;
import android.hardware.Camera.Face;
import android.hardware.Camera.FaceDetectionListener;
import android.hardware.Camera.PreviewCallback;
import android.hardware.SensorEvent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.example.pjj50.xinjitai.tool.CameraSettingInfo;
import com.example.pjj50.xinjitai.tool.DBHelper;
import com.example.pjj50.xinjitai.tool.FaceDetectTool;
import com.example.pjj50.xinjitai.tool.ParameterInit;

public class CameraControl extends SurfaceView implements SurfaceHolder.Callback {
	private boolean openDebug = true;
	private String TAG = "FaceShot";
	private static SurfaceHolder mHolder = null;
	private static Context mContext;
	private Camera camera;
	private DBHelper dbHelper;
	private int display_rotation;
	private  final int TIME_OUT = 10*1000;   //超时时间
	private  final String CHARSET = "utf-8"; //设置编码
	private String uploadUrl="https://xjt.cloud.aipsy.net/Api-uploadbeat";
	private static String userid="no";
	private static String testid="0";
	private static long lastTime=0;
	private FaceDetectionListener myFaceDetectionListener = new FaceDetectionListener() {
		@Override
		public void onFaceDetection(Face[] faces, Camera camera) {
//			System.out.println("pfpf device find faces");
//			if (faces.length > 0 && ShotService.askFaceDetecedPermission()) {
//				ShotService.lockFaceDeteced();
//				takePicture();
//
//			}
		}
	};

	private PreviewCallback mPreviewCallback = new PreviewCallback() {
		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			//延时2秒
			long now=System.currentTimeMillis()/1000;
			if(now-lastTime>2){
				lastTime=now;
				if (ShotService.askFaceDetecedPermission()) {
					ShotService.lockFaceDeteced();
					Bitmap bmp = FaceDetectTool.decodeToBitMap(data, camera, display_rotation);
					if (FaceDetectTool.faceDeted(bmp)) {
						saveBitmap(bmp);
					}
					if (bmp != null) {
						bmp.recycle();
						bmp = null;
					}
				}
			}

		}
	};

	private void saveBitmap(Bitmap bmp) {
		String filename=getFileName();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		bmp.compress(Bitmap.CompressFormat.JPEG, 70, baos);
		byte[] b = baos.toByteArray();
		uploadPic(b,filename,uploadUrl);

	}
	private  void uploadPic(byte[] b,String filename, String RequestURL)
	{
		String  BOUNDARY =  "*********";  //边界标识   随机生成
		String PREFIX = "--" , LINE_END = "\r\n";
		String CONTENT_TYPE = "multipart/form-data";   //内容类型
		try {
			URL url = new URL(RequestURL+"?userid="+userid+"&testid="+testid);
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
			if(b!=null)
			{
				DataOutputStream dos = new DataOutputStream( conn.getOutputStream());
				StringBuffer sb = new StringBuffer();
                //写入文件
				sb.append(PREFIX);
				sb.append(BOUNDARY);
				sb.append(LINE_END);
				sb.append("Content-Disposition: form-data; name=\"jpg\"; filename=\""+filename+"\""+LINE_END);
				sb.append("Content-Type: application/octet-stream; charset="+CHARSET+LINE_END);
                sb.append(LINE_END);
				dos.write(sb.toString().getBytes());
				dos.write(b, 0, b.length);
				dos.write(LINE_END.getBytes());
				byte[] end_data = (PREFIX+BOUNDARY+PREFIX+LINE_END).getBytes();
				dos.write(end_data);
				dos.flush();
				dos.close();
				int res = conn.getResponseCode();
				InputStream input =  conn.getInputStream();
				StringBuffer sb1= new StringBuffer();
				int ss ;
				while((ss=input.read())!=-1)
				{
					sb1.append((char)ss);
				}
				Log.i("uploadmsg",sb1.toString());
			}
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}
	private Camera.ShutterCallback shutterCallback = new Camera.ShutterCallback() {
		public void onShutter() {
		}
	};

	private Camera.PictureCallback jpegPictureCallback = new Camera.PictureCallback() {
		public void onPictureTaken(byte[] data, Camera cam) {
			// n.b., this is automatically run in a different thread
			System.gc();
			BitmapFactory.Options options = new BitmapFactory.Options();
			// options.inMutable = true;
			options.inPurgeable = true;
			Bitmap bitmap = null;
			Bitmap bitmap_temp = BitmapFactory.decodeByteArray(data, 0, data.length, options);
			if (FaceDetectTool.faceDeted(bitmap_temp)) {
				bitmap = FaceDetectTool.rotBmpToDisplay(bitmap_temp, display_rotation);
				saveBitmap(bitmap);
			} else {
				System.gc();
				return;
			}
			System.gc();
			camera.startPreview();
		}
	};

	@SuppressWarnings("deprecation")
	public CameraControl(Context context) {
		super(context);
		mContext = context;
		dbHelper = DBHelper.getInstance(context);
		mHolder = getHolder();
		mHolder.addCallback(this);
		mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void openCamera(String _userid,String _testid) {
		userid=_userid;
		testid=_testid;
		ContentValues cameraData = dbHelper.downloadCameraData(CameraSettingInfo.camera_fornt);
		int cameraId = ParameterInit.initCameraId(cameraData);
		camera = Camera.open(cameraId);
		camera.setErrorCallback(new ErrorCallback() {
			@Override
			public void onError(int arg, Camera camera) {
				return;
			}
		});
		Camera.CameraInfo info = new Camera.CameraInfo();
		Camera.getCameraInfo(cameraId, info);
		Camera.Parameters parameters = camera.getParameters();
		display_rotation = ParameterInit.initDisplayOrientation(camera, info);
		ParameterInit.initWhiteBalance(parameters, cameraData);
		ParameterInit.initExposures(parameters);
		ParameterInit.initPictureSize(parameters, cameraData);
		parameters.setJpegQuality(90);
		ParameterInit.initPreviewSize(parameters, cameraData);
		dbHelper.uploadCameraData(cameraData);
		camera.setParameters(parameters);
		try {
			camera.setPreviewDisplay(mHolder);
		} catch (IOException e) {
			e.printStackTrace();
		}
//		if (ParameterInit.initFaceDetection(camera, parameters, cameraData)) {
		if (false){
			camera.setFaceDetectionListener(myFaceDetectionListener);
			camera.startFaceDetection();
			camera.startPreview();
		} else {
			camera.setPreviewCallback(mPreviewCallback);
			camera.startPreview();
		}
	}

	private void takePicture() {
		takePictureWhenFocused();
	}

	private void takePictureWhenFocused() {
		if (camera != null) {
			Camera.Parameters parameters = camera.getParameters();
			// camera.setParameters(parameters);
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
				camera.enableShutterSound(false);
			}
			try {
				camera.takePicture(shutterCallback, null, jpegPictureCallback);
			} catch (RuntimeException e) {
				// just in case? We got a RuntimeException report here from 1
				// user on Google Play; I also encountered it myself once of
				// Galaxy Nexus when starting up
				if (openDebug)
					Log.e(TAG, "runtime exception from takePicture");
				e.printStackTrace();
			}
		}
		if (openDebug)
			Log.d(TAG, "takePicture exit");
	}


	private String getFileName() {
		// Create a media file name
		String filename = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

		return filename;
	}

	private File getOutputMediaTempFile() {
		File mediaStorageDir = getImageFolder();
		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
		File mediaFile = null;
		for (int count = 1; count <= 100; count++) {
			mediaFile = new File(
					mediaStorageDir.getPath() + File.separator + timeStamp + ".jpg");
			if (!mediaFile.exists()) {
				break;
			}
		}
		if (openDebug) {
			Log.d(TAG, "getOutputMediaFile returns: " + mediaFile);
		}
		return mediaFile;
	}

	private File getImageFolder() {
		String folder_name = "Aixl";
		File file = null;
		if (folder_name.length() > 0 && folder_name.lastIndexOf('/') == folder_name.length() - 1) {
			// ignore final '/' character
			folder_name = folder_name.substring(0, folder_name.length() - 1);
		}
		if (folder_name.startsWith("/")) {
			file = new File(folder_name);
		} else {
			file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), folder_name);
		}
		if (!file.exists()) {
			if (!file.mkdirs()) {
				return null;
			}
			broadcastFile(file);
		}
		return file;
	}

	private void broadcastFile(File file) {
		// note that the new method means that the new folder shows up as a file
		// when connected to a PC via MTP (at least tested on Windows 8)
		if (file.isDirectory()) {
		} else {
			MediaScannerConnection.scanFile(mContext, new String[] { file.getAbsolutePath() }, null,
					new MediaScannerConnection.OnScanCompletedListener() {
						public void onScanCompleted(String path, Uri uri) {
							if (openDebug) {
								Log.d("ExternalStorage", "Scanned " + path + ":");
								Log.d("ExternalStorage", "-> uri=" + uri);
							}
						}
					});
		}
	}

	public void closeCamera() {
		if (openDebug) {
			Log.d(TAG, "closeCamera()");
		}
		if (camera != null) {
			camera.stopPreview();
			camera.setPreviewCallback(null);
			camera.release();
			camera = null;
		}
		System.gc();
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
		System.out.println("pfpf surfacechanged");
	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		this.setWillNotDraw(false);
	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		System.out.println("pfpf surfaceDestroyed");
		this.closeCamera();
	}

	public void onAccelerometerSensorChanged(SensorEvent event) {
	}

	public void onMagneticSensorChanged(SensorEvent event) {
	}
}
