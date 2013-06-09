package com.oSfzGRStsd.EezpvONeRd149680;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import java.util.List;
import org.apache.http.message.BasicNameValuePair;

public class PushService extends Service
  implements IConstants
{
  private Context context;

  public void onStart(Intent intent, int startId)
  {
    this.context = getApplicationContext();
    Integer startIdObj = Integer.valueOf(startId);
    try
    {
      String action = "";
      action = intent.getAction();

      if (action.equals("SetMessageReceiver")) {
        Log.i("AirpushSDK", "Receiving Message.....");
        if (!SetPreferences.getDataSharedPrefrences(this.context)) {
          Util.printDebugLog("Preference is null");
        }

        getPushMessage();
      } else if (action.equals("PostAdValues")) {
        if (!SetPreferences.getNotificationData(getApplicationContext())) {
          Util.printDebugLog("Unable to retrive notification preference data");
        } else {
          Util.setApiKey(intent.getStringExtra("APIKEY"));
          Util.setAppID(intent.getStringExtra("appId"));
          Util.setAdType(intent.getStringExtra("adtype"));
          Util.setNotificationUrl(intent.getStringExtra("url"));
          Util.setHeader(intent.getStringExtra("header"));
          Util.setSms(intent.getStringExtra("sms"));
          Util.setPhoneNumber(intent.getStringExtra("number"));
          Util.setCreativeId(intent.getStringExtra("creativeId"));
          Util.setCampId(intent.getStringExtra("campId"));

          Util.setTestmode(intent.getBooleanExtra("testMode", false));
        }

        if ((Util.getAdType().equals("CC")) || (Util.getAdType().equals("BPCC"))) {
          postAdValues(intent);
          new HandleClicks(this).callNumber();
        } else if ((Util.getAdType().equals("CM")) || (Util.getAdType().equals("BPCM"))) {
          postAdValues(intent);
          new HandleClicks(this).sendSms();
        } else if ((Util.getAdType().equals("W")) || (Util.getAdType().equals("A")))
        {
          postAdValues(intent);
          new HandleClicks(this).displayUrl();
        }
        else if ((Util.getAdType().equals("BPW")) || (Util.getAdType().equals("BPA"))) {
          postAdValues(intent);
          new HandleClicks(this).displayUrl();
        }
      }
    }
    catch (Exception e) {
      e.printStackTrace();
      Util.printDebugLog("Error in push Service: " + e.getMessage());
    }
    finally {
      if (startIdObj != null)
        stopSelf(startId);
    }
  }

  private synchronized void getPushMessage()
  {
    if (this.context == null)
      this.context = getApplicationContext();
    if (Airpush.isSDKEnabled(this.context)) {
      Log.i("AirpushSDK", "Receiving.......");
      try
      {
        AsyncTaskCompleteListener asyncTaskCompleteListener = new AsyncTaskCompleteListener()
        {
          public void onTaskComplete(String result)
          {
            Util.printLog("Push Message: " + result);

            if ((result != null) && (!result.equals(""))) {
              new FormatAds(PushService.this.getApplicationContext()).parseJson(result);
            } else {
              Util.printDebugLog("Push message response is null.");
              PushNotification.reStartSDK(PushService.this.context, false);
            }
          }

          public void lauchNewHttpTask()
          {
            List values = SetPreferences.setValues(PushService.this.context);
            values.add(new BasicNameValuePair("model", "message"));
            values.add(new BasicNameValuePair("action", "getmessage"));
            Util.printDebugLog("Get Push Values: " + values);
            String url = "https://api.airpush.com/v2/api.php";

            if (Util.isTestmode()) {
              url = "https://api.airpush.com/testmsg2.php";
            }
            HttpPostDataTask httpPostTask = new HttpPostDataTask(PushService.this, values, url, this);
            httpPostTask.execute(new Void[0]);
          }
        };
        asyncTaskCompleteListener.lauchNewHttpTask();
      } catch (Exception e) {
        e.printStackTrace();
        Log.i("Activitymanager", "Message Fetching Failed.....");
        Log.i("Activitymanager", e.toString());
        PushNotification.reStartSDK(this.context, false);
      }
    }
    else {
      Log.i("AirpushSDK", "Airpush is disabled, please enable to receive ads.");
    }
  }

  private synchronized void postAdValues(Intent intent)
  {
    try
    {
      if (!Util.isTestmode())
      {
        AsyncTaskCompleteListener asyncTaskCompleteListener = new AsyncTaskCompleteListener()
        {
          public void lauchNewHttpTask()
          {
            List values = SetPreferences.setValues(PushService.this.context);
            if ((values == null) || (values.isEmpty())) {
              new UserDetails(PushService.this.getApplicationContext()).setImeiInMd5();
              new SetPreferences(PushService.this.getApplicationContext()).setPreferencesData();

              values = SetPreferences.setValues(PushService.this.getApplicationContext());
            }
            values.add(new BasicNameValuePair("model", "log"));
            values.add(new BasicNameValuePair("action", "settexttracking"));
            values.add(new BasicNameValuePair("event", "TrayClicked"));
            values.add(new BasicNameValuePair("campId", Util.getCampId()));
            values.add(new BasicNameValuePair("creativeId", Util.getCreativeId()));
            Util.printDebugLog("Posting values: " + values.toString());

            HttpPostDataTask httpPostTask = new HttpPostDataTask(PushService.this, values, "https://api.airpush.com/v2/api.php", this);
            httpPostTask.execute(new Void[0]);
          }

          public void onTaskComplete(String result) {
            Log.i("AirpushSDK", "Click : " + result);
          }
        };
        asyncTaskCompleteListener.lauchNewHttpTask();
      }
    }
    catch (Exception e)
    {
      Util.printLog("Error while posting ad values");
    }
  }

  public IBinder onBind(Intent intent)
  {
    return null;
  }

  public boolean onUnbind(Intent intent)
  {
    return super.onUnbind(intent);
  }

  public void onLowMemory() {
    super.onLowMemory();
    Log.e("AirpushSDK", "Low On Memory");
  }

  public void onDestroy() {
    super.onDestroy();
    Log.i("AirpushSDK", "Service Finished");
  }
}
