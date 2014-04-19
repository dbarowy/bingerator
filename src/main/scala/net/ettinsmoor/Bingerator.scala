package net.ettinsmoor

import java.net._
import java.io._
import scala.xml._
import scala.collection.immutable.Seq

class Bingerator(key: String, debug: Boolean) {
  val _key_b64 = KeyFormat(key)
  val _results_size = 50    // maximum allowable # results per invocation

  // alternate (simple) constructor (no debugging)
  def this(key: String) = this(key, false)

  // User-facing web search function
  def SearchWeb(query: String) : Stream[WebResult] = {
    val url_fn = _web_request_url(query)(_)
    val new_fn = _new_web_result(_)
    val parse_fn = _web_xml_parser(_)
    _search[WebResult](query, 0, url_fn, new_fn, parse_fn)
  }

  // User-facing image search function
  def SearchImages(query: String) : Stream[ImageResult] = {
    val url_fn = _image_request_url(query)(_)
    val new_fn = _new_image_result(_)
    val parse_fn = _image_xml_parser(_)
    _search[ImageResult](query, 0, url_fn, new_fn, parse_fn)
  }

  // PRIVATE METHODS

  // Top-level generic private method for searching;
  // This method lazily builds the SearchResult stream as needed.
  private def _search[T <: SearchResult](query: String,
                                         offset: Int,
                                         url_generator: Int => URL,
                                         type_instantiator: Node => T,
                                         parser: Node => NodeSeq) : Stream[T] = {
    val result = _execute_search[T](query, offset, url_generator, type_instantiator, parser)
    // this function is called when the user requests more items in the sequence
    // than we have already fetched;
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

  // This non-lazy generic method returns a sequence of SearchResults. It
  // should only be called by _search, which decides when calling is appropriate.
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

  // Method for creating new instances of WebResult.
  private def _new_web_result(xml: Node) = new WebResult(xml)

  // Method for creating new instance of ImageResult.
  private def _new_image_result(xml: Node) = new ImageResult(xml)

  // Method for generating URL strings for web searches.
  private def _web_request_url(query: String)(offset: Int) : URL =
    new URL("https://api.datamarket.azure.com/Bing/Search/Web?Query=%27" +
            URLEncoder.encode(query, "UTF-8") +
            "%27&$top=" + _results_size +
            "&$skip=" + offset + "&$format=ATOM")

  // Method for generating URL strings for image searches.
  private def _image_request_url(query: String)(offset: Int) : URL =
    new URL("https://api.datamarket.azure.com/Bing/Search/v1/Composite?Sources=%27image%27&Query=%27" +
      URLEncoder.encode(query, "UTF-8") +
      "%27&$top=" + _results_size +
      "&$skip=" + offset + "&$format=ATOM")

  // Method for generating a sequence of web result XML
  // nodes from an XML web result tree
  private def _web_xml_parser(xml: Node) : NodeSeq =
    xml \\ "entry"

  // Method for generating a sequence of image result XML
  // nodes from an XML image result tree
  private def _image_xml_parser(xml: Node) : NodeSeq =
    xml \\ "feed" \ "entry" \ "link" \ "inline" \ "feed" \ "entry"
}