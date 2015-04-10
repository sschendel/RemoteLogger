package com.rogansoft.remotelogger;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;

public class RemoteLogger {
	public static final String TAG = "RemoteLogger";

	private static final String PREFS_KEY_IS_LOGGING = "PREFS_KEY_IS_LOGGING";

    public static final String DATE_TIME_STAMP_HIRES_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";

    // Timestamp methods

    private static String calToStr(Calendar date, String format) {
        DateFormat formatter = new SimpleDateFormat(format);
        return formatter.format(date.getTime());
    }

    private static String calToDateTimeHiresStr(Calendar adatetime){
        return calToStr(adatetime,DATE_TIME_STAMP_HIRES_FORMAT);
    }

    private static String getTimeStampString() {
        Calendar now = Calendar.getInstance();
        return calToDateTimeHiresStr(now);
    }

    // Prefs methods

    private static Boolean getPrefBoolean(Context context, String key, Boolean defValue) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getBoolean(key, defValue);
    }


    private static void setPrefBoolean(Context context, String key, Boolean value) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor edit = prefs.edit();
        edit.putBoolean(key, value);
        edit.commit();
    }

    // App name methods

    private static String getAppNameFileName(Context context) {
        String result = getAppName(context);
        // replace non-alphanumeric with _
        result = result.replaceAll("[^a-zA-Z0-9.-]", "_")+".txt";
        return result;
    }

    private static String getAppName(Context context) {
        PackageManager lPackageManager = context.getPackageManager();
        ApplicationInfo lApplicationInfo = null;
        try {
            lApplicationInfo = lPackageManager.getApplicationInfo(context.getApplicationInfo().packageName, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return (String) (lApplicationInfo != null ? lPackageManager.getApplicationLabel(lApplicationInfo) : "Unknown");
    }

    // Public methods

    public static boolean deleteLog(Context context) {
		boolean result = true;
		File root = Environment.getExternalStorageDirectory(); //con.getExternalFilesDir(null); 
		File logFile = new File(root, getAppNameFileName(context));
		if (logFile.isFile()) {
			result = logFile.delete();
		}
		return result;
	}

	public static void appendLog(Context context, String text) {
		File root = Environment.getExternalStorageDirectory(); //con.getExternalFilesDir(null); 
		File logFile = new File(root, getAppNameFileName(context));
		String timeStamp = getTimeStampString();
		if (!logFile.exists()) {
			try {
				logFile.createNewFile();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			// BufferedWriter for performance, true to set append to file flag
			BufferedWriter buf = new BufferedWriter(new FileWriter(logFile,
					true));
			buf.append(timeStamp+" ");
			buf.append(text);
			buf.newLine();
			buf.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

    public static boolean isLogging(Context context) {
		return getPrefBoolean(context, getAppName(context)+PREFS_KEY_IS_LOGGING, false);
	}

	public static boolean toggleLogging(Context context) {
		boolean isLogging =  isLogging(context);
		boolean newIsLoggingValue = !isLogging;
		setPrefBoolean(context, getAppName(context) + PREFS_KEY_IS_LOGGING, newIsLoggingValue);
		return newIsLoggingValue;
	}

    public static void launchSendLogWithAttachment(Context context) {
        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        String appName = getAppName(context);

        emailIntent.setType("jpeg/image");
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, appName+" log file export "+calToDateTimeHiresStr(Calendar.getInstance()));

        File root = Environment.getExternalStorageDirectory(); //con.getExternalFilesDir(null);
        File logFile = new File(root, getAppNameFileName(context));

        Log.d(TAG, "file:"+logFile.getAbsolutePath());
        Log.d(TAG, "exists?" + (logFile.exists()?"true":"false"));
        Log.d(TAG, "canRead?" + (logFile.canRead()?"true":"false"));

        if (logFile.canRead()) {
            emailIntent.putExtra(Intent.EXTRA_TEXT,"Attached is "+appName+" log file!");
            Uri uri = Uri.fromFile(logFile);
            emailIntent.putExtra(Intent.EXTRA_STREAM, uri);
        } else {
            emailIntent.putExtra(Intent.EXTRA_TEXT,"Nothing logged.  No file to send.");
        }

        context.startActivity(Intent.createChooser(emailIntent, "Send log file..."));
    }
}
