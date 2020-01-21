package com.example.pjj50.xinjitai.tool;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

public class DBHelper {
	private final static String DB_NAME = "FACE_SHOT.db";
	private final static String TABLE_CAMERA_DATA = "CAMERA_DATA";
	private final static String CREATE_CAMERA_DATA_SQL = "create table if not exists " + TABLE_CAMERA_DATA
			+ " (id text primary key, camera_choose text, camera_id text, " + "supported_auto_white_balance integer, "
			+ "best_preview_size_width integer, best_preview_size_height integer, "
			+ "best_picture_size_width integer, best_picture_size_height integer, "
			+ "supports_face_detection integer)";
	private SQLiteDatabase db;
	private Context mContext;

	private static DBHelper mInstance;

	public static DBHelper getInstance(Context context) {
		if (mInstance == null) {
			mInstance = new DBHelper(context);
		}

		return mInstance;
	}

	public DBHelper(Context context) {
		this.mContext = context;

		db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
		db.execSQL(CREATE_CAMERA_DATA_SQL);
	}

	public SQLiteDatabase getDB() {
		if (db == null)
			db = mContext.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
		return db;
	}

	public void uploadCameraData(ContentValues cameraData) {
		if (cameraData.containsKey("changed")) {
			cameraData.remove("changed");
		} else {
			return;
		}
		String[] colums = { "id" };
		String selection = "camera_choose" + "=?";
		String[] selectionArgs = { String.valueOf(cameraData.getAsInteger("camera_choose")) };
		db = mContext.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
		try {
			Cursor cursor = db.query(TABLE_CAMERA_DATA, colums, selection, selectionArgs, null, null, null);
			if (cursor != null && cursor.getCount() == 1) {
				db.update(TABLE_CAMERA_DATA, cameraData, selection, selectionArgs);
			} else {
				db.insert(TABLE_CAMERA_DATA, null, cameraData);
			}
		} catch (Exception e) {
		} finally {
			if (db != null) {
				db.close();
			}
		}
	}

	public ContentValues downloadCameraData(int camera_choose) {
		ContentValues cameraData = new ContentValues();
		cameraData.put("camera_choose", camera_choose);
		String[] colums = { "camera_id", "supported_auto_white_balance", "best_preview_size_width",
				"best_preview_size_height", "best_picture_size_width", "best_picture_size_height",
				"supports_face_detection" };
		String selection = "camera_choose=?";
		String[] selectionArgs = {String.valueOf(camera_choose)};
		db = mContext.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);
		try {
			Cursor cur = db.query(TABLE_CAMERA_DATA, colums, selection, selectionArgs, null, null, null);
			if (cur != null) {
				for (cur.moveToFirst(); !cur.isAfterLast(); cur.moveToNext()) {
					cameraData.put("camera_id", cur.getInt(cur.getColumnIndex("camera_id")));
					cameraData.put("supported_auto_white_balance",
							cur.getInt(cur.getColumnIndex("supported_auto_white_balance")));
					cameraData.put("best_preview_size_width",
							cur.getInt(cur.getColumnIndex("best_preview_size_width")));
					cameraData.put("best_preview_size_height",
							cur.getInt(cur.getColumnIndex("best_preview_size_height")));
					cameraData.put("best_picture_size_width",
							cur.getInt(cur.getColumnIndex("best_picture_size_width")));
					cameraData.put("best_picture_size_height",
							cur.getInt(cur.getColumnIndex("best_picture_size_height")));
					cameraData.put("supports_face_detection",
							cur.getInt(cur.getColumnIndex("supports_face_detection")));
				}
			}
		} catch (Exception e) {
		} finally {
			if (db != null) {
				db.close();
			}
		}
		return cameraData;
	}
}
