package com.oSfzGRStsd.EezpvONeRd149680;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

final class ImageTask extends AsyncTask<Void, Void, Void>
{
  Bitmap bmpicon = null;
  final String IMAGE_URL;
  final AsyncTaskCompleteListener<Bitmap> listener;

  public ImageTask(String image_url, AsyncTaskCompleteListener<Bitmap> completeListener)
  {
    this.IMAGE_URL = image_url;
    this.listener = completeListener;
  }

  protected Void doInBackground(Void[] params)
  {
    HttpURLConnection httpConnection = null;
    try
    {
      URL u = new URL(this.IMAGE_URL);
      httpConnection = (HttpURLConnection)u.openConnection();
      httpConnection.setRequestMethod("GET");

      httpConnection.setConnectTimeout(20000);

      httpConnection.setReadTimeout(20000);
      httpConnection.setUseCaches(false);
      httpConnection.setDefaultUseCaches(false);
      httpConnection.connect();
      if (httpConnection.getResponseCode() == 200) {
        InputStream iconStream = httpConnection.getInputStream();

        this.bmpicon = BitmapFactory.decodeStream(iconStream);
      }
    }
    catch (Exception ex) {
      Log.e("AirpushSDK", "Network Error, please try again later");
    } finally {
      if (httpConnection != null)
        httpConnection.disconnect();
    }
    return null;
  }

  protected void onPostExecute(Void result)
  {
    if (this.listener != null)
      this.listener.onTaskComplete(this.bmpicon);
  }
}
