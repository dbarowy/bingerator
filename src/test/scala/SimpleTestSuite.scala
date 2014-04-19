import java.io.FileInputStream
import java.util.Properties
import net.ettinsmoor.{WebResult, Bingerator}
import org.scalatest.{CancelAfterFailure, FlatSpec}

class SimpleTestSuite extends FlatSpec with CancelAfterFailure {
  // variables needed later in the test suite
  val prop = new Properties()
  var key: String = null

  "The file BingAccount.properties" should "exist." in {
    try {
      prop.load(new FileInputStream("BingAccount.properties"))
    } catch {
      case e: Exception => fail("Unable to open properties file because of: " + e.getMessage())
    }
  }

  "The file BingAccount.properties" should "contain a \"key\" field." in {
    key = prop.getProperty("key")
    assert(key != null)
  }

  "A Bingerator web search request" should "return something." in {
    val b = new Bingerator(key, true)
    assert(b.SearchWeb("cowboy").take(150).toList.size == 150)
  }

  "Bingerator" should "work in a for loop" in {
    val b = new Bingerator(key, true)
    for (result <- b.SearchWeb("cowboy").take(5)) {
      println("Result is: " + result.title)
    }
  }

  "A Bingerator image search request" should "return something." in {
    val b = new Bingerator(key, true)
    assert(b.SearchImages("cowboy").take(75).toList.size == 75)
  }

  "Bingerator image searches" should "allow downloading the image to a file" in {
    val b = new Bingerator(key, true)
    val results = b.SearchImages("Mulder and Scully").take(1).toList
    assert(results.size == 1)
    val f = results.head.saveImageFile("XFiles")
    assert(f.exists && f.isFile)
    f.delete()
  }
}
