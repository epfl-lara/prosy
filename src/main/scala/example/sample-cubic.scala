package example
package cubic

abstract class A
abstract class B
abstract class C
case class A1_(b: B) extends A
case class B1_(c: C) extends B
case class C1_() extends C


case class A2_(b: B) extends A
case class B2_(c: C) extends B
case class C2_() extends C


case class A3_(b: B) extends A
case class B3_(c: C) extends B
case class C3_() extends C


case class A4_(b: B) extends A
case class B4_(c: C) extends B
case class C4_() extends C
