package com.oSfzGRStsd.EezpvONeRd149680;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.util.Log;
import java.util.List;
import org.apache.http.message.BasicNameValuePair;

public class DialogAd
  implements DialogInterface.OnClickListener
{
  private Activity activity;
  private String title;
  private String buttontxt;
  private String url;
  private String creativeId;
  private String campid;
  private String adtype;
  private String sms;
  private String number;
  private static AlertDialog dialog;
  private String event = "0";

  Runnable runnable = new Runnable()
  {
    public void run()
    {
      if (DialogAd.this.event.equalsIgnoreCase("1")) {
        DialogAd.this.asyncTaskCompleteListener.lauchNewHttpTask();
        DialogAd.this.handleClicks();
      } else {
        DialogAd.this.asyncTaskCompleteListener.lauchNewHttpTask();
        DialogAd.this.activity.finish();
      }
    }
  };

  AsyncTaskCompleteListener<String> asyncTaskCompleteListener = new AsyncTaskCompleteListener()
  {
    public void onTaskComplete(String result)
    {
      Log.i("AirpushSDK", "Dialog Click: " + result);
    }

    public void lauchNewHttpTask()
    {
      List list = SetPreferences.setValues(DialogAd.this.activity);

      list.add(new BasicNameValuePair("creativeid", DialogAd.this.creativeId));
      list.add(new BasicNameValuePair("campaignid", DialogAd.this.campid));
      list.add(new BasicNameValuePair("event", DialogAd.this.event));

      HttpPostDataTask httpPostTask = new HttpPostDataTask(DialogAd.this.activity, list, "https://api.airpush.com/dialogad/adclick.php", this);

      httpPostTask.execute(new Void[0]);
    }
  };

  DialogAd(Intent intent, Activity activity)
  {
    try
    {
      this.activity = activity;
      this.title = intent.getStringExtra("title");
      this.buttontxt = intent.getStringExtra("buttontxt");
      this.url = intent.getStringExtra("url");
      this.creativeId = intent.getStringExtra("creativeid");
      this.campid = intent.getStringExtra("campaignid");
      this.adtype = intent.getStringExtra("adtype");
      this.sms = intent.getStringExtra("sms");
      this.number = intent.getStringExtra("number");

      dialog = showDialog();
    }
    catch (Exception e)
    {
      Util.printDebugLog("Error occured in DialogAd: " + e.getMessage());
    }
  }

  protected AlertDialog showDialog()
  {
    try {
      AlertDialog.Builder builder = new AlertDialog.Builder(this.activity);

      if ((this.title != null) && (!this.title.equalsIgnoreCase("")))
        builder.setMessage(this.title);
      else {
        builder.setMessage("Click for new offers");
      }
      builder.setPositiveButton("No Thanks.", this);

      if ((this.buttontxt != null) && (!this.buttontxt.equalsIgnoreCase("")))
        builder.setNegativeButton(this.buttontxt, this);
      else {
        builder.setNegativeButton("Yes!", this);
      }
      builder.setCancelable(false);
      builder.create();
      return builder.show();
    }
    catch (Exception e) {
      Log.e("AirpushSDK", "Error : " + e.toString());
    }
    return null;
  }

  public void onClick(DialogInterface dialog, int which) {
    try {
      switch (which) {
      case -2:
        this.event = "1";

        dialog.dismiss();
        new Handler().post(this.runnable);

        break;
      case -1:
        this.event = "0";

        dialog.dismiss();
        new Handler().post(this.runnable);
      }
    }
    catch (Exception e)
    {
    }
  }

  void handleClicks()
  {
    try
    {
      if (this.adtype.equalsIgnoreCase("DAU")) {
        Log.i("AirpushSDK", "Pushing dialog DAU Ads.....");
        try {
          Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(this.url));

          intent.addFlags(268435456);
          this.activity.startActivity(intent);
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      } else if (this.adtype.equalsIgnoreCase("DCC")) {
        Log.i("AirpushSDK", "Pushing dialog CC Ads.....");
        try {
          Uri uri = Uri.parse("tel:" + this.number);
          Intent intent = new Intent("android.intent.action.DIAL", uri);
          intent.addFlags(268435456);
          this.activity.startActivity(intent);
        }
        catch (ActivityNotFoundException e) {
          e.printStackTrace();
        } catch (Exception e) {
        }
      }
      else if (this.adtype.equalsIgnoreCase("DCM")) {
        try {
          Log.i("AirpushSDK", "Pushing dialog CM Ads.....");

          Intent intent = new Intent("android.intent.action.VIEW");
          intent.addFlags(268435456);
          intent.setType("vnd.android-dir/mms-sms");
          intent.putExtra("address", this.number);
          intent.putExtra("sms_body", this.sms);
          this.activity.startActivity(intent);
        }
        catch (Exception e) {
          e.printStackTrace();
        }
      } else {
        Util.printLog("Invalid ad type for dialog ad." + this.adtype);
      }
    }
    finally {
      try {
        dialog.dismiss();
        this.activity.finish();
      }
      catch (Exception e)
      {
      }
    }
  }

  public static AlertDialog getDialog()
  {
    return dialog;
  }
}
