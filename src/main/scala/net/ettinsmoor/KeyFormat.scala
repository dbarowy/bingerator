package net.ettinsmoor

import javax.xml.bind.DatatypeConverter

object KeyFormat {
  def apply(key: String) : String = {
    val _key_bingformat = key + ":" + key
    DatatypeConverter.printBase64Binary(_key_bingformat.getBytes("UTF-8"))
  }
}