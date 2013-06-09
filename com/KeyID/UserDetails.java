package com.oSfzGRStsd.EezpvONeRd149680;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.telephony.TelephonyManager;
import android.util.Log;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.UUID;

class UserDetails
{
  private Context context;
  private Location location;

  public UserDetails(Context context)
  {
    this.context = context;
  }

  String getImeiNoMd5()
  {
    try
    {
      String imeinumber = ((TelephonyManager)this.context.getSystemService("phone")).getDeviceId();

      if ((imeinumber == null) || (imeinumber.equals(""))) {
        Class c = Class.forName("android.os.SystemProperties");
        Method get = c.getMethod("get", new Class[] { String.class });
        imeinumber = (String)get.invoke(c, new Object[] { "ro.serialno" });

        Util.setDevice_unique_type("serial");

        if ((imeinumber == null) || (imeinumber.equals("")))
          if (0 == this.context.getPackageManager().checkPermission("android.permission.ACCESS_WIFI_STATE", Util.getPackageName(this.context))) {
            WifiManager manager = (WifiManager)this.context.getSystemService("wifi");
            System.out.println("WIFI " + manager.isWifiEnabled());
            imeinumber = manager.getConnectionInfo().getMacAddress();

            Util.setDevice_unique_type("WIFI_MAC");
          }
          else
          {
            imeinumber = new DeviceUuidFactory(this.context).getDeviceUuid().toString();

            Util.setDevice_unique_type("UUID");
          }
      }
      else {
        Util.setDevice_unique_type("IMEI");
      }

      return imeinumber;
    } catch (Exception ignored) {
      ignored.printStackTrace();
    }
    return "invalid";
  }

  boolean setImeiInMd5()
  {
    try
    {
      String imeinumber = getImeiNoMd5();
      if ((imeinumber == null) || (imeinumber.equals("")) || (imeinumber.equals("invalid"))) {
        Util.printDebugLog("Can not get device unique id.");
        return false;
      }

      MessageDigest mdEnc = MessageDigest.getInstance("MD5");
      mdEnc.update(imeinumber.getBytes(), 0, imeinumber.length());
      String imei = new BigInteger(1, mdEnc.digest()).toString(16);
      Util.setImei(imei);
      return true;
    }
    catch (NoSuchAlgorithmException algorithmException) {
      Log.e("AirpushSDK", "Error occured while converting IMEI to md5." + algorithmException.getMessage());
    }
    catch (Exception e) {
      e.printStackTrace();
    }

    return false;
  }

  Location getLocation()
  {
    Util.printDebugLog("fetching Location.");
    try
    {
      if ((!Util.getLatitude().equals("0")) || (Util.getLastLocationTime() + 900000L > System.currentTimeMillis()))
      {
        return null;
      }
      synchronized (this.context)
      {
        if ((!Util.getLatitude().equals("0")) || (Util.getLastLocationTime() + 900000L > System.currentTimeMillis()))
        {
          Util.printDebugLog("failed in last");
          return null;
        }

        boolean ACCESS_COARSE_LOCATION = 0 == this.context.getPackageManager().checkPermission("android.permission.ACCESS_COARSE_LOCATION", this.context.getPackageName());

        boolean ACCESS_FINE_LOACTION = 0 == this.context.getPackageManager().checkPermission("android.permission.ACCESS_FINE_LOCATION", this.context.getPackageName());

        if ((ACCESS_COARSE_LOCATION) && (ACCESS_FINE_LOACTION))
        {
          LocationManager mlocManager = (LocationManager)this.context.getSystemService("location");

          if (mlocManager == null) {
            Util.printDebugLog("Location manager null");
            return null;
          }

          Criteria criteria = new Criteria();
          criteria.setCostAllowed(false);
          String provider = null;

          if (ACCESS_COARSE_LOCATION) {
            criteria.setAccuracy(2);
            provider = mlocManager.getBestProvider(criteria, true);
          }

          if ((provider == null) && (ACCESS_FINE_LOACTION)) {
            criteria.setAccuracy(1);
            provider = mlocManager.getBestProvider(criteria, true);
          }

          if (provider == null) {
            Util.printDebugLog("Provider null");
            return null;
          }

          this.location = mlocManager.getLastKnownLocation(provider);
          if (this.location != null) {
            Util.printDebugLog("Location found via get last known location.");
            return this.location;
          }
          final LocationManager finalizedLocationManager = mlocManager;

          Util.setLastLocationTime(System.currentTimeMillis());
          mlocManager.requestLocationUpdates(provider, 0L, 0.0F, new LocationListener()
          {
            public void onLocationChanged(Location location)
            {
              Util.setLastLocationTime(System.currentTimeMillis());

              UserDetails.this.location = location;

              finalizedLocationManager.removeUpdates(this);
            }

            public void onProviderDisabled(String provider)
            {
            }

            public void onProviderEnabled(String provider)
            {
            }

            public void onStatusChanged(String provider, int status, Bundle extras)
            {
            }
          }
          , this.context.getMainLooper());
        }
        else
        {
          Util.printDebugLog("Location permission not found.");
        }
      }

    }
    catch (Exception e)
    {
      Util.printLog("Error occured while fetching location. " + e.getMessage());
    }
    catch (Throwable e) {
      Log.e("AirpushSDK", "Error in location: " + e.getMessage());
    }
    return this.location;
  }

  private class DeviceUuidFactory
  {
    protected static final String PREFS_FILE = "device_id.xml";
    protected static final String PREFS_DEVICE_ID = "device_id";
    protected UUID uuid;

    public DeviceUuidFactory(Context context)
    {
      if (this.uuid == null)
        synchronized (DeviceUuidFactory.class) {
          if (this.uuid == null) {
            SharedPreferences prefs = context.getSharedPreferences("device_id.xml", 0);

            String id = prefs.getString("device_id", null);

            if (id != null)
            {
              this.uuid = UUID.fromString(id);
            }
            else
            {
              String androidId = Settings.Secure.getString(context.getContentResolver(), "android_id");
              try
              {
                if (!"9774d56d682e549c".equals(androidId)) {
                  this.uuid = UUID.nameUUIDFromBytes(androidId.getBytes("utf8"));
                }
                else {
                  String deviceId = ((TelephonyManager)context.getSystemService("phone")).getDeviceId();
                  this.uuid = (deviceId != null ? UUID.nameUUIDFromBytes(deviceId.getBytes("utf8")) : UUID.randomUUID());
                }

              }
              catch (UnsupportedEncodingException e)
              {
                throw new RuntimeException(e);
              }

              prefs.edit().putString("device_id", this.uuid.toString()).commit();
            }
          }
        }
    }

    public UUID getDeviceUuid()
    {
      return this.uuid;
    }
  }
}
