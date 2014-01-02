import java.io.FileInputStream
import java.util.Properties
import org.scalatest.FlatSpec

class aOpenKeyFileSpec extends FlatSpec {
  try {
    val prop = new Properties()
    prop.load(new FileInputStream("BingAccount.properties"))
  } catch {
    case e: Exception => fail("Unable to open properties file because of: " + e.getMessage())
  }
}
