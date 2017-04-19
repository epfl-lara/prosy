package prosy

import scala.util.Random

import Util._
import Notations._

import dk.brics.automaton._

object Transducer {


  case class STW[Y](constants: Map[String, List[List[Y]]]) {
    
    def apply(t: Tree): List[Y] = {
      def loop(node: Tree): List[Y] = {
        val Tree(root, children) = node
        
        if (constants.contains(root)) {
          val cs: List[List[Y]] = constants(root)
          val ss: List[List[Y]] = children.map(loop)
          interleave(cs,ss).flatten
        }
        else {
          println(this)
          println("Tree:")
          println(t)
          throw new Exception("There is a transition missing." + t.getClass)
        }
      }
      loop(t)
    }
    
    private def removeIfEmpty(repr: String) = if(repr == "\"\"") Nil else List(repr)
    private def quotesIfEmpty(repr: String) = if(repr == "") "\"\"" else repr
    
    override def toString = {
      "def print(t: Any): String = t match {\n" +
      constants.toList.sortBy { _._1}
      .filter{ case (f, l) => 
        f != StringA && f != StringB && f != Int0 && f != Int1 && f != BooleanTrue && f != BooleanFalse
      }
      .map { case (f,l) =>
        val n = l.length - 1
        "  case " + f + (1 to n).map(i => "t" + i).mkString("(",",",")") + 
          " => " + quotesIfEmpty((removeIfEmpty(toStrConst(l(0).mkString))++ 
          (1 to n).toList.flatMap(
            i => List("print(t" + i + ")") ++ (removeIfEmpty(toStrConst(l(i).mkString)))
          )).mkString(" + "))
      }.mkString("\n") + "\n  case e => e.toString\n}"
    }
    
  }

  object STW {
  
    def apply(m: Map[String,Automaton]): Option[STW[Char]] = {
      val solutions = m.map { case (f,a) => (f, a.getShortestExample(true)) }
      if (solutions.exists { case (f,s) => s == null }) {
        println("null")
        None
      } else {
        Some(STW(solutions.mapValues { s => (dieseDecompose(s).map(_.toList)) }))
      }
    }
    
    def default(r: RankedAlphabet) = {
      STW(
        r.arity.map { case (f,k) => (f, (0 to k).toList.map(i => List((f,i)))) }
      )
    }
    
  }
  
}
