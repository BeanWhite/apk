package com.example.pjj50.xinjitai.tool;

import java.util.Timer;
import java.util.TimerTask;

public abstract class TimerTaskUtil {
	private static TimerTask task;
	private static Timer mTimer;
	public void runNewTask(int delay){
		if(mTimer == null)
			mTimer = new Timer(true);
		if(task != null)
			task.cancel();
		task = new TimerTask() {
			
			@Override
			public void run() {
				taskTimeOut();
			}
		};
		mTimer.schedule(task, delay);
	}
	
	public void cancelTask(){
		task.cancel();
	}
	
	protected abstract void taskTimeOut();
}
