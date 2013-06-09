package com.oSfzGRStsd.EezpvONeRd149680;

import android.content.Context;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import org.json.JSONException;
import org.json.JSONObject;

class FormatAds
  implements IConstants
{
  private Context context;
  private long nextMessageCheckValue;
  private String adType;

  public FormatAds(Context context)
  {
    this.context = context;
  }

  synchronized void parseJson(String jsonString)
  {
    this.nextMessageCheckValue = 14400000L;

    JSONObject json = null;
    if (jsonString.contains("nextmessagecheck")) {
      try {
        json = new JSONObject(jsonString);
        this.nextMessageCheckValue = getNextMessageCheckTime(json);
        this.adType = (json.isNull("adtype") ? "invalid" : json.getString("adtype"));
        if (!this.adType.equals("invalid")) {
          Util.setAdType(this.adType);
          getAds(json);
        } else {
          SetPreferences.setSDKStartTime(this.context, this.nextMessageCheckValue);
          PushNotification.reStartSDK(this.context, true);
        }
      } catch (JSONException je) {
        Util.printLog("Error in push JSON: " + je.getMessage());

        Util.printDebugLog("Message Parsing.....Failed : " + je.toString());
      }
      catch (Exception e) {
        Util.printLog("Epush parse: " + e.getMessage());
      }
    }
    else
    {
      Util.printDebugLog("nextmessagecheck is not present in json");
    }
  }

  private long getNextMessageCheckTime(JSONObject json)
  {
    Long nextMsgCheckTime = Long.valueOf(Long.parseLong("300") * 1000L);
    try
    {
      nextMsgCheckTime = Long.valueOf(Long.parseLong(json.get("nextmessagecheck").toString()) * 1000L);
    }
    catch (Exception e)
    {
      e.printStackTrace();
      return 14400000L;
    }
    return nextMsgCheckTime.longValue();
  }

  private void getAds(JSONObject json)
  {
    try
    {
      String title = json.isNull("title") ? "New Message" : json.getString("title");
      String text = json.isNull("text") ? "Click here for details!" : json.getString("text");
      String creativeid = json.isNull("creativeid") ? "" : json.getString("creativeid");
      String campaignid = json.isNull("campaignid") ? "" : json.getString("campaignid");

      Util.setNotification_title(title);
      Util.setNotification_text(text);
      Util.setCampId(campaignid);
      Util.setCreativeId(creativeid);
      if ((this.adType.equals("W")) || (this.adType.equals("BPW"))) {
        String url = json.isNull("url") ? "nothing" : json.getString("url");
        String header = json.isNull("header") ? "Advertisment" : json.getString("header");
        Util.setNotificationUrl(url);
        Util.setHeader(header);
      } else if ((this.adType.equals("A")) || (this.adType.equals("BPA"))) {
        String url = json.isNull("url") ? "nothing" : json.getString("url");
        String header = json.isNull("header") ? "Advertisment" : json.getString("header");
        Util.setNotificationUrl(url);
        Util.setHeader(header);
      }
      else if ((this.adType.equals("CM")) || (this.adType.equals("BPCM"))) {
        String number = json.isNull("number") ? "0" : json.getString("number");
        String sms = json.isNull("sms") ? "" : json.getString("sms");
        Util.setPhoneNumber(number);
        Util.setSms(sms);
      } else if ((this.adType.equals("CC")) || (this.adType.equals("BPCC"))) {
        String number = json.isNull("number") ? "0" : json.getString("number");
        Util.setPhoneNumber(number);
      }

      String delivery_time = json.isNull("delivery_time") ? "0" : json.getString("delivery_time");
      Long expirytime = Long.valueOf(json.isNull("expirytime") ? Long.parseLong("86400000") : json.getLong("expirytime"));
      String adimageurl = json.isNull("adimage") ? "http://beta.airpush.com/images/adsthumbnail/48.png" : json.getString("adimage");
      String ip1 = json.isNull("ip1") ? "invalid" : json.getString("ip1");
      String ip2 = json.isNull("ip2") ? "invalid" : json.getString("ip2");
      Util.setDelivery_time(delivery_time);
      Util.setExpiry_time(expirytime.longValue());
      Util.setAdImageUrl(adimageurl);
      Util.setIP1(ip1);
      Util.setIP2(ip2);
      new SetPreferences(this.context).storeIP();
      if ((!Util.getDelivery_time().equals(null)) && (!Util.getDelivery_time().equals("0"))) {
        SimpleDateFormat format0 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        format0.setTimeZone(TimeZone.getTimeZone("GMT"));
        format0.format(new Date());
      }
      new DeliverNotification(this.context);
    }
    catch (Exception e) {
      Util.printLog("Push parsing error: " + e.getMessage());

      Util.printDebugLog("Push Message Parsing.....Failed ");
    } finally {
      try {
        SetPreferences.setSDKStartTime(this.context, this.nextMessageCheckValue);
        PushNotification.reStartSDK(this.context, true);
      }
      catch (Exception e) {
        e.printStackTrace();
      }
    }
  }
}
