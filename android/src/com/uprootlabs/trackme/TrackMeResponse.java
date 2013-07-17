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

final class TrackMeResponse {
  public int uploadId;
  public List<BatchResponse> batchResponse;

  public TrackMeResponse(final HttpResponse resp) {
    final String response = getResponseString(resp);
    final Document dom = getDom(response);
    uploadId = Integer.parseInt(dom.getDocumentElement().getAttribute("uid"));
    final NodeList nl = dom.getElementsByTagName("batch");
    batchResponse = getBatchResponse(nl);
  }

  private List<BatchResponse> getBatchResponse(final NodeList nl) {
    List<BatchResponse> batchResp = new ArrayList<BatchResponse>();
    for (int i = 0; i < nl.getLength(); i++) {
      final Element e = (Element) nl.item(i);
      batchResp.add(new BatchResponse(e));
    }

    return batchResp;

  }

  private String getResponseString(HttpResponse resp) {

    final HttpEntity entity = resp.getEntity();

    try {
      return EntityUtils.toString(entity);
    } catch (final ParseException e) {
      e.printStackTrace();
      return "";
    } catch (final IOException e) {
      e.printStackTrace();
      return "";
    }

  }

  private Document getDom(String xml) {
    Document doc = null;
    final DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    try {

      final DocumentBuilder db = dbf.newDocumentBuilder();

      final InputSource is = new InputSource();
      is.setCharacterStream(new StringReader(xml));
      doc = db.parse(is);

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
    return doc;
  }

}
