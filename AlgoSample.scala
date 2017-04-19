package transducer

import Notations._
import Transducer._

/** Algorithm 1 of the paper "Proactive Synthesis" */
object AlgoSample {


  def getEquation(r: RankedAlphabet, t: Tree, w: String): Equation[String] = {
    Equation(
      Concatenation(r.default(t).map {
        case (f,i) => Left(f + i)
      }),
      Concatenation(List(Right(w)))
    )
  }

  def formula(r: RankedAlphabet, s: Sample): Formula[String] = {
    val default = STW.default(r)
    
    Formula(s.m.map { case (t,w) => getEquation(r, t,w) }.toList)
  }

  def learning[Y](r: RankedAlphabet, s: Sample): Option[STW[Char]]  = {
  
    val f = formula(r,s)
    val m = f.solve("temp.smt2")
    
//     println(m)
    
    m.map(getTransducer(_,r))
  }


}
