package net.ettinsmoor

abstract class ContentType
case class ImageJPEG() extends ContentType
case class ImagePNG() extends ContentType
case class ImageGIF() extends ContentType

object ContentType {
  def isGIF(ct_str: String) = ct_str.matches("^image/gif$")
  def isJPEG(ct_str: String) = ct_str.matches("^image/jpe?g$")
  def isPNG(ct_str: String) = ct_str.matches("^image/png$")

  def get(ct_str: String) = ct_str match {
    case gif if isGIF(gif) => ImageGIF
    case jpeg if isJPEG(jpeg) => ImageJPEG
    case png if isPNG(png) => ImagePNG
    case _ => throw new NotImplementedError("Unknown content type.")
  }
}