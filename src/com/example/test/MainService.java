package com.example.test;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.widget.Toast;

import com.techown.push.client.PushListener;
import com.techown.push.client.TechownPushPostListener;
import com.techown.push.service.TechownPush;


public class MainService extends Service {

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public TechownPush tpush;
	private PushPostListener pushPostListener;

	@Override
	public void onCreate() {
		super.onCreate();
		tpush = new TechownPush(this, new PushListener() {
			@Override
			public void push(String code, String message) {
				System.out.println("code" + code);
				System.out.println("message" + message);
			}
		});
		pushPostListener = new PushPostListener();

//		tpush.addBeginDownloadListener(new BeginDownloadListener() {
//			@Override
//			public String openDownloadDir() {
//
//				return "/mnt/sdcard/1111.png";
//			}
//
//			@Override
//			public void Downloadsucceed() {
//				Toast.makeText(MainService.this, "Download Success",
//						Toast.LENGTH_LONG);
//			}
//
//			@Override
//			public void DownloadFailure() {
//				Toast.makeText(MainService.this, "Download Failure",
//						Toast.LENGTH_LONG);
//			}
//		});
//		tpush.setAppkey("b72f16e98ada487789e4616a0153ba39");
		tpush.setNotificationIcon(R.drawable.ic_launcher);
		IntentFilter filter1 = new IntentFilter();
		filter1.addAction("ELITOR_CLOCK");
		filter1.setPriority(Integer.MAX_VALUE);
		registerReceiver(isPushReceiver, filter1);
		tpush.setIp("emmqa.shanghaigm.com");
		tpush.setXmppPort("5222");
		tpush.setPort("80");
//		tpush.setHttps(true);
//		tpush.setIp("tpush.techown.com");
//		tpush.setXmppPort("15222");
//		tpush.setPort("9443");
		// 服务器ip或域名
//		tpush.setIp("192.168.0.50");
		// 推送接口 端口
//		tpush.setPort("9080");
		// 推送服务 端口
//		tpush.setXmppPort("15222");
		
		// 初始化push 完成设备的注册，连接推送服务器
		initPush();
		initAlarmManager();

			
		//  设置别名    根据设备类型动态设置  如 samsung-iemi-123346090912367 其中samsung imei可以动态获取
		tpush.setAlias(MainService.this, "lcy123", pushPostListener);
		// 设置标签  可根据sim卡的所属地进行设置 
		tpush.setTags(MainService.this, "shanghai", pushPostListener);
		// 设置推送时间 在9:00-22:00 推送接收并显示,其他时间接收不显示，不提醒
		tpush.setPushTime(9, 22);
	}

	PendingIntent piClock;

	private void initAlarmManager() {

		Intent intent = new Intent("ELITOR_CLOCK");

		piClock = PendingIntent.getBroadcast(this, 0, intent, 0);

		AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

		am.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(),
				30 * 1000, piClock);
	}

	private void cancelAlarmManager() {
		if (piClock != null) {
			AlarmManager am = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

			am.cancel(piClock);
		}

	}

	private void initPush() {
		tpush.init(pushPostListener);

	}

	private boolean isPush;
	private BroadcastReceiver isPushReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (!isPush) {
				initPush();
			}

		}

	};

	class PushPostListener implements TechownPushPostListener {

		public void postListener(String errorCode,boolean isError) {
 			if (isError&&!"0".equals(errorCode)) {
				msgStr = "启动失败,正在尝试重新连接:"+isError;
				isPush = false;
			} else {
				msgStr = "启动成功"; 
				isPush = true;
				cancelAlarmManager();
			}

			h.sendMessage(h.obtainMessage());

		}

	}

	String msgStr;
	Handler h = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			Toast.makeText(MainService.this, msgStr, Toast.LENGTH_SHORT).show();
		}

	};

	public static boolean isDestroy;

	@Override
	public void onDestroy() {
		if (isDestroy) {

			if (tpush != null) {
				tpush.stopPush();
			}
			try {
				unregisterReceiver(isPushReceiver);
			} catch (Exception e) {
				e.printStackTrace();
			}

		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		flags = START_STICKY;
		return super.onStartCommand(intent, flags, startId);
		// return START_REDELIVER_INTENT;
	}
}