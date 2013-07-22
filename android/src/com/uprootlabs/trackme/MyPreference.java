package com.uprootlabs.trackme;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

final class MyPreference {

  final private SharedPreferences myPreferences;
  private final String USER_ID;
  private final String PASSKEY;
  private final String SERVER_LOCATION;
  private final String AUTO_UPDATE;
  private final String CAPTURE_INTERVAL;
  private final String UPDATE_INTERVAL;
  private final String SESSION_ID;
  private final String UPLOAD_ID;
  private final String NOT_SET = "";

  public MyPreference(final Context context) {
    myPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    USER_ID = context.getResources().getString(R.string.key_userid);
    PASSKEY = context.getResources().getString(R.string.key_passkey);
    SERVER_LOCATION = context.getResources().getString(R.string.key_server_location);
    AUTO_UPDATE = context.getResources().getString(R.string.key_auto_update);
    CAPTURE_INTERVAL = context.getResources().getString(R.string.key_capture_interval);
    UPDATE_INTERVAL = context.getResources().getString(R.string.key_update_interval);
    SESSION_ID = context.getResources().getString(R.string.key_session_id);
    UPLOAD_ID = context.getResources().getString(R.string.key_upload_id);
  }

  public boolean userDetailsNotNull() {
    return !(isNullOrEmpty(getUserID()) || isNullOrEmpty(getPassKey()));
  }

  public boolean serverLocationSet() {
    return !(isNullOrEmpty(getServerLocation()));
  }
  
  private boolean isNullOrEmpty(final String string) {
    return string.trim().equals("") || string == null;
  }

  public String getUserID() {
    return myPreferences.getString(USER_ID, NOT_SET);
  }

  public String getPassKey() {
    return myPreferences.getString(PASSKEY, NOT_SET);
  }

  public String getServerLocation() {
    return myPreferences.getString(SERVER_LOCATION, NOT_SET);
  }

  public String getSessionID() {
    return myPreferences.getString(SESSION_ID, NOT_SET);
  }

  public int getCaptureIntervalMillis() {
    return Integer.parseInt(myPreferences.getString(CAPTURE_INTERVAL, "10")) * TrackMeHelper.MILLISECONDS_PER_SECOND;
  }

  public int getUpdateIntervalMillis() {
    return Integer.parseInt(myPreferences.getString(UPDATE_INTERVAL, "15")) * TrackMeHelper.SECONDS_PER_MINUTE
        * TrackMeHelper.MILLISECONDS_PER_SECOND;
  }

  public boolean isAutoUpdateSet() {
    return myPreferences.getBoolean(AUTO_UPDATE, false);
  }

  public String getNewSessionID() {
    return getSessionID();
  }

  public void setSessoinID(final String sessionID) {
    final SharedPreferences.Editor myPreferencesEditor = myPreferences.edit();
    myPreferencesEditor.putString(SESSION_ID, sessionID);
    myPreferencesEditor.commit();
  }

  public int mkNewUploadID() {
    int uploadID = myPreferences.getInt(UPLOAD_ID, 0);
    uploadID += 1;
    final SharedPreferences.Editor myPreferencesEditor = myPreferences.edit();
    myPreferencesEditor.putInt(UPLOAD_ID, uploadID);
    myPreferencesEditor.commit();
    return uploadID;
  }

}
