package com.example.test;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class SystemStartReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		// TODO Auto-generated method stub
		if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())){
			Intent i=new Intent();
			i.setClass(context, MainService.class);
			context.startService(i);
			
		}
	}

}
