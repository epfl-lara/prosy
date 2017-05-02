package example
package peano

abstract class BinNumber

case class Zero(n: BinNumber) extends BinNumber
case class One(n: BinNumber) extends BinNumber
case class Empty() extends BinNumber