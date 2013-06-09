package com.oSfzGRStsd.EezpvONeRd149680;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.util.List;
import java.util.zip.GZIPInputStream;
import org.apache.http.Header;
import org.apache.http.HeaderElement;
import org.apache.http.HttpEntity;
import org.apache.http.HttpRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.HttpResponseInterceptor;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.HttpEntityWrapper;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicHttpResponse;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.protocol.HttpContext;
import org.apache.http.util.EntityUtils;

final class HttpPostDataTask extends AsyncTask<Void, Void, Boolean>
{
  private final AsyncTaskCompleteListener<String> callback;
  private static final String HEADER_ACCEPT_ENCODING = "Accept-Encoding";
  private static final String ENCODING_GZIP = "gzip";
  private List<NameValuePair> valuePairs;
  private final String URL_TO_CALL;
  private String responseString;
  private Context mContext;

  public HttpPostDataTask(Context context, List<NameValuePair> values, String api_url, AsyncTaskCompleteListener<String> asyncTaskCompleteListener)
  {
    Util.printDebugLog("Calling URL:> " + api_url);
    this.mContext = context;
    this.valuePairs = values;
    this.URL_TO_CALL = api_url;
    this.callback = asyncTaskCompleteListener;
  }

  protected synchronized Boolean doInBackground(Void[] params)
  {
    if (Util.checkInternetConnection(this.mContext))
      try
      {
        HttpPost httpPost = new HttpPost(this.URL_TO_CALL);
        httpPost.setEntity(new UrlEncodedFormEntity(this.valuePairs));
        BasicHttpParams httpParameters = new BasicHttpParams();

        int timeoutConnection = 7000;
        HttpConnectionParams.setConnectionTimeout(httpParameters, timeoutConnection);

        int timeoutSocket = 7000;
        HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);

        DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);

        httpClient.addRequestInterceptor(new HttpRequestInterceptor()
        {
          public void process(HttpRequest request, HttpContext context) {
            if (!request.containsHeader("Accept-Encoding"))
            {
              request.addHeader("Accept-Encoding", "gzip");
            }
          }
        });
        httpClient.addResponseInterceptor(new HttpResponseInterceptor()
        {
          public void process(HttpResponse response, HttpContext context)
          {
            HttpEntity entity = response.getEntity();
            Header encoding = entity.getContentEncoding();

            if (encoding != null)
              for (HeaderElement element : encoding.getElements())
              {
                if (element.getName().equalsIgnoreCase("gzip"))
                {
                  response.setEntity(new HttpPostDataTask.InflatingEntity(response.getEntity()));

                  break;
                }
              }
          }
        });
        BasicHttpResponse httpResponse = (BasicHttpResponse)httpClient.execute(httpPost);

        Log.i("AirpushSDK", "Status Code: " + httpResponse.getStatusLine().getStatusCode());

        this.responseString = EntityUtils.toString(httpResponse.getEntity());
        Util.printDebugLog("Response String:" + this.responseString);
        if ((this.responseString != null) && (!this.responseString.equals(""))) {
          return Boolean.TRUE;
        }
      }
      catch (SocketTimeoutException e)
      {
        Log.d("SocketTimeoutException Thrown", e.toString());
      }
      catch (ClientProtocolException e)
      {
        Log.d("ClientProtocolException Thrown", e.toString());
      }
      catch (MalformedURLException e)
      {
        Log.d("MalformedURLException Thrown", e.toString());
      }
      catch (IOException e) {
        Log.d("IOException Thrown", e.toString());
        e.printStackTrace();
      }
      catch (Exception iex)
      {
      }
      catch (Throwable e)
      {
      }
    return Boolean.FALSE;
  }

  protected synchronized void onPostExecute(Boolean result)
  {
    try {
      if (result.booleanValue()) {
        this.callback.onTaskComplete(this.responseString);
      } else {
        this.callback.onTaskComplete(this.responseString);
        Util.printDebugLog("Call Failed due to Network error. ");
      }
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private static class InflatingEntity extends HttpEntityWrapper {
    public InflatingEntity(HttpEntity wrapped) {
      super();
    }

    public InputStream getContent() throws IOException
    {
      return new GZIPInputStream(this.wrappedEntity.getContent());
    }

    public long getContentLength()
    {
      return -1L;
    }
  }
}
