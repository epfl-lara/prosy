package example
package grammar

trait Symbol
case class Terminal(s: String) extends Symbol
case class NonTerminal(s: String) extends Symbol
case class Rule(lhs: NonTerminal, rhs: ListSymbol)
abstract class ListRule
case class ConsRule(r: Rule, tail: ListRule) extends ListRule
case class NilRule() extends ListRule
abstract class ListSymbol
case class ConsSymbol(s: Symbol, tail: ListSymbol) extends ListSymbol
case class NilSymbol() extends ListSymbol

case class Grammar(s: NonTerminal, r: ListRule)
