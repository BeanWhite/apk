package com.example.pjj50.xinjitai;


import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;

public class LoadService extends Activity implements ShotService.ServiceStartCallBack {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_load_service);

	}
	@Override
	protected void onResume() {
		super.onResume();
		ShotService.addServiceStartListener(this);
		startService(new Intent(this, ShotService.class));
	}

	@Override
	public void doServiceStart() {
		ShotService.removeServiceStartListener();
		Intent intent = new Intent(this, SpalshActivity.class);
		startActivity(intent);
		this.finish();
	}
}
