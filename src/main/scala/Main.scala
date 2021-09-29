import java.io._
import scala.concurrent._
import scala.util.{Failure, Success}
import ExecutionContext.Implicits.global
import akka.actor._
import akka.actor.ActorSystem
import akka.stream._
import akka.stream.scaladsl._
import akka.{ Done, NotUsed }
import akka.util.ByteString
import java.nio.file.Paths
import java.nio.file.Path
object Main extends App {
  // Futures demo
  //  def validate_row(): Future[String] = Future {
  //    Thread.sleep(1000)
  //    "This string is returned after sometime"
  //  }
  //
  //  val f: Future[String] = validate_row()
  //  f.onComplete {
  //    case Success(result) => println(s"result = $result")
  //    case Failure(e) => e.printStackTrace
  //  }
  //
  //  println("Hello from main scala object")
  val pw = new PrintWriter(new File("hello.txt" ))
  implicit val system = ActorSystem("actor-system")
  implicit val materialzier = ActorMaterializer()
  def isAllDigits(x: String) = x forall Character.isDigit

  def processRowValue(str: String): Future[String] = Future {
    var columnValues = str.split(",")
    var returnValues: Array[String] = Array.empty
    for (cv <- columnValues) {
      if (isAllDigits(cv)) {
        Thread.sleep(10)
        returnValues = returnValues :+ (cv.toFloat + 10).toString().concat("Added 10")
      } else if (cv.toDoubleOption.isDefined) {
        returnValues = returnValues :+ (cv.toFloat + 111).toString().concat("Added 111")
      } else {
        returnValues = returnValues :+ cv.concat("hello world")
      }
    }
    returnValues.mkString(", ")
  }
//  val processRow: Flow[String, String, NotUsed] = Flow[String].map(element => processRowValue(element))
//  val sink = Sink.foreach[String](element => println(element))
  val lineDelimiter: Flow[ByteString, ByteString, NotUsed] =
    Framing.delimiter(
      ByteString("\n"),
      maximumFrameLength = 256,
      allowTruncation = true
    )
  val p1 = Paths.get("hw_25000.csv")
  FileIO
    .fromPath(p1)
    .via(lineDelimiter)
    .map(byteString => byteString.utf8String)
    .mapAsyncUnordered(1000)(processRowValue(_))
    .runWith(Sink.foreach(println))
//    .run()
    .onComplete(_ => system.terminate())
//  val done: Future[Done] = source.runWith( Sink.foreachAsync[String](element => println(element)))
//  done
  //  done.onComplete {
  //    case Success(value) => println(s"stream completed successfully $value")
  //    case Failure(e) => println(s"stream completed with failure: $e")
  //  }
  pw.close()
}