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
		// ������ip������
//		tpush.setIp("192.168.0.50");
		// ���ͽӿ� �˿�
//		tpush.setPort("9080");
		// ���ͷ��� �˿�
//		tpush.setXmppPort("15222");
		
		// ��ʼ��push ����豸��ע�ᣬ�������ͷ�����
		initPush();
		initAlarmManager();

			
		//  ���ñ���    �����豸���Ͷ�̬����  �� samsung-iemi-123346090912367 ����samsung imei���Զ�̬��ȡ
		tpush.setAlias(MainService.this, "lcy123", pushPostListener);
		// ���ñ�ǩ  �ɸ���sim���������ؽ������� 
		tpush.setTags(MainService.this, "shanghai", pushPostListener);
		// ��������ʱ�� ��9:00-22:00 ���ͽ��ղ���ʾ,����ʱ����ղ���ʾ��������
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
				msgStr = "����ʧ��,���ڳ�����������:"+isError;
				isPush = false;
			} else {
				msgStr = "�����ɹ�"; 
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