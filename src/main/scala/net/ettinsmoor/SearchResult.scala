package net.ettinsmoor

import scala.xml.Node
import java.awt.image.BufferedImage

abstract class SearchResult

case class WebResult(result_xml: Node) extends SearchResult {
  val id = (result_xml \ "content" \ "properties" \ "ID").text
  val title = (result_xml \ "content" \ "properties" \ "Title").text
  val description = (result_xml \ "content" \ "properties" \ "Description").text
  val display_url = (result_xml \ "content" \ "properties" \ "DisplayUrl").text
  val url = (result_xml \ "content" \ "properties" \ "Url").text
}

case class ImageResult(result_xml: Node) extends SearchResult {
  import java.net.URL
  import javax.imageio._
  import java.io._

  val id = (result_xml \ "content" \ "properties" \ "ID").text
  val title = (result_xml \ "content" \ "properties" \ "Title").text
  val media_url = (result_xml \ "content" \ "properties" \ "MediaUrl").text
  val source_url = (result_xml \ "content" \ "properties" \ "SourceUrl").text
  val display_url = (result_xml \ "content" \ "properties" \ "DisplayUrl").text
  val content_type = ContentType.get((result_xml \ "content" \ "properties" \ "ContentType").text)
  val file_size = (result_xml \ "content" \ "properties" \ "FileSize").text.toLong
  val height = (result_xml \ "content" \ "properties" \ "Height").text.toInt
  val width = (result_xml \ "content" \ "properties" \ "Width").text.toInt

  private lazy val _imagedata = ImageIO.read(new URL(media_url))

  def getImage : BufferedImage = {
    _imagedata
  }

  // Given a basename, this method downloads (as needed)
  // and saves the image data in an appropriately-named file.
  def saveImageFile(basename: String) : File = {
    content_type match {
      case ImageGIF =>
        val outputfile = new File(basename + ".gif")
        ImageIO.write(_imagedata, "gif", outputfile)
        outputfile
      case ImageJPEG =>
        val outputfile = new File(basename + ".jpg")
        ImageIO.write(_imagedata, "jpg", outputfile)
        outputfile
      case ImagePNG =>
        val outputfile = new File(basename + ".png")
        ImageIO.write(_imagedata, "png", outputfile)
        outputfile
    }
  }
}