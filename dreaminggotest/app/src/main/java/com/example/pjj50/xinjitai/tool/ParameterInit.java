package com.example.pjj50.xinjitai.tool;

import java.util.List;
import java.util.Vector;

import android.content.ContentValues;
import android.hardware.Camera;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.Parameters;
import android.view.Surface;

import com.example.pjj50.xinjitai.ShotService;

public class ParameterInit {
	public static int initCameraId(ContentValues cameraData) {
		if (cameraData.containsKey("camera_id")) {
			return cameraData.getAsInteger("camera_id");
		}
		int n_cameras = Camera.getNumberOfCameras();
		switch (cameraData.getAsInteger("camera_choose")) {
		case CameraSettingInfo.camera_fornt:
			for (int i = 0; i < n_cameras; i++) {
				CameraInfo info = new CameraInfo();
				Camera.getCameraInfo(i, info);
				if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
					cameraData.put("camera_id", i);
					cameraData.put("changed", true);
					return i;
				}
			}
			break;
		case CameraSettingInfo.camera_back:
			for (int i = 0; i < n_cameras; i++) {
				CameraInfo info = new CameraInfo();
				Camera.getCameraInfo(i, info);
				if (info.facing == CameraInfo.CAMERA_FACING_BACK) {
					cameraData.put("camera_id", i);
					cameraData.put("changed", true);
					return i;
				}
			}
			break;
		}
		return 0;
	}

	public static int initDisplayOrientation(Camera camera, CameraInfo info) {
		int rotation = ShotService.getWindowM().getDefaultDisplay().getRotation();
		int degrees = 0;
		switch (rotation) {
		case Surface.ROTATION_0:
			degrees = 0;
			break;
		case Surface.ROTATION_90:
			degrees = 90;
			break;
		case Surface.ROTATION_180:
			degrees = 180;
			break;
		case Surface.ROTATION_270:
			degrees = 270;
			break;
		}

		int result = 0;
		if (info.facing == CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			// result = (360 - result) % 360; // compensate the mirror
		} else { // back-facing
			result = (info.orientation - degrees + 360) % 360;
		}
		camera.setDisplayOrientation(result);
		return result;
	}

	public static void initWhiteBalance(Parameters parameters, ContentValues cameraData) {
		if (cameraData.containsKey("supported_auto_white_balance")) {
			boolean supportedWhiteBalance = cameraData.getAsBoolean("supported_auto_white_balance");
			if (supportedWhiteBalance)
				parameters.setWhiteBalance(Parameters.WHITE_BALANCE_AUTO);
			return;
		}
		List<String> white_balances = parameters.getSupportedWhiteBalance();
		if (white_balances.contains(Parameters.WHITE_BALANCE_AUTO)) {
			parameters.setWhiteBalance(Parameters.WHITE_BALANCE_AUTO);
			cameraData.put("supported_auto_white_balance", 1);
		} else {
			cameraData.put("supported_auto_white_balance", 0);
		}
		cameraData.put("changed", true);
	}

	public static void initExposures(Parameters parameters) {
		List<String> exposures = null;
		int min_exposure = parameters.getMinExposureCompensation();
		int max_exposure = parameters.getMaxExposureCompensation();
		if (min_exposure != 0 || max_exposure != 0)
			exposures = new Vector<String>();
		for (int i = min_exposure; i <= max_exposure; i++) {
			exposures.add("" + i);
		}
		String exposure_s = "0";
		if (exposure_s != null) {
			try {
				int exposure = Integer.parseInt(exposure_s);
				parameters.setExposureCompensation(exposure);
			} catch (NumberFormatException exception) {
			}
		}
	}

	public static void initPreviewSize(Parameters parameters, ContentValues cameraData) {
		if (cameraData.containsKey("best_preview_size_width") && cameraData.containsKey("best_preview_size_height")) {
			int best_size_width = cameraData.getAsInteger("best_preview_size_width");
			int best_size_height = cameraData.getAsInteger("best_preview_size_height");
			parameters.setPreviewSize(best_size_width, best_size_height);
			return;
		}
		List<Camera.Size> supported_preview_sizes = parameters.getSupportedPreviewSizes();
		Camera.Size best_size = null;
		if (supported_preview_sizes.size() > 0) {
			for (int i = 0; i < supported_preview_sizes.size(); i++) {
				Camera.Size size = supported_preview_sizes.get(i);
				if (best_size == null) {
					best_size = size;
				} else if ((size.width * size.height < best_size.width * best_size.height)
						&& (best_size.width >= CameraSettingInfo.camera_quality_max
								|| best_size.height >= CameraSettingInfo.camera_quality_max)) {
					best_size = size;
				}
			}
			parameters.setPreviewSize(best_size.width, best_size.height);
			cameraData.put("best_preview_size_width", best_size.width);
			cameraData.put("best_preview_size_height", best_size.height);
			cameraData.put("changed", true);
		}
	}

	public static void initPictureSize(Parameters parameters, ContentValues cameraData) {
		if (cameraData.containsKey("best_picture_size_width") && cameraData.containsKey("best_picture_size_height")) {
			int best_size_width = cameraData.getAsInteger("best_picture_size_width");
			int best_size_height = cameraData.getAsInteger("best_picture_size_height");
			parameters.setPictureSize(best_size_width, best_size_height);
			return;
		}
		List<Camera.Size> sizes = parameters.getSupportedPictureSizes();
		if (sizes.size() > 0) {
			// set to largest
			Camera.Size current_size = null;
			for (int i = 0; i < sizes.size(); i++) {
				Camera.Size size = sizes.get(i);
				if (current_size == null || size.width * size.height > current_size.width * current_size.height) {
					current_size = size;
				}
			}
			parameters.setPictureSize(current_size.width, current_size.height);
			cameraData.put("best_picture_size_width", current_size.width);
			cameraData.put("best_picture_size_height", current_size.height);
			cameraData.put("changed", true);
		}

	}
	
	public static boolean initFaceDetection(Camera camera, Parameters parameters, ContentValues cameraData){
		boolean supports_face_detection = false;
		if (cameraData.containsKey("supports_face_detection")) {
			supports_face_detection = cameraData.getAsBoolean("supports_face_detection");
		}else{
			supports_face_detection = parameters.getMaxNumDetectedFaces() > 0;
		}
		if (supports_face_detection) {
			cameraData.put("supports_face_detection", 1);
			cameraData.put("changed", true);
		}else{
			cameraData.put("supports_face_detection", 0);
			cameraData.put("changed", true);
		}
		return supports_face_detection;
	}
}
