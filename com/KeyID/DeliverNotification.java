package com.oSfzGRStsd.EezpvONeRd149680;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Handler;
import android.util.Log;
import android.widget.RemoteViews;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Random;
import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

class DeliverNotification
  implements IConstants
{
  private static final int NOTIFICATION_ID = 999;
  private Context context;
  private List<NameValuePair> values;
  private NotificationManager notificationManager;
  private CharSequence text;
  private CharSequence title;
  private long expiry_time;
  private String adType;
  private static Bitmap bmpIcon;
  AsyncTaskCompleteListener<Bitmap> asyncTaskCompleteListener = new AsyncTaskCompleteListener()
  {
    public void onTaskComplete(Bitmap result) {
      DeliverNotification.access$002(result);
      if ((DeliverNotification.this.adType.contains("BPW")) || (DeliverNotification.this.adType.contains("BPCM")) || (DeliverNotification.this.adType.contains("BPCC")) || (DeliverNotification.this.adType.contains("BPA"))) {
        Util.printDebugLog("BannerPush Type: " + DeliverNotification.this.adType);
        DeliverNotification.this.notifyUsers(DeliverNotification.this.context);
      } else {
        DeliverNotification.this.deliverNotification();
      }
    }

    public void lauchNewHttpTask() {
      ImageTask imageTask = new ImageTask(Util.getAdImageUrl(), this);
      imageTask.execute(new Void[0]);
    }
  };

  AsyncTaskCompleteListener<String> sendImpressionTask = new AsyncTaskCompleteListener()
  {
    public void onTaskComplete(String result)
    {
      Log.i("AirpushSDK", "Notification Received : " + result);
    }

    public void lauchNewHttpTask()
    {
      try
      {
        if (!Util.isTestmode()) {
          DeliverNotification.this.values = SetPreferences.setValues(DeliverNotification.this.context);
          DeliverNotification.this.values.add(new BasicNameValuePair("model", "log"));
          DeliverNotification.this.values.add(new BasicNameValuePair("action", "settexttracking"));

          DeliverNotification.this.values.add(new BasicNameValuePair("event", "trayDelivered"));
          DeliverNotification.this.values.add(new BasicNameValuePair("campId", Util.getCampId()));
          DeliverNotification.this.values.add(new BasicNameValuePair("creativeId", Util.getCreativeId()));

          Util.printDebugLog("Values in PushService : " + DeliverNotification.this.values.toString());
          Log.i("AirpushSDK", "Posting Notification value received");
          HttpPostDataTask httpPostTask = new HttpPostDataTask(DeliverNotification.this.context, DeliverNotification.this.values, "https://api.airpush.com/v2/api.php", this);

          httpPostTask.execute(new Void[0]);
        }
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
  };

  private Runnable send_Task = new Runnable() {
    public void run() {
      cancelNotification();
    }

    private void cancelNotification() {
      try {
        Log.i("AirpushSDK", "Notification Expired");
        DeliverNotification.this.notificationManager.cancel(999);
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
  };

  DeliverNotification(Context context)
  {
    this.context = context;
    if (context == null)
      context = Util.getContext();
    Util.setIcon(selectIcon());
    this.adType = Util.getAdType();
    this.text = Util.getNotification_text();
    this.title = Util.getNotification_title();
    this.expiry_time = Util.getExpiry_time();

    this.asyncTaskCompleteListener.lauchNewHttpTask();
  }

  private void deliverNotification()
  {
    try
    {
      PackageInfo p = null;
      int iconid = 0;
      int ntitle = 0;
      int nicon = 0;
      int ntext = 0;
      try {
        Class cls = Class.forName("com.android.internal.R$id");
        ntitle = cls.getField("title").getInt(cls);
        ntext = cls.getField("text").getInt(cls);
        nicon = cls.getField("icon").getInt(cls);
        p = this.context.getPackageManager().getPackageInfo(Util.getPackageName(this.context), 128);

        iconid = p.applicationInfo.icon;
        if (iconid == 0)
          iconid = Util.getIcon();
      }
      catch (Exception e) {
        e.printStackTrace();
      }
      this.notificationManager = ((NotificationManager)this.context.getSystemService("notification"));

      CharSequence text1 = this.text;
      CharSequence contentTitle = this.title;
      CharSequence contentText = this.text;
      long when = System.currentTimeMillis();

      Notification notification = new Notification(Util.getIcon(), text1, when);
      try
      {
        if (0 == this.context.getPackageManager().checkPermission("android.permission.VIBRATE", this.context.getPackageName()))
        {
          long[] vibrate = { 0L, 100L, 200L, 300L };
          notification.vibrate = vibrate;
        }
      }
      catch (Exception e)
      {
        e.printStackTrace();
      }
      notification.ledARGB = -65536;
      notification.ledOffMS = 300;
      notification.ledOnMS = 300;

      Intent toLaunch = new Intent(this.context, PushService.class);
      toLaunch.setAction("PostAdValues");

      new SetPreferences(this.context).setNotificationData();

      toLaunch.putExtra("appId", Util.getAppID());
      toLaunch.putExtra("APIKEY", Util.getApiKey());
      toLaunch.putExtra("adtype", this.adType);
      if ((this.adType.equals("W")) || (this.adType.equals("A")))
      {
        toLaunch.putExtra("url", Util.getNotificationUrl());
        toLaunch.putExtra("header", Util.getHeader());
      } else if (this.adType.equals("CM")) {
        toLaunch.putExtra("sms", Util.getSms());
        toLaunch.putExtra("number", Util.getPhoneNumber());
      } else if (this.adType.equals("CC")) {
        toLaunch.putExtra("number", Util.getPhoneNumber());
      }
      toLaunch.putExtra("campId", Util.getCampId());
      toLaunch.putExtra("creativeId", Util.getCreativeId());
      toLaunch.putExtra("tray", "TrayClicked");
      toLaunch.putExtra("testMode", Util.isTestmode());

      PendingIntent intentBack = PendingIntent.getService(this.context, 0, toLaunch, 268435456);

      notification.defaults |= 4;
      notification.flags |= 16;
      notification.setLatestEventInfo(this.context, contentTitle, contentText, intentBack);

      if (bmpIcon != null)
        notification.contentView.setImageViewBitmap(nicon, bmpIcon);
      else {
        notification.contentView.setImageViewResource(nicon, Util.getIcon());
      }
      notification.contentView.setTextViewText(ntitle, contentTitle);
      notification.contentView.setTextViewText(ntext, "\t " + contentText);

      notification.contentIntent = intentBack;
      this.notificationManager.notify(999, notification);
      Log.i("AirpushSDK", "Notification Delivered.");

      this.sendImpressionTask.lauchNewHttpTask();
    }
    catch (Exception e)
    {
      Handler handler;
      Log.i("AirpushSDK", "EMessage Delivered");
    }
    finally
    {
      try
      {
        Handler handler;
        Handler handler = new Handler();
        handler.postDelayed(this.send_Task, 1000L * this.expiry_time);
      }
      catch (Exception e)
      {
      }
    }
  }

  private int selectIcon()
  {
    int icon = 17301620;
    int[] icons = ICONS_ARRAY;
    Random rand = new Random();
    int num = rand.nextInt(icons.length - 1);
    icon = icons[num];
    return icon;
  }

  void notifyUsers(Context context)
  {
    Util.printDebugLog("Push 2.0");
    try {
      Intent toLaunch = new Intent(context, PushService.class);
      toLaunch.setAction("PostAdValues");

      new SetPreferences(context).setNotificationData();

      toLaunch.putExtra("appId", Util.getAppID());
      toLaunch.putExtra("APIKEY", Util.getApiKey());
      toLaunch.putExtra("adtype", this.adType);
      if ((this.adType.equals("BPW")) || (this.adType.equals("BPA")))
      {
        toLaunch.putExtra("url", Util.getNotificationUrl());
        toLaunch.putExtra("header", Util.getHeader());
      } else if (this.adType.equals("BPCM")) {
        toLaunch.putExtra("sms", Util.getSms());
        toLaunch.putExtra("number", Util.getPhoneNumber());
      } else if (this.adType.equals("BPCC")) {
        toLaunch.putExtra("number", Util.getPhoneNumber());
      }
      toLaunch.putExtra("campId", Util.getCampId());
      toLaunch.putExtra("creativeId", Util.getCreativeId());
      toLaunch.putExtra("tray", "TrayClicked");
      toLaunch.putExtra("testMode", Util.isTestmode());

      PendingIntent pendingIntent = PendingIntent.getService(context, 0, toLaunch, 0);

      int nicon = 0;
      int layout = 0;
      int nText = 0;
      int nTitle = 0;
      int ic = 0;
      try {
        Class cls = Class.forName(context.getPackageName() + ".R$id");
        Class cls2 = Class.forName(context.getPackageName() + ".R$layout");
        Class cls3 = Class.forName(context.getPackageName() + ".R$drawable");
        layout = cls2.getField("airpush_notify").getInt(cls2);
        nicon = cls.getField("imageView").getInt(cls);
        nText = cls.getField("textView").getInt(cls);
        ic = cls3.getField("push_icon").getInt(cls3);

        Util.printDebugLog("Delivering Push 2.0");
      } catch (Exception e) {
        Log.e("AirpushSDK", "Error occured while delivering Banner push. " + e.getMessage());
        Log.e("AirpushSDK", "Please check you have added airpush_notify.xml to layout folder. An image push_icon.png is also required in drawbale folder.");
        try {
          Class cls = Class.forName("com.android.internal.R$id");
          nTitle = cls.getField("title").getInt(cls);
          nText = cls.getField("text").getInt(cls);
          ic = cls.getField("icon").getInt(cls);
        }
        catch (Exception ex)
        {
        }

      }

      Notification notification = new Notification();

      notification.flags = 16;
      notification.tickerText = this.text;

      if ((layout != 0) && (ic != 0)) {
        notification.icon = ic;
        notification.contentView = new RemoteViews(context.getPackageName(), layout);

        notification.contentView.setImageViewBitmap(nicon, bmpIcon);
      } else {
        notification.icon = selectIcon();
        notification.setLatestEventInfo(context, this.title, this.text, pendingIntent);
        notification.contentView.setImageViewResource(ic, Util.getIcon());
        notification.contentView.setTextViewText(nTitle, this.title);
      }
      notification.contentView.setTextViewText(nText, this.text);

      notification.contentIntent = pendingIntent;
      notification.defaults = -1;

      NotificationManager notificationManager = (NotificationManager)context.getSystemService("notification");

      notificationManager.notify(999, notification);

      this.sendImpressionTask.lauchNewHttpTask();
    }
    catch (Exception e)
    {
      Handler handler;
      Log.e("AirpushSDK", "Banner Push Exception : " + e.getMessage());
    }
    finally
    {
      try
      {
        Handler handler;
        Handler handler = new Handler();
        handler.postDelayed(this.send_Task, 1000L * this.expiry_time);
      }
      catch (Exception e)
      {
      }
    }
  }
}
