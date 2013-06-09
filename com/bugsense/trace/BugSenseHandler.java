package com.bugsense.trace;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Build.VERSION;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.json.JSONObject;

public class BugSenseHandler
{
  private static ArrayList<String[]> sStackTraces = null;
  private static ActivityAsyncTask<Processor, Object, Object, Object> sTask;
  private static boolean sVerbose = false;
  private static int sMinDelay = 0;
  private static int sTimeout = 1;
  private static boolean sSetupCalled = false;
  public static Context gContext = null;
  private static Map<String, String> extraData = new HashMap();

  public static boolean setup(Context paramContext, Processor paramProcessor, String paramString1, String paramString2)
  {
    G.API_KEY = paramString1;
    G.APPID = paramString2;
    gContext = paramContext;
    if (sSetupCalled)
    {
      if ((sTask != null) && (!sTask.postProcessingDone()))
      {
        sTask.connectTo(null);
        sTask.connectTo(paramProcessor);
      }
      return false;
    }
    sSetupCalled = true;
    Log.i(G.TAG, "Registering default exceptions handler");
    G.FILES_PATH = paramContext.getFilesDir().getAbsolutePath();
    G.PHONE_MODEL = Build.MODEL;
    G.ANDROID_VERSION = Build.VERSION.RELEASE;
    G.HAS_ROOT = checkForRoot();
    PackageManager localPackageManager = paramContext.getPackageManager();
    try
    {
      PackageInfo localPackageInfo = localPackageManager.getPackageInfo(paramContext.getPackageName(), 0);
      G.APP_VERSION = localPackageInfo.versionName;
      G.APP_PACKAGE = localPackageInfo.packageName;
    }
    catch (PackageManager.NameNotFoundException localNameNotFoundException)
    {
      Log.e(G.TAG, "Error collecting trace information", localNameNotFoundException);
    }
    if (sVerbose)
    {
      Log.i(G.TAG, new StringBuilder().append("TRACE_VERSION: ").append(G.TraceVersion).toString());
      Log.d(G.TAG, new StringBuilder().append("APP_VERSION: ").append(G.APP_VERSION).toString());
      Log.d(G.TAG, new StringBuilder().append("APP_PACKAGE: ").append(G.APP_PACKAGE).toString());
      Log.d(G.TAG, new StringBuilder().append("FILES_PATH: ").append(G.FILES_PATH).toString());
      Log.d(G.TAG, new StringBuilder().append("URL: ").append(G.URL).toString());
    }
    getStackTraces();
    installHandler();
    paramProcessor.handlerInstalled();
    return submit(paramProcessor);
  }

  private static boolean checkForRoot()
  {
    boolean bool = false;
    String[] arrayOfString1 = { "/sbin/", "/system/bin/", "/system/xbin/", "/data/local/xbin/", "/data/local/bin/", "/system/sd/xbin/", "/system/bin/failsafe/", "/data/local/" };
    for (String str : arrayOfString1)
    {
      File localFile = new File(new StringBuilder().append(str).append("su").toString());
      if (localFile.exists())
      {
        bool = true;
        break;
      }
    }
    return bool;
  }

  public static boolean setup(Context paramContext, String paramString1, String paramString2)
  {
    return setup(paramContext, new Processor()
    {
      public boolean beginSubmit()
      {
        return true;
      }

      public void submitDone()
      {
      }

      public void handlerInstalled()
      {
      }
    }
    , paramString1, paramString2);
  }

  private static void notifyContextGone()
  {
    if (sTask == null)
      return;
    sTask.connectTo(null);
  }

  private static boolean submit(Processor paramProcessor)
  {
    if (!sSetupCalled)
      throw new RuntimeException("you need to call setup() first");
    boolean bool1 = hasStrackTraces();
    if (bool1)
    {
      boolean bool2 = paramProcessor.beginSubmit();
      if (bool2)
      {
        final ArrayList localArrayList = sStackTraces;
        sStackTraces = null;
        sTask = new ActivityAsyncTask(paramProcessor)
        {
          private long mTimeStarted;

          protected void onPreExecute()
          {
            super.onPreExecute();
            this.mTimeStarted = System.currentTimeMillis();
          }

          protected Object doInBackground(Object[] paramAnonymousArrayOfObject)
          {
            BugSenseHandler.submitStackTraces(localArrayList);
            long l = BugSenseHandler.sMinDelay - (System.currentTimeMillis() - this.mTimeStarted);
            if (l > 0L)
              try
              {
                Thread.sleep(l);
              }
              catch (InterruptedException localInterruptedException)
              {
                localInterruptedException.printStackTrace();
              }
            return null;
          }

          protected void onCancelled()
          {
            super.onCancelled();
          }

          protected void processPostExecute(Object paramAnonymousObject)
          {
            ((BugSenseHandler.Processor)this.mWrapped).submitDone();
          }
        };
        sTask.execute(new Object[0]);
      }
    }
    return bool1;
  }

  private static boolean submit()
  {
    return submit(new Processor()
    {
      public boolean beginSubmit()
      {
        return true;
      }

      public void submitDone()
      {
      }

      public void handlerInstalled()
      {
      }
    });
  }

  public static void log(String paramString, Map<String, String> paramMap, Exception paramException)
  {
    StringWriter localStringWriter = new StringWriter();
    PrintWriter localPrintWriter = new PrintWriter(localStringWriter);
    if (G.API_KEY == null)
    {
      Log.d(G.TAG, "Could not send: API KEY is missing");
    }
    else
    {
      Log.d(G.TAG, "Transmitting log data");
      try
      {
        paramException.printStackTrace(localPrintWriter);
        BugSense.submitError(gContext, 0, null, localStringWriter.toString(), new StringBuilder().append("LOG_").append(paramString).toString(), paramMap);
      }
      catch (Exception localException)
      {
        Log.d(G.TAG, "Failed to transmit log data:");
      }
    }
  }

  public static void log(String paramString, Exception paramException)
  {
    log(paramString, new HashMap(), paramException);
  }

  private static boolean hasStrackTraces()
  {
    return getStackTraces().size() > 0;
  }

  private static ArrayList<String[]> getStackTraces()
  {
    if (sStackTraces != null)
      return sStackTraces;
    Log.d(G.TAG, new StringBuilder().append("Looking for exceptions in: ").append(G.FILES_PATH).toString());
    File localFile1 = new File(new StringBuilder().append(G.FILES_PATH).append("/").toString());
    if (!localFile1.exists())
      localFile1.mkdir();
    FilenameFilter local4 = new FilenameFilter()
    {
      public boolean accept(File paramAnonymousFile, String paramAnonymousString)
      {
        return paramAnonymousString.endsWith(".stacktrace");
      }
    };
    String[] arrayOfString = localFile1.list(local4);
    Log.d(G.TAG, new StringBuilder().append("Found ").append(arrayOfString.length).append(" stacktrace(s)").toString());
    try
    {
      sStackTraces = new ArrayList();
      for (int i = 0; (i < arrayOfString.length) && (sStackTraces.size() < 5); i++)
      {
        String str1 = new StringBuilder().append(G.FILES_PATH).append("/").append(arrayOfString[i]).toString();
        try
        {
          Object localObject1 = null;
          Object localObject2 = null;
          String str2 = arrayOfString[i].split("-")[0];
          Log.d(G.TAG, new StringBuilder().append("Stacktrace in file '").append(str1).append("' belongs to version ").append(str2).toString());
          StringBuilder localStringBuilder = new StringBuilder();
          BufferedReader localBufferedReader = new BufferedReader(new FileReader(str1));
          try
          {
            str3 = null;
            while ((str3 = localBufferedReader.readLine()) != null)
              if (localObject1 == null)
              {
                localObject1 = str3;
              }
              else if (localObject2 == null)
              {
                localObject2 = str3;
              }
              else
              {
                localStringBuilder.append(str3);
                localStringBuilder.append(System.getProperty("line.separator"));
              }
          }
          finally
          {
            localBufferedReader.close();
          }
          String str3 = localStringBuilder.toString();
          sStackTraces.add(new String[] { str2, localObject1, localObject2, str3 });
        }
        catch (FileNotFoundException localFileNotFoundException)
        {
          Log.e(G.TAG, "Failed to load stack trace", localFileNotFoundException);
        }
        catch (IOException localIOException)
        {
          Log.e(G.TAG, "Failed to load stack trace", localIOException);
        }
      }
      ArrayList localArrayList = sStackTraces;
      int j;
      File localFile2;
      return localArrayList;
    }
    finally
    {
      for (int k = 0; k < arrayOfString.length; k++)
        try
        {
          File localFile3 = new File(new StringBuilder().append(G.FILES_PATH).append("/").append(arrayOfString[k]).toString());
          localFile3.delete();
        }
        catch (Exception localException2)
        {
          Log.e(G.TAG, new StringBuilder().append("Error deleting trace file: ").append(arrayOfString[k]).toString(), localException2);
        }
    }
  }

  private static void submitStackTraces(ArrayList<String[]> paramArrayList)
  {
    try
    {
      if (paramArrayList == null)
        return;
      for (int i = 0; i < paramArrayList.size(); i++)
      {
        String[] arrayOfString = (String[])paramArrayList.get(i);
        String str1 = arrayOfString[0];
        String str2 = arrayOfString[1];
        String str3 = arrayOfString[2];
        String str4 = arrayOfString[3];
        BugSense.submitError(gContext, sTimeout, null, str4);
      }
    }
    catch (Exception localException)
    {
      Log.e(G.TAG, "Error submitting trace", localException);
    }
  }

  private static void installHandler()
  {
    Thread.UncaughtExceptionHandler localUncaughtExceptionHandler = Thread.getDefaultUncaughtExceptionHandler();
    if ((localUncaughtExceptionHandler != null) && (sVerbose))
      Log.d(G.TAG, new StringBuilder().append("current handler class=").append(localUncaughtExceptionHandler.getClass().getName()).toString());
    if (!(localUncaughtExceptionHandler instanceof DefaultExceptionHandler))
      Thread.setDefaultUncaughtExceptionHandler(new DefaultExceptionHandler(localUncaughtExceptionHandler));
  }

  private static String CheckNetworkConnection(String paramString)
  {
    String str = "false";
    PackageManager localPackageManager = gContext.getPackageManager();
    if (localPackageManager.checkPermission("android.permission.ACCESS_NETWORK_STATE", G.APP_PACKAGE) == 0)
    {
      ConnectivityManager localConnectivityManager = (ConnectivityManager)gContext.getSystemService("connectivity");
      NetworkInfo[] arrayOfNetworkInfo1 = localConnectivityManager.getAllNetworkInfo();
      for (NetworkInfo localNetworkInfo : arrayOfNetworkInfo1)
        if ((localNetworkInfo.getTypeName().equalsIgnoreCase(paramString)) && (localNetworkInfo.isConnected()))
          str = "true";
    }
    else
    {
      str = "not available [permissions]";
    }
    return str;
  }

  public static String isWifiOn()
  {
    return CheckNetworkConnection("WIFI");
  }

  public static String isMobileNetworkOn()
  {
    return CheckNetworkConnection("MOBILE");
  }

  public static String isGPSOn()
  {
    String str = "true";
    PackageManager localPackageManager = gContext.getPackageManager();
    if (localPackageManager.checkPermission("android.permission.ACCESS_FINE_LOCATION", G.APP_PACKAGE) == 0)
    {
      LocationManager localLocationManager = (LocationManager)gContext.getSystemService("location");
      if (!localLocationManager.isProviderEnabled("gps"))
        str = "false";
    }
    else
    {
      str = "not available [permissions]";
    }
    return str;
  }

  public static String[] ScreenProperties()
  {
    String[] arrayOfString = { "Not available", "Not available", "Not available", "Not available", "Not available" };
    DisplayMetrics localDisplayMetrics = new DisplayMetrics();
    PackageManager localPackageManager = gContext.getPackageManager();
    Display localDisplay = ((WindowManager)gContext.getSystemService("window")).getDefaultDisplay();
    int i = localDisplay.getWidth();
    int j = localDisplay.getHeight();
    Log.i(G.TAG, Build.VERSION.RELEASE);
    int k = localDisplay.getOrientation();
    arrayOfString[0] = Integer.toString(i);
    arrayOfString[1] = Integer.toString(j);
    String str = "";
    switch (k)
    {
    case 0:
      str = "normal";
      break;
    case 2:
      str = "180";
      break;
    case 3:
      str = "270";
      break;
    case 1:
      str = "90";
    }
    arrayOfString[2] = str;
    localDisplay.getMetrics(localDisplayMetrics);
    arrayOfString[3] = Float.toString(localDisplayMetrics.xdpi);
    arrayOfString[4] = Float.toString(localDisplayMetrics.ydpi);
    return arrayOfString;
  }

  public static void showUpgradeNotification(String paramString)
  {
    try
    {
      Context localContext = gContext;
      String str1 = "notification";
      NotificationManager localNotificationManager = (NotificationManager)gContext.getSystemService(str1);
      JSONObject localJSONObject1 = new JSONObject(paramString);
      JSONObject localJSONObject2 = new JSONObject(localJSONObject1.getString("data"));
      String str2 = localJSONObject2.getString("tickerText");
      long l = System.currentTimeMillis();
      Resources localResources = gContext.getResources();
      int i = localResources.getIdentifier("icon", "drawable", gContext.getPackageName());
      Notification localNotification = new Notification(i, str2, l);
      localNotification.flags = 16;
      String str3 = localJSONObject2.getString("contentTitle");
      String str4 = localJSONObject2.getString("contentText");
      Intent localIntent = new Intent("android.intent.action.VIEW", Uri.parse(localJSONObject2.getString("url")));
      PendingIntent localPendingIntent = PendingIntent.getActivity(localContext, 0, localIntent, 268435456);
      localNotification.setLatestEventInfo(localContext, str3, str4, localPendingIntent);
      localNotificationManager.notify(1, localNotification);
    }
    catch (Exception localException)
    {
      Log.e(G.TAG, "Error starting", localException);
    }
  }

  private static int getResId(String paramString, Context paramContext, Class<?> paramClass)
  {
    try
    {
      Field localField = paramClass.getDeclaredField(paramString);
      return localField.getInt(localField);
    }
    catch (Exception localException)
    {
      localException.printStackTrace();
    }
    return -1;
  }

  public static Map<String, String> getExtraData()
  {
    if (extraData == null)
      extraData = new HashMap();
    return extraData;
  }

  public static void addExtra(String paramString1, String paramString2)
  {
    if (extraData == null)
      extraData = new HashMap();
    extraData.put(paramString1, paramString2);
  }

  public static void addExtras(HashMap<String, String> paramHashMap)
  {
    if (extraData == null)
      extraData = new HashMap();
    Iterator localIterator = paramHashMap.entrySet().iterator();
    while (localIterator.hasNext())
    {
      Map.Entry localEntry = (Map.Entry)localIterator.next();
      extraData.put(localEntry.getKey(), localEntry.getValue());
    }
  }

  public static abstract interface Processor
  {
    public abstract boolean beginSubmit();

    public abstract void submitDone();

    public abstract void handlerInstalled();
  }
}
