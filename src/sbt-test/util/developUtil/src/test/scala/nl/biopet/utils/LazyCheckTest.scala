package nl.biopet.utils

import nl.biopet.test.BiopetTest
import org.testng.annotations.Test

class LazyCheckTest extends BiopetTest {
  @Test
  def test(): Unit = {
    val lazyCheck = new LazyCheck("bla")
    lazyCheck.isSet shouldBe false
    lazyCheck.get shouldBe "bla"
    lazyCheck() shouldBe "bla"
    lazyCheck.isSet shouldBe true
  }
}
