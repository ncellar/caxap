package compiler;

import static compiler.util.Quoter.dynamicQuote;
import static compiler.util.Quoter.primitiveQuote;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static trees.MatchSpec.hasExprAtPos;
import static trees.MatchSpec.rule;

import compiler.util.Quoter;
import driver.Context;
import grammar.Grammar;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;
import parser.Match;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class QuoterTests
{
  /****************************************************************************/
  Grammar grammar = Context.get().grammar();

  /****************************************************************************/
  @Test public void aa_quotePrimitive()
  {
    String str = "1 + myMethod() + array[0]";
    int    l1  = "1 + ".length();
    int    l2  = "1 + myMethod() + ".length();
    int    l3  = "1 + myMethod() + array".length();

    Match result = Quoter.primitiveQuote("expression", str,
      hasExprAtPos(grammar.rule("integerLiteral")             , 0),
      hasExprAtPos(grammar.rule("primaryMethodInvocation")    , l1),
      hasExprAtPos(grammar.rule("suffixedPrimaryExpression")  , l2),
      hasExprAtPos(grammar.rule("squareExpr")                 , l3));

    assertEquals(str, result.string());
    assertEquals(0, result.begin);
    assertEquals(str.length(), result.end);
  }

  /****************************************************************************/
  @Test public void ab_dynamicQuote()
  {
    Match result = dynamicQuote("expression", "#1 + #2 + #3",
      Quoter.primitiveQuote("integerLiteral", "1"),
      Quoter.primitiveQuote("primaryMethodInvocation", "myMethod()"),
      Quoter.primitiveQuote("suffixedPrimaryExpression", "array[0]"));

    String expected = "1 + myMethod() + array[0]";

    assertEquals(expected, result.string());
    assertEquals(0, result.begin);
    assertEquals(expected.length(), result.end);
    assertTrue  (result.is(rule("expression")));
    assertTrue(result.has(rule("integerLiteral")));
    assertTrue(result.has(rule("primaryMethodInvocation")));
    assertTrue(result.has(rule("suffixedPrimaryExpression")));

    result = dynamicQuote("expression", "#1#2", 4, 2);

    assertEquals("42", result.string());
    assertTrue(result.has(rule("integerLiteral")));

    result = dynamicQuote("expression", "myMethod(#@ ||,|| 1)",
      (Object) new Integer[]{42, 43});

    assertEquals("myMethod(42,43)", result.string());
    assertTrue  (result.is(rule("expression")));
    assertEquals(2, result.all(rule("integerLiteral")).length);

    result = dynamicQuote("expression", "myMethod #@ |(|,|)| 1",
      (Object) new Match[] {
        primitiveQuote("expression", "42"), primitiveQuote("expression", "43")
      });

    assertEquals("myMethod (42,43)", result.string());
    assertTrue  (result.is(rule("expression")));
    assertEquals(2, result.all(rule("integerLiteral")).length);
  }
}
