package example
package xml

abstract class Ls
abstract class La

case class Node(t: String, attrs: La, children: Ls)
case class Cons(n: Node, l: Ls) extends Ls
case class Nil() extends Ls

case class ConsA(a: Attribute, l: La) extends La
case class NilA() extends La
case class Attribute(key: String, value: String)
