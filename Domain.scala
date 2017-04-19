package transducer

import Notations._
import TestSet._
import Util._

case class Domain[T](initialStates: Set[T], states: Set[T], delta: List[(T,String,List[T])], r: RankedAlphabet) {

  def getGrammar: Grammar = {
    val s = NonTerminal("NTS")
    
    Grammar(s, 
      initialStates.map { q => Rule(s, List(NonTerminal(q.toString))) } ++ 
      delta.map {
      case (q,f,l) =>
        Rule(
          NonTerminal(q.toString),
          interleave(
            (0 to r(f)).toList.map(i => Terminal(f + i)),
            l.map(q2 => NonTerminal(q2.toString))
          )
        )
    }.toSet)
  }

    
  override def toString = {
    "==================================\n" + 
    "States\n" ++ 
    states.toList.map(_.toString).sorted.sortBy(_.size).mkString("\n") ++
    "\n\n--\n" ++ 
    "Transitions\n" ++ 
    delta.map { case (q,f,l) => 
      q + "," + f + " -> " + l.mkString(" . ")
    }.sorted.sortBy(_.size).mkString("\n") +
    "\n==================================\n\n"
  }
}

object Domain {


  def fullDomain(r: RankedAlphabet) = {
    Domain(
      Set(0), 
      Set(0), 
      r.arity.toList.map { case (f,k) => ((0,f,(1 to k).toList.map(i => 0))) },
      r
    )
  }
}