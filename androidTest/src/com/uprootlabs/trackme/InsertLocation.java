package com.uprootlabs.trackme;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.test.AndroidTestCase;

public class InsertLocation extends AndroidTestCase {

  SQLiteDatabase myDb;
  TrackMeDB db;
  MyPreference myPreference;

  public static Location mkLocation(Double lat, Double lng, long alt, long acc, long ts) {
    Location l = new Location("");
    l.setLatitude(lat);
    l.setLongitude(lng);
    l.setAltitude(alt);
    l.setAccuracy(acc);
    l.setTime(ts);
    return l;
  }

  public static Location mkLocationWithoutAltitude(Double lat, Double lng, long acc, long ts) {
    Location l = new Location("");
    l.setLatitude(lat);
    l.setLongitude(lng);
    l.setAccuracy(acc);
    l.setTime(ts);
    return l;
  }

  public static Location mkLocationWithoutAccuracy(Double lat, Double lng, long alt, long ts) {
    Location l = new Location("");
    l.setLatitude(lat);
    l.setLongitude(lng);
    l.setAltitude(alt);
    l.setTime(ts);
    return l;
  }

  public static Location mkLocatiosWithoutAccuracyAndAltitude(Double lat, Double lng, long ts) {
    Location l = new Location("");
    l.setLatitude(lat);
    l.setLongitude(lng);
    l.setTime(ts);
    return l;
  }

  @Override
  public void setUp() {
    Context c = getContext();
    myDb = new TrackMeDBHelper(c).getWritableDatabase();
    db = new TrackMeDB(myDb, c);
    myPreference = new MyPreference(c);
  }

  @Override
  public void tearDown() {
    myDb.delete(TrackMeDBDetails.TABLE_LOCATIONS, null, null);
    myDb.delete(TrackMeDBDetails.TABLE_ARCHIVED_LOCATIONS, null, null);
    myDb.delete(TrackMeDBDetails.TABLE_SESSION, null, null);
  }

  public void testMkStringLocationWithAltitude() {
    Location l = mkLocation(15.35, 74.544, 12, 23, 1352323523);
    String s = TrackMeDB.mkString(l);
    String s1 = "<loc lat=\"15.35\" lng=\"74.544\" alt=\"12\" acc=\"23\" ts=\"1352323523\" />";
    assertEquals("Wrong string", s1, s);
  }

  public void testMkStringLocationWithoutAltitude() {
    Location l = mkLocationWithoutAltitude(15.35, 74.544, 23, 1352323523);
    String s = TrackMeDB.mkString(l);
    String s1 = "<loc lat=\"15.35\" lng=\"74.544\" acc=\"23\" ts=\"1352323523\" />";
    assertEquals("Wrong string", s1, s);
  }

  public void testMkStringLocationWithoutAccuracy() {
    Location l = mkLocationWithoutAccuracy(15.35, 74.544, 12, 1352323523);
    String s = TrackMeDB.mkString(l);
    assertNull(s);
  }

  public void testMkStringLocationNull() {
    Location l = null;
    String s = TrackMeDB.mkString(l);
    assertNull(s);
  }

  public void testInsertLocationsWithoutAltitude() {
    Location l = mkLocationWithoutAltitude(15.35, 74.544, 12, 1);
    long timeStamp = 1;
    myPreference.setSessoinID("session1");
    boolean inserted = db.insertNewLocation(l, timeStamp);
    assertTrue(inserted);
    Cursor c = myDb.query(TrackMeDBDetails.TABLE_LOCATIONS, null, "timeStamp = 1", null, null, null, null);
    assertEquals(1, c.getCount());
    c.moveToFirst();

    assertEquals("session1", c.getString(1));
    assertEquals(15.35 * TrackMeHelper.PI_BY_180, c.getDouble(2));
    assertEquals(74.544 * TrackMeHelper.PI_BY_180, c.getDouble(3));
    if (!c.isNull(4)) {
      fail("Altitude exists");
    }
    assertEquals(12, c.getLong(5));
    assertEquals(1, c.getLong(6));
    if (!c.isNull(7)) {
      fail("Batch exists");
    }
    if (!c.isNull(8)) {
      fail("Upload exists");
    }
  }

  public void testInsertLocationsWithoutAccuracy() {
    Location l = mkLocationWithoutAccuracy(15.35, 74.544, 12, 2);
    long timeStamp = 2;
    myPreference.setSessoinID("session2");
    boolean inserted = db.insertNewLocation(l, timeStamp);
    assertFalse(inserted);
    Cursor c = myDb.query(TrackMeDBDetails.TABLE_LOCATIONS, null, "timeStamp = 2", null, null, null, null);
    if (c.moveToFirst()) {
      fail("Location Should not exist");
    }
  }

  public void testInsertLocationsWithoutAccuracyAndAltitude() {
    Location l = mkLocatiosWithoutAccuracyAndAltitude(15.35, 74.544, 3);
    long timeStamp = 3;
    myPreference.setSessoinID("session3");
    boolean inserted = db.insertNewLocation(l, timeStamp);
    assertFalse(inserted);
    Cursor c = myDb.query(TrackMeDBDetails.TABLE_LOCATIONS, null, "timeStamp = 3", null, null, null, null);
    if (c.moveToFirst()) {
      fail("Location Should not exist");
    }
  }

  public void testInsertLocationsWithoutLocations() {
    Location l = new Location("");
    long timeStamp = 4;
    myPreference.setSessoinID("session4");
    boolean inserted = db.insertNewLocation(l, timeStamp);
    assertFalse(inserted);
    Cursor c = myDb.query(TrackMeDBDetails.TABLE_LOCATIONS, null, "timeStamp = 4", null, null, null, null);
    if (c.moveToFirst()) {
      fail("Location Should not exist");
    }
  }

  public void testInsertLocationsWithLowerAccuracy() {
    Location l = mkLocationWithoutAltitude(15.35, 74.544, 5000, 0);
    long timeStamp = 5;
    myPreference.setSessoinID("session5");
    boolean inserted = db.insertNewLocation(l, timeStamp);
    assertFalse(inserted);
    Cursor c = myDb.query(TrackMeDBDetails.TABLE_LOCATIONS, null, "timeStamp = 5", null, null, null, null);
    if (c.moveToFirst()) {
      fail("Location Should not exist");
    }
  }

  public void testInsertLocationsNull() {
    Location l = null;
    long timeStamp = 6;
    myPreference.setSessoinID("session6");
    boolean inserted = db.insertNewLocation(l, timeStamp);
    assertFalse(inserted);
    Cursor c = myDb.query(TrackMeDBDetails.TABLE_LOCATIONS, null, "timeStamp = 6", null, null, null, null);
    if (c.moveToFirst()) {
      fail("Location Should not exist");
    }
  }

  public void testInsertLocationsWithoutLatitude() {
    Location l = new Location("");
    l.setLongitude(74.544);
    l.setAccuracy(23);
    long timeStamp = 7;
    myPreference.setSessoinID("session7");
    boolean inserted = db.insertNewLocation(l, timeStamp);
    assertTrue(inserted);
    Cursor c = myDb.query(TrackMeDBDetails.TABLE_LOCATIONS, null, "timeStamp = 7", null, null, null, null);
    assertEquals(1, c.getCount());
    c.moveToFirst();

    assertEquals("session7", c.getString(1));
    assertEquals(0 * TrackMeHelper.PI_BY_180, c.getDouble(2));
    assertEquals(74.544 * TrackMeHelper.PI_BY_180, c.getDouble(3));
    if (!c.isNull(4)) {
      fail("Altitude exists");
    }
    assertEquals(23, c.getLong(5));
    assertEquals(7, c.getLong(6));
    if (!c.isNull(7)) {
      fail("Batch exists");
    }
    if (!c.isNull(8)) {
      fail("Upload exists");
    }
  }

  public void testInsertLocationsWithoutLongitude() {
    Location l = new Location("");
    l.setLatitude(15.232);
    l.setAccuracy(23);
    long timeStamp = 8;
    myPreference.setSessoinID("session8");
    boolean inserted = db.insertNewLocation(l, timeStamp);
    assertTrue(inserted);
    Cursor c = myDb.query(TrackMeDBDetails.TABLE_LOCATIONS, null, "timeStamp = 8", null, null, null, null);
    assertEquals(1, c.getCount());
    c.moveToFirst();

    assertEquals("session8", c.getString(1));
    assertEquals(15.232 * TrackMeHelper.PI_BY_180, c.getDouble(2));
    assertEquals(0 * TrackMeHelper.PI_BY_180, c.getDouble(3));
    if (!c.isNull(4)) {
      fail("Altitude exists");
    }
    assertEquals(23, c.getLong(5));
    assertEquals(8, c.getLong(6));
    if (!c.isNull(7)) {
      fail("Batch exists");
    }
    if (!c.isNull(8)) {
      fail("Upload exists");
    }
  }

}
