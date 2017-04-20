package example
package html

abstract class Ls
abstract class La
abstract class Element

case class Div(attrs: La, children: Ls) extends Element
case class Pre(attrs: La, children: Ls) extends Element
case class Span(attrs: La, children: Ls) extends Element

case class Cons(n: Element, l: Ls) extends Ls
case class Nil() extends Ls

case class ConsA(a: Attribute, l: La) extends La
case class NilA() extends La
case class Attribute(key: String, value: String)
