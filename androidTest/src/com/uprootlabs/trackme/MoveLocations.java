package com.uprootlabs.trackme;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.test.AndroidTestCase;

public class MoveLocations extends AndroidTestCase {
  
  SQLiteDatabase myDb;
  TrackMeDB db;
  MyPreference myPreference;
  private long time = 0;

  private void populateTable(String sid, int n) {
    for (long i = 1; i <= n; i++) {
      Location l = InsertLocation.mkLocationWithoutAltitude(15.232, 74.544, 10, i);
      time += 1;
      myPreference.setSessoinID(sid);
      db.insertNewLocation(l, time);
    }
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
    myDb.delete("sid1", null, null);
    myDb.delete("sid2", null, null);
  }
  
  public void testMoveLocationsToSessionTable() {
    int n = db.getQueuedLocationsCount(4);
    assertEquals(0, n);
    populateTable("sid1", 3);
    db.assignUploadID(1, 4);
    Cursor c = db.getLocationsByUploadID(1);
    db.batching(c, 1);
    c.close();
    n = db.getQueuedLocationsCount(4);
    assertEquals(3, n);

    db.moveLocationsToSessionTable(1, "sid1", 1);
    n = db.getQueuedLocationsCount(4);
    assertEquals(0, n);
    c = myDb.query("sid1", null, null, null, null, null, null);
    assertEquals(3, c.getCount());
    c.close();
    
    populateTable("sid1", 2);
    populateTable("sid2", 3);

    db.assignUploadID(2, 9);
    c = db.getLocationsByUploadID(2);
    assertEquals(5, c.getCount());
    db.batching(c, 2);
    c.close();
    n = db.getQueuedLocationsCount(9);
    assertEquals(5, n);

    db.moveLocationsToSessionTable(2, "sid1", 2);
    n = db.getQueuedLocationsCount(9);
    assertEquals(3, n);
    c = myDb.query("sid1", null, null, null, null, null, null);
    assertEquals(5, c.getCount());
    c.close();

    db.moveLocationsToSessionTable(2, "sid2", 1);
    c = myDb.query("sid2", null, null, null, null, null, null);
    assertEquals(3, c.getCount());
    n = db.getQueuedLocationsCount(9);
    assertEquals(0, n);
    c.close();
  }

  public void testArchiveLocations() {
    int n = db.getQueuedLocationsCount(20);
    assertEquals(0, n);
    populateTable("sid1", 3);
    db.assignUploadID(1, 20);
    Cursor c = db.getLocationsByUploadID(1);
    db.batching(c, 1);
    c.close();
    n = db.getQueuedLocationsCount(20);
    assertEquals(3, n);

    db.archiveLocations(1, "sid1", 1);
    n = db.getQueuedLocationsCount(20);
    assertEquals(0, n);
    c = myDb.query(TrackMeDBDetails.TABLE_ARCHIVED_LOCATIONS, null, null, null, null, null, null);
    assertEquals(3, c.getCount());
    c.close();
    
    populateTable("sid1", 2);
    populateTable("sid2", 3);

    db.assignUploadID(2, 20);
    c = db.getLocationsByUploadID(2);
    assertEquals(5, c.getCount());
    db.batching(c, 2);
    c.close();
    n = db.getQueuedLocationsCount(20);
    assertEquals(5, n);

    db.archiveLocations(2, "sid1", 2);
    n = db.getQueuedLocationsCount(20);
    assertEquals(3, n);
    c = myDb.query(TrackMeDBDetails.TABLE_ARCHIVED_LOCATIONS, null, null, null, null, null, null);
    assertEquals(5, c.getCount());
    c.close();

    db.archiveLocations(2, "sid2", 1);
    c = myDb.query(TrackMeDBDetails.TABLE_ARCHIVED_LOCATIONS, null, null, null, null, null, null);
    assertEquals(8, c.getCount());
    n = db.getQueuedLocationsCount(20);
    assertEquals(0, n);
    c.close();
  }

}
