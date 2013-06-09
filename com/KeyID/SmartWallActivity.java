package com.oSfzGRStsd.EezpvONeRd149680;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public class SmartWallActivity extends Activity
{
  private WebView mWebView;
  private String adType;
  private ProgressDialog dialog;

  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    try {
      this.dialog = ProgressDialog.show(this, null, "Loading....");
      this.dialog.setCancelable(true);

      Intent intent = getIntent();
      this.adType = intent.getStringExtra("adtype");
      if ((this.adType != null) && (this.adType.equalsIgnoreCase("AW"))) {
        requestWindowFeature(1);
        Util.printDebugLog("Appwall called: ");
        appWallAd(intent);
      } else if ((this.adType != null) && ((this.adType.equalsIgnoreCase("DAU")) || (this.adType.equalsIgnoreCase("DCM")) || (this.adType.equalsIgnoreCase("DCC")))) {
        requestWindowFeature(1);
        Util.printDebugLog("Dialog Ad called: ");
        setTheme(16973839);
        new DialogAd(intent, this);
      } else if ((this.adType != null) && (this.adType.equalsIgnoreCase("FP"))) {
        Util.printDebugLog("Landing page called: ");
        requestWindowFeature(1);
        getWindow().setFlags(1024, 1024);

        LandingPageAd();
      }
      else {
        this.dialog.dismiss();
        finish();
      }
    }
    catch (Exception e)
    {
    }
  }

  private void appWallAd(Intent intent)
  {
    try
    {
      String url = intent.getStringExtra("url");

      this.mWebView = new WebView(getApplicationContext());
      this.mWebView.getSettings().setJavaScriptEnabled(true);

      this.mWebView.setWebChromeClient(new WebChromeClient());
      this.mWebView.setScrollBarStyle(33554432);
      this.mWebView.setWebViewClient(new AirpushWebClient(null));

      this.mWebView.loadUrl(url);

      setContentView(this.mWebView);
    }
    catch (Exception e)
    {
    }
    catch (Throwable e)
    {
    }
  }

  private void LandingPageAd()
  {
    try
    {
      DisplayMetrics metrics = getResources().getDisplayMetrics();

      float density = metrics.density;
      LinearLayout linearLayout = new LinearLayout(this);
      linearLayout.setOrientation(1);
      LinearLayout.LayoutParams mainLayoutParams = new LinearLayout.LayoutParams(-1, -1);

      linearLayout.setLayoutParams(mainLayoutParams);

      ImageView imageView = new ImageView(this);
      imageView.setBackgroundColor(Color.parseColor("#00B0F0"));
      LinearLayout.LayoutParams borderLayoutParams = new LinearLayout.LayoutParams(-1, (int)density * 7);

      linearLayout.addView(imageView, borderLayoutParams);

      RelativeLayout relativeLayout = new RelativeLayout(this);
      RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(-1, -2);

      relativeLayout.setLayoutParams(layoutParams);

      RelativeLayout.LayoutParams txtLayoutParams = new RelativeLayout.LayoutParams(-2, -2);

      txtLayoutParams.addRule(15, -1);

      TextView textView = new TextView(this);
      textView.setText("Ad ");
      textView.setTextColor(-1);
      textView.setLayoutParams(txtLayoutParams);
      textView.setGravity(16);
      textView.setId(11);

      Button button = new Button(this);
      RelativeLayout.LayoutParams buttonLayoutParams = new RelativeLayout.LayoutParams(-2, -2);

      buttonLayoutParams.addRule(11);
      button.setLayoutParams(buttonLayoutParams);
      button.setText("X");
      button.setPadding(0, (int)density * 2, (int)density * 10, (int)density * 2);

      button.setTextSize(15.0F);
      button.setTypeface(Typeface.DEFAULT, 1);
      button.setTextColor(-1);
      button.setBackgroundColor(Color.parseColor("#31849B"));

      button.setOnClickListener(new View.OnClickListener() {
        public void onClick(View v) {
          SmartWallActivity.this.finish();
        }
      });
      this.mWebView = new WebView(this);
      this.mWebView.getSettings().setJavaScriptEnabled(true);
      this.mWebView.setWebChromeClient(new WebChromeClient());
      this.mWebView.setScrollBarStyle(33554432);
      this.mWebView.setWebViewClient(new AirpushWebClient(null));

      this.mWebView.loadUrl(Util.getLandingPageAdUrl());

      linearLayout.addView(relativeLayout);
      relativeLayout.addView(textView);
      relativeLayout.addView(button);
      relativeLayout.setBackgroundColor(Color.parseColor("#31849B"));
      linearLayout.addView(this.mWebView, mainLayoutParams);

      setContentView(linearLayout);
    }
    catch (Exception e)
    {
      Log.e("AirpushSDK", "An error occured while starting LandingPageAd.");

      finish();
    }
  }

  protected void onPause()
  {
    try
    {
      this.dialog.dismiss();
    }
    catch (Exception e) {
    }
    super.onPause();
  }

  public boolean onKeyDown(int keyCode, KeyEvent event) {
    try {
      if ((this.adType != null) && ((this.adType.equalsIgnoreCase("DAU")) || (this.adType.equalsIgnoreCase("DCM")) || (this.adType.equalsIgnoreCase("DCC"))) && 
        (keyCode == 4) && (event.getAction() == 0)) {
        return false;
      }
      if (keyCode == 4) {
        if (this.dialog != null)
          this.dialog.dismiss();
        if (this.mWebView != null)
          this.mWebView.destroy();
        finish();
        return false;
      }
    }
    catch (Exception e) {
    }
    return super.onKeyDown(keyCode, event);
  }

  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
  }

  private class AirpushWebClient extends WebViewClient
  {
    private AirpushWebClient()
    {
    }

    public boolean shouldOverrideUrlLoading(WebView view, String url)
    {
      try
      {
        if (SmartWallActivity.this.dialog != null)
          SmartWallActivity.this.dialog.dismiss();
      } catch (Exception e) {
      }
      try {
        Util.printDebugLog("SmartWall Url: " + url);

        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(url));
        intent.addFlags(268435456);
        SmartWallActivity.this.startActivity(intent);
        if (SmartWallActivity.this.mWebView != null)
          SmartWallActivity.this.mWebView.destroy();
        SmartWallActivity.this.finish();
        return true;
      }
      catch (Exception e) {
      }
      return true;
    }

    public void onPageFinished(WebView view, String url)
    {
      try
      {
        SmartWallActivity.this.dialog.dismiss();
      }
      catch (Exception e) {
      }
      super.onPageFinished(view, url);
    }
  }
}
