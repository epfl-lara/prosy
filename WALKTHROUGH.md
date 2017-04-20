Artifact description
====================

This artifact is the implementation of the algorithms of the paper "Proactive Synthesis of Recursive Tree-to-String Functions from Examples", published at ECOOP 2017.
In `src/main/scala`, under the package name `prosy`, you can find the implementation in Scala of the 3 algorithms of the paper.
We provide the implementation of the first two algorithms for completeness. However, since the third algorithm generalizes the first two, our artefact walkthrough is only about the third and most important algorithm, the proactive one.

In `src/main/scala`, under the package name `example`, you can find some examples that can be given to the command line of our tool.

This artifact can be run through the Scala Built Tool, alias `sbt`.

How to use the artifact
=======================

Prerequisites
-------------

* Have git installed and on the path.
* Have java 7 or 8
* Have [SBT](http://www.scala-sbt.org/) installed and on the path.
* Clone the following directory using git:

    `git clone https://github.com/epfl-lara/prosy`
  
* Test your installation by doing

    `sbt compile`

It should work fine.

Walkthrough Grammar
-------------------

We will show how to build a printer for a representation of a Context-Free grammar, the example of the paper, with some variations.
The file for which we are going to create a pretty-printer is located at: `src/main/scala/example/sample-paper.scala`.  
We will use a two-letter (a, b) alphabet, which will be used both for the terminal characters and for indexing the non-terminals.
Rules will be prefixed by a newline, symbols at the right-hand-side of a rule will be prefixed by a space.
Instead of a textual representation, we will use a latex-like representation of the grammar.

Run the following command:

    sbt "run src/main/scala/example/sample-paper.scala"

This walkthrough intentionnally comports some errors which can be recovered. 
Questions are represented before the question mark, and answers follow after the space following the question mark. No space is added *after* an answer, but in one case before - see WARNING.
We comment some of the lines using `#`. Do not write these comments in the tool, nor the spaces before them.

```bash
a ? a      # The letter a is printed as-it.
b ? b      # Same for letter b
NilChar ?        # Press Enter, nothing to display if there is no char.
NilSymbol ?      # Press Enter, nothing to display if there is no symbol.
NilRule ?        # Press Enter, nothing to display if there is no rule.
Terminal(a) ? b              # Try putting b. It will complain.
Terminal(a) ? a              # Now that's ok.
NonTerminal(NilChar) ? N_{}  # We want non-terminals to be displayed N_{name}.
ConsChar(b,NilChar) ? b      # ConsChar and NilChar are used to build "strings"
NonTerminal(ConsChar(b,NilChar))  ? 6   # There are 5 possibilities, so 6 will make the system re-ask the question because it is not an acceptable answer.
NonTerminal(ConsChar(b,NilChar))  ? 0   # Let us input this manually.
NonTerminal(ConsChar(b,NilChar))  ? Nb  # This will fail, although it followed the hing. Note that it correctly suggests consistent outputs.
NonTerminal(ConsChar(b,NilChar))  ? 2   # We give the correct output index
Grammar(NonTerminal(NilChar),NilRule) ? N_{} is the start symbol # We first describe the start non-terminal, and the rules wills follow.
ConsSymbol(Terminal(a),NilSymbol) ?  a  # WARNING: add one space before a. It is the concatenation of symbols in a rule.
Rule(NonTerminal(NilChar),NilSymbol) ? N_{} \longrightarrow         # No space after the arrow.
ConsRule(Rule(NonTerminal(NilChar),NilSymbol),NilRule) ? \
N_{} \longrightarrow     # Note that we write \ and press enter to indicate a new line.
Rule(NonTerminal(NilChar),ConsSymbol(Terminal(a),NilSymbol)) ? 10 # The way we want to print it does not appear in the list. Although we could input 0 and then write it ourselves, we can also write 10 to enumerate the remaining suggestions.
Rule(NonTerminal(NilChar),ConsSymbol(Terminal(a),NilSymbol)) ? 7  # The answer appeared there. Note that if you enter 9 or 10, it will complain and restart the questions.
Grammar(NonTerminal(NilChar),ConsRule(Rule(NonTerminal(NilChar),NilSymbol),NilRule)) ? 10
Grammar(NonTerminal(NilChar),ConsRule(Rule(NonTerminal(NilChar),NilSymbol),NilRule)) ? 10
Grammar(NonTerminal(NilChar),ConsRule(Rule(NonTerminal(NilChar),NilSymbol),NilRule)) ? 3
```

It then prints the synthesized function to print the context-free grammar as expected.
Note the statistics. If you were to do this questioning without any errors, you would get these:

```
Statistics:
116 elements computed in test set:
  101 elements automatically inferred
  15 questions asked.
    6 regular questions
    6 with an hint of the type [...]foo[...]
    3 with explicit suggestions
```

Walkthrough HTML
----------------

In this walkthrough, we are going to pretty-print a simple HTML tree, composed of three node elements.

Run the command:

    sbt "run src/main/scala/example/sample-html.scala"

The walkthrough is the following

```bash
Nil ?   # No children, just press enter
NilA ?  # No attribute, just press enter
Span(NilA,Nil) ? <span></span>   # If you want something flat, you would \ and press enter before entering the closing </span> tag. See below for div.
Attribute("foo", "foo") ? foo="foo"  # You can try to put spaces or omit quotes.
Div(NilA,Nil) ? <div>\
</div>    # Here we want the closing and opening tags on two different lines.
Pre(NilA,Nil) ? <pre>\
</pre>    # Same for pre.
ConsA(Attribute("foo","bar"),NilA) ?  foo="bar" # WARNING: put a space before foo.
Cons(Div(NilA,Nil),Nil) ? \
<div>\
</div>  # There is a newline before the div.
Pre(NilA,Cons(Div(NilA,Nil),Nil)) ? 10  # The answer is in the other page.
Pre(NilA,Cons(Div(NilA,Nil),Nil)) ? 4   # Indeed we want something flat.
Span(ConsA(Attribute("foo","bar"),NilA),Nil) ? 6
Div(NilA,Cons(Span(NilA,Nil),Nil)) ? 10
Div(NilA,Cons(Span(NilA,Nil),Nil)) ? 4 # Span was on the same line.
Div(ConsA(Attribute("foo","bar"),NilA),Nil) ? 5  # Correct attributes
Pre(ConsA(Attribute("foo","foo"),NilA),Nil) ? 5  # Correct attributes
Span(NilA,Cons(Pre(NilA,Nil),Nil)) ? 2
```

and it was able to find the correct pretty-printing with the given statistics:

```
Statistics:
193 elements computed in test set:
  179 elements automatically inferred
  14 questions asked.
    5 regular questions
    3 with an hint of the type [...]foo[...]
    6 with explicit suggestions
```

Note that because of the linear complexity, you can try to edit the file to add more tags. and you will observe that the number of questions grows linearly, although the number of elements in the test sets grows cubically.
But this complexity is most visible in the following example:


Walkthrough Cubic illustration
------------------------------

We will show an example of how doubling the size of the type system multiplies by 8 the number of
examples in the test set, but fortunately for us multiplies only by 2 the number of questions.
This example is also interesting because we can force the system to ask how to print any element among 
the test set of elements which is of *cubic* size.

For that, we imagine the type system available in

    src/main/scala/example/sample-cubic2.scala

Note that this type system allows the existence of exactly 2 + 4 + 8 = 14 unique elements: `C1_()`, `C2_()`, `B1_(C2_())` ...
`A1_(B2_(C1_()))`... `A2_(B2_(C2_()))`. The name ends with `_` because of our parser.

Let us define two transducers that we want to learn by example. The first one is `print`, the second `disp` :

```scala
def print(t: Any): String = t match {
  case A1_(t1) => print(t1) + "ab"
  case A2_(t1) => print(t1)
  case B1_(t1) => print(t1)
  case B2_(t1) => "a" + print(t1) + "b"
  case C1_() => "ba"
  case C2_() => ""
}

def disp(t: Any): String = t match {
  case A1_(t1) => "ab" + disp(t1)
  case A2_(t1) => disp(t1)
  case B1_(t1) => disp(t1)
  case B2_(t1) => "a" + disp(t1) + "b"
  case C1_() => "ba"
  case C2_() => ""
}
```

These two transducers always display the same output, except for the unique value `A1_(B1_(C1()))`,
for which `print` displays `baab` and `disp` displays `abba`.
Since the role of `A1_` and `A2_` is interchangeable, and similarly for `B` and `C`,
we can make two transducers differ exactly on one chosen element, here `A1_(B1_(C1()))`.
Because of that, it looks like that we have to ask questions about all the elements, which would grow cubically in the number of classes. But we proved that we can infer all but a linear number of them, depending on the answers of the questions our system asks.
We will now explore this.

Let us learn `print` by launching the command:

    sbt "run src/main/scala/example/sample-cubic2.scala"

Here is the walkthrough of the interaction. When there is A? it means that it asks how to print A.
Answer using the characters after the space after the question mark, and then press ENTER. If nothing is displayed, just press ENTER.

```
C2_ ? 
C1_ ? ba
B1_(C1_) ? ba
B2_(C1_) ? abab
A2_(B2_(C1_)) ? abab
A1_(B1_(C2_)) ? ab
A1_(B2_(C1_)) ? 2
A1_(B1_(C1_)) ? baab  #   Note that we can enter the answer directly instead of a number if it is not a valid index and was part of the solutions.
```

Similarly, if at a choice question we answer something which is out of bounds, it will re-ask the question.
Answering 1 to the last question gives exactly the function `disp`.

Without any errors, we obtain the statistics :

```
14 elements computed in test set:
  6 elements automatically inferred
  8 questions asked.
    3 regular questions
    3 with an hint of the type [...]foo[...]
    2 with explicit suggestions
```

Now let us run the second one which has twice as many classes:

    sbt "run src/main/scala/example/sample-cubic4.scala"

Here we learn similarly but we treat `A2_`, `A3_` and `A4_` the same way, same for `B` and `C`.

```
C4_ ? 
C3_ ? 
C2_ ? 
C1_ ? ba
B3_(C2_) ? ab
B2_(C4_) ? ab
B4_(C2_) ? ab
B1_(C1_) ? ba
B2_(C1_) ? 1
B3_(C1_) ? 1
B4_(C1_) ? 1
A2_(B2_(C1_)) ? abab
A4_(B3_(C2_)) ? ab
A1_(B1_(C2_)) ? ab
A3_(B3_(C3_)) ? ab
A1_(B2_(C4_)) ? 2
A1_(B1_(C1_)) ? 2
```

You obtain the following statistics:

```
Statistics:
84 elements computed in test set:
  67 elements automatically inferred
  17 questions asked.
    8 regular questions
    4 with an hint of the type [...]foo[...]
    5 with explicit suggestions
```

Note that the number of question doubled from 8 to 17, whereas the number of elements that could have been ambiguous was roughly multiplied by 8 from  14 to 84.

The order of questions, when there is choice, is to ask questions with the least number of solutions first.




