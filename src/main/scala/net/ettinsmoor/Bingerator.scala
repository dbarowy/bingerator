package net.ettinsmoor

import java.net._
import java.io._
import scala.xml._
import javax.xml.bind.DatatypeConverter
import scala.collection.immutable.Seq

class Bingerator(key: String, debug: Boolean) {
  val _key_b64 = KeyFormat(key)
  val _results_size = 50    // maximum allowable # results per invocation

  def this(key: String) = this(key, false)

  def SearchWeb(query: String) : Stream[WebResult] = {
    _search_web(query, 0)
  }

  def _search_web(query: String, offset: Int) : Stream[WebResult] = {
    val result = _execute_search(query, offset)
    // if we get fewer than the expected number of results, do not recurse
    if (result.length < _results_size) {
      result.toStream
    } else {
      _execute_search(query, offset).toStream #::: _search_web(query, offset + _results_size)
    }
  }

  private def _execute_search(query: String, offset: Int) : Seq[WebResult] = {
    if (debug) println("[debug]: Query: \"" + query + "\". Getting results " + offset + " to " + (offset + _results_size) + ".")

    // init accumulator
    val buf = new StringBuilder()

    // issue query and get handle to results
    val conn: URLConnection = _request_url(query, offset).openConnection()
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

    // parse and return result objects
    val output: Seq[WebResult] = res.map ( resxml => new WebResult(resxml))

    if (debug) println("[debug]: Query: \"" + query + "\". Got " + output.size + " results.")

    output
  }

  private def _request_url(query: String, offset: Int) : URL =
    new URL("https://api.datamarket.azure.com/Bing/Search/Web?Query=%27" + URLEncoder.encode(query, "UTF-8") + "%27&$top=" + _results_size + "&$skip=" + offset + "&$format=ATOM")
}

case class WebResult(result_xml: Node) {
  val id = (result_xml \ "content" \ "properties" \ "ID").text
  val title = (result_xml \ "content" \ "properties" \ "Title").text
  val description = (result_xml \ "content" \ "properties" \ "Description").text
  val display_url = (result_xml \ "content" \ "properties" \ "DisplayUrl").text
  val url = (result_xml \ "content" \ "properties" \ "Url").text
}

object KeyFormat {
  def apply(key: String) : String = {
    val _key_bingformat = key + ":" + key
    DatatypeConverter.printBase64Binary(_key_bingformat.getBytes("UTF-8"))
  }
}