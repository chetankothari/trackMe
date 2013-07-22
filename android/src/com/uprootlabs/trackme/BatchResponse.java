package com.uprootlabs.trackme;

final class BatchResponse {

  final public String sessionId;
  final public int batchId;
  final public boolean accepted;

  public BatchResponse(final String sessionId, final int batchId, final boolean accepted) {
    this.sessionId = sessionId;
    this.batchId = batchId;
    this.accepted = accepted;
  }

}
