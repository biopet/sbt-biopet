package nl.biopet.utils.conversions

import java.io.File
import java.util

import nl.biopet.test.BiopetTest
import org.testng.annotations.Test
import play.api.libs.json.{JsArray, JsBoolean, JsNull, JsNumber, JsObject, JsString}

import scala.io.Source

class ConversionsTest extends BiopetTest {
  @Test
  def testAnyToList(): Unit = {
    val input1: Any = List(1,2)
    anyToList(input1) shouldBe input1
    anyToList(Some(input1)) shouldBe input1

    val input2: Any = List("1",2)
    anyToList(input2) shouldBe input2

    val input3: Any = "test"
    anyToList(input3) shouldBe input3 :: Nil

    val input4: util.ArrayList[String] = new util.ArrayList()
    input4.add("1")
    input4.add("2")
    anyToList(input4) shouldBe List("1", "2")

    anyToList(None) shouldBe Nil
    anyToList(null) shouldBe Nil
  }

  @Test
  def testAnyToStringList(): Unit = {
    anyToStringList(List("1","2")) shouldBe List("1", "2")
    anyToStringList(List(1,2)) shouldBe List("1", "2")
  }

  @Test
  def testAnyToDoubleList(): Unit = {
    anyToDoubleList(List("1","2")) shouldBe List(1.0, 2.0)
    anyToDoubleList(List(1,2)) shouldBe List(1.0, 2.0)
    anyToDoubleList(List(1.0, 2.0)) shouldBe List(1.0, 2.0)
  }

  @Test
  def testMergeMaps(): Unit = {
    mergeMaps(Map(), Map()) shouldBe Map()
    mergeMaps(Map("key1" -> 1), Map("key2" -> 2)) shouldBe Map("key1" -> 1, "key2" -> 2)

    mergeMaps(Map("key1" -> 1), Map("key1" -> 2)) shouldBe Map("key1" -> 1)

    intercept[IllegalArgumentException] {
      mergeMaps(Map("key1" -> 1), Map("key1" -> 2), (_, _, _) => throw new IllegalArgumentException) shouldBe Map("key1" -> 1)
    }

    mergeMaps(Map("map" -> Map("key1" -> 1)), Map("map" -> Map("key2" -> 2))) shouldBe Map("map" -> Map("key1" -> 1, "key2" -> 2))
    mergeMaps(Map("map" -> Map("key1" -> 1)), Map("map" -> "something else")) shouldBe Map("map" -> Map("key1" -> 1))
  }

  @Test
  def testAnyToMap(): Unit = {
    any2map(null) shouldBe null
    any2map(Map("bla" -> 3)) shouldBe Map("bla" -> 3)
    any2map(new java.util.LinkedHashMap) shouldBe Map()

    intercept[IllegalStateException] {
      any2map("not_a_map")
    }.getMessage shouldBe "Value 'not_a_map' is not an Map"
  }

  @Test
  def testAnyToJson(): Unit = {
    anyToJson(null) shouldBe JsNull
    anyToJson(None) shouldBe JsNull
    anyToJson(4) shouldBe JsNumber(4)
    anyToJson(4.0) shouldBe JsNumber(4.0)
    anyToJson(4.0f) shouldBe JsNumber(4.0)
    anyToJson(Some(4)) shouldBe JsNumber(4)
    anyToJson(Some(4.toByte)) shouldBe JsNumber(4)
    anyToJson(Some(4.toShort)) shouldBe JsNumber(4)
    anyToJson(Some(4L)) shouldBe JsNumber(4L)
    anyToJson(true) shouldBe JsBoolean(true)
    anyToJson("bla") shouldBe JsString("bla")
    anyToJson(List("bla", 4)) shouldBe JsArray(JsString("bla") :: JsNumber(4) :: Nil)
    anyToJson(Array("bla", 4)) shouldBe JsArray(JsString("bla") :: JsNumber(4) :: Nil)
    anyToJson(Map("key" -> "value")) shouldBe JsObject(Seq("key" -> JsString("value")))
    anyToJson(Map("key" -> Map("key2" -> "value"))) shouldBe JsObject(Seq("key" -> JsObject(Seq("key2" -> JsString("value")))))
  }

  @Test
  def testMapToYamlFile(): Unit = {
    val outputFile = File.createTempFile("test.", ".yml")
    outputFile.deleteOnExit()
    mapToYamlFile(Map("key" -> "value", "key2" -> Map("bla" -> 4)), outputFile)

    Source.fromFile(outputFile).getLines().toList shouldBe List("key: value", "key2: {bla: 4}", "")
  }

  @Test
  def testScalaListToJavaObjectArrayList(): Unit = {
    scalaListToJavaObjectArrayList(List()) shouldBe new util.ArrayList[Object]()

    case class Bla()
    val result = new util.ArrayList[Object]()
    result.add(Int.box(4))
    result.add(Char.box(4))
    result.add(Byte.box(4))
    result.add(Long.box(4L))
    result.add(Double.box(4.0))
    result.add(Float.box(4.0f))
    result.add(Boolean.box(true))
    result.add("bla")
    result.add(Bla())
    scalaListToJavaObjectArrayList(List(4, 4.toChar, 4.toByte, 4L, 4.0, 4.0f, true, "bla", Bla())) shouldBe result
  }

  @Test
  def testNestedJavaHashMaptoScalaMap(): Unit = {
    val map = new java.util.LinkedHashMap[String, Any]()
    val map2 = new java.util.LinkedHashMap[String, Any]()
    map.put("key", "value")
    map2.put("key", "value")
    map.put("key2", map2)

    nestedJavaHashMaptoScalaMap(map) shouldBe Map("key" -> "value", "key2" -> Map("key" -> "value"))
  }
}
