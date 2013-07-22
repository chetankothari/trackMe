package com.uprootlabs.trackme;

import java.util.HashMap;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.test.AndroidTestCase;

public class SessionTableTesting extends AndroidTestCase {

  SQLiteDatabase myDb;
  TrackMeDB db;
  MyPreference myPreference;

  private void populateTable() {
    for (long i = 10; i <= 20; i++) {
      Location l = InsertLocation.mkLocationWithoutAltitude(15.232, 74.544, 10, i);
      long timeStamp = i;
      myPreference.setSessoinID("session" + i);
      db.insertNewLocation(l, timeStamp);
    }
  }

  @Override
  public void setUp() {
    Context c = getContext();
    myDb = new TrackMeDBHelper(c).getWritableDatabase();
    db = new TrackMeDB(myDb, c);
    myPreference = new MyPreference(c);
    populateTable();
  }

  @Override
  public void tearDown() {
    myDb.delete(TrackMeDBDetails.TABLE_LOCATIONS, null, null);
    myDb.delete(TrackMeDBDetails.TABLE_ARCHIVED_LOCATIONS, null, null);
    myDb.delete(TrackMeDBDetails.TABLE_SESSION, null, null);
  }

  public void testNewSessionCreation1() {
    db.newSession("session1");

    Cursor c = myDb.query(TrackMeDBDetails.TABLE_SESSION, null, TrackMeDBDetails.COLUMN_NAME_SESSION_ID + " = \"session1\"", null, null,
        null, null);
    assertEquals(1, c.getCount());
    if (!c.moveToFirst()) {
      fail("Session not inserted");
    }
    if (c.isNull(2)) {
      assertEquals("session1", c.getString(1));
    }
  }

  public void testNewSessionCreation2() {
    db.newSession("session1");

    Cursor c = myDb.query(TrackMeDBDetails.TABLE_SESSION, null, TrackMeDBDetails.COLUMN_NAME_SESSION_ID + " = \"session1\"", null, null,
        null, null);
    assertEquals(1, c.getCount());
    if (!c.moveToFirst()) {
      fail("Session not inserted");
    }
    if (c.isNull(2)) {
      assertEquals("session1", c.getString(1));
    }
  }

  public void testGetBatchID() {
    int bid = db.getBatchID("session2");

    assertEquals(0, bid);
  }

  public void tesetGetNewBatchID() {
    int bid = db.mkNewBatchID("session2");
    assertEquals(1, bid);
    bid = db.mkNewBatchID("session2");
    assertEquals(2, bid);
  }

  public void testUpdateBatchID() {
    populateTable();
    db.assignUploadID(10, 15);
    Map<String, Integer> sessionBatch = new HashMap<String, Integer>();
    sessionBatch.put("session10", db.mkNewBatchID("session10"));
    sessionBatch.put("session11", db.mkNewBatchID("session11") + 1);
    sessionBatch.put("session21", db.mkNewBatchID("session21"));
    db.updateBatchIDs(sessionBatch, 10);
    Cursor c = myDb.query(TrackMeDBDetails.TABLE_LOCATIONS, null, TrackMeDBDetails.COLUMN_NAME_SESSION_ID + " = \"session10\" OR "
        + TrackMeDBDetails.COLUMN_NAME_SESSION_ID + " = \" session11\" ", null, null, null, null);
    assertEquals(2, c.getCount());
    c = myDb.query(TrackMeDBDetails.TABLE_SESSION, null, TrackMeDBDetails.COLUMN_NAME_SESSION_ID + " = \"session10\"", null, null, null,
        null);
    assertEquals(1, c.getCount());
    c.moveToFirst();
    assertEquals(1, c.getInt(2));
    c = myDb.query(TrackMeDBDetails.TABLE_SESSION, null, TrackMeDBDetails.COLUMN_NAME_SESSION_ID + " = \"session11\"", null, null, null,
        null);
    assertEquals(1, c.getCount());
    c.moveToFirst();
    assertEquals(2, c.getInt(2));
  }
}
