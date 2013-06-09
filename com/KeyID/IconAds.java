package com.oSfzGRStsd.EezpvONeRd149680;

import android.content.Context;
import android.content.Intent;
import android.content.Intent.ShortcutIconResource;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import java.util.List;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

class IconAds
  implements IConstants
{
  private Context mContext;
  private String iconImage;
  private String iconText;
  private String iconUrl;
  private String[] iconImageArr;
  private String[] iconTextArr;
  private String[] iconUrlArr;
  private Intent addIntent;
  private JSONObject jsonObject;
  private String campaignId;
  private String creativeId;
  private String[] campaignArr = null;
  private String[] creativeArr = null;
  private JSONObject post;
  private boolean sendInstall = true;
  private Bitmap bmpicon;
  AsyncTaskCompleteListener<Bitmap> getIconImageListener = new AsyncTaskCompleteListener()
  {
    public void onTaskComplete(Bitmap result)
    {
      if (result != null) {
        IconAds.this.bmpicon = result;
        IconAds.this.createShortcut();
      }
    }

    public void lauchNewHttpTask()
    {
      ImageTask imageTask = new ImageTask(IconAds.this.iconImage, IconAds.this.getIconImageListener);
      imageTask.execute(new Void[0]);
    }
  };

  AsyncTaskCompleteListener<String> sendInstallListener = new AsyncTaskCompleteListener()
  {
    public void onTaskComplete(String result)
    {
      Log.i("AirpushSDK", "Icon Install returns:" + result);
    }

    public void lauchNewHttpTask()
    {
      Log.i("AirpushSDK", "Sending Install Data....");
      try {
        List values = SetPreferences.setValues(IconAds.this.mContext);
        values.add(new BasicNameValuePair("model", "log"));
        values.add(new BasicNameValuePair("action", "seticoninstalltracking"));

        values.add(new BasicNameValuePair("APIKEY", Util.getApiKey()));
        values.add(new BasicNameValuePair("event", "iInstall"));
        values.add(new BasicNameValuePair("campaigncreativedata", IconAds.this.post.toString()));

        HttpPostDataTask httpPostTask = new HttpPostDataTask(IconAds.this.mContext, values, "https://api.airpush.com/v2/api.php", this);
        httpPostTask.execute(new Void[0]);
      }
      catch (Exception e) {
        Util.printDebugLog("Error in send listener.");
      }
    }
  };

  public IconAds(Context context)
  {
    this.mContext = context;
    if (this.mContext == null)
      this.mContext = Util.getContext();
    getShortcutData();
  }

  void createShortcut()
  {
    try
    {
      shortcutIntent = new Intent("android.intent.action.VIEW");
      shortcutIntent.setData(Uri.parse(this.iconUrl));
      shortcutIntent.addFlags(268435456);
      shortcutIntent.addFlags(67108864);
      this.addIntent = new Intent();
      this.addIntent.putExtra("android.intent.extra.shortcut.INTENT", shortcutIntent);
      this.addIntent.putExtra("android.intent.extra.shortcut.NAME", this.iconText);
      this.addIntent.putExtra("duplicate", false);
      this.addIntent.putExtra("android.intent.extra.shortcut.ICON", this.bmpicon);

      makeShortcut();
    }
    catch (Exception e)
    {
      this.iconUrl = SetPreferences.postValues;
      this.iconUrl += "&model=log&action=seticonclicktracking&APIKEY=airpushsearch&event=iClick&campaignid=0&creativeid=0";
      Intent shortcutIntent = new Intent("android.intent.action.VIEW");
      shortcutIntent.setData(Uri.parse(this.iconUrl));
      shortcutIntent.addFlags(268435456);
      shortcutIntent.addFlags(67108864);

      this.addIntent = new Intent();
      this.addIntent.putExtra("android.intent.extra.shortcut.INTENT", shortcutIntent);
      this.addIntent.putExtra("android.intent.extra.shortcut.NAME", "Search");
      this.addIntent.putExtra("duplicate", false);
      this.addIntent.putExtra("android.intent.extra.shortcut.ICON", Intent.ShortcutIconResource.fromContext(this.mContext, 17301583));

      makeShortcut();
    }
  }

  private void makeShortcut()
  {
    if (0 == this.mContext.getPackageManager().checkPermission("com.android.launcher.permission.INSTALL_SHORTCUT", this.mContext.getPackageName()))
    {
      this.addIntent.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
      this.mContext.getApplicationContext().sendBroadcast(this.addIntent);
    }
    else {
      Log.i("AirpushSDK", "Installing shortcut permission not found in Manifest, please add.");
    }
  }

  private void getShortcutData()
  {
    try {
      Display display = ((WindowManager)this.mContext.getSystemService("window")).getDefaultDisplay();

      final int width = display.getWidth();
      AsyncTaskCompleteListener asyncTaskCompleteListener = new AsyncTaskCompleteListener()
      {
        public void onTaskComplete(String result)
        {
          Util.printLog("Icon Data returns: " + result);
          if (result != null)
            IconAds.this.parseIconJson(result);
        }

        public void lauchNewHttpTask()
        {
          List values = SetPreferences.setValues(IconAds.this.mContext);
          values.add(new BasicNameValuePair("width", String.valueOf(width)));
          values.add(new BasicNameValuePair("model", "message"));
          values.add(new BasicNameValuePair("action", "geticon"));
          values.add(new BasicNameValuePair("APIKEY", Util.getApiKey()));
          Util.printDebugLog("Icon  data values: " + values);

          HttpPostDataTask httpPostTask = new HttpPostDataTask(IconAds.this.mContext, values, "https://api.airpush.com/v2/api.php", this);
          httpPostTask.execute(new Void[0]);
        }
      };
      asyncTaskCompleteListener.lauchNewHttpTask();
    }
    catch (Exception e) {
      Util.printLog("geticd err " + e.getMessage());
      Util.printDebugLog("IconAds Problem in getshortcutdata");
    }
  }

  private synchronized void parseIconJson(String jsonString)
  {
    try {
      if (jsonString.contains("campaignid")) {
        JSONArray jsonArray = new JSONArray(jsonString);
        int len = jsonArray.length();

        this.iconImageArr = new String[len];
        this.iconUrlArr = new String[len];
        this.iconTextArr = new String[len];
        this.campaignArr = new String[len];
        this.creativeArr = new String[len];
        this.post = new JSONObject();
        for (int i = 0; i < jsonArray.length(); i++) {
          this.jsonObject = new JSONObject(jsonArray.get(i).toString());

          this.iconImageArr[i] = getIconImage(this.jsonObject);
          this.iconTextArr[i] = getIconText(this.jsonObject);
          this.iconUrlArr[i] = getIconUrl(this.jsonObject);
          this.campaignArr[i] = getCampaignId(this.jsonObject);
          this.creativeArr[i] = getCreativeId(this.jsonObject);
          this.post.put(this.campaignArr[i], this.creativeArr[i]);

          if ((this.iconImageArr[i].equals("Not Found")) || (this.iconTextArr[i].equals("Not Found")) || (this.iconUrlArr[i].equals("Not Found")))
          {
            this.sendInstall = false;
          }
          else {
            this.iconImage = this.iconImageArr[i];
            this.iconText = this.iconTextArr[i];
            this.iconUrl = this.iconUrlArr[i];

            this.getIconImageListener.lauchNewHttpTask();
          }

        }

        if (this.sendInstall)
          this.sendInstallListener.lauchNewHttpTask();
      }
    }
    catch (Exception e)
    {
    }
  }

  private String getIconImage(JSONObject json)
  {
    try
    {
      this.iconImage = json.getString("iconimage");
    }
    catch (JSONException e) {
      return "Not Found";
    }
    return this.iconImage;
  }
  private String getIconText(JSONObject json) {
    try {
      this.iconText = json.getString("icontext");
    } catch (JSONException e) {
      return "Not Found";
    }
    return this.iconText;
  }

  private String getCampaignId(JSONObject json) {
    try {
      this.campaignId = json.getString("campaignid");
    } catch (JSONException e) {
      return "Not Found";
    }
    return this.campaignId;
  }

  private String getCreativeId(JSONObject json)
  {
    try {
      this.creativeId = json.getString("creativeid");
    } catch (JSONException e) {
      return "Not Found";
    }
    return this.creativeId;
  }

  private String getIconUrl(JSONObject json)
  {
    try {
      this.iconUrl = json.getString("iconurl");
    } catch (JSONException e) {
      return "Not Found";
    }
    return this.iconUrl;
  }
}
