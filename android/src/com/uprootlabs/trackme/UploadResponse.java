package com.uprootlabs.trackme;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.util.EntityUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import android.net.ParseException;
import android.util.Log;

final class UploadResponse {
  public int uploadId;
  public List<BatchResponse> batchResponse;

  private static List<BatchResponse> getBatchResponse(final NodeList nl) {
    List<BatchResponse> batchResp = new ArrayList<BatchResponse>();
    for (int i = 0; i < nl.getLength(); i++) {
      final Element e = (Element) nl.item(i);
      if (e.getAttribute("sid").equals("") | e.getAttribute("bid").equals("") | e.getAttribute("accepted").equals("")) {

      } else {
        String sessionId = e.getAttribute("sid");
        int batchId = Integer.parseInt(e.getAttribute("bid"));
        String status = e.getAttribute("accepted");
        batchResp.add(new BatchResponse(sessionId, batchId, status));
      }

    }

    return batchResp;

  }

  public static UploadResponse parse(HttpResponse resp) {

    final HttpEntity entity = resp.getEntity();
    String response;

    try {
      response = EntityUtils.toString(entity);
    } catch (final ParseException e) {
      response = "ParseException";
    } catch (final IOException e) {
      response = "IOException";
    }

    return parse(response);

  }

  public static UploadResponse parse(String xml) {
    Document doc = null;
    final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    try {

      final DocumentBuilder db = dbf.newDocumentBuilder();

      final InputSource is = new InputSource();
      is.setCharacterStream(new StringReader(xml));
      doc = db.parse(is);
      UploadResponse uploadResponse = new UploadResponse();
      if (doc.getDocumentElement().getAttribute("uid").equals("")) {
        return null;
      } else {

        if(doc.getElementsByTagName("upload") != null){
        uploadResponse.uploadId = Integer.parseInt(doc.getDocumentElement().getAttribute("uid"));
        final NodeList nl = doc.getElementsByTagName("batch");
        uploadResponse.batchResponse = getBatchResponse(nl);
        return uploadResponse;
        } else {
          return null;
        }

      }

    } catch (final ParserConfigurationException e) {
      Log.e("Error: ", e.getMessage());
      return null;
    } catch (final SAXException e) {
      Log.e("Error: ", e.getMessage());
      return null;
    } catch (final IOException e) {
      Log.e("Error: ", e.getMessage());
      return null;
    }

  }

}
