package transducer

object ParseScala {
  import Util._
  val ext = """((?:\s*(?:extends|with)\s+[^ \r\n]+)*)"""
  // Does not support Strings, generics, or abstract classes with members yet.
  val name = """([\w_]+)"""
  val args = """\s*\(([^\)]*)\)\s*"""
  val space = """\s*"""
  val CaseClasses = s"case class $name$args$ext".r //"
  val CaseObjects = s"case object $name$ext".r //"
  val AbstractClasses = s"abstract class $name$ext".r //"
  val Traits = s"trait $name$ext".r //"
  
  val Argument = s"$name$space:$space$name".r
  val Extension = s"$space(?:extends|with)$space([^ \r\n]+)".r
  
  // To every case class or case object, associate a symbol s(A)
  // To every abstract class or trait, associate a state
  // To every case class A(b: B, c: C, d: D), associate the transition   (s(A), A, (B, C, D))
  // To every relation A extends B, associate all the transitions (_, A, ...) to (s(B), A, ...)
  def apply(input: String): Domain[String] = {
    var states = Set[String]()
    var superClasses = Map[String, List[String]]()
    var subClasses = Map[String, List[String]]()
    var caseClasses = Map[String, List[String]]() // Only for case classes
    var arities = Map[String, Int]()
    
    def allsuperClasses(a: String): List[String] = {
      (fixpoint{ (la: Set[String]) => la ++ la.flatMap((x: String) => superClasses.getOrElse(x, Nil).toSet).toSet }(Set(a)) -- Set(a)).toList
    }
    
    val transitions = scala.collection.mutable.ListBuffer[(String, String, List[String])]()
    
    CaseClasses.findAllIn(input).foreach {
      case CaseClasses(name, arguments, extensions) =>
        val rhs = Argument.findAllIn(arguments).toList.map {
          case Argument(_, tpe) =>
            states += tpe
            tpe
        }
        states += name
        transitions += ((name, name, rhs))
        caseClasses += name -> rhs
        arities += name -> rhs.length
        Extension.findAllIn(extensions).foreach {
          case Extension(superClass) => 
            superClasses += name -> (superClass::superClasses.getOrElse(name, Nil))
            subClasses += superClass -> (name::subClasses.getOrElse(superClass, Nil))
        }
    }
    CaseObjects.findAllIn(input).foreach {
      case CaseObjects(name, extensions) =>
        val rhs = Nil
        states += name
        transitions += ((name, name, rhs))
        caseClasses += name -> rhs
        arities += name -> rhs.length
        Extension.findAllIn(extensions).foreach {
          case Extension(superClass) => 
            superClasses += name -> (superClass::superClasses.getOrElse(name, Nil))
            subClasses += superClass -> (name::subClasses.getOrElse(superClass, Nil))
        }
    }

    AbstractClasses.findAllIn(input).foreach {
      case AbstractClasses(name, extensions) =>
        Extension.findAllIn(extensions).foreach {
          case Extension(superClass) => 
            superClasses += name -> (superClass::superClasses.getOrElse(name, Nil))
            subClasses += superClass -> (name::subClasses.getOrElse(superClass, Nil))
        }
    }
    
    Traits.findAllIn(input).foreach {
      case Traits(name, extensions) =>
        Extension.findAllIn(extensions).foreach {
          case Extension(superClass) => 
            superClasses += name -> (superClass::superClasses.getOrElse(name, Nil))
            subClasses += superClass -> (name::subClasses.getOrElse(superClass, Nil))
        }
    }
    for ((cname, cargs) <- caseClasses) {
      for (sname <- allsuperClasses(cname)) {
        val transition = (sname, cname, cargs)
        transitions += transition
      }
    }
    import Notations._
    if (states contains "String") {
      transitions += (("String", StringA, List()))
      transitions += (("String", StringB, List()))
    }
    arities += StringA -> 0
    arities += StringB -> 0
    if (states contains "Int") {
      transitions += (("Int", Int0, List()))
      transitions += (("Int", Int1, List()))
    }
    arities += Int0 -> 0
    arities += Int1 -> 0
    if (states contains "Boolean") {
      transitions += (("Boolean", BooleanTrue, List()))
      transitions += (("Boolean", BooleanFalse, List()))
    }
    arities += BooleanTrue -> 0
    arities += BooleanFalse -> 0
    
    val res = Domain[String](states, states, transitions.toList, Notations.RankedAlphabet(arities)) 
    /*println("Parsed domain:")
    println(res)*/
    res
  }
}