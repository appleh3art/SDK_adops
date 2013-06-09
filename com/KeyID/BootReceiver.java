package com.oSfzGRStsd.EezpvONeRd149680;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

public class BootReceiver extends BroadcastReceiver
{
  public void onReceive(final Context arg0, Intent arg1)
  {
    try
    {
      if (!SetPreferences.getDataSharedPrefrences(arg0))
        return;
      new Handler().postDelayed(new Runnable() {
        public void run() {
          if ((!SetPreferences.isShowOptinDialog(arg0)) && (Util.isDoPush())) {
            new PushNotification(arg0).startAirpush();
            Util.printLog("Airpush SDK started from BootReciver.");
          }
        }
      }
      , 4000L);
    }
    catch (Exception e)
    {
      Util.printLog("Error occurred while starting BootReciver. " + e.getMessage());
    }
  }
}
