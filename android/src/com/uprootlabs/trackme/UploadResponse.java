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
    final List<BatchResponse> batchResp = new ArrayList<BatchResponse>();
    for (int i = 0; i < nl.getLength(); i++) {
      final Element e = (Element) nl.item(i);
      if (e.getAttribute("sid").equals("") | e.getAttribute("bid").equals("") | e.getAttribute("accepted").equals("")) {

      } else {
        final String sessionId = e.getAttribute("sid");
        final int batchId = Integer.parseInt(e.getAttribute("bid"));
        final boolean accepted = e.getAttribute("accepted").equals("true");
        batchResp.add(new BatchResponse(sessionId, batchId, accepted));
      }

    }

    return batchResp;

  }

  public static UploadResponse parse(final HttpResponse resp) {

    try {
      final HttpEntity entity = resp.getEntity();
      final String response;
      response = EntityUtils.toString(entity);
      return parse(response);
    } catch (final ParseException e) {
      return null;
    } catch (final IOException e) {
      return null;
    }

  }

  public static UploadResponse parse(final String xml) {
    Document doc = null;
    final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    try {

      final DocumentBuilder db = dbf.newDocumentBuilder();

      final InputSource is = new InputSource();
      is.setCharacterStream(new StringReader(xml));
      doc = db.parse(is);
      if (doc.getDocumentElement().getAttribute("uid").equals("")) {
        return null;
      } else {

        if (doc.getElementsByTagName("upload") != null) {
          final UploadResponse uploadResponse = new UploadResponse();
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
