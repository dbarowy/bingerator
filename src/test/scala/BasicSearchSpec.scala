import java.io.FileInputStream
import java.util.Properties
import org.scalatest.FlatSpec
import net.ettinsmoor._

class BasicSearchSpec extends FlatSpec {
  val prop = new Properties()
  prop.load(new FileInputStream("BingAccount.properties"))
  val key = prop.getProperty("key")

  "A Bingerator search request" should "return something" in {
    val b = new Bingerator(key, true)
    assert(b.SearchWeb("cowboy").take(150).toList.size == 150)
  }
}