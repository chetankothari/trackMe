package com.uprootlabs.trackme;

final class BatchResponse {

  public String sessionId;
  public int batchId;
  public String status;

  public BatchResponse(String sessionId, int batchId, String status) {
    this.sessionId = sessionId;
    this.batchId = batchId;
    this.status = status;
  }

}
