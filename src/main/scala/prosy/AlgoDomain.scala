package prosy 

import Transducer._
import TestSet._
import Util._
import Notations._

/** Algorithm 2 of the paper "Proactive Synthesis"
    Asks for the output of every element of the generated test set.
    Asks O(n^3) questions, where n is the size of the grammar.
 */
object AlgoDomain {


  def learning[T](d: Domain[T]): Option[STW[Char]] = {
    val g = d.getGrammar
    val ling = g.getLinear
    val tg = ling.generateTestSet
    
    val tts = tg.map(evalRules).map(d.r.naiveParsing)
    
    
    val sample = Sample(tts.toList.sortBy(_.size).map { t => 
      println("What should be the output the following tree?")
      println(t)
      (t,scala.io.StdIn.readLine())
    }.toMap)
    
    AlgoSample.learning(d.r, sample)
    
  }

}
