package example
package htmlpaper

abstract class Tag
case class Div() extends Tag
case class Pre() extends Tag
case class Span() extends Tag

case class Element(tag: Tag, children: Ls)

case class Cons(n: Element, l: Ls) extends Ls
case class Nil() extends Ls