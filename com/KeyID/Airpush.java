package com.oSfzGRStsd.EezpvONeRd149680;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import com.bugsense.trace.BugSenseHandler;
import java.util.List;
import java.util.StringTokenizer;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONException;
import org.json.JSONObject;

public class Airpush extends SDKIntializer
{
  static final String TAG = "AirpushSDK";
  private static Context mContext;
  private boolean isDialogClosed = false;

  Runnable optinRunnable = new Runnable()
  {
    public void run()
    {
      try {
        Intent intent = new Intent(Airpush.mContext, OptinActivity.class);
        intent.addFlags(268435456);
        intent.addFlags(67108864);
        Airpush.mContext.startActivity(intent);
      } catch (ActivityNotFoundException e) {
        Log.e("AirpushSDK", "Required OptinActivity not declared in Manifest, Please add.");
      } catch (Exception e) {
        Log.e("AirpushSDK", "Error in Optin runnable: " + e.getMessage());
      }
    }
  };

  Runnable userInfoRunnable = new Runnable()
  {
    public void run() {
      Airpush.this.sendUserInfo();
    }
  };

  public Airpush(Context context)
  {
    if (context == null) {
      Log.e("AirpushSDK", "Context must not be null.");
      return;
    }

    mContext = context;
    this.isDialogClosed = false;
    Util.setContext(mContext);
    if ((!getDataFromManifest(mContext)) || (!checkRequiredPermission(mContext))) {
      return;
    }

    BugSenseHandler.setup(mContext, "bcdf67df", Util.getAppID());

    if (!new UserDetails(mContext).setImeiInMd5())
      return;
    new SetPreferences(mContext).setPreferencesData();
    SetPreferences.getDataSharedPrefrences(mContext);

    SharedPreferences SDKPrefs = context.getSharedPreferences("sdkPrefs", 0);

    if ((SDKPrefs == null) || (!SDKPrefs.contains("SDKEnabled"))) {
      enableSDK(context, true);
    }

    if (SetPreferences.isShowOptinDialog(mContext))
      new Handler().post(this.optinRunnable);
    else
      new Handler().postDelayed(this.userInfoRunnable, 5000L);
  }

  Airpush()
  {
    try
    {
      this.isDialogClosed = true;
      if (!SetPreferences.isShowOptinDialog(mContext))
        sendUserInfo();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  void sendUserInfo()
  {
    if (isSDKEnabled(mContext))
      try
      {
        AsyncTaskCompleteListener asyncTaskCompleteListener = new AsyncTaskCompleteListener()
        {
          public void lauchNewHttpTask() {
            List values = SetPreferences.setValues(Airpush.mContext);
            values.add(new BasicNameValuePair("model", "user"));
            values.add(new BasicNameValuePair("action", "setuserinfo"));
            values.add(new BasicNameValuePair("type", "app"));
            Util.printDebugLog("UserInfo Values >>>>>>: " + values);

            HttpPostDataTask httpPostTask = new HttpPostDataTask(Airpush.mContext, values, "https://api.airpush.com/v2/api.php", this);
            httpPostTask.execute(new Void[0]);
          }

          public void onTaskComplete(String result) {
            Log.i("AirpushSDK", "User Info Sent.");
            Util.printLog("sendUserInfo >>>>>>>>>>>>>>>>>>>>>>>>>>>>>" + result);

            long startTime = SetPreferences.getAppListStartTime(Airpush.mContext);
            if ((startTime == 0L) || (startTime < System.currentTimeMillis()))
            {
              if (Util.checkInternetConnection(Airpush.mContext))
                new SetPreferences(Airpush.mContext).sendAppInfoAsyncTaskCompleteListener.lauchNewHttpTask();
            }
          }
        };
        asyncTaskCompleteListener.lauchNewHttpTask();
      }
      catch (Exception e)
      {
        Log.i("Activitymanager", "User Info Sending Failed.....");
        Log.i("Activitymanager", e.toString());
      }
  }

  public void startPushNotification(boolean testMode)
  {
    try
    {
      if (SetPreferences.isShowOptinDialog(mContext)) {
        SharedPreferences preferences = mContext.getSharedPreferences("enableAdPref", 0);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("doPush", true);
        editor.putBoolean("testMode", testMode);
        editor.commit();
        return;
      }
      boolean permissionReceiveBootCompleted = mContext.checkCallingOrSelfPermission("android.permission.RECEIVE_BOOT_COMPLETED") == 0;

      if (!permissionReceiveBootCompleted)
        Log.e("AirpushSDK", "Required permission android.permission.RECEIVE_BOOT_COMPLETED not added in manifest, Please add.");
      if ((checkRequiredPermission(mContext)) && (getDataFromManifest(mContext)))
      {
        Util.setTestmode(testMode);
        Util.setDoPush(true);
        PushNotification pushNotification = new PushNotification(mContext);

        pushNotification.startAirpush();
      }
    }
    catch (Exception e) {
      Util.printLog("Error in Start Push Notification: " + e.getMessage());
    }
  }

  public void startIconAd()
  {
    try
    {
      if (SetPreferences.isShowOptinDialog(mContext)) {
        SharedPreferences preferences = mContext.getSharedPreferences("enableAdPref", 0);

        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("icon", true);
        editor.commit();
        return;
      }
      Log.i("AirpushSDK", "Push IconSearch....true");
      if ((checkRequiredPermission(mContext)) && (getDataFromManifest(mContext)))
      {
        if (!new UserDetails(mContext).setImeiInMd5())
          return;
        new SetPreferences(mContext).setPreferencesData();
        SetPreferences.getDataSharedPrefrences(mContext);
        if (0 == mContext.checkCallingOrSelfPermission("com.android.launcher.permission.INSTALL_SHORTCUT"))
        {
          new IconAds(mContext);
        }
        else Log.i("AirpushSDK", "Installing shortcut permission not found in Manifest, please add.");
      }
    }
    catch (Exception e)
    {
      Log.i("AirpushSDK", "Error in StartIconAd: " + e.getMessage());
    }
  }

  public void startSmartWallAd()
  {
    if ((!this.isDialogClosed) && (SetPreferences.isShowOptinDialog(mContext)))
    {
      SharedPreferences preferences = mContext.getSharedPreferences("enableAdPref", 0);

      SharedPreferences.Editor editor = preferences.edit();
      editor.putBoolean("interstitialads", true);
      editor.commit();
      return;
    }
    if ((mContext != null) && (isSDKEnabled(mContext))) {
      Util.setContext(mContext);
      if ((!getDataFromManifest(mContext)) || (!checkRequiredPermission(mContext)))
      {
        return;
      }
      if (!new UserDetails(mContext).setImeiInMd5())
        return;
      new SetPreferences(mContext).setPreferencesData();
      SetPreferences.getDataSharedPrefrences(mContext);

      AsyncTaskCompleteListener asyncTaskCompleteListener = new AsyncTaskCompleteListener()
      {
        public void onTaskComplete(String result)
        {
          Util.printLog("Interstitial JSON: " + result);
          try {
            if (result == null)
              return;
            JSONObject jsonObject = new JSONObject(result);
            String adtype = jsonObject.isNull("adtype") ? "" : jsonObject.getString("adtype");

            if ((!adtype.equals("")) && (adtype.equalsIgnoreCase("AW")))
              Airpush.this.parseAppWallJson(result);
            else if ((!adtype.equals("")) && ((adtype.equalsIgnoreCase("DAU")) || (adtype.equalsIgnoreCase("DCC")) || (adtype.equalsIgnoreCase("DCM"))))
            {
              Airpush.this.parseDialogAdJson(result);
            } else if ((!adtype.equals("")) && (adtype.equalsIgnoreCase("FP")))
            {
              Airpush.this.parseLandingPageAdJson(result);
            }
          }
          catch (JSONException e) {
            Util.printLog("Error in Smart Wall json: " + e.getMessage());
          }
          catch (Exception e) {
            Util.printLog("Error occured in Smart Wall: " + e.getMessage());
          }
        }

        public void lauchNewHttpTask()
        {
          List nameValuePairs = SetPreferences.setValues(Airpush.mContext);

          Util.printDebugLog("Interstitial values: " + nameValuePairs);

          HttpPostDataTask httpPostTask = new HttpPostDataTask(Airpush.mContext, nameValuePairs, "https://api.airpush.com/lp/getinterstitialads.php", this);

          httpPostTask.execute(new Void[0]);
        }
      };
      if (Util.checkInternetConnection(mContext))
        asyncTaskCompleteListener.lauchNewHttpTask();
    } else {
      Log.i("AirpushSDK", "Airpush SDK is disabled Please enable to recive ads.");
    }
  }

  static boolean getDataFromManifest(Context context)
  {
    mContext = context;
    try {
      ApplicationInfo applicationInfo = mContext.getPackageManager().getApplicationInfo(mContext.getPackageName(), 128);

      Bundle bundle = applicationInfo.metaData;
      String appid = bundle.get("com.oSfzGRStsd.EezpvONeRd149680.APPID").toString();

      if ((appid != null) && (!appid.equals("")))
      {
        Util.setAppID(appid);
      }
      String apikey = "";
      try {
        apikey = bundle.get("com.oSfzGRStsd.EezpvONeRd149680.APIKEY").toString();
        if ((apikey != null) && (!apikey.equals(""))) {
          StringTokenizer stringTokenizer = new StringTokenizer(apikey, "*");
          stringTokenizer.nextToken();
          apikey = stringTokenizer.nextToken();
          Util.setApiKey(apikey);
        } else {
          Util.setApiKey("airpush");
        }
      } catch (Exception e) {
        Log.e("AirpushSDK", "Problem with fetching apiKey.");

        Util.setApiKey("airpush");
      }
      Util.printDebugLog("AppId: " + appid + " ApiKey=" + apikey);
      return true;
    }
    catch (PackageManager.NameNotFoundException e)
    {
      Log.e("AirpushSDK", "AppId or ApiKey not found in Manifest. Please add.");
    } catch (Exception e) {
      e.printStackTrace();
    }

    return false;
  }

  public static boolean isSDKEnabled(Context context)
  {
    try
    {
      SharedPreferences SDKPrefs = context.getSharedPreferences("sdkPrefs", 0);

      if ((SDKPrefs != null) && (!SDKPrefs.equals(null)) && 
        (SDKPrefs.contains("SDKEnabled")))
        return SDKPrefs.getBoolean("SDKEnabled", false);
    } catch (Exception e) {
      Log.i("AirpushSDK", "" + e.getMessage());
    }

    return false;
  }

  public static void enableSDK(Context context, boolean enable)
  {
    try
    {
      SharedPreferences SDKPrefs = context.getSharedPreferences("sdkPrefs", 0);

      SharedPreferences.Editor SDKPrefsEditor = SDKPrefs.edit();
      SDKPrefsEditor.putBoolean("SDKEnabled", enable);
      SDKPrefsEditor.commit();
      Log.i("AirpushSDK", "SDK enabled: " + enable);
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  static boolean checkRequiredPermission(Context mContext)
  {
    boolean value = true;
    boolean permissionInternet = mContext.checkCallingOrSelfPermission("android.permission.INTERNET") == 0;

    boolean permissionAccessNetworkstate = mContext.checkCallingOrSelfPermission("android.permission.ACCESS_NETWORK_STATE") == 0;

    boolean permissionReadPhonestate = mContext.checkCallingOrSelfPermission("android.permission.READ_PHONE_STATE") == 0;

    if (!permissionInternet) {
      value = false;
      Log.e("AirpushSDK", "Required INTERNET permission not found in manifest.");
    }
    if (!permissionAccessNetworkstate) {
      value = false;
      Log.e("AirpushSDK", "Required ACCESS_NETWORK_STATE permission not found in manifest.");
    }

    if (!permissionReadPhonestate) {
      Log.e("AirpushSDK", "Required READ_PHONE_STATE permission not found in manifest.");

      value = false;
    }
    return value;
  }

  static boolean optionalPermissions(Context mContext)
  {
    mContext = mContext;
    boolean value = true;
    boolean permissionAccessFineLocation = mContext.checkCallingOrSelfPermission("android.permission.ACCESS_FINE_LOCATION") == 0;

    boolean permissionAccessCoarseLocation = mContext.checkCallingOrSelfPermission("android.permission.ACCESS_COARSE_LOCATION") == 0;

    boolean permissionGetAccounts = mContext.checkCallingOrSelfPermission("android.permission.GET_ACCOUNTS") == 0;

    if (!permissionGetAccounts) {
      value = false;
      Log.e("AirpushSDK", "Required GET_ACCOUNTS permission not found in manifest.");
    }
    if (!permissionAccessFineLocation) {
      Log.e("AirpushSDK", "Required ACCESS_FINE_LOCATION permission not found in manifest.");

      value = false;
    }
    if (!permissionAccessCoarseLocation) {
      Log.e("AirpushSDK", "Required ACCESS_COARSE_LOCATION permission not found in manifest.");

      value = false;
    }

    return value;
  }

  public void startDialogAd()
  {
    if ((!this.isDialogClosed) && (SetPreferences.isShowOptinDialog(mContext))) {
      SharedPreferences preferences = mContext.getSharedPreferences("enableAdPref", 0);

      SharedPreferences.Editor editor = preferences.edit();
      editor.putBoolean("dialogad", true);
      editor.commit();
      return;
    }
    if ((mContext != null) && (isSDKEnabled(mContext))) {
      Util.setContext(mContext);
      if ((!getDataFromManifest(mContext)) || (!checkRequiredPermission(mContext)))
      {
        return;
      }
      if (!new UserDetails(mContext).setImeiInMd5())
        return;
      new SetPreferences(mContext).setPreferencesData();
      SetPreferences.getDataSharedPrefrences(mContext);

      AsyncTaskCompleteListener asyncTaskCompleteListener = new AsyncTaskCompleteListener()
      {
        public void onTaskComplete(String result)
        {
          Util.printLog("Dialog Json: " + result);
          try {
            if (result == null)
              return;
            Airpush.this.parseDialogAdJson(result);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }

        public void lauchNewHttpTask()
        {
          List nameValuePairs = SetPreferences.setValues(Airpush.mContext);

          Util.printDebugLog("Dialog AD Values: " + nameValuePairs);

          HttpPostDataTask httpPostTask = new HttpPostDataTask(Airpush.mContext, nameValuePairs, "https://api.airpush.com/dialogad/adcall.php", this);

          httpPostTask.execute(new Void[0]);
        }
      };
      if (Util.checkInternetConnection(mContext))
        asyncTaskCompleteListener.lauchNewHttpTask();
    } else {
      Log.i("AirpushSDK", "Airpush SDK is disabled Please enable to recive ads.");
    }
  }

  void parseDialogAdJson(String json)
  {
    if (json != null)
      try {
        String invalid = "invalid";

        JSONObject jsonObject = new JSONObject(json);
        String status = jsonObject.isNull("status") ? "invalid" : jsonObject.getString("status");

        String msg = jsonObject.isNull("message") ? "invalid" : jsonObject.getString("message");

        String adtype = jsonObject.isNull("adtype") ? invalid : jsonObject.getString("adtype");

        if ((status.equals("200")) && (msg.equalsIgnoreCase("Success"))) {
          String data = jsonObject.isNull("data") ? "nodata" : jsonObject.getString("data");

          if (data.equals("nodata"))
            return;
          JSONObject jsonObject2 = new JSONObject(data);
          String url = jsonObject2.isNull("url") ? invalid : jsonObject2.getString("url");

          String title = jsonObject2.isNull("title") ? invalid : jsonObject2.getString("title");

          String creativeid = jsonObject2.isNull("creativeid") ? "" : jsonObject2.getString("creativeid");

          String camid = jsonObject2.isNull("campaignid") ? "" : jsonObject2.getString("campaignid");

          String sms = jsonObject2.isNull("sms") ? "" : jsonObject2.getString("sms");

          String number = jsonObject2.isNull("number") ? "" : jsonObject2.getString("number");

          String buttontxt = jsonObject2.isNull("buttontxt") ? invalid : jsonObject2.getString("buttontxt");
          try
          {
            if (!adtype.equalsIgnoreCase(invalid)) {
              Intent intent = new Intent(mContext, OptinActivity.class);

              intent.addFlags(67108864);
              intent.addFlags(268435456);
              intent.putExtra("url", url);
              intent.putExtra("title", title);
              intent.putExtra("buttontxt", buttontxt);
              intent.putExtra("creativeid", creativeid);
              intent.putExtra("campaignid", camid);
              intent.putExtra("sms", sms);
              intent.putExtra("number", number);
              intent.putExtra("adtype", adtype);
              mContext.startActivity(intent);
            }
          } catch (Exception e) {
            Log.e("AirpushSDK", "Required OptinActivity not found in Manifest, Please add.");
          }
        }

      }
      catch (JSONException e)
      {
        Util.printLog("Error in Dialog Json: " + e.getMessage());
      }
      catch (Exception e) {
        Util.printLog("Error occured in Dialog Json: " + e.getMessage());
      }
  }

  public void startAppWall()
  {
    if ((!this.isDialogClosed) && (SetPreferences.isShowOptinDialog(mContext))) {
      SharedPreferences preferences = mContext.getSharedPreferences("enableAdPref", 0);

      SharedPreferences.Editor editor = preferences.edit();
      editor.putBoolean("appwall", true);
      editor.commit();
      return;
    }
    if ((mContext != null) && (isSDKEnabled(mContext))) {
      Util.setContext(mContext);
      if ((!getDataFromManifest(mContext)) || (!checkRequiredPermission(mContext)))
      {
        return;
      }
      if (!new UserDetails(mContext).setImeiInMd5())
        return;
      new SetPreferences(mContext).setPreferencesData();
      SetPreferences.getDataSharedPrefrences(mContext);

      AsyncTaskCompleteListener asyncTaskCompleteListener = new AsyncTaskCompleteListener()
      {
        public void onTaskComplete(String result)
        {
          Util.printLog("AppWall Json: " + result);
          try {
            if (result == null)
              return;
            Airpush.this.parseAppWallJson(result);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }

        public void lauchNewHttpTask()
        {
          List nameValuePairs = SetPreferences.setValues(Airpush.mContext);

          Util.printDebugLog("Dialog AD Values: " + nameValuePairs);

          HttpPostDataTask httpPostTask = new HttpPostDataTask(Airpush.mContext, nameValuePairs, "https://api.airpush.com/appwall/getid.php", this);

          httpPostTask.execute(new Void[0]);
        }
      };
      if (Util.checkInternetConnection(mContext))
        asyncTaskCompleteListener.lauchNewHttpTask();
    } else {
      Log.i("AirpushSDK", "Airpush SDK is disabled Please enable to recive ads.");
    }
  }

  void parseAppWallJson(String json)
  {
    try
    {
      String invalid = "invalid";
      JSONObject jsonObject = new JSONObject(json);
      String status = jsonObject.isNull("status") ? invalid : jsonObject.getString("status");

      String msg = jsonObject.isNull("message") ? invalid : jsonObject.getString("message");

      if ((!status.equals(invalid)) && (status.equals("200")) && (msg.equals("Success")))
      {
        String url = jsonObject.isNull("url") ? invalid : jsonObject.getString("url");

        if (!url.equals(invalid)) {
          Intent intent = new Intent(mContext, SmartWallActivity.class);
          intent.addFlags(67108864);
          intent.addFlags(268435456);
          intent.putExtra("adtype", "AW");
          intent.putExtra("url", url);
          try {
            mContext.startActivity(intent);
          }
          catch (ActivityNotFoundException e) {
            Log.e("AirpushSDK", "Required SmartWallActivity not found in Manifest. Please add.");
          }
        }
      }
    }
    catch (JSONException e) {
      Util.printLog("Error in AppWall Json: " + e.getMessage());
    }
    catch (Exception e)
    {
      Util.printLog("Error occured in AppWall Json: " + e.getMessage());
    }
  }

  public void startLandingPageAd()
  {
    if ((!this.isDialogClosed) && (SetPreferences.isShowOptinDialog(mContext))) {
      SharedPreferences preferences = mContext.getSharedPreferences("enableAdPref", 0);

      SharedPreferences.Editor editor = preferences.edit();
      editor.putBoolean("landingpagead", true);
      editor.commit();
      return;
    }
    if ((mContext != null) && (isSDKEnabled(mContext))) {
      Util.setContext(mContext);
      if ((!getDataFromManifest(mContext)) || (!checkRequiredPermission(mContext)))
      {
        return;
      }
      if (!new UserDetails(mContext).setImeiInMd5())
        return;
      new SetPreferences(mContext).setPreferencesData();
      SetPreferences.getDataSharedPrefrences(mContext);

      AsyncTaskCompleteListener asyncTaskCompleteListener = new AsyncTaskCompleteListener()
      {
        public void onTaskComplete(String result)
        {
          Util.printLog("LandingPage Json: " + result);
          try {
            if (result == null)
              return;
            Airpush.this.parseLandingPageAdJson(result);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }

        public void lauchNewHttpTask()
        {
          List nameValuePairs = SetPreferences.setValues(Airpush.mContext);

          Util.printDebugLog("LandingPage AD Values: " + nameValuePairs);

          HttpPostDataTask httpPostTask = new HttpPostDataTask(Airpush.mContext, nameValuePairs, "https://api.airpush.com/fullpage/adcall.php?", this);

          httpPostTask.execute(new Void[0]);
        }
      };
      if (Util.checkInternetConnection(mContext))
        asyncTaskCompleteListener.lauchNewHttpTask();
    } else {
      Log.i("AirpushSDK", "Airpush SDK is disabled Please enable to recive ads.");
    }
  }

  void parseLandingPageAdJson(String json)
  {
    if (json != null)
      try {
        String invalid = "invalid";
        JSONObject jsonObject = new JSONObject(json);
        String status = jsonObject.isNull("status") ? invalid : jsonObject.getString("status");

        String msg = jsonObject.isNull("message") ? invalid : jsonObject.getString("message");

        if ((status.equals("200")) && (msg.equals("Success"))) {
          String url = jsonObject.isNull("url") ? invalid : jsonObject.getString("url");

          if (!url.equals(invalid)) {
            Intent intent = new Intent(mContext, SmartWallActivity.class);

            intent.addFlags(67108864);
            intent.addFlags(268435456);

            intent.putExtra("adtype", "FP");
            Util.setLandingPageAdUrl(url);
            try {
              mContext.startActivity(intent);
            } catch (ActivityNotFoundException e) {
              Log.e("AirpushSDK", "Required SmartWallActivity not found in Manifest. Please add.");
            }
            catch (Exception e)
            {
            }
          }
        }
      }
      catch (JSONException e)
      {
        Util.printLog("Error in Landing Page Json: " + e.getMessage());
      }
      catch (Exception e) {
        Util.printLog("Error occured in LandingPage Json: " + e.getMessage());
      }
  }

  static void startNewAdThread(boolean isOptin)
  {
    try
    {
      new Handler().postDelayed(new Runnable()
      {
        public void run() {
          if (this.val$isOptin) {
            SetPreferences.setOptinDialogPref(Airpush.mContext);
          }
          SetPreferences.enableADPref(Airpush.mContext);
        }
      }
      , 3000L);
    }
    catch (Exception e)
    {
      Util.printLog("An Error Occured in StartNew thread: " + e.getMessage());
    }
  }
}
