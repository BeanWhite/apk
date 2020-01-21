package com.example.pjj50.xinjitai.tool;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

public class SharedPreferenceUtil {
	
	public static void savePreference(Context context, String key, String value){
		Context appContext = context.getApplicationContext();
		SharedPreferences sharedPreferences = appContext.getSharedPreferences(appContext.getPackageName(),
				Activity.MODE_PRIVATE);
		SharedPreferences.Editor ethEditor = sharedPreferences.edit();
		ethEditor.putString(key, value);
		ethEditor.commit();
	}
	
	public static void savePreference(Context context, String key, int value){
		Context appContext = context.getApplicationContext();
		SharedPreferences sharedPreferences = appContext.getSharedPreferences(appContext.getPackageName(),
				Activity.MODE_PRIVATE);
		SharedPreferences.Editor ethEditor = sharedPreferences.edit();
		ethEditor.putInt(key, value);
		ethEditor.commit();
	}
	
	public static void savePreference(Context context, String key, boolean value){
		Context appContext = context.getApplicationContext();
		SharedPreferences sharedPreferences = appContext.getSharedPreferences(appContext.getPackageName(),
				Activity.MODE_PRIVATE);
		SharedPreferences.Editor ethEditor = sharedPreferences.edit();
		ethEditor.putBoolean(key, value);
		ethEditor.commit();
	}
	
	public static String getPreference(Context context, String key, String default_value){
		Context appContext = context.getApplicationContext();
		SharedPreferences sharedPreferences = appContext.getSharedPreferences(appContext.getPackageName(),
				Activity.MODE_PRIVATE);
		return sharedPreferences.getString(key, default_value);
	}
	
	public static int getPreference(Context context, String key, int default_value){
		Context appContext = context.getApplicationContext();
		SharedPreferences sharedPreferences = appContext.getSharedPreferences(appContext.getPackageName(),
				Activity.MODE_PRIVATE);
		return sharedPreferences.getInt(key, default_value);
	}
	
	public static boolean getPreference(Context context, String key, boolean default_value){
		Context appContext = context.getApplicationContext();
		SharedPreferences sharedPreferences = appContext.getSharedPreferences(appContext.getPackageName(),
				Activity.MODE_PRIVATE);
		return sharedPreferences.getBoolean(key, default_value);
	}

}
