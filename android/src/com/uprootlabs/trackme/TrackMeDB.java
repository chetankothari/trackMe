package com.uprootlabs.trackme;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;

import static com.uprootlabs.trackme.TrackMeDBDetails.*;

final class TrackMeDB {
  private final SQLiteDatabase db;
  private final MyPreference myPreferences;

  public TrackMeDB(final SQLiteDatabase db, final Context context) {
    this.db = db;
    myPreferences = new MyPreference(context);
  }

  public boolean insertNewLocation(final Location location, final long timeStamp) {
    if (location != null && location.hasAccuracy()) {
      final double lat = location.getLatitude() * TrackMeHelper.PI_BY_180;
      final double lng = location.getLongitude() * TrackMeHelper.PI_BY_180;
      final long acc = (long) location.getAccuracy();
      final String sessionID = myPreferences.getSessionID();

      if (acc <= LOCATIONS_ACCURACY_LIMIT) {
        final ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_SESSION_ID, sessionID);
        values.put(COLUMN_NAME_LAT, lat);
        values.put(COLUMN_NAME_LNG, lng);
        if (location.hasAltitude()) {
          final double alt = (long) location.getAltitude();
          values.put(COLUMN_NAME_ALT, alt);
        }
        values.put(COLUMN_NAME_ACC, acc);
        values.put(COLUMN_NAME_TS, timeStamp);

        db.insert(TABLE_LOCATIONS, null, values);
        return true;
      } else {
        return false;
      }
    } else {
      return false;
    }
  }

  public void insertLocation(final Double lat, final Double lng, final long timeStamp) {
    final long acc = 12;
    final Location l = new Location("");
    l.setLatitude(lat);
    l.setLongitude(lng);
    l.setAccuracy(acc);
    insertNewLocation(l, timeStamp);
  }

  // private Cursor getLocations(final String selection, final String[]
  // selectionArgs, final String orderBy, final String limit) {
  //
  // final String[] columns = { COLUMN_NAME_SESSION_ID,
  // COLUMN_NAME_LAT, COLUMN_NAME_LNG,
  // COLUMN_NAME_ACC, COLUMN_NAME_TS,
  // COLUMN_NAME_BATCH_ID,
  // COLUMN_NAME_UPLOAD_ID };
  // Cursor c = db.query(TABLE_LOCATIONS, columns,
  // selection, selectionArgs, null, null, orderBy, limit);
  // return c;
  // }

  public static String mkString(final Location l) {
    if (l != null && l.hasAccuracy()) {
      if (l.hasAltitude()) {
        return "<loc lat=\"" + l.getLatitude() + "\" lng=\"" + l.getLongitude() + "\" alt=\"" + (long) l.getAltitude() + "\" acc=\""
            + (long) l.getAccuracy() + "\" ts=\"" + l.getTime() + "\" />";
      } else {
        return "<loc lat=\"" + l.getLatitude() + "\" lng=\"" + l.getLongitude() + "\" acc=\"" + (long) l.getAccuracy() + "\" ts=\""
            + l.getTime() + "\" />";
      }
    } else
      return null;
  }

  public static String mkString(final List<Location> locations) {
    final StringBuffer locationsString = new StringBuffer();
    for (final Location l : locations) {
      locationsString.append(mkString(l));
    }
    return locationsString.toString();
  }

  public String locationsToXML(final Map<SessionBatchTuple, List<Location>> sessions, final int uploadID) {
    final StringBuffer locationsAsXML = new StringBuffer();
    final String userID = myPreferences.getUserID();
    final String passKey = myPreferences.getPassKey();
    locationsAsXML.append("<upload userid=\"" + userID + "\" passkey=\"" + passKey + "\" uid=\"" + uploadID + "\">");
    for (final Map.Entry<SessionBatchTuple, List<Location>> session : sessions.entrySet()) {
      final StringBuffer batch = new StringBuffer();
      final SessionBatchTuple t = session.getKey();
      final List<Location> locations = session.getValue();
      batch.append("<batch sid=\"" + t.getSessionID() + "\" bid=\"" + t.getBatchID() + "\">");
      batch.append(mkString(locations));
      batch.append("</batch>");
      locationsAsXML.append(batch);
    }
    locationsAsXML.append("</upload>");
    return locationsAsXML.toString();
  }

  public Cursor getLocationsByUploadID(final int uploadID) {
    final String[] columns = { COLUMN_NAME_SESSION_ID, COLUMN_NAME_LAT, COLUMN_NAME_LNG, COLUMN_NAME_ALT, COLUMN_NAME_ACC, COLUMN_NAME_TS,
        COLUMN_NAME_BATCH_ID, COLUMN_NAME_UPLOAD_ID };
    final Cursor c = db.query(TABLE_LOCATIONS, columns, COLUMN_NAME_UPLOAD_ID + "=" + uploadID, null, null, null, COLUMN_NAME_TS + " ASC",
        LOCATIONS_QUERY_LIMIT);
    return c;
  }

  public int getQueuedLocationsCount(final long uploadTime) {
    final String[] columns = { _ID };
    final Cursor c = db.query(TABLE_LOCATIONS, columns, COLUMN_NAME_TS + "<=" + uploadTime, null, null, null, null, null);
    final int count = c.getCount();
    c.close();
    return count;
  }

  public void assignUploadID(final int uploadID, final long uploadTime) {
    final String select = "SELECT " + _ID + " FROM " + TABLE_LOCATIONS + " WHERE " + COLUMN_NAME_TS + " < " + uploadTime + " ORDER BY "
        + COLUMN_NAME_TS + " ASC " + " LIMIT " + LOCATIONS_QUERY_LIMIT;
    final String sql = "UPDATE " + TABLE_LOCATIONS + " SET " + COLUMN_NAME_UPLOAD_ID + " = " + uploadID + " WHERE " + _ID + " IN " + "("
        + select + ")";
    db.execSQL(sql);
  }

  public int getBatchID(final String sessionID) {
    final String[] columns = { COLUMN_NAME_LAST_BATCH_ID };
    final String selection = COLUMN_NAME_SESSION_ID + "=?";
    final String[] selectionArgs = { String.valueOf(sessionID) };
    int batchID;
    final Cursor c = db.query(TABLE_SESSION, columns, selection, selectionArgs, null, null, null, null);
    if (c.moveToFirst()) {
      batchID = c.getInt(c.getColumnIndexOrThrow(COLUMN_NAME_LAST_BATCH_ID));
    } else {
      newSession(sessionID);
      batchID = FIRST_BATCH_ID;
    }
    c.close();
    return batchID;
  }

  public int mkNewBatchID(final String sessionID) {
    int batchID = getBatchID(sessionID);
    batchID = batchID + 1;
    return batchID;
  }

  public void updateBatchIDs(final Map<String, Integer> sessionBatches, final int uploadID) {
    for (final Map.Entry<String, Integer> session : sessionBatches.entrySet()) {
      final String sessionID = session.getKey();
      final int batchID = session.getValue();
      final ContentValues lvalues = new ContentValues();
      lvalues.put(COLUMN_NAME_LAST_BATCH_ID, batchID);
      final String selection = COLUMN_NAME_SESSION_ID + "=?";
      final String[] selectionArgs = { sessionID };
      db.update(TABLE_SESSION, lvalues, selection, selectionArgs);

      final ContentValues values = new ContentValues();
      values.put(COLUMN_NAME_BATCH_ID, batchID);
      final String where = COLUMN_NAME_SESSION_ID + "=? AND " + COLUMN_NAME_BATCH_ID + " is null AND " + COLUMN_NAME_UPLOAD_ID + "=?";
      final String[] whereArgs = new String[] { sessionID, "" + uploadID };
      db.update(TABLE_LOCATIONS, values, where, whereArgs);

    }
  }

  private int moveLocations(final String tableName, final int uploadID, final String sessionID, final int batchID) {
    final String where = COLUMN_NAME_UPLOAD_ID + "=" + uploadID + " AND " + COLUMN_NAME_SESSION_ID + "=\"" + sessionID + "\" AND "
        + COLUMN_NAME_BATCH_ID + "=" + batchID;

    final String cols = COLUMN_NAME_SESSION_ID + ", " + COLUMN_NAME_LAT + ", " + COLUMN_NAME_LNG + ", " + COLUMN_NAME_ALT + ", "
        + COLUMN_NAME_ACC + ", " + COLUMN_NAME_TS + ", " + COLUMN_NAME_BATCH_ID;

    final String moveSql = "INSERT INTO " + tableName + " (" + cols + ") " + "SELECT " + cols + " FROM " + TABLE_LOCATIONS + " WHERE "
        + where;

    db.execSQL(moveSql);

    return db.delete(TABLE_LOCATIONS, where, null);
  }

  public int archiveLocations(final int uploadID, final String sessionID, final int batchID) {
    return moveLocations(TABLE_ARCHIVED_LOCATIONS, uploadID, sessionID, batchID);
  }

  public int moveLocationsToSessionTable(final int uploadID, final String sessionID, final int batchID) {
    db.execSQL(TrackMeDBHelper.makeSessionTableSQL(sessionID));
    return moveLocations(sessionID, uploadID, sessionID, batchID);
  }

  public Map<SessionBatchTuple, List<Location>> batching(final Cursor c, final int uploadID) {
    final Map<SessionBatchTuple, List<Location>> map = new HashMap<SessionBatchTuple, List<Location>>();
    final Map<String, Integer> sessionBatches = new HashMap<String, Integer>();
    int batchID;
    final boolean nonEmptyCursor = c.moveToFirst();
    if (nonEmptyCursor) {
      do {
        final String sessionID = c.getString(c.getColumnIndexOrThrow(COLUMN_NAME_SESSION_ID));
        final boolean batchIdAssigned = c.isNull(c.getColumnIndexOrThrow(COLUMN_NAME_BATCH_ID));

        if (batchIdAssigned) {
          batchID = c.getInt(c.getColumnIndexOrThrow(COLUMN_NAME_BATCH_ID));
        } else {
          if (sessionBatches.containsKey(sessionID)) {
            batchID = sessionBatches.get(sessionID);
          } else {
            batchID = mkNewBatchID(sessionID);
            sessionBatches.put(sessionID, batchID);
          }
        }

        final SessionBatchTuple mapKey = new SessionBatchTuple(sessionID, batchID);
        List<Location> batch = map.get(mapKey);
        final Location location = mkLocation(c);

        if (batch == null) {
          map.put(mapKey, batch = new ArrayList<Location>());
        }
        batch.add(location);

      } while (c.moveToNext());
    }

    updateBatchIDs(sessionBatches, uploadID);

    return map;
  }

  private Location mkLocation(final Cursor c) {
    final double latitude = c.getDouble(c.getColumnIndexOrThrow(COLUMN_NAME_LAT));
    final double longitude = c.getDouble(c.getColumnIndexOrThrow(COLUMN_NAME_LNG));
    final long accuracy = (long) c.getDouble(c.getColumnIndexOrThrow(COLUMN_NAME_ACC));
    final long timeStamp = (long) c.getDouble(c.getColumnIndexOrThrow(COLUMN_NAME_TS));
    Location location;
    if (!c.isNull(c.getColumnIndexOrThrow(COLUMN_NAME_ALT))) {
      final Double altitude = c.getDouble(c.getColumnIndexOrThrow(COLUMN_NAME_ALT));
      location = new Location("");
      location.setLatitude(latitude);
      location.setLongitude(longitude);
      location.setAltitude(altitude);
      location.setAccuracy(accuracy);
      location.setTime(timeStamp);
    } else {
      location = new Location("");
      location.setLatitude(latitude);
      location.setLongitude(longitude);
      location.setAccuracy(accuracy);
      location.setTime(timeStamp);
    }
    return location;
  }

  public void newSession(final String sessionID) {
    final ContentValues values = new ContentValues();
    values.put(COLUMN_NAME_SESSION_ID, sessionID);
    db.insert(TABLE_SESSION, null, values);
  }

  public void clearUploadIDs() {
    final String sql = "UPDATE " + TABLE_LOCATIONS + " SET " + COLUMN_NAME_UPLOAD_ID + " = null" + " WHERE " + COLUMN_NAME_UPLOAD_ID
        + " NOT null ";
    db.execSQL(sql);
  }

}
