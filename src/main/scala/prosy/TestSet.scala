package prosy 

import Util._
import Notations._

object TestSet {


  sealed abstract class Letter {
    def isNonTerminal: Boolean
  }
  
  case class NonTerminal(s: String) extends Letter {
    override def toString = s
    def isNonTerminal = true
  }
  
  case class Terminal(s: String) extends Letter {
    override def toString = s
    def isNonTerminal = false
    
  }
  
  def isConstant(l: List[Letter]) = {
    l.forall {
      case Terminal(_) => true
      case _ => false
    }
  }
  
  def nonTerminals(l: List[Letter]): List[NonTerminal] = {
    l.flatMap {
      case NonTerminal(t) => Some(NonTerminal(t))
      case _ => None
    }
  }
  
  def west(l: List[Letter]): List[Terminal] = l match {
    case Nil => Nil 
    case Terminal(x) :: xs => Terminal(x) :: west(xs)
    case NonTerminal(x) :: _ => Nil
  }
  
  
  def east(l: List[Letter]): List[Terminal] = l match {
    case Nil => Nil 
    case Terminal(x) :: xs => east(xs)
    case NonTerminal(x) :: ys =>
      if (isConstant(ys))
        ys.flatMap {
          case Terminal(t) => Some(Terminal(t))
          case _ => None
        }
      else 
        throw new Exception("There are too many non-terminals in this rule: " + l)
  }
  
//   def mergeTerminals(l: List[Letter]): List[Letter] = l match {
//     case Nil => Nil
//     case Terminal(s1) :: Terminal(s2) :: xs => mergeTerminals(Terminal(s1 ++ s2) :: xs)
//     case x :: xs => x :: mergeTerminals(xs)
//   }

  case class Rule(left: NonTerminal, right: List[Letter]) {
    
    def collectNonTerminals: Set[NonTerminal] = right.flatMap {
      case NonTerminal(s) => Some(NonTerminal(s))
      case _ => None
    }.toSet + left
    
    def firstNonTerminal: Option[NonTerminal] = right.find { _.isNonTerminal} match {
      case None => None
      case Some(NonTerminal(s)) => Some(NonTerminal(s))
      case _ => throw new Exception("Not possible based on find")
    }
    
    
//     def norm = Rule(left, mergeTerminals(right))
    
    def isLoop = firstNonTerminal match {
      case Some(n2) if (n2 == left) => true
      case _ => false
    }
    
    def isEnding = right match {
      case List(Terminal(_)) => true
      case _ => false
    }
    
    override def toString = {
      left + " -> " + right.mkString(" . ")
    }
  }


  case class Grammar(initial: NonTerminal, rules:  Set[Rule]) {
  
    override def toString = {
      "Initial: " + initial + "\n" + 
      rules.mkString("\n")
    }
    
    val collectNonTerminals = rules.flatMap(_.collectNonTerminals) + initial
    
    // this keeps all rules associated to a NonTerminal
    val allrhs: Map[NonTerminal, Set[Rule]] = rules.groupBy { _.left }
    
    def generateTestSet: Set[List[Rule]] = {
      for { 
        rs <- sublists(rules, 3)
        res <- makePath(rs, initial)
      } yield 
        res
    }
    
    
    val optimals: Map[(NonTerminal, Option[NonTerminal]), List[Rule]] = {
      
      var result = Map[(NonTerminal, Option[NonTerminal]), List[Rule]]()
      
      
      for (n <- collectNonTerminals)
        result += (n, Some(n)) -> List()
      
      
      
      def loop(): Unit = {
        var temp = Map[(NonTerminal, Option[NonTerminal]), List[Rule]]()
      
        for {
          a <- collectNonTerminals;
          b <- collectNonTerminals.map (Some(_): Option[NonTerminal]) + None
        }
        {
          if (!result.contains((a, b))) {
//             println("Exploring: " + (a,b))
            allrhs(a).toList.sortBy(_.toString).find { 
              case r@Rule(left, rhs) => 
                r.firstNonTerminal match {
                  case None => b == None
                  case Some(c) => result.contains((c,b))
                }
            } match {
              case None => ()
              case Some(r) => 
                r.firstNonTerminal match {
                  case None => temp += (a,b) -> List(r)
                  case Some(c) => temp += (a,b) -> (r :: result((c,b)))
                }
            }
          }
        }
        
        if (temp.isEmpty) ()
        else {
          result = result ++ temp
          loop()
        }
        
      }
      
      loop()

      result
    }
    
//     println("--------------")
//     println(optimals.mkString("\n"))
//     println("--------------")
    
    
    
    def optimal(a: NonTerminal, b: Option[NonTerminal]) = optimals.get((a,b))
    
    val produced: Map[NonTerminal, List[Terminal]] = {
      
      def loop(result: Map[NonTerminal, List[Terminal]]): Map[NonTerminal, List[Terminal]] = {
//         val notproduced = collectNonTerminals -- result.keySet
        rules.toList.sortBy { case Rule(n,rhs) => rhs.size }.find {
          case Rule(n, rhs) => 
            !result.contains(n) && nonTerminals(rhs).forall(result.contains(_))
        } match {
          case None => result
          case Some(Rule(n,rhs)) =>
            loop(result + (n -> rhs.flatMap {
              case Terminal(t) => List(Terminal(t))
              case NonTerminal(t) => result(NonTerminal(t))
            }))
        }
      }
      
      loop(Map())
    }
    
    
    def makePath(l: List[Rule], first: NonTerminal): Option[List[Rule]] = {

//       println("Making path for: " + l)
//       println("From: " + first)
    
      val v = 
      l match {
        case Nil =>
//           println("===")
//           println(optimals)
//           println(first)
//           println(optimal(first,None))
//           println("===")
          optimal(first, None)
        case (r@Rule(left,rhs)) :: rs =>
          r.firstNonTerminal match {
            case None =>
              optimal(first,Some(left)).map { x => 
                x ::: List(r)
              }
            case Some(right) =>
              optimal(first,Some(left)).flatMap { x => 
                makePath(rs, right).map(q => x ::: (r :: q))
              }
          }
            
      }
      

//       println("Making path for: " + l)
//       println("From: " + first)
//       println("Result: " + v)
      
      v
    }
    
    
    
    def expandRule(r: Rule) = {
//       println(produced)  
//       println("rule: " + r)
      r match {
      case Rule(n, rhs) =>
        val rhsi = rhs.zipWithIndex
        if (isConstant(rhs)) List(r)
        else
          rhsi.flatMap {
            case (Terminal(_), _) => None
            case (NonTerminal(a), i) =>
              Some(Rule(n, rhsi.flatMap {
                case (Terminal(t), _) => List(Terminal(t))
                case (NonTerminal(a), j) if (i == j) => List(NonTerminal(a))
                case (NonTerminal(a), j) =>
                  produced(NonTerminal(a))
              }))
          }
      }
    }
    
    
    
    def getLinear = {
      Grammar(initial, rules.flatMap(expandRule))
    }
    
  }
  
  def evalRules(l: List[Rule]) = {
//     println("evaluating: " + l)
    def loop(s: List[Terminal], l: List[Rule]): List[Terminal] = l match {
      case Nil => s
      case Rule(n, rhs) :: rs =>
        loop(west(rhs) ::: s ::: east(rhs), rs)
    }
    loop(Nil, l.reverse)
  }
  

}
