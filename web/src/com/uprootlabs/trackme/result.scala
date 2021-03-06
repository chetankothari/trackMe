package com.uprootlabs.trackme;

sealed abstract class Result()

case class Redirect(url: String) extends Result

class Content(val contentType: String, val content: String, val responseCode: Int) extends Result

case class XmlContent(xContent: xml.Node, respCode: Int = 200) extends Content("text/xml", xContent.toString, respCode)

case class HtmlContent(xContent: xml.Node, respCode: Int = 200) extends Content("text/html", xContent.toString, respCode)

case class JsonContent(jContent: String, respCode: Int = 200) extends Content("application/json", jContent, respCode)

case class ResponseStatus(success: Boolean, message: String) {
  def mkJson = "{\"responseStatus\":{\"success\":\"" + success + "\", \"message\":\"" + message + "\"}}"

  def mkXML = (<responseStatus success={success.toString} message={message} />)
}