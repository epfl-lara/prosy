package transducer

object Util {

  def escape(s: String) = s.replaceAll("\\\\","\\\\\\\\").replaceAll("\"","\\\"").replaceAll("\n","\\\\n")

  def toStrConst(s: String) = "\"" + escape(s) + "\""

  def sublists[T](s: Set[T], k: Int): Set[List[T]] = {
    if (k == 0) Set(Nil)
    else {
      val r = sublists(s, k-1) 
      r ++ (for (x <- s; t <- r) yield x :: t)
    }
  }
  
  def interleave[T](l1: List[T], l2: List[T]): List[T] = {
    l1 match {
      case Nil => l2
      case x :: xs => x :: interleave(l2,xs)
    }
  }

  def replaceAll(s: String, l: List[(String,String)]): String = l match {
    case Nil => s
    case (a,b) :: ls => replaceAll(s.replaceAll(a,b), ls)
  }

  def `#`(i: Int) = (1000 + i).toChar
  def dieseDecompose(s: String) = {
    def loop(rest: String, i: Int): List[String] = {
      val j = rest.indexOf(`#`(i))
      if (j == -1)
        List(rest)
      else
        rest.substring(0, j) :: loop(rest.substring(j+1, rest.length), i+1)
    }
    loop(s, 0)
  }

    
  def fixpoint[T](f: T => T, limit: Int = -1)(e: T): T = {
    var v1 = e
    var v2 = f(v1)
    var lim = limit
    while(v2 != v1 && lim != 0) {
      v1 = v2
      lim -= 1
      v2 = f(v2)
    }
    v2
  }
}
