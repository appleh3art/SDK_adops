package com.bugsense.trace;

import android.os.AsyncTask;
import android.os.AsyncTask.Status;

public abstract class ActivityAsyncTask<Connect, Params, Progress, Result> extends AsyncTask<Params, Progress, Result>
{
  protected volatile Connect mWrapped;
  private volatile boolean mPostProcessingDone = false;
  private Result mResult;

  public ActivityAsyncTask(Connect paramConnect)
  {
    connectTo(paramConnect);
  }

  public void connectTo(Connect paramConnect)
  {
    if ((this.mWrapped != null) && (paramConnect != null))
      throw new IllegalStateException();
    this.mWrapped = paramConnect;
    if (this.mWrapped != null)
      if (getStatus() == AsyncTask.Status.RUNNING)
      {
        onPreExecute();
      }
      else if ((getStatus() == AsyncTask.Status.FINISHED) && (!this.mPostProcessingDone))
      {
        this.mPostProcessingDone = true;
        processPostExecute(this.mResult);
        this.mResult = null;
      }
  }

  public boolean postProcessingDone()
  {
    return this.mPostProcessingDone;
  }

  protected void onPostExecute(Result paramResult)
  {
    super.onPostExecute(paramResult);
    if (this.mWrapped != null)
    {
      this.mPostProcessingDone = true;
      processPostExecute(paramResult);
    }
    else
    {
      this.mResult = paramResult;
    }
  }

  protected abstract void processPostExecute(Result paramResult);
}
