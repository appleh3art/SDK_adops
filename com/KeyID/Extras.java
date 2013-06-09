package com.oSfzGRStsd.EezpvONeRd149680;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.Context;
import android.os.Build.VERSION;
import android.util.Log;

public abstract class Extras
{
  static String getEmail(Context context)
  {
    String email = "";
    try {
      if ((Build.VERSION.SDK_INT >= 5) && (context.checkCallingOrSelfPermission("android.permission.GET_ACCOUNTS") == 0)) {
        Account[] accounts = AccountManager.get(context).getAccountsByType("com.google");
        email = accounts[0].name;
      }
    }
    catch (Exception e1) {
      Log.i("AirpushSDK", "No email account found.");
    }
    return email;
  }
}
