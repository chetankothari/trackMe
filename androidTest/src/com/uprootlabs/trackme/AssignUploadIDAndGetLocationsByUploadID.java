package com.uprootlabs.trackme;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.test.AndroidTestCase;

public class AssignUploadIDAndGetLocationsByUploadID extends AndroidTestCase {
  SQLiteDatabase myDb;
  TrackMeDB db;
  MyPreference myPreference;

  private void populateTable() {
    for (long i = 1; i <= 10; i++) {
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

  public void testAssignUploadID1() {
    db.assignUploadID(1, 0);
    Cursor c = db.getLocationsByUploadID(1);
    assertEquals(0, c.getCount());
  }

  public void testAssignUploadID2() {
    db.assignUploadID(2, 3);
    Cursor c = db.getLocationsByUploadID(2);
    assertEquals(2, c.getCount());
  }

  public void testAssignUploadID3() {
    db.assignUploadID(3, 3);
    Cursor c = db.getLocationsByUploadID(3);
    assertEquals(2, c.getCount());
  }

  public void testAssignUploadID4() {
    db.assignUploadID(4, 10);
    Cursor c = db.getLocationsByUploadID(4);
    assertEquals(9, c.getCount());
  }

  public void testAssignUploadID5() {
    db.assignUploadID(5, 11);
    Cursor c = db.getLocationsByUploadID(5);
    assertEquals(10, c.getCount());
    c = myDb.query(TrackMeDBDetails.TABLE_LOCATIONS, null, TrackMeDBDetails.COLUMN_NAME_UPLOAD_ID + " NOT null", null, null, null, null);
    assertEquals(10, c.getCount());
    db.clearUploadIDs();
    c = db.getLocationsByUploadID(5);
    assertEquals(0, c.getCount());
    c = myDb.query(TrackMeDBDetails.TABLE_LOCATIONS, null, TrackMeDBDetails.COLUMN_NAME_UPLOAD_ID + " NOT null", null, null, null, null);
    assertEquals(0, c.getCount());
  }

  public void testAssignUploadID6() {
    db.assignUploadID(6, 3);
    Cursor c = db.getLocationsByUploadID(6);
    assertEquals(2, c.getCount());
  }


}
