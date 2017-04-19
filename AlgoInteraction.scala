package transducer 

import Transducer._
import TestSet._
import Util._
import Notations._
import Main._

object AlgoInteraction {

  
  
  def lcp(s1: String, s2: String): String = {
    def loop(i: Int): String = {
      if (i < s1.length && i < s2.length && s1(i) == s2(i))
        loop(i+1)
      else 
        s1.take(i)
    }
    loop(0)
  }
  
  def lcs(s1: String, s2: String) = lcp(s1.reverse, s2.reverse).reverse
  import dk.brics.automaton._
  
  /** Creates the automaton representing all solutions to the equation
      X1 + l(0) + X2 + l(1) + ... + l(k) + X(k+1) == s */
  def createAutomaton(s: String, l: List[String]): Automaton = {
    val imax = s.length
    val jmax = l.length
    val array = Array.fill[State](imax + 1, jmax + 1)(new State())

    for{i <- 0 to imax
        j <- 0 to jmax} {
      val state = array(i)(j)
      if (i < imax) {
        state.addTransition(new Transition(s(i), array(i+1)(j)))
      }
      if (j < jmax && i + l(j).length <= imax && l(j) == s.substring(i, i+l(j).length)) {
         state.addTransition(new Transition(`#`(j), array(i+l(j).length)(j+1)))
      }
    }
    array(imax)(jmax).setAccept(true)
    val a = new Automaton()
    a.setInitialState(array(0)(0))
    a.minimize()
    a
  }

  /** Returns the first num different substrings recognized by the automaton, as a stream */
  def obtainRecognitions(a: Automaton, num: Int = 10, excluded: List[String] = Nil): Stream[String] = {
    val b = a.intersection(BasicAutomata.makeStringUnion(excluded:_*).complement)
    
    if(num == 0) Stream.empty else {
      val s = b.getShortestExample(true)
      if(s == null) Stream.empty else {
        s #:: obtainRecognitions(a, num-1, s :: excluded)
      }
    }
  }
  
  def removeLeft(s1: String, s2: String) = {
    assert(s2.startsWith(s1))
    s2.drop(s1.length) 
  }
  
  def removeRight(s1: String, s2: String) = {
    assert(s1.endsWith(s2))
    s1.take(s1.length - s2.length)
  }
  
  def multiLineRead() = {
  
    def loop(acc: String): String = {
      val s = scala.io.StdIn.readLine()
      if (s.endsWith("\\")) loop(acc + s.substring(0,s.length-1) + "\n")
      else acc + s
    }
    
    loop("")
  }

  def learning[T](d: Domain[T], choices: Int): Option[STW[Char]] = {
    println("Proactive Synthesis.")
    
    println("If you ever want to enter a new line, terminate your line by \\ and press Enter.")
    val g = d.getGrammar
    val ling = g.getLinear
    
    val tg = ling.generateTestSet
    
    val tts = tg.map(evalRules).map(d.r.naiveParsing)
    
    val ranked = d.r

    
    var known: Map[Tree,String] =
      Map(Tree(StringA, Nil) -> StringAvalue, Tree(StringB, Nil) -> StringBvalue, 
          Tree(Int0, Nil) -> Int0value, Tree(Int1, Nil) -> Int1value, 
          Tree(BooleanTrue, Nil) -> BooleanTruevalue, Tree(BooleanFalse, Nil) -> BooleanFalsevalue
      )

    var knownAutomata = Map[String, Automaton](
      StringA -> BasicAutomata.makeString(StringAvalue),
      StringB -> BasicAutomata.makeString(StringBvalue), 
      Int0 -> BasicAutomata.makeString(Int0value), Int1 -> BasicAutomata.makeString(Int1value), 
      BooleanTrue -> BasicAutomata.makeString(BooleanTruevalue), BooleanFalse -> BasicAutomata.makeString(BooleanFalsevalue)
    )

    var unknown = tts -- known.keys
    
    var testSetSize = unknown.size
    var automaticallyInferred = 0
    var implicitSuggestions = 0
    var explicitSuggestions = 0
    var questions = 0
    var errors = 0
    
    def sortCriterion(x: Tree): Int = {
      /*( if (x.children.exists(c => known.get(c) == Some(""))) {
        1000
      } else 0) + */
      x.size
    }
    
    while (!unknown.isEmpty) {
      val t = unknown.toList.sortBy(sortCriterion).find { t =>
        t.children.forall(t2 => known.contains(t2))
      }.get
      
      val Tree(f,children) = t
      val knownAutomaton = knownAutomata.getOrElse(f, BasicAutomata.makeAnyString())
      val alreadyStartedToLearnF = knownAutomata contains f

      def gatherKnowledge(v: String, allpossibilitiesautomata: Option[Automaton]): Boolean = {
        val newAutomaton = (knownAutomaton intersection createAutomaton(v, t.children.map(known)))
        newAutomaton.minimize()
        if (newAutomaton.isEmpty()) {
          errors += 1
          println(s"We cannot have the transducer convert $t to $v.\nPlease enter something consistent with what you previously entered" + (allpossibilitiesautomata match {
          case Some(a) => 
            val l = obtainRecognitions(a, 3).toList
            val end = if(l.length == 3) ",..." else ""
            val quotes = if(l.take(2).exists(_.exists(_ == "'"))) {
              if(l.take(2).exists(_.exists(_ == "`"))) {
                "\"" //"
              } else "`"
            } else "'"
            " (e.g. "+l.take(2).map(quotes + _ + quotes).mkString("", ",", end)+")"
          case None => ""}))
          false
        } else {
          knownAutomata += f -> newAutomaton
          known += (t -> v)
          true
        }
      }

      def ask(allpossibilitiesautomata: Option[Automaton]): Boolean = {
        questions += 1
        println("What should be the output the following tree?")
        println(t)
        val childrenFiltered = t.children.map(known).filter(_.nonEmpty)
        if (childrenFiltered.length > 0) {
          println("Something of the form: " + childrenFiltered.mkString("[...]","[...]","[...]"))
          implicitSuggestions += 1
        }
        val v = multiLineRead()
        gatherKnowledge(v, allpossibilitiesautomata)
      }
      
      val success = if (alreadyStartedToLearnF) {
        val childrenReprs : List[String] = children.map(known)
        val allpossibilitiesautomata = (knownAutomaton.clone() /: childrenReprs.zipWithIndex) {
          case (automata, (repr, i)) => automata.subst(`#`(i), repr) 
        }
        val propositions = obtainRecognitions(allpossibilitiesautomata, choices+1)

        if (propositions.isEmpty) { // Should not happen.
          throw new Exception("No transducer is compatible with your outputs.")
        } else if (propositions.tail.isEmpty) {
          known += (t -> propositions.head)
          println("We were able to determine the output for the following tree.")
          println(t)
          println("Output: " + known(t))
          automaticallyInferred += 1
          true
        } else {
          questions += 1
          println("What should be the output for the following input tree?")
          println(t)
          val prArray = propositions.toList.sorted.toArray
          for (i <- 0 until Math.min(choices,prArray.length)) {
            println((i+1) + ") " + prArray(i))
          }
          explicitSuggestions += 1
          
          if (prArray.length <= choices)
            println("Please enter a number between 1 and " + prArray.length + ", or 0 if you really want to enter your answer manually")
          else
            println("Please enter a number between 1 and " + Math.min(choices,prArray.length) + ", or 0 if your answer does not appear in the list")
          
          val line = scala.io.StdIn.readLine() 
          try {
            val v = line.toInt
            
            if (v == 0)
              ask(Some(allpossibilitiesautomata))
            else {
              gatherKnowledge(prArray(v-1), Some(allpossibilitiesautomata))
            }
          } catch {
            case e: java.lang.NumberFormatException =>
              if (prArray.contains(line)) {
                println("We understood your answer as the string \""+line+"\"")
                gatherKnowledge(line, Some(allpossibilitiesautomata))
              } else {
                println("This is not a valid number or answer.")
                errors += 1
                false
              }
          }
        }
      } 
      else {
        val childrenReprs : List[String] = children.map(known)
        
        val allpossibilitiesautomata = (BasicAutomata.makeStringUnion("", "ABCD", "MNKP") /: childrenReprs) {
          case (a, s) => BasicOperations.concatenate(BasicOperations.concatenate(a, BasicAutomata.makeString(s)), BasicAutomata.makeStringUnion("", "foo", "bar"))
        }
        ask(Some(allpossibilitiesautomata))
      }
      
      if (success) {
        println("----")
        unknown -= t
      }
    }
    
    println(s"""Statistics:
$testSetSize elements computed in test set:
  $automaticallyInferred elements automatically inferred
  $questions questions asked.
    ${questions - implicitSuggestions - explicitSuggestions} regular questions
    $implicitSuggestions with an hint of the type [...]foo[...]
    $explicitSuggestions with explicit suggestions${(if(errors == 0) "" else "\n" + errors.toString)}
""")
    
    STW(knownAutomata)
  }

}
