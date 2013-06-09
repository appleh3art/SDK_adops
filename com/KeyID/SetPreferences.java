package com.oSfzGRStsd.EezpvONeRd149680;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build.VERSION;
import android.util.Log;
import android.webkit.WebSettings;
import android.webkit.WebView;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

class SetPreferences
  implements IConstants
{
  private static Context ctx;
  static JSONObject json = null;
  private String encodedAsp;
  private static String token = "0";
  static List<NameValuePair> values;
  static String postValues;
  private static SharedPreferences preferences;
  AsyncTaskCompleteListener<String> sendAppInfoAsyncTaskCompleteListener = new AsyncTaskCompleteListener()
  {
    public void lauchNewHttpTask()
    {
      if (Airpush.isSDKEnabled(SetPreferences.ctx))
        try
        {
          new Thread(new Runnable()
          {
            public void run()
            {
              StringBuilder builder = new StringBuilder();
              PackageManager pm = SetPreferences.ctx.getPackageManager();
              List apps = pm.getInstalledApplications(128);

              for (ApplicationInfo app : apps) {
                String dataString = "\"" + app.packageName + "\"";

                builder.append(dataString + ",");
              }

              String app_data = builder.toString();
              List values = new ArrayList();
              values.add(new BasicNameValuePair("imei", Util.getImei()));
              values.add(new BasicNameValuePair("inputlist", app_data));
              Util.printDebugLog("App Info Values >>>>>>: " + values);

              HttpPostDataTask httpPostTask = new HttpPostDataTask(SetPreferences.ctx, values, "https://api.airpush.com/lp/log_sdk_request.php", SetPreferences.this.sendAppInfoAsyncTaskCompleteListener);
              httpPostTask.execute(new Void[0]);
            }
          }).start();
        }
        catch (Exception e)
        {
          Log.i("Activitymanager", "App Info Sending Failed.....");
          Log.i("Activitymanager", e.toString());
        }
    }

    public void onTaskComplete(String result)
    {
      Util.printDebugLog("App info result: " + result);
      SetPreferences.nextAppListStartTime(SetPreferences.ctx);
    }
  };

  public SetPreferences(Context context)
  {
    ctx = context;
  }

  void setPreferencesData()
  {
    try
    {
      String user_agent = new WebView(ctx).getSettings().getUserAgentString();
      Util.setUser_agent(user_agent);

      UserDetails userDetails = new UserDetails(ctx);
      try {
        Location location = userDetails.getLocation();
        if (location != null) {
          String lat = "" + location.getLatitude();
          String lon = "" + location.getLongitude();
          Util.printDebugLog("Location: lat " + lat + ", lon " + lon);
          Util.setLatitude(lat);
          Util.setLongitude(lon);
        } else {
          Util.printDebugLog("Location null: ");
        }
      } catch (Exception e) {
        e.printStackTrace();
      }
      token = userDetails.getImeiNoMd5() + "" + Util.getAppID() + "" + Util.getDate();
      MessageDigest mdEnc2 = MessageDigest.getInstance("MD5");
      mdEnc2.update(token.getBytes(), 0, token.length());
      token = new BigInteger(1, mdEnc2.digest()).toString(16);

      setSharedPreferences();
    }
    catch (Exception e)
    {
      Util.printDebugLog("Token conversion Error ");
    }
  }

  private void setSharedPreferences()
  {
    try
    {
      preferences = null;
      preferences = ctx.getSharedPreferences("dataPrefs", 0);

      SharedPreferences.Editor dataPrefsEditor = preferences.edit();

      dataPrefsEditor.putString("APIKEY", Util.getApiKey());
      dataPrefsEditor.putString("appId", Util.getAppID());
      dataPrefsEditor.putString("imei", Util.getImei());
      dataPrefsEditor.putInt("wifi", Util.getConnectionType(ctx));
      dataPrefsEditor.putString("token", token);
      dataPrefsEditor.putString("request_timestamp", Util.getDate());
      dataPrefsEditor.putString("packageName", Util.getPackageName(ctx));
      dataPrefsEditor.putString("version", Util.getVersion());
      dataPrefsEditor.putString("carrier", Util.getCarrier(ctx));
      dataPrefsEditor.putString("networkOperator", Util.getNetworkOperator(ctx));
      dataPrefsEditor.putString("phoneModel", Util.getPhoneModel());
      dataPrefsEditor.putString("manufacturer", Util.getManufacturer());
      dataPrefsEditor.putString("longitude", Util.getLongitude());
      dataPrefsEditor.putString("latitude", Util.getLatitude());
      dataPrefsEditor.putString("sdkversion", Util.getSDKVersion());
      dataPrefsEditor.putString("android_id", Util.getAndroidId(ctx));
      dataPrefsEditor.putBoolean("testMode", Util.isTestmode());
      dataPrefsEditor.putBoolean("doPush", Util.isDoPush());

      dataPrefsEditor.putString("screenSize", Util.getScreen_size(ctx));
      dataPrefsEditor.putString("networkSubType", Util.getNetworksubType(ctx));
      dataPrefsEditor.putString("deviceUniqueness", Util.getDevice_unique_type());
      dataPrefsEditor.putInt("icon", Util.getIcon());
      dataPrefsEditor.putString("useragent", Util.getUser_agent());

      String asp = Util.getAppID() + Util.getImei() + Util.getConnectionType(ctx) + token + Util.getDate() + Util.getPackageName(ctx) + Util.getVersion() + Util.getCarrier(ctx) + Util.getNetworkOperator(ctx) + Util.getPhoneModel() + Util.getManufacturer() + Util.getLongitude() + Util.getLatitude() + Util.getUser_agent();

      this.encodedAsp = Base64.encodeString(asp);
      dataPrefsEditor.putString("asp", this.encodedAsp);

      dataPrefsEditor.commit();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  static boolean getDataSharedPrefrences(Context context)
  {
    try
    {
      preferences = null;
      preferences = context.getSharedPreferences("dataPrefs", 0);

      if (preferences != null) {
        Util.setAppID(preferences.getString("appId", "invalid"));
        Util.setApiKey(preferences.getString("APIKEY", "airpush"));
        Util.setImei(preferences.getString("imei", "invalid"));
        Util.setTestmode(preferences.getBoolean("testMode", false));
        Util.setDoPush(preferences.getBoolean("doPush", false));
        token = preferences.getString("token", "invalid");
        Util.setLongitude(preferences.getString("longitude", "0"));
        Util.setLatitude(preferences.getString("latitude", "0"));
        Util.setIcon(preferences.getInt("icon", 17301620));
        Util.setUser_agent(preferences.getString("useragent", "Default"));
        Util.setDevice_unique_type(preferences.getString("deviceUniqueness", "invalid"));

        return true;
      }
      Util.setAppInfo(ctx);
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return false;
  }

  static List<NameValuePair> setValues(Context context)
  {
    try
    {
      ctx = context;
      getDataSharedPrefrences(ctx);

      values = new ArrayList();
      values.add(new BasicNameValuePair("APIKEY", Util.getApiKey()));
      values.add(new BasicNameValuePair("appId", Util.getAppID()));
      values.add(new BasicNameValuePair("imei", Util.getImei()));
      values.add(new BasicNameValuePair("token", token));
      values.add(new BasicNameValuePair("request_timestamp", Util.getDate()));
      values.add(new BasicNameValuePair("packageName", Util.getPackageName(ctx)));
      values.add(new BasicNameValuePair("version", Util.getVersion()));
      values.add(new BasicNameValuePair("carrier", Util.getCarrier(ctx)));
      values.add(new BasicNameValuePair("networkOperator", Util.getNetworkOperator(ctx)));
      values.add(new BasicNameValuePair("phoneModel", Util.getPhoneModel()));
      values.add(new BasicNameValuePair("manufacturer", Util.getManufacturer()));
      values.add(new BasicNameValuePair("longitude", Util.getLongitude()));
      values.add(new BasicNameValuePair("latitude", Util.getLatitude()));
      values.add(new BasicNameValuePair("sdkversion", Util.getSDKVersion()));
      values.add(new BasicNameValuePair("wifi", "" + Util.getConnectionType(ctx)));
      values.add(new BasicNameValuePair("useragent", Util.getUser_agent()));
      values.add(new BasicNameValuePair("android_id", Util.getAndroidId(ctx)));
      values.add(new BasicNameValuePair("screenSize", Util.getScreen_size(ctx)));
      values.add(new BasicNameValuePair("deviceUniqueness", Util.getDevice_unique_type()));
      values.add(new BasicNameValuePair("networkSubType", Util.getNetworksubType(ctx)));
      values.add(new BasicNameValuePair("isTablet", String.valueOf(Util.isTablet(ctx))));
      values.add(new BasicNameValuePair("SD", Util.getScreenDp(ctx)));
      values.add(new BasicNameValuePair("isConnectionFast", "" + Util.isConnectionFast(ctx)));
      values.add(new BasicNameValuePair("unknownsource", "" + Util.isInstallFromMarketOnly(ctx)));
      values.add(new BasicNameValuePair("appName", Util.getAppName(ctx)));
      try
      {
        if (Build.VERSION.SDK_INT >= 5) {
          values.add(new BasicNameValuePair("email", "" + Extras.getEmail(ctx)));
        }

        values.add(new BasicNameValuePair("phonenumber", "" + Util.getNumber(ctx)));

        values.add(new BasicNameValuePair("language", "" + Util.getLanguage()));

        String[] country = Util.getCountryName(ctx);
        values.add(new BasicNameValuePair("country", "" + country[0]));
        values.add(new BasicNameValuePair("zip", "" + country[1]));
      }
      catch (Exception e)
      {
      }

      postValues = "https://api.airpush.com/v2/api.php?apikey=" + Util.getApiKey() + "&appId=" + Util.getAppID() + "&imei=" + Util.getImei() + "&token=" + token + "&request_timestamp=" + Util.getDate() + "&packageName=" + Util.getPackageName(ctx) + "&version=" + Util.getVersion() + "&carrier=" + Util.getCarrier(ctx) + "&networkOperator=" + Util.getNetworkOperator(ctx) + "&phoneModel=" + Util.getPhoneModel() + "&manufacturer=" + Util.getManufacturer() + "&longitude=" + Util.getLongitude() + "&latitude=" + Util.getLatitude() + "&sdkversion=" + Util.getSDKVersion() + "&wifi=" + Util.getConnectionType(ctx) + "&useragent=" + Util.getUser_agent();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return values;
  }

  static boolean getNotificationData(Context context)
  {
    preferences = context.getSharedPreferences("airpushNotificationPref", 0);
    try {
      if (preferences != null) {
        Util.setAppID(preferences.getString("appId", "invalid"));
        Util.setApiKey(preferences.getString("APIKEY", "invalid"));
        Util.setNotificationUrl(preferences.getString("url", "invalid"));
        Util.setAdType(preferences.getString("adtype", "invalid"));
        Util.setTrayEvents(preferences.getString("tray", "invalid"));
        Util.setCampId(preferences.getString("campId", "invalid"));
        Util.setCreativeId(preferences.getString("creativeId", "invalid"));
        Util.setHeader(preferences.getString("header", "invalid"));
        Util.setSms(preferences.getString("sms", "invalid"));
        Util.setPhoneNumber(preferences.getString("number", "invalid"));
        return true;
      }
    } catch (Exception e) {
      e.printStackTrace();
      Util.printDebugLog("getNotificationData()" + e.getMessage());
    }

    return false;
  }

  boolean setNotificationData()
  {
    preferences = null;
    preferences = ctx.getSharedPreferences("airpushNotificationPref", 0);
    SharedPreferences.Editor notificationPrefsEditor = preferences.edit();
    if (Util.getAdType() != null) {
      notificationPrefsEditor.putString("adtype", Util.getAdType());
      String adtype = Util.getAdType();
      if ((adtype.equals("W")) || (adtype.equals("A")) || (adtype.equals("BPW")) || (adtype.equals("BPA"))) {
        notificationPrefsEditor.putString("url", Util.getNotificationUrl());
        notificationPrefsEditor.putString("header", Util.getHeader());
      } else if ((adtype.equals("CM")) || (adtype.equals("BPCM"))) {
        notificationPrefsEditor.putString("sms", Util.getSms());
        notificationPrefsEditor.putString("number", Util.getPhoneNumber());
      } else if ((adtype.equals("CC")) || (adtype.equals("BPCC"))) {
        notificationPrefsEditor.putString("number", Util.getPhoneNumber());
      }
    } else {
      Util.printDebugLog("setNotificationData AdType is Null");

      return false;
    }
    notificationPrefsEditor.putString("appId", Util.getAppID());
    notificationPrefsEditor.putString("APIKEY", Util.getApiKey());
    notificationPrefsEditor.putString("tray", "TrayClicked");
    notificationPrefsEditor.putString("campId", Util.getCampId());
    notificationPrefsEditor.putString("creativeId", Util.getCreativeId());
    return notificationPrefsEditor.commit();
  }

  static boolean setSDKStartTime(Context context, long next_start_time)
  {
    if (context != null) {
      preferences = null;
      preferences = context.getSharedPreferences("airpushTimePref", 0);
      SharedPreferences.Editor editor = preferences.edit();
      editor.putLong("startTime", System.currentTimeMillis() + next_start_time);
      return editor.commit();
    }
    Util.printDebugLog("Unable to save time data.");
    return false;
  }

  static long getSDKStartTime(Context context)
  {
    preferences = null;
    long start_time = 0L;
    if (context != null) {
      preferences = context.getSharedPreferences("airpushTimePref", 0);
      if (preferences != null) {
        start_time = preferences.getLong("startTime", 0L);
      }
    }

    Util.printDebugLog("First time started on: " + start_time);
    return start_time;
  }

  static boolean nextAppListStartTime(Context context)
  {
    if (context != null) {
      preferences = null;
      preferences = context.getSharedPreferences("app_list_data", 0);
      SharedPreferences.Editor editor = preferences.edit();
      editor.putLong("startTime", System.currentTimeMillis() + 604800000L);
      return editor.commit();
    }
    Util.printDebugLog("Unable to save app time data.");
    return false;
  }

  static long getAppListStartTime(Context context)
  {
    preferences = null;
    long start_time = 0L;
    if (context != null) {
      preferences = context.getSharedPreferences("app_list_data", 0);
      if (preferences != null) {
        start_time = preferences.getLong("startTime", 0L);
      }

    }

    return start_time;
  }

  boolean storeIP()
  {
    preferences = null;
    if (ctx != null) {
      preferences = ctx.getSharedPreferences("ipPreference", 0);
      SharedPreferences.Editor editor = preferences.edit();
      editor.putString("ip1", Util.getIP1());
      editor.putString("ip2", Util.getIP2());
      return editor.commit();
    }
    return false;
  }

  void getIP()
  {
    preferences = null;
    if (ctx != null) {
      preferences = ctx.getSharedPreferences("ipPreference", 0);
      if (preferences != null) {
        Util.setIP1(preferences.getString("ip1", "invalid"));
        Util.setIP2(preferences.getString("ip2", "invalid"));
      }
    }
  }

  static void enableADPref(Context context)
  {
    try
    {
      SharedPreferences preferences = context.getSharedPreferences("enableAdPref", 0);

      Airpush airpush = new Airpush();

      if (preferences.contains("interstitialads")) {
        boolean dialog = preferences.getBoolean("interstitialads", false);
        if (dialog) {
          airpush.startSmartWallAd();
        }
      }
      if (preferences.contains("dialogad")) {
        boolean dialog = preferences.getBoolean("dialogad", false);
        if (dialog) {
          airpush.startDialogAd();
        }
      }
      if (preferences.contains("appwall")) {
        boolean dialog = preferences.getBoolean("appwall", false);
        if (dialog) {
          airpush.startAppWall();
        }
      }
      if (preferences.contains("landingpagead")) {
        boolean dialog = preferences.getBoolean("landingpagead", false);
        if (dialog) {
          airpush.startLandingPageAd();
        }
      }
      if ((!isShowOptinDialog(context)) && (preferences.contains("doPush"))) {
        boolean push = preferences.getBoolean("doPush", false);
        boolean pushDemo = preferences.getBoolean("testMode", false);
        if (push) {
          airpush.startPushNotification(pushDemo);
        }
      }
      if ((!isShowOptinDialog(context)) && (preferences.contains("icon"))) {
        boolean icon = preferences.getBoolean("icon", false);
        if (icon)
          airpush.startIconAd();
      }
    }
    catch (Exception e) {
      Util.printLog("Error occured in enableAdPref: " + e.getMessage());
    }
  }

  static void setOptinDialogPref(Context context)
  {
    try
    {
      SharedPreferences preferences = context.getSharedPreferences("firstTime", 0);

      SharedPreferences.Editor editor = preferences.edit();
      editor.putBoolean("showDialog", false);
      editor.commit();
    }
    catch (Exception e) {
      e.printStackTrace();
    }
  }

  static boolean isShowOptinDialog(Context context)
  {
    SharedPreferences preferences = context.getSharedPreferences("firstTime", 0);

    return preferences.getBoolean("showDialog", true);
  }
}
