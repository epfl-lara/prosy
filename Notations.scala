package transducer

import Transducer._
import TestSet._
import Util._

import java.io.FileWriter
import scala.sys.process._
import scala.language.postfixOps
import scala.collection.mutable.ListBuffer

object Notations {
  val StringA = "ZZZStringAZZZ"
  val StringB = "ZZZStringBZZZ"
  val Int0 = "ZZZInt0ZZZ"
  val Int1 = "ZZZInt1ZZZ"
  val BooleanTrue = "ZZZBooleanTrueZZZ"
  val BooleanFalse = "ZZZBooleanFalseZZZ"
  val StringAvalue = "foo"
  val StringBvalue = "bar"
  val Int0value = "42"
  val Int1value = "19"
  val BooleanTruevalue = "true"
  val BooleanFalsevalue = "false"
  
  object PrimitiveTypeVar {
    def unapply(s: String): Option[String] = {
      s match {
        case StringA => Some(StringAvalue)
        case StringB => Some(StringBvalue)
        case Int0 => Some(Int0value)
        case Int1 => Some(Int1value)
        case BooleanTrue => Some(BooleanTruevalue)
        case BooleanFalse => Some(BooleanFalsevalue)
      }
    }
  }

  case class Morphism[X](m: Map[X, String]) {
    
  }
  
  def getTransducer(mu: Morphism[String], r: RankedAlphabet) = {
    STW[Char](
      r.arity.map { case (f,k) => (f, (0 to k).toList.map(i => mu.m(f + i).toList )) }
    )
  }

  case class Tree(root: String, children: List[Tree]) {
    val size: Int = 1 + children.map(_.size).sum
    
    override def toString(): String = {
      root match {
      case StringA => "\""+StringAvalue+"\""
      case StringB => "\""+StringBvalue+"\""
      case Int0 => Int0value
      case Int1 => Int1value
      case BooleanTrue => BooleanTruevalue
      case BooleanFalse => BooleanFalsevalue
      case _ => 
        if (children.isEmpty) root
        else root + children.map(_.toString).mkString("(",",",")")
      }
    }
  }


  case class RankedAlphabet(arity: Map[String,Int]) {
  
    def apply(s: String) = {
      if (arity.contains(s)) arity(s)
      else throw new Exception("The ranked alphabet does not contain symbol: " + s)
    }
    
    val symbols = arity.map(_._1)
    
    val default = STW.default(this)
    
    
    
    val IdNumber  = """([a-zA-Z0-9%_]+)(\d+)""".r
    
    def naiveParsing(l: List[Terminal]) = {
      def ignored = Tree("ignore", List())
    
      def loop(rest: List[Terminal]): (Tree, List[Terminal]) = {
        rest match {
          case Nil => throw new Exception("No tree to parse")
          case Terminal(IdNumber(id, "0")) :: ys =>
            val children = 
              (1 to arity(id)).scanLeft ( (ignored,ys) ) {
                case ((_, tail), i) =>
                  val (child, head :: tl) = loop(tail)
                  assert(head == Terminal(id+i))
                  (child, tl)
              }
            (Tree(id,children.map(_._1).toList.tail), children.last._2)
        }
      }
      
      
      val (res, Nil) = loop(l)
      res
    }
  }
  
  object RankedAlphabet {
    
  }
  

  case class Sample(m: Map[Tree, String]) {
    override def toString = m.map(_.toString).toList.sorted.sortBy(_.size).mkString("\n")
    
    def apply(t: Tree): String = m(t)
    
    def add(t: Tree, s: String) = Sample(m.updated(t, s))
  }
  
  
  case class Concatenation[X](l: List[Either[X, String]]) {
    override def toString = {
      l.map {
        case Left(x) => "X" + x
        case Right(s) => s
      }.mkString(" ")
    }
    
    def collectVariables: Set[X] = l.flatMap {
      case Left(x) => Some(x)
      case Right(_) => None 
    }.toSet
    
    def toSMT2 = {
      val inside = l.map {
        case Left(x) => x
        case Right(s) => '"' + escape(s) + '"'
      }.mkString(" ")
      
      val n = l.size
      
      if (l.size == 0) throw new Exception("Empty concatenations are not allowed")
      else if (l.size == 1) inside
      else "(str.++ " + inside + ")"
    }
    
    
  }
  
  case class Equation[X](lhs: Concatenation[X], rhs: Concatenation[X]) {
  
    override def toString = {
      lhs + " = " + rhs
    }
    
    def toSMT2 = {
      "(assert (= " + lhs.toSMT2 + " " + rhs.toSMT2 + "))"
    }
    
    def collectVariables: Set[X] = lhs.collectVariables ++ rhs.collectVariables
    
  }
  
  case class Formula[X](l: List[Equation[X]]) {
    val Unsat = """unsat""".r.unanchored
    val Sat   = """sat""".r.unanchored
    val Assignment = """\(define-fun (\w+) \(\) String\s+"([^"]*)"\)""".r //"
  
    override def toString = {
      l.mkString(" and\n")
    }
    
    def collectVariables: Set[X] = l.flatMap(_.collectVariables).toSet
  
    def solve(filename: String): Option[Morphism[String]]  = {
      
      val fw = new FileWriter(filename)
      
      fw.write("(set-logic QF_S)\n")
      
      for (x <- collectVariables) {
        fw.write("(declare-fun " + x + " () String)\n")
      }
      
      for (e <- l) {
        fw.write(e.toSMT2 + "\n")
      }
      
      fw.write("(check-sat)\n")
      fw.write("(get-model)\n")

      fw.close
      
      
      var output = ""
      val pl = ProcessLogger(s => { output += s })
      try {
        val code = ("z3 " + filename) ! pl
        
        if (code == 0)
          Some(Morphism(Assignment.findAllIn(output).map {
            case Assignment(x,v) => x -> v
          }.toMap))
        else {
          println("code: " + code)
          None
        }
      } catch {
        case e: java.io.IOException => // Fallback on our solver
          val s = new StringSolver[X]
          val problem = for (e@Equation(lhs, rhs) <- l) yield {
            lhs match {
              case Concatenation(Nil) => (rhs.l.map(_.swap), "")
              case Concatenation(List(Right(s))) => (rhs.l.map(_.swap), s)
              case _ =>
                rhs match {
                  case Concatenation(Nil) => (lhs.l.map(_.swap), "")
                  case Concatenation(List(Right(s))) => (lhs.l.map(_.swap), s)
                  case _ => throw new Exception("Was not able to put equation " + e + " into internal solver.")
                }
            }
          }
          val res = s.solve(problem)
          res.headOption.map(m => Morphism(m.map{ case (k, v) => (k.toString, v) }))
      }
    }
  
  }

}
