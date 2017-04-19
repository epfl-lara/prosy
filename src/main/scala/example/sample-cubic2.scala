package example
package cubic2

abstract class A
abstract class B
abstract class C
case class A1_(b: B) extends A
case class B1_(c: C) extends B
case class C1_() extends C


case class A2_(b: B) extends A
case class B2_(c: C) extends B
case class C2_() extends C
