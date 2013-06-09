package com.oSfzGRStsd.EezpvONeRd149680;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.GradientDrawable.Orientation;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.List;
import org.apache.http.message.BasicNameValuePair;

public class OptinActivity extends Activity
{
  private static String TAG = "AirpushSDK";
  private static final String OPT_IN_TEXT = "<html><body style='background:#C4C4C4;font-family:Arial;font-size:11pt;line-height:18px'><p align='justify'>Thank you for downloading this free, ad-supported application! Please read carefully. This application is ad-supported and our advertising partner, Airpush, Inc., may place ads within applications and in your device's notification tray and home screen.  Airpush collects certain information in accordance with the permissions you just granted through the prior screen.  When you click on advertisements delivered by Airpush, you will typically be directed to a third party's web page and we may pass certain of your information to the third parties operating or hosting these pages, including your email address, phone number and a list of the apps on your device.</p><p align='justify'>  For more information on how Airpush collects, uses and shares your information, and to learn about your information choices, please visit the <a href='http://m.airpush.com/privacypolicy'><i>Airpush Privacy Policy</i> </a>. If you do not wish to receive ads delivered by Airpush in the future, you may visit the <a href='http://m.airpush.com/optout'><i>Airpush opt-out page</i></a> or delete this app.</p></body></html>";
  private static final String TITLE = "Privacy Policy & Advertising Terms";
  private static String event = "optOut";
  private static WebView webView;
  private OptinDialog dialog;
  private String adType;
  private Intent intent;
  AsyncTaskCompleteListener<String> asyncTaskCompleteListener = new AsyncTaskCompleteListener()
  {
    public void onTaskComplete(String result)
    {
      Log.i(OptinActivity.TAG, OptinActivity.event + " data sent: " + result);
      OptinActivity.this.finish();
    }

    public void lauchNewHttpTask()
    {
      List list = new ArrayList();
      list.add(new BasicNameValuePair("event", OptinActivity.event));
      list.add(new BasicNameValuePair("imei", "" + Util.getImei()));

      list.add(new BasicNameValuePair("appId", Util.getAppID()));

      Log.i(OptinActivity.TAG, OptinActivity.event + " Data: " + list);
      HttpPostDataTask httpPostTask = new HttpPostDataTask(OptinActivity.this, list, "https://api.airpush.com/optin/", this);

      httpPostTask.execute(new Void[0]);
    }
  };

  protected void onCreate(Bundle savedInstanceState)
  {
    super.onCreate(savedInstanceState);
    requestWindowFeature(1);
    try {
      this.intent = getIntent();
      if ((this.intent != null) && ((this.intent.getStringExtra("adtype").equalsIgnoreCase("DAU")) || (this.intent.getStringExtra("adtype").equalsIgnoreCase("DCM")) || (this.intent.getStringExtra("adtype").equalsIgnoreCase("DCC")))) {
        this.adType = this.intent.getStringExtra("adtype");
        new DialogAd(this.intent, this);
      }
      return;
    }
    catch (Exception e)
    {
      if (SetPreferences.isShowOptinDialog(getApplicationContext())) {
        this.dialog = new OptinDialog(this);
        this.dialog.show();
      }
    }
  }

  protected void onUserLeaveHint() {
    try {
      if ((this.adType != null) && ((this.adType.equalsIgnoreCase("DAU")) || (this.adType.equalsIgnoreCase("DCM")) || (this.adType.equalsIgnoreCase("DCC")))) {
        DialogAd.getDialog().dismiss();
        finish();
      }
    }
    catch (Exception e)
    {
      finish();
    }

    super.onUserLeaveHint();
  }

  public void onConfigurationChanged(Configuration newConfig)
  {
    super.onConfigurationChanged(newConfig);
  }

  public boolean onKeyDown(int keyCode, KeyEvent event)
  {
    try {
      if ((this.adType != null) && ((this.adType.equalsIgnoreCase("DAU")) || (this.adType.equalsIgnoreCase("DCM")) || (this.adType.equalsIgnoreCase("DCC"))) && 
        (keyCode == 4) && (event.getAction() == 0)) {
        return false;
      }
      if ((keyCode == 4) && (event.getAction() == 0))
      {
        if (this.dialog != null)
          this.dialog.dismiss();
        if (webView != null)
          webView.destroy();
        finish();
      }
    }
    catch (Exception e)
    {
    }
    return true;
  }

  private class MyWebViewClient extends WebViewClient
  {
    private MyWebViewClient()
    {
    }

    public boolean shouldOverrideUrlLoading(WebView view, String url)
    {
      try
      {
        Intent intent = new Intent("android.intent.action.VIEW", Uri.parse(url));
        OptinActivity.this.startActivity(intent);
      }
      catch (Exception e) {
      }
      return true;
    }
  }

  public class OptinDialog extends AlertDialog
  {
    Context context;

    protected OptinDialog(Context context)
    {
      super();
      this.context = context;
      showOptinDialog();
    }
    private void showOptinDialog() {
      Log.i(OptinActivity.TAG, "Display Privacy & Terms");
      try
      {
        setTitle("Privacy Policy & Advertising Terms");

        int[] colors = { Color.parseColor("#A5A5A5"), Color.parseColor("#9C9C9C"), Color.parseColor("#929493") };
        GradientDrawable drawable = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM, colors);

        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(-1, -1);

        LinearLayout linearLayout = new LinearLayout(this.context);
        linearLayout.setLayoutParams(layoutParams);

        linearLayout.setOrientation(1);
        float scale = this.context.getResources().getDisplayMetrics().density;

        LinearLayout buttonLayout = new LinearLayout(this.context);
        buttonLayout.setGravity(17);

        buttonLayout.setBackgroundDrawable(drawable);
        LinearLayout.LayoutParams buttonLayoutParams = new LinearLayout.LayoutParams(-1, (int)(scale * 60.0F), 2.0F);

        buttonLayoutParams.topMargin = ((int)-(60.0F * scale));
        buttonLayoutParams.gravity = 80;
        buttonLayout.setOrientation(0);
        buttonLayout.setLayoutParams(buttonLayoutParams);

        TextView closeText = new TextView(this.context);
        closeText.setGravity(17);
        LinearLayout.LayoutParams btparParams = new LinearLayout.LayoutParams(-1, -2, 2.0F);

        btparParams.gravity = 17;

        closeText.setLayoutParams(btparParams);
        closeText.setTextColor(-16777216);

        closeText.setTextAppearance(this.context, 16843271);

        SpannableString content = new SpannableString("Close");
        content.setSpan(new UnderlineSpan(), 0, content.length(), 0);
        closeText.setText(content);
        closeText.setId(-2);

        buttonLayout.addView(closeText);

        Button continueButton = new Button(this.context);

        continueButton.setId(-1);
        continueButton.setLayoutParams(new LinearLayout.LayoutParams(-1, -2, 2.0F));

        continueButton.setText("Ok");

        buttonLayout.addView(continueButton);

        buttonLayout.setBackgroundColor(-3355444);

        LinearLayout layout = new LinearLayout(this.context);
        LinearLayout.LayoutParams webLayoutParams = new LinearLayout.LayoutParams(-1, -1);

        webLayoutParams.bottomMargin = ((int)(scale * 60.0F));
        layout.setLayoutParams(webLayoutParams);

        OptinActivity.access$102(new WebView(this.context));

        OptinActivity.webView.loadData("<html><body style='background:#C4C4C4;font-family:Arial;font-size:11pt;line-height:18px'><p align='justify'>Thank you for downloading this free, ad-supported application! Please read carefully. This application is ad-supported and our advertising partner, Airpush, Inc., may place ads within applications and in your device's notification tray and home screen.  Airpush collects certain information in accordance with the permissions you just granted through the prior screen.  When you click on advertisements delivered by Airpush, you will typically be directed to a third party's web page and we may pass certain of your information to the third parties operating or hosting these pages, including your email address, phone number and a list of the apps on your device.</p><p align='justify'>  For more information on how Airpush collects, uses and shares your information, and to learn about your information choices, please visit the <a href='http://m.airpush.com/privacypolicy'><i>Airpush Privacy Policy</i> </a>. If you do not wish to receive ads delivered by Airpush in the future, you may visit the <a href='http://m.airpush.com/optout'><i>Airpush opt-out page</i></a> or delete this app.</p></body></html>", "text/html", "utf-8");
        OptinActivity.webView.setWebChromeClient(new WebChromeClient());
        OptinActivity.webView.setWebViewClient(new OptinActivity.MyWebViewClient(OptinActivity.this, null));
        OptinActivity.webView.setScrollBarStyle(33554432);
        layout.addView(OptinActivity.webView);

        linearLayout.addView(layout);
        linearLayout.addView(buttonLayout);

        setView(linearLayout);

        setCancelable(true);

        closeText.setOnClickListener(new View.OnClickListener()
        {
          public void onClick(View arg0) {
            try {
              if (Util.checkInternetConnection(OptinActivity.this)) {
                OptinActivity.access$302("optOut");
                OptinActivity.OptinDialog.this.dismiss();
                OptinActivity.this.asyncTaskCompleteListener.lauchNewHttpTask();
                Airpush.startNewAdThread(false);
              } else {
                OptinActivity.OptinDialog.this.dismiss();
                Airpush.startNewAdThread(false);
                OptinActivity.this.finish();
              }
            } catch (Exception e) {
              e.printStackTrace();
            }
          }
        });
        continueButton.setOnClickListener(new View.OnClickListener()
        {
          public void onClick(View arg0)
          {
            try {
              OptinActivity.OptinDialog.this.dismiss();

              if (Util.checkInternetConnection(OptinActivity.OptinDialog.this.context)) {
                OptinActivity.access$302("optIn");

                OptinActivity.this.asyncTaskCompleteListener.lauchNewHttpTask();
                Airpush.startNewAdThread(true);
              }
              else
              {
                Airpush.startNewAdThread(true);

                OptinActivity.this.finish();
              }
            }
            catch (Exception e) {
              e.printStackTrace();
            }
          } } );
      }
      catch (Exception e) {
        e.printStackTrace();
        OptinActivity.this.finish();
      }
    }
  }
}
