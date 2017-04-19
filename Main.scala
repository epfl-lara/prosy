package transducer 

import Notations._
import Transducer._
import AlgoSample._
import AlgoInteraction._
import TestSet._


object Main {

  
  def help() = {
    println("""Usage: java -jar proactive-synthesis.jar [NUMBER_CHOICE] SCALA_FILES
      |NUMBER_CHOICE: the maximal number of propositions (default: 9)
      |SCALA_FILES: a space-separated list of scala files, containing case class definitions""".stripMargin)
  }
  
  def main(args: Array[String]): Unit = {
    val (choices,realArgs) =
      if (args.length == 0) return help()
      else {
        if (args(0).forall(_.isDigit))
          (args(0).toInt, args.tail)
        else
          (9, args)
      }
    
    if (realArgs.isEmpty) return help()
      
    val content = realArgs.map((x: String) => scala.io.Source.fromFile(x).getLines().mkString("\n")).mkString("\n")
    val domain = ParseScala(content)
    
    AlgoInteraction.learning(domain,choices) match {
      case None => println("No transducer can produce the provided outputs")
      case Some(t) => println(t)
    }
  }

}
