package com.bugsense.trace;

import android.util.Log;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Date;
import java.util.Random;

public class DefaultExceptionHandler
  implements Thread.UncaughtExceptionHandler
{
  private Thread.UncaughtExceptionHandler defaultExceptionHandler;

  public DefaultExceptionHandler(Thread.UncaughtExceptionHandler paramUncaughtExceptionHandler)
  {
    this.defaultExceptionHandler = paramUncaughtExceptionHandler;
  }

  public void uncaughtException(Thread paramThread, Throwable paramThrowable)
  {
    Date localDate = new Date();
    int i = 0;
    StringWriter localStringWriter = new StringWriter();
    PrintWriter localPrintWriter = new PrintWriter(localStringWriter);
    paramThrowable.printStackTrace(localPrintWriter);
    try
    {
      BugSense.submitError(BugSenseHandler.gContext, i, localDate, localStringWriter.toString());
    }
    catch (Exception localException1)
    {
      Log.e(G.TAG, "Error sending exception stacktrace", paramThrowable);
      try
      {
        Random localRandom = new Random();
        int j = localRandom.nextInt(99999);
        String str = G.APP_VERSION + "-" + Integer.toString(j);
        Log.d(G.TAG, "Writing unhandled exception to: " + G.FILES_PATH + "/" + str + ".stacktrace");
        BufferedWriter localBufferedWriter = new BufferedWriter(new FileWriter(G.FILES_PATH + "/" + str + ".stacktrace"));
        localBufferedWriter.write(G.ANDROID_VERSION + "\n");
        localBufferedWriter.write(G.PHONE_MODEL + "\n");
        localBufferedWriter.write(localDate + "\n");
        localBufferedWriter.write(localStringWriter.toString());
        localBufferedWriter.flush();
        localBufferedWriter.close();
      }
      catch (Exception localException2)
      {
        Log.e(G.TAG, "Error saving exception stacktrace", paramThrowable);
      }
    }
    Log.d(G.TAG, localStringWriter.toString());
    this.defaultExceptionHandler.uncaughtException(paramThread, paramThrowable);
  }
}
