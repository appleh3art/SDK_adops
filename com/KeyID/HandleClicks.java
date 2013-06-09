package com.oSfzGRStsd.EezpvONeRd149680;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;

class HandleClicks
{
  private final String TAG = "AirpushSDK";
  private Context context;
  private Intent intent;
  private Uri uri;

  public HandleClicks(Context context)
  {
    this.context = context;
  }
  void callNumber() {
    Log.i("AirpushSDK", "Pushing CC Ads.....");
    try {
      this.uri = Uri.parse("tel:" + Util.getPhoneNumber());
      this.intent = new Intent("android.intent.action.DIAL", this.uri);
      this.intent.addFlags(268435456);
      this.context.startActivity(this.intent);
    }
    catch (ActivityNotFoundException e) {
      Log.e("AirpushSDK", "Error whlie displaying push ad......: " + e.getMessage());
    }
  }

  void sendSms() {
    try {
      Log.i("AirpushSDK", "Pushing CM Ads.....");

      this.intent = new Intent("android.intent.action.VIEW");
      this.intent.addFlags(268435456);
      this.intent.setType("vnd.android-dir/mms-sms");
      this.intent.putExtra("address", Util.getPhoneNumber());
      this.intent.putExtra("sms_body", Util.getSms());
      this.context.startActivity(this.intent);
    }
    catch (Exception e) {
      Log.e("AirpushSDK", "Error whlie displaying push ad......: " + e.getMessage());
    }
  }

  void displayUrl() {
    Log.i("AirpushSDK", "Pushing Web and App Ads.....");
    try {
      this.intent = new Intent("android.intent.action.VIEW", Uri.parse(Util.getNotificationUrl()));
      this.intent.addFlags(268435456);
      this.context.startActivity(this.intent);
    } catch (ActivityNotFoundException e) {
      Log.e("AirpushSDK", "Error whlie displaying push ad......: " + e.getMessage());
    }
  }
}
