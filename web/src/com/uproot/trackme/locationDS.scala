package com.uproot.trackme;

import Helper._

object implicitObject {
  implicit class XMLHelper(n: xml.Node) {
    def attr(name: String) = n.attribute(name).get(0).text
    def attrDouble(name: String) = n.attr(name).toDouble
    def attrOptDouble(name: String) = n.attribute(name).map(_(0).text.toDouble)
    def attrInt(name: String) = n.attr(name).toInt
    def attrLong(name: String) = n.attr(name).toLong
  }
}

import implicitObject._

object Constants {
  val ACCURACY_LIMIT = 3000
  val PI = math.Pi
  val MINUS_PI = -PI
  val PIby2 = PI / 2
  val MINUS_PIby2 = -PIby2
  val MIN_ALTITUDE = -1000
  val MAX_ALTITUDE = 20000
}

case class Location(latLongAlt: LatLongAlt, accuracy: Long, timeStamp: Long) {
  def this(locDetails: scala.xml.Node) = this(LatLongAlt(locDetails.attrDouble(XML_ATTRIBUTE_LATITUDE),
    locDetails.attrDouble(XML_ATTRIBUTE_LONGITUDE), locDetails.attrOptDouble(XML_ATTRIBUTE_ALTITUDE)),
    locDetails.attrLong(XML_ATTRIBUTE_ACCURACY), locDetails.attrLong(XML_ATTRIBUTE_TIME_STAMP))

  def isValid(maxTime: Long) = {
    latLongAlt.isValid && accuracy < Constants.ACCURACY_LIMIT && timeStamp < maxTime
  }

  def mkJSON = "{\"lat\":" + latLongAlt.latitude + ", \"long\":" + latLongAlt.longitude + ", \"ts\":" + timeStamp +
    ", \"acc\":" + accuracy + "}"
}

case class Upload(uploadid: Int, userid: String, passkey: String, batches: List[Batch]) {
  def this(upload: scala.xml.Elem) = this(upload.attrInt(XML_ATTRIBUTE_UPLOAD_ID), upload.attr(XML_ATTRIBUTE_USER_ID),
    upload.attr(XML_ATTRIBUTE_PASS_KEY), (upload \ XML_TAG_BATCH).toList.map(new Batch(_)));
}

case class Batch(sid: String, bid: Int, locations: List[Location]) {
  def this(node: scala.xml.Node) = this(node.attr(XML_ATTRIBUTE_SESSION_ID), node.attrInt(XML_ATTRIBUTE_BATCH_ID),
    (node \ XML_TAG_LOCATION).toList.map(new Location(_)))
}

case class LatLongAlt(latitude: Double, longitude: Double, altitudeOpt: Option[Double] = None) {
  def validateAlt = altitudeOpt.map{ alt => alt >= Constants.MIN_ALTITUDE && alt <= Constants.MAX_ALTITUDE }.getOrElse(true)

  def isValid = {
    latitude >= Constants.MINUS_PIby2 && latitude <= Constants.PIby2 &&
      longitude >= Constants.MINUS_PI && longitude <= Constants.PI && validateAlt
  }

  def mkJSON = "{\"lat\":" + latitude + ", \"long\":" + longitude + "}"
}
