package com.oSfzGRStsd.EezpvONeRd149680;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.location.Address;
import android.location.Geocoder;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Build.VERSION;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;

class Util
{
  private static String imei = "0";
  private static String apiKey = "airpush";
  private static String appID = "0";
  private static boolean testmode = false;
  private static boolean doPush = false;

  private static String longitude = "0";
  private static String latitude = "0";
  private static int icon;
  private static Context context;
  private static String jsonstr;
  private static String campId;
  private static String creativeId;
  private static String user_agent;
  private static String device_unique_type;
  private static String notification_title;
  private static String notification_text;
  private static String phoneNumber;
  private static String adType = "";
  private static String trayEvents;
  private static String header;
  private static String notificationUrl;
  private static String sms;
  private static String adImageUrl;
  private static String delivery_time;
  private static long expiry_time;
  private static String landingPageAdUrl;
  private static long lastLocationTime = 0L;
  private static final int NETWORK_TYPE_EHRPD = 14;
  private static final int NETWORK_TYPE_EVDO_B = 12;
  private static final int NETWORK_TYPE_HSPAP = 15;
  private static final int NETWORK_TYPE_IDEN = 11;
  private static final int NETWORK_TYPE_LTE = 13;
  private static final int NETWORK_TYPE_HSDPA = 8;
  private static final int NETWORK_TYPE_HSPA = 10;
  private static final int NETWORK_TYPE_HSUPA = 9;
  private static String IP1;
  private static String IP2;

  Util(Context context)
  {
    context = context;
  }

  static String getSDKVersion()
  {
    return "5.0";
  }

  static boolean isTablet(Context context)
  {
    boolean isTablet = false;
    try
    {
      DisplayMetrics dm = context.getResources().getDisplayMetrics();
      float screenWidth = dm.widthPixels / dm.xdpi;
      float screenHeight = dm.heightPixels / dm.ydpi;
      double size = Math.sqrt(Math.pow(screenWidth, 2.0D) + Math.pow(screenHeight, 2.0D));

      isTablet = size >= 6.0D;
    } catch (NullPointerException e) {
      printDebugLog("" + e.getMessage());
    } catch (Exception e) {
      printDebugLog("" + e.getMessage());
    } catch (Throwable t) {
      printDebugLog("" + t.getMessage());
    }
    return isTablet;
  }

  static Context getContext() {
    return context;
  }

  static void setContext(Context context) {
    context = context;
  }

  static String getImei() {
    return imei;
  }

  static void setImei(String imei) {
    imei = imei;
  }

  static String getApiKey() {
    return apiKey;
  }

  static void setApiKey(String apiKey) {
    apiKey = apiKey;
  }

  static String getAppID() {
    return appID;
  }

  static void setAppID(String appID) {
    appID = appID;
  }

  static boolean isTestmode() {
    return testmode;
  }

  static void setTestmode(boolean testmode) {
    testmode = testmode;
  }

  static boolean isDoPush() {
    return doPush;
  }

  static void setDoPush(boolean doPush) {
    doPush = doPush;
  }

  static void setUser_agent(String user_agent) {
    user_agent = user_agent;
  }

  static String getUser_agent() {
    return user_agent;
  }

  static String getLatitude() {
    return latitude;
  }

  static void setLatitude(String latitude) {
    latitude = latitude;
  }

  static String getLongitude() {
    return longitude;
  }

  static void setLongitude(String longitude) {
    longitude = longitude;
  }
  static void setLastLocationTime(long lastLocationTime) {
    lastLocationTime = lastLocationTime;
  }
  static long getLastLocationTime() {
    return lastLocationTime;
  }
  static String getDate() {
    try {
      String format = "yyyy-MM-dd HH:mm:ss";
      SimpleDateFormat dateFormat = new SimpleDateFormat(format);
      return "" + dateFormat.format(new Date()) + "_" + dateFormat.getTimeZone().getDisplayName() + "_" + dateFormat.getTimeZone().getID() + "_" + dateFormat.getTimeZone().getDisplayName(false, 0);
    }
    catch (Exception e)
    {
    }

    return "00";
  }

  static String getPhoneModel()
  {
    return Build.MODEL;
  }

  static String getVersion() {
    return "" + Build.VERSION.SDK_INT;
  }

  static String getAndroidId(Context context) {
    if (context == null)
      return "";
    return Settings.Secure.getString(context.getContentResolver(), "android_id");
  }

  static void setIcon(int icon)
  {
    icon = icon;
  }

  static int getIcon() {
    return icon;
  }

  static String getPackageName(Context context) {
    try {
      return context.getPackageName(); } catch (Exception e) {
    }
    return "";
  }

  static String getCarrier(Context context)
  {
    if (context == null)
      return "";
    TelephonyManager manager = (TelephonyManager)context.getSystemService("phone");

    if ((manager != null) && (manager.getSimState() == 5))
    {
      return manager.getSimOperatorName();
    }
    return "";
  }

  static String getNetworkOperator(Context context)
  {
    if (context == null) {
      return "";
    }
    TelephonyManager manager = (TelephonyManager)context.getSystemService("phone");

    if ((manager != null) && (manager.getPhoneType() == 1)) {
      return manager.getNetworkOperatorName();
    }
    return "";
  }

  static String getManufacturer()
  {
    return Build.MANUFACTURER;
  }

  static int getConnectionType(Context ctx)
  {
    if (ctx == null)
      return 0;
    ConnectivityManager cm = (ConnectivityManager)ctx.getSystemService("connectivity");

    NetworkInfo ni = cm.getActiveNetworkInfo();
    if ((ni != null) && (ni.isConnected()) && (ni.getTypeName().equals("WIFI"))) {
      return 1;
    }
    return 0;
  }

  static String getNetworksubType(Context context)
  {
    if (context != null) {
      ConnectivityManager cm = (ConnectivityManager)context.getSystemService("connectivity");

      NetworkInfo ni = cm.getActiveNetworkInfo();
      if ((ni != null) && (ni.isConnected()) && (!ni.getTypeName().equals("WIFI")))
      {
        return ni.getSubtypeName();
      }
    }
    return "";
  }

  static boolean isConnectionFast(Context context)
  {
    try
    {
      if (context == null)
        return false;
      ConnectivityManager cm = (ConnectivityManager)context.getSystemService("connectivity");

      NetworkInfo ni = cm.getActiveNetworkInfo();
      if ((ni == null) || (!ni.isConnected())) {
        return false;
      }
      int type = ni.getType();
      if (type == 1) {
        System.out.println("CONNECTED VIA WIFI");
        return true;
      }if (type == 0) {
        int subType = ni.getSubtype();
        switch (subType) {
        case 7:
          return false;
        case 4:
          return false;
        case 2:
          return false;
        case 5:
          return true;
        case 6:
          return true;
        case 1:
          return false;
        case 8:
          return true;
        case 10:
          return true;
        case 9:
          return true;
        case 3:
          return true;
        case 14:
          return true;
        case 12:
          return true;
        case 15:
          return true;
        case 11:
          return false;
        case 13:
          return true;
        case 0:
          return false;
        }
        return false;
      }
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return false;
  }

  static String getJsonstr() {
    return jsonstr;
  }

  static void setJsonstr(Context ctx) {
    String urlString = "https://api.airpush.com/model/user/getappinfo.php?packageName=" + getPackageName(ctx);
    try
    {
      new Thread(new Runnable()
      {
        public void run()
        {
          try
          {
            URL url = new URL(this.val$urlString);
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();

            connection.setRequestMethod("GET");
            connection.setConnectTimeout(2000);
            connection.connect();
            if (connection.getResponseCode() == 200)
            {
              StringBuffer sb = new StringBuffer();
              BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
              String line;
              while ((line = reader.readLine()) != null) {
                sb.append(line);
              }
              Util.access$002(sb.toString());
            }

            connection.disconnect();
          }
          catch (MalformedURLException exception)
          {
          }
          catch (IOException exception)
          {
          }
          catch (Exception e)
          {
          }
        }
      }).start();
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
  }

  static String getAppIdFromJSON()
  {
    try {
      JSONObject json = new JSONObject(getJsonstr());
      return json.getString("appid");
    } catch (JSONException e) {
    }
    return "";
  }

  static String getApiKeyFromJSON()
  {
    try {
      JSONObject json = new JSONObject(getJsonstr());
      return json.getString("authkey");
    } catch (JSONException e) {
    }
    return "invalid key";
  }

  static void setAppInfo(Context ctx)
  {
    setJsonstr(ctx);
    setAppID(getAppIdFromJSON());
    setApiKey(getApiKeyFromJSON());
  }

  static String getCampId() {
    return campId;
  }

  static void setCampId(String campId) {
    campId = campId;
  }

  static String getCreativeId() {
    return creativeId;
  }

  static void setCreativeId(String creativeId) {
    creativeId = creativeId;
  }

  static String getPhoneNumber() {
    return phoneNumber;
  }

  static void setPhoneNumber(String phoneNumber) {
    phoneNumber = phoneNumber;
  }

  static String getAdType() {
    return adType;
  }

  static void setAdType(String adType) {
    adType = adType;
  }

  static String getTrayEvents() {
    return trayEvents;
  }

  static void setTrayEvents(String trayEvents) {
    trayEvents = trayEvents;
  }

  static String getHeader() {
    return header;
  }

  static void setHeader(String header) {
    header = header;
  }

  static String getNotificationUrl() {
    return notificationUrl;
  }

  static void setNotificationUrl(String notificationUrl)
  {
    notificationUrl = notificationUrl;
  }

  static String getNotification_title() {
    return notification_title;
  }

  static void setNotification_title(String notification_title) {
    notification_title = notification_title;
  }

  static String getNotification_text() {
    return notification_text;
  }

  static void setNotification_text(String notification_text) {
    notification_text = notification_text;
  }

  static String getAdImageUrl() {
    return adImageUrl;
  }

  static void setAdImageUrl(String adImageUrl) {
    adImageUrl = adImageUrl;
  }

  static String getDelivery_time() {
    return delivery_time;
  }

  static void setDelivery_time(String delivery_time) {
    delivery_time = delivery_time;
  }

  static long getExpiry_time() {
    return expiry_time;
  }

  static void setExpiry_time(long expiry_time) {
    expiry_time = expiry_time;
  }

  static String getSms() {
    return sms;
  }

  static void setSms(String sms) {
    sms = sms;
  }

  static long getMessageIntervalTime()
  {
    if (testmode) {
      return 120000L;
    }
    return 14400000L;
  }

  static String getDevice_unique_type() {
    return device_unique_type;
  }

  static void setDevice_unique_type(String device_unique_type)
  {
    device_unique_type = device_unique_type;
  }

  static String getScreen_size(Context context)
  {
    String size = "";
    if (context != null) {
      Display display = ((WindowManager)context.getSystemService("window")).getDefaultDisplay();

      size = "" + display.getWidth() + "_" + display.getHeight();
    }
    return size;
  }

  static String getIP1()
  {
    return IP1;
  }

  static void setIP1(String iP1)
  {
    IP1 = iP1;
  }

  static String getIP2()
  {
    return IP2;
  }

  static void setIP2(String iP2)
  {
    IP2 = iP2;
  }

  static String getLandingPageAdUrl()
  {
    return landingPageAdUrl;
  }

  static void setLandingPageAdUrl(String fullPageAdUrl)
  {
    landingPageAdUrl = fullPageAdUrl;
  }

  static String[] getCountryName(Context context)
  {
    String[] country = { "", "" };
    try
    {
      Geocoder geocoder = new Geocoder(context);
      if ((latitude == null) || (latitude.equals("invalid")) || (longitude == null) || (longitude.equals("invalid")))
      {
        return country;
      }List addresses = geocoder.getFromLocation(Double.parseDouble(latitude), Double.parseDouble(longitude), 1);

      if (!addresses.isEmpty()) {
        country[0] = ((Address)addresses.get(0)).getCountryName();
        country[1] = ((Address)addresses.get(0)).getPostalCode();
        printDebugLog("Postal Code: " + country[1] + " Country Code: " + ((Address)addresses.get(0)).getCountryCode());
      }
    }
    catch (IOException e)
    {
    }
    catch (Exception e)
    {
    }
    catch (Throwable e)
    {
      e.printStackTrace();
    }
    return country;
  }

  static String getLanguage() {
    Locale locale = Locale.getDefault();
    return locale.getDisplayLanguage();
  }

  static String getNumber(Context context)
  {
    String number = "";
    if (context != null) {
      TelephonyManager manager = (TelephonyManager)context.getSystemService("phone");

      if (manager != null)
        number = manager.getLine1Number();
    }
    return number;
  }

  static String getScreenDp(Context context)
  {
    DisplayMetrics metrics = context.getResources().getDisplayMetrics();
    float density = metrics.density;
    return "" + density;
  }

  static boolean checkInternetConnection(Context context)
  {
    try
    {
      ConnectivityManager cm = (ConnectivityManager)context.getSystemService("connectivity");

      if ((cm.getActiveNetworkInfo() != null) && (cm.getActiveNetworkInfo().isAvailable()) && (cm.getActiveNetworkInfo().isConnected()))
      {
        return true;
      }
      Log.e("AirpushSDK", "Internet Connection not found.");
      return false;
    }
    catch (Exception e) {
      e.printStackTrace();
    }return false;
  }

  static String getAppName(Context context)
  {
    try
    {
      PackageManager pm = context.getPackageManager();
      ApplicationInfo ai;
      try
      {
        ai = pm.getApplicationInfo(context.getPackageName(), 0);
      } catch (PackageManager.NameNotFoundException e) {
        ai = null;
      }
      return (String)(ai != null ? pm.getApplicationLabel(ai) : "(unknown)");
    }
    catch (Exception e)
    {
      e.printStackTrace();
    }
    return "";
  }

  static String isInstallFromMarketOnly(Context context)
  {
    return Settings.Secure.getString(context.getContentResolver(), "install_non_market_apps");
  }

  static void printDebugLog(String message)
  {
    boolean isLogable = false;
  }

  static void printLog(String message)
  {
    Log.d("System.out", " " + message);
  }
}
