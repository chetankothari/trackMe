package com.uprootlabs.trackme;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.test.AndroidTestCase;

public class BatchingTest extends AndroidTestCase {

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
  }

  public void testBatching1() {
    List<String> sessions = new ArrayList<String>();
    sessions.add("sid1");
    populateTable(sessions.get(0), 3);
    db.assignUploadID(1, 4);
    Cursor c = db.getLocationsByUploadID(1);
    assertEquals(3, c.getCount());
    Map<SessionBatchTuple, List<Location>> sessionLocations = db.batching(c, 1);
    assertEquals(1, sessionLocations.size());
    for (Map.Entry<SessionBatchTuple, List<Location>> batch : sessionLocations.entrySet()) {
      int i = 0;
      assertEquals(sessions.get(i), (batch.getKey()).getSessionID());
      assertEquals(1, batch.getKey().getBatchID());
      assertEquals(3, batch.getValue().size());
    }

  }

  public void testBatching2() {
    List<String> sessions = new ArrayList<String>();
    sessions.add("sid1");
    sessions.add("sid2");
    sessions.add("sid1");
    List<Integer> batchIDs = new ArrayList<Integer>();
    batchIDs.add(2);
    batchIDs.add(1);
    batchIDs.add(1);
    List<Integer> loc = new ArrayList<Integer>();
    loc.add(2);
    loc.add(3);
    loc.add(3);
    populateTable(sessions.get(0), 3);
    db.assignUploadID(1, 4);
    Cursor c = db.getLocationsByUploadID(1);
    assertEquals(3, c.getCount());
    db.batching(c, 1);
    populateTable(sessions.get(1), 3);
    populateTable(sessions.get(2), 2);
    db.assignUploadID(2, 10);
    int n = db.getQueuedLocationsCount(10);
    assertEquals(8, n);
    c = db.getLocationsByUploadID(2);
    assertEquals(8, c.getCount());
    Map<SessionBatchTuple, List<Location>> sessionLocations = db.batching(c, 2);
    assertEquals(3, sessionLocations.size());
    int i = 0;
    for (Map.Entry<SessionBatchTuple, List<Location>> batch : sessionLocations.entrySet()) {
      assertEquals(sessions.get(i), (batch.getKey()).getSessionID());
      assertEquals((int) batchIDs.get(i), (batch.getKey()).getBatchID());
      assertEquals((int) loc.get(i), batch.getValue().size());
      i += 1;
    }

  }

  public void testLocationsToXML() {
    List<String> sessions = new ArrayList<String>();
    sessions.add("sid1");
    sessions.add("sid2");
    sessions.add("sid1");
    List<Integer> batchIDs = new ArrayList<Integer>();
    batchIDs.add(2);
    batchIDs.add(1);
    batchIDs.add(1);
    List<Integer> loc = new ArrayList<Integer>();
    loc.add(1);
    loc.add(2);
    loc.add(2);
    populateTable(sessions.get(0), 2);
    db.assignUploadID(1, 333);
    Cursor c = db.getLocationsByUploadID(1);
    assertEquals(2, c.getCount());
    db.batching(c, 1);
    populateTable(sessions.get(1), 2);
    populateTable(sessions.get(2), 1);
    db.assignUploadID(2, 6);
    int n = db.getQueuedLocationsCount(6);
    assertEquals(5, n);
    c = db.getLocationsByUploadID(2);
    assertEquals(5, c.getCount());
    Map<SessionBatchTuple, List<Location>> sessionLocations = db.batching(c, 2);
    assertEquals(3, sessionLocations.size());
    int i = 0;
    for (Map.Entry<SessionBatchTuple, List<Location>> batch : sessionLocations.entrySet()) {
      assertEquals(sessions.get(i), (batch.getKey()).getSessionID());
      assertEquals((int) batchIDs.get(i), (batch.getKey()).getBatchID());
      assertEquals((int) loc.get(i), batch.getValue().size());
      i += 1;
    }
    
    String xml = db.locationsToXML(sessionLocations, 2);
    
    Double lat = 15.232 * TrackMeHelper.PI_BY_180;
    Double lng = 74.544 * TrackMeHelper.PI_BY_180;
    
    String locXML = "<upload userid=\"chetan.cmk@gmail.com\" passkey=\"123456\" uid=\"2\">"
    + "<batch sid=\"sid1\" bid=\"2\">"
        + "<loc lat=\"" + lat + "\" lng=\"" + lng + "\" acc=\"10\" ts=\"5\" />"
    + "</batch>"
    + "<batch sid=\"sid2\" bid=\"1\">"
        + "<loc lat=\"" + lat + "\" lng=\"" + lng + "\" acc=\"10\" ts=\"3\" />"
        + "<loc lat=\"" + lat + "\" lng=\"" + lng + "\" acc=\"10\" ts=\"4\" />"
    + "</batch>"
    + "<batch sid=\"sid1\" bid=\"1\">"
        + "<loc lat=\"" + lat + "\" lng=\"" + lng + "\" acc=\"10\" ts=\"1\" />"
        + "<loc lat=\"" + lat + "\" lng=\"" + lng + "\" acc=\"10\" ts=\"2\" />"
    + "</batch>"
    + "</upload>";
    
    assertEquals(locXML, xml);

  }
}
