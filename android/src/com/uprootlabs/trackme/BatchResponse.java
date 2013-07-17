package com.uprootlabs.trackme;

import org.w3c.dom.Element;

final class BatchResponse {
  public String sessionId;
  public int batchId;
  public String status;

  public BatchResponse(Element e) {
    sessionId = e.getAttribute("sid");
    batchId = Integer.parseInt(e.getAttribute("bid"));
    status = e.getAttribute("accepted");
  }

}
