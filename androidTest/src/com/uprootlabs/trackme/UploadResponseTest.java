package com.uprootlabs.trackme;

import com.uprootlabs.trackme.UploadResponse;

import junit.framework.TestCase;

public class UploadResponseTest extends TestCase {

  public void testParseEmptyString() {
    String xml = "";
    UploadResponse resp = UploadResponse.parse(xml);
    assertNull("no response", resp);
  }
  
  public void testParseInvalidString() {
    String xml = "<upload uid=\"2\" > <batch sid=\"sid1\" bid=\"a\" /></upload>";
    UploadResponse resp = UploadResponse.parse(xml);
    assertEquals("Wrong uploadID", 2, resp.uploadId);
    assertEquals("wrong list size", 0, resp.batchResponse.size());
  }
  
  public void testParseInvalidString1() {
    String xml = "<pload uid=\"2\" > <batch sid=\"sid1\" bid=\"a\" /></upload>";
    UploadResponse resp = UploadResponse.parse(xml);
    assertNull("no response", resp);
  }

  public void testParseValidString() {
    String xml = "<upload uid=\"2\" > <batch sid=\"sid1\" bid=\"1\" accepted=\"true\" /></upload>";
    UploadResponse resp = UploadResponse.parse(xml);
    assertEquals("Wrong uploadID", 2, resp.uploadId);
    assertEquals("wrong list size", 1, resp.batchResponse.size());
    assertEquals("Wrong sessionID", "sid1", resp.batchResponse.get(0).sessionId);
    assertEquals("wrong batchID", 1, resp.batchResponse.get(0).batchId);
    assertEquals("Wrong accepted", "true", resp.batchResponse.get(0).accepted);
  }

  public void testParseValidString1() {
    String xml = "<upload uid=\"2\" > <batch sid=\"sid1\" bid=\"1\" accepted=\"true\" /><batch sid=\"sid2\" bid=\"2\" accepted=\"false\" /></upload>";
    UploadResponse resp = UploadResponse.parse(xml);
    assertEquals("Wrong uploadIDno response", 2, resp.uploadId);
    assertEquals("wrong list size", 2, resp.batchResponse.size());
    assertEquals("wrong sessionID", "sid1", resp.batchResponse.get(0).sessionId);
    assertEquals("wrong batchID", 1, resp.batchResponse.get(0).batchId);
    assertEquals("wrong accepted", "true", resp.batchResponse.get(0).accepted);
    assertEquals("wrong sessionID", "sid2", resp.batchResponse.get(1).sessionId);
    assertEquals("wrong batchID", 2, resp.batchResponse.get(1).batchId);
    assertEquals("wrong accepted", "false", resp.batchResponse.get(1).accepted);
  }

  public void testParseNoBatchString() {
    String xml = "<upload uid=\"2\" > </upload>";
    UploadResponse resp = UploadResponse.parse(xml);
    assertEquals("Wrong uploadIDno response", 2, resp.uploadId);
    assertEquals("wrong list size", 0, resp.batchResponse.size());
  }

  public void testParseNoBatchAttributeString1() {
    String xml = "<upload uid=\"2\" > <batch sid=\"sid1\"  /></upload>";
    UploadResponse resp = UploadResponse.parse(xml);
    assertEquals("wrong UploadID", 2, resp.uploadId);
    assertEquals("Batch error", 0, resp.batchResponse.size());
  }

  public void testParseNoBatchAttributeString2() {
    String xml = "<upload uid=\"2\" > <batch bid=\"1\" sid=\"sid1\" /></upload>";
    UploadResponse resp = UploadResponse.parse(xml);
    assertEquals("wrong UploadID", 2, resp.uploadId);
    assertEquals("Batch error", 0, resp.batchResponse.size());
  }

  public void testParseNoBatchAttributeString3() {
    String xml = "<upload uid=\"2\" > <batch bid=\"1\" accepted=\"false\" /></upload>";
    UploadResponse resp = UploadResponse.parse(xml);
    assertEquals("wrong UploadID", 2, resp.uploadId);
    assertEquals("Batch error", 0, resp.batchResponse.size());
  }

  public void testParseNoBatchAttributeString() {
    String xml = "<upload uid=\"2\" > <batch /></upload>";
    UploadResponse resp = UploadResponse.parse(xml);
    assertEquals("Wrong uploadID", 2, resp.uploadId);
    assertEquals("", 0, resp.batchResponse.size());
  }

  public void testParseNoUploadClosingTagString() {
    String xml = "<upload uid=\"2\" > <batch sid=\"sid1\" bid=\"1\" />";
    UploadResponse resp = UploadResponse.parse(xml);
    assertNull("Broken response", resp);
  }

  public void testParseNoUploadOpeningTagString() {
    String xml = "<batch sid=\"sid1\" bid=\"1\" /></upload>";
    UploadResponse resp = UploadResponse.parse(xml);
    assertNull("no response", resp);
  }

  public void testParseNoUploadAttributeString() {
    String xml = "<upload> <batch sid=\"sid1\" bid=\"1\" /></upload>";
    UploadResponse resp = UploadResponse.parse(xml);
    assertNull("no response", resp);
  }

}
