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

  "A Bingerator search request" should "return something." in {
    val b = new Bingerator(key, true)
    assert(b.SearchWeb("cowboy").take(150).toList.size == 150)
  }

  "A Bingerator search request" should "return no more than the maximum number of results." in {
    // A very unlikely match (returns 3 results at the time of writing)
    val bronner = "\"Balanced Mineral Bouillon, Balanced Mineral Seasoning, Barley Malt Sweetener, Mineralized Corn Sesame Chips, Supermild Peppermint Oil Soap, Sal Suds, Ethanol, Esperanto\""
    val b = new Bingerator(key, true)
    assertResult(3) {
      b.SearchWeb(bronner).take(2500).toList.size
    }
  }

  "Bingerator" should "work in a for loop" in {
    val b = new Bingerator(key, true)
    for (result <- b.SearchWeb("cowboy").take(5)) {
      println("Result is: " + result.title)
    }
  }
}
