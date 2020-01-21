package com.example.pjj50.xinjitai.tool;

import java.io.ByteArrayOutputStream;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.graphics.YuvImage;
import android.graphics.Bitmap.Config;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.media.FaceDetector;
import android.util.Log;

import com.example.pjj50.xinjitai.ShotService;

public class FaceDetectTool {
	public static Bitmap rotBmpToDisplay(Bitmap bmp_temp, int display_rotation) {
		Matrix m = new Matrix();
		m.setRotate(display_rotation, (float) bmp_temp.getWidth() / 2, (float) bmp_temp.getHeight() / 2);
		Bitmap bitmap = Bitmap.createBitmap(bmp_temp, 0, 0, bmp_temp.getWidth(), bmp_temp.getHeight(), m, true);
		if (bitmap != bmp_temp) {
			bmp_temp.recycle();
			bmp_temp = null;
		}
		return bitmap;
	}

	public static Bitmap decodeToBitMap(byte[] data, Camera mCamera, int display_rotation) {
		Size size = mCamera.getParameters().getPreviewSize();
		Bitmap bitmap = null;
		try {
			YuvImage image = new YuvImage(data, ImageFormat.NV21, size.width, size.height, null);
			if (image != null) {
				ByteArrayOutputStream stream = new ByteArrayOutputStream();
				image.compressToJpeg(new Rect(0, 0, size.width, size.height), 100, stream);
				Bitmap bmp_temp = BitmapFactory.decodeByteArray(stream.toByteArray(), 0, stream.size());
				stream.close();
				bitmap = rotBmpToDisplay(bmp_temp, display_rotation);
			}
		} catch (Exception ex) {
			Log.e("Sys", "Error:" + ex.getMessage());
		}
		return bitmap;
	}

	public static boolean faceDeted(Bitmap bitmap) {
		if (bitmap == null) {
			return false;
		}
		Bitmap srcFace = bitmap.copy(Config.RGB_565, true);
		boolean foundFace = false;
		int N_MAX = 2;
		int w = srcFace.getWidth();
		int h = srcFace.getHeight();
		FaceDetector faceDetector = new FaceDetector(w, h, N_MAX);
		FaceDetector.Face[] face = new FaceDetector.Face[N_MAX];
		int nFace = faceDetector.findFaces(srcFace, face);
		if (nFace > 0) {
			foundFace = true;
			ShotService.insertFaceItem();
		}
		ShotService.unlockFaceDeteced();
		srcFace.recycle();
		srcFace = null;
		return foundFace;
	}
}
