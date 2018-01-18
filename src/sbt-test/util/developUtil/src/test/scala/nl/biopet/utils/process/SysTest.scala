package nl.biopet.utils.process

import nl.biopet.test.BiopetTest
import org.testng.annotations.Test

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Await
import scala.concurrent.duration.Duration

class SysTest extends BiopetTest {
  @Test
  def testCmd(): Unit = {
    val process = Sys.exec("echo bla")
    process._1 shouldBe 0
    process._2 shouldBe "bla\n"
    process._3 shouldBe ""
  }

  @Test
  def testCmdSeq(): Unit = {
    val process = Sys.exec(Seq("echo", "bla"))
    process._1 shouldBe 0
    process._2 shouldBe "bla\n"
    process._3 shouldBe ""
  }

  @Test
  def testCmdAsync(): Unit = {
    val process = Sys.execAsync("echo bla")
    val result = Await.result(process.get, Duration.Inf)
    result._1 shouldBe 0
    result._2 shouldBe "bla\n"
    result._3 shouldBe ""
  }

  @Test
  def testCmdSeqAsync(): Unit = {
    val process = Sys.execAsync(Seq("echo", "bla"))
    val result = Await.result(process.get, Duration.Inf)
    result._1 shouldBe 0
    result._2 shouldBe "bla\n"
    result._3 shouldBe ""
  }

  @Test
  def testMultiAsync(): Unit = {
    Sys.maxRunningProcesses = 1
    val process = Sys.execAsync(Seq("echo", "bla"))
    val process2 = Sys.execAsync(Seq("echo", "bla"))
    val process3 = Sys.execAsync(Seq("echo", "bla"))
    List(process, process2, process3).foreach { p =>
      val result = Await.result(p.get, Duration.Inf)
      result._1 shouldBe 0
      result._2 shouldBe "bla\n"
      result._3 shouldBe ""
    }
  }

  @Test
  def testCancel(): Unit = {
    Sys.maxRunningProcesses = 1
    val process = Sys.execAsync(Seq("echo", "bla"))
    process.cancel()
  }


}
