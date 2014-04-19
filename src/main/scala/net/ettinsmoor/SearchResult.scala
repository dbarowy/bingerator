package net.ettinsmoor

import scala.xml.Node

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
  val content_type = ContentType.get((result_xml \ "content" \ "properties" \ "ContentType").text)
  val file_size = (result_xml \ "content" \ "properties" \ "FileSize").text.toLong
  val height = (result_xml \ "content" \ "properties" \ "Height").text.toInt
  val width = (result_xml \ "content" \ "properties" \ "Width").text.toInt
}