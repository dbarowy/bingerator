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
  val source_url = (result_xml \ "content" \ "properties" \ "SourceUrl").text
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
    val url_fn = _web_request_url(query)(_)
    val new_fn = _new_web_result(_)
    val parse_fn = _web_xml_parser(_)
    _search[WebResult](query, 0, url_fn, new_fn, parse_fn)
  }

  def SearchImages(query: String) : Stream[ImageResult] = {
    val url_fn = _image_request_url(query)(_)
    val new_fn = _new_image_result(_)
    val parse_fn = _image_xml_parser(_)
    _search[ImageResult](query, 0, url_fn, new_fn, parse_fn)
  }

  private def _search[T <: SearchResult](query: String,
                                         offset: Int,
                                         url_generator: Int => URL,
                                         type_instantiator: Node => T,
                                         parser: Node => NodeSeq) : Stream[T] = {
    val result = _execute_search[T](query, offset, url_generator, type_instantiator, parser)
    // if we get fewer than the expected number of results, do not recurse
    def more : Stream[T] = {
      if (result.length < _results_size) {
        Stream.empty
      } else {
        _search[T](query, offset + _results_size, url_generator, type_instantiator, parser)
      }
    }
    result.toStream #::: more
  }

  private def _execute_search[T <: SearchResult](query: String,
                                                 offset: Int,
                                                 url_generator: Int => URL,
                                                 type_instantiator: Node => T,
                                                 parser: Node => NodeSeq) : Seq[T] = {
    if (debug) println("[debug]: Query: \"" + query +
                       "\". Getting results " + offset +
                       " to " + (offset + _results_size - 1) + ".")

    // init accumulator
    val buf = new StringBuilder()

    // issue query and get handle to results
    val conn: URLConnection = url_generator(offset).openConnection()
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
    val res = parser(xmldata)

    if (debug) println("[debug]: Query: \"" + query + "\". Got " + res.size + " results.")

    // parse and return result objects
    res.map (resxml => type_instantiator(resxml))
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

  private def _web_xml_parser(xml: Node) = xml \\ "entry"

  private def _image_xml_parser(xml: Node) = xml \\ "feed" \ "entry" \ "link" \ "inline" \ "feed" \ "entry"
}

object KeyFormat {
  def apply(key: String) : String = {
    val _key_bingformat = key + ":" + key
    DatatypeConverter.printBase64Binary(_key_bingformat.getBytes("UTF-8"))
  }
}