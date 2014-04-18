package net.ettinsmoor

import java.net._
import java.io._
import scala.xml._
import javax.xml.bind.DatatypeConverter
import scala.collection.immutable.Seq

object SearchType extends Enumeration {
  type SearchType = Value
  val Web, Image = Value
}
import SearchType._

abstract class SearchResult

case class WebResult(result_xml: Node) extends SearchResult {
  val id = (result_xml \ "content" \ "properties" \ "ID").text
  val title = (result_xml \ "content" \ "properties" \ "Title").text
  val description = (result_xml \ "content" \ "properties" \ "Description").text
  val display_url = (result_xml \ "content" \ "properties" \ "DisplayUrl").text
  val url = (result_xml \ "content" \ "properties" \ "Url").text
}

case class ImageResult(result_xml: Node) extends SearchResult {
  val id = (result_xml \ "content" \ "properties" \ "ID").text
  val title = (result_xml \ "content" \ "properties" \ "Title").text
  val media_url = (result_xml \ "content" \ "properties" \ "MediaUrl").text
  val display_url = (result_xml \ "content" \ "properties" \ "DisplayUrl").text
  val content_type = (result_xml \ "content" \ "properties" \ "ContentType").text
  val file_size = (result_xml \ "content" \ "properties" \ "FileSize").text
  val height = (result_xml \ "content" \ "properties" \ "Height").text
  val width = (result_xml \ "content" \ "properties" \ "Width").text
}

class Bingerator(key: String, debug: Boolean) {
  val _key_b64 = KeyFormat(key)
  val _results_size = 50    // maximum allowable # results per invocation

  def this(key: String) = this(key, false)

  def SearchWeb(query: String) : Stream[WebResult] = {
    val urlf = _web_request_url(query)(_)
    val newT = _new_web_result(_)
    _search[WebResult](query, 0, urlf, newT)
  }

  def SearchImages(query: String) : Stream[ImageResult] = {
    val urlf = _image_request_url(query)(_)
    val newT = _new_image_result(_)
    _search[ImageResult](query, 0, urlf, newT)
  }

  private def _search[T <: SearchResult](query: String,
                                         offset: Int,
                                         url_factory: Int => URL,
                                         type_factory: Node => T) : Stream[T] = {
    val result = _execute_search[T](query, offset, url_factory, type_factory)
    // if we get fewer than the expected number of results, do not recurse
    if (result.length < _results_size) {
      result.toStream
    } else {
      result.toStream #::: _search[T](query, offset + _results_size - 1, url_factory, type_factory)
    }
  }

  private def _execute_search[T <: SearchResult](query: String, offset: Int, url_factory: Int => URL, type_factory: Node => T) : Seq[T] = {
    if (debug) println("[debug]: Query: \"" + query +
                       "\". Getting results " + offset +
                       " to " + (offset + _results_size - 1) + ".")

    // init accumulator
    val buf = new StringBuilder()

    // issue query and get handle to results
    val conn: URLConnection = url_factory(offset).openConnection()
    conn.setRequestProperty("Authorization", "Basic " + _key_b64)
    val reader: BufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()))

    // read data into buffer
    var inputLine: String = reader.readLine()
    while (inputLine != null) {
      buf.append(inputLine)
      inputLine = reader.readLine()
    }

    // close handle
    reader.close()

    // debug
    if (debug) println(buf.toString())

    // parse data
    val xmldata = XML.loadString(buf.toString())

    // get results
    val res: NodeSeq = xmldata \\ "entry"

    if (debug) println("[debug]: Query: \"" + query + "\". Got " + res.size + " results.")

    // parse and return result objects
    res.map (resxml => type_factory(resxml))
  }

  private def _new_web_result(xml: Node) = new WebResult(xml)

  private def _new_image_result(xml: Node) = new ImageResult(xml)

  private def _web_request_url(query: String)(offset: Int) : URL =
    new URL("https://api.datamarket.azure.com/Bing/Search/Web?Query=%27" +
            URLEncoder.encode(query, "UTF-8") +
            "%27&$top=" + _results_size +
            "&$skip=" + offset + "&$format=ATOM")

  private def _image_request_url(query: String)(offset: Int) : URL =
    new URL("https://api.datamarket.azure.com/Bing/Search/v1/Composite?Sources=%27image%27&Query=%27" +
      URLEncoder.encode(query, "UTF-8") +
      "%27&$top=" + _results_size +
      "&$skip=" + offset + "&$format=ATOM")
}

object KeyFormat {
  def apply(key: String) : String = {
    val _key_bingformat = key + ":" + key
    DatatypeConverter.printBase64Binary(_key_bingformat.getBytes("UTF-8"))
  }
}