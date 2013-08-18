package compiler.util;

import static compiler.util.PEGCompiler.compile;

import driver.Context;

import grammar.Expression;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

import static grammar.GrammarDSL.*;
import static org.junit.Assert.assertEquals;

/**
 * Test {@link PEGCompiler} and its underlying mechanism in
 * {@link grammar.java.CallbacksExpression}.
 */
@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class PEGCompilerTests
{
  void assertExpr(Expression expected, Expression actual)
  {
    assertEquals(Context.get().grammar().clean(expected), actual);
  }

  /****************************************************************************/
  @Test public void aa_test()
  {
    Expression e;

    e = compile("expression");
    assertExpr(ref("expression"), e);

    e = compile("expression | statement");
    assertExpr(choice(ref("expression"), ref("statement")), e);

    e = compile("expression statement | statement");
    assertExpr(choice(
      seq(ref("expression"), ref("statement")), ref("statement")), e);

    e = compile("expression statement | statement");
    assertExpr(choice(
      seq(ref("expression"), ref("statement")), seq(ref("statement"))), e);

    e = compile("expression statement | statement");
    assertExpr(choice(
      seq(ref("expression"), ref("statement")), seq(ref("statement"))), e);

    e = compile("\"test\"*");
    assertExpr(star(str("test"), ref("spacing")), e);

    e = compile("\"test\"-*");
    assertExpr(star(str("test")), e);

    e = compile("(\"test\")?");
    assertExpr(opt(str("test"), ref("spacing")), e);

    e = compile("_+");
    assertExpr(plus(any), e);

    e = compile("[abc] *+ ^[xyz]");
    assertExpr(until(chars("abc"), notChars("xyz")), e);

    e = compile("[a-c] ++ ^[x-z]");
    assertExpr(untilOnce(range('a', 'c'), notRange('x', 'z')), e);

    e = compile("[a-c] +/ [x-z]");
    assertExpr(list(range('x', 'z'), range('a', 'c')), e);

    e = compile("_* *+ _");
    assertExpr(until(star(any), any), e);

    e = compile("_+ ++ _");
    assertExpr(untilOnce(plus(any), any), e);

    e = compile("_+ +/ _");
    assertExpr(list(any, plus(any)), e);

    e = compile("&expression _*");
    assertExpr(seq(and(ref("expression")), star(any)), e);

    e = compile("!(!expression) *+ _");
    assertExpr(until(not(not(ref("expression"))), any), e);

    e = compile("[a-c] ++ ^[x-z] | \"test\"* expression | !(!expression) *+ _");
    assertExpr(choice(
      untilOnce(range('a', 'c'), notRange('x', 'z')),
      seq(star(str("test"), ref("spacing")), ref("expression")),
      until(not(not(ref("expression"))), any)
    ), e);

    e = compile("\"\\\"\\n\"-");
    assertExpr(str("\"\n"), e);

    e = compile("[\\\"\\n]");
    assertExpr(chars("\"\n"), e);

    e = compile("[\\]-\\]]");
    assertExpr(range(']', ']'), e);

    // untested (but should work): captures
  }
}
