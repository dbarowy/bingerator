import java.io.FileInputStream
import java.util.Properties
import org.scalatest.FlatSpec
import net.ettinsmoor._

class TakeMoreThanAvailableSpec extends FlatSpec {
  val prop = new Properties()
  prop.load(new FileInputStream("BingAccount.properties"))
  val key = prop.getProperty("key")

  // A very unlikely match (returns 3 results at the time of writing)
  val bronner = "\"Balanced Mineral Bouillon, Balanced Mineral Seasoning, Barley Malt Sweetener, Mineralized Corn Sesame Chips, Supermild Peppermint Oil Soap, Sal Suds, Ethanol, Esperanto\""
  val b = new Bingerator(key, true)
  assert(b.SearchWeb(bronner).take(2500).toList.size < 2500)
}
