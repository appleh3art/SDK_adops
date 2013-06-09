package com.oSfzGRStsd.EezpvONeRd149680;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

class PushNotification
{
  private static final String TAG = "AirpushSDK";
  private static Context context;
  private Runnable send_Task = new Runnable() {
    public void run() {
      PushNotification.reStartSDK(PushNotification.context, true);
    }
  };

  public PushNotification(Context context)
  {
    context = context;
  }

  void startAirpush()
  {
    if (!Airpush.checkRequiredPermission(context)) {
      Log.i("AirpushSDK", "Unable to start airpush.");
      return;
    }

    if (!new UserDetails(context).setImeiInMd5())
      return;
    try {
      new SetPreferences(context).setPreferencesData();
      SetPreferences.getDataSharedPrefrences(context);

      if (Util.isTestmode()) {
        Log.i("AirpushSDK", "Airpush push notification is running in test mode.");
      }
      Log.i("AirpushSDK", "Push Notification Service...." + Util.isDoPush());
      Log.i("AirpushSDK", "Initialising push.....");

      if (Util.checkInternetConnection(context))
        new Handler().postDelayed(this.send_Task, 6000L);
      else
        reStartSDK(context, false);
    }
    catch (Exception e) {
      Util.printLog("" + e.getMessage());
    }
  }

  static void reStartSDK(Context context, boolean connectivity)
  {
    context = context;
    long timeDifference = 0L;

    if (connectivity) {
      long startTime = 0L;
      long currentTime = 0L;
      startTime = SetPreferences.getSDKStartTime(context);
      if (startTime != 0L) {
        currentTime = System.currentTimeMillis();
        if (currentTime < startTime) {
          long diff = startTime - currentTime;
          Log.i("AirpushSDK", "SDK will restart after " + diff + " ms.");
          timeDifference = diff;

          diff /= 60000L;
          Util.printDebugLog("time difference : " + diff + " minutes");
        }

      }

    }
    else
    {
      timeDifference = 1800000L;
      Util.printDebugLog("SDK will start after " + timeDifference + " ms.");
    }

    try
    {
      Intent messageIntent = new Intent(context, PushService.class);
      messageIntent.setAction("SetMessageReceiver");

      PendingIntent pendingIntent = PendingIntent.getService(context, 0, messageIntent, 0);

      AlarmManager msgAlarmMgr = (AlarmManager)context.getSystemService("alarm");

      msgAlarmMgr.setInexactRepeating(0, System.currentTimeMillis() + timeDifference + IConstants.INTERVAL_FIRST_TIME.intValue(), Util.getMessageIntervalTime(), pendingIntent);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }
}
