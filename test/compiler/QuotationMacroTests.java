package compiler;

import static compiler.util.StringMatcher.matchString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;
import parser.Match;

import static compiler.QuotationMacroImplem.PRIMITIVE_QUOTE;
import static compiler.QuotationMacroImplem.DYNAMIC_QUOTE;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class QuotationMacroTests
{
  /****************************************************************************/
  static final MacroExpander expander = new MacroExpander();

  /****************************************************************************/
  Match expand(String quotation)
  {
    Match m = matchString(quotation, "unaryExpression");
    return expander.transform(m);
  }

  /****************************************************************************/
  void assertExpand(Match m, String rule, String code)
  {
    assertEquals(PRIMITIVE_QUOTE + "(\"" + rule + "\", \"" + code + "\")",
      m.string());
  }

  /****************************************************************************/
  void assertExpand(Match m, String rule, String code, String inserts)
  {
    assertEquals(DYNAMIC_QUOTE + "(\"" + rule + "\", \"" + code
      + "\", (Object)" + inserts + ")", m.string());
  }

  /****************************************************************************/
  @Test public void test()
  {
    /* Note that the quoted expressions are not necessary legal, but we are
     * trying to find out if they expand correctly to a Java quotePrimitive()
     * dynamicQuote() call. */

    Match m1 = expand("'expression[ 1 + 1 ]'");
    assertExpand(m1, "expression", "1 + 1");

    Match m2 = expand("`expression[ 1 + 1 ]`");
    assertExpand(m2, "expression", "1 + 1");

    Match m3 = expand("`expression[ #(1 + 1) ]`");
    assertExpand(m3, "expression", "#1 ", "(1 + 1)");

    Match m4 = expand("'expression[ #(1 + 1) ]'");
    assertExpand(m4, "expression", "#(1 + 1)");

    Match m5 = expand("'expression[ `expression [ #(1 + 1) ]` ]'");
    assertExpand(m5, "expression", "`expression [ #(1 + 1) ]`");

    Match m6 = expand("'expression[ 'expression [ 1 + 1 ]' ]'");
    assertExpand(m6, "expression", "'expression [ 1 + 1 ]'");

    Match m7 = expand("'expression[ 'expression [ \"\\]'\" ]' ]'");
    assertExpand(m7, "expression", "'expression [ \\\"\\\\]'\\\" ]'");

    Match m7p = expand("'expression [ \"\\]'\" ]'");
    assertExpand(m7p, "expression", "\\\"]'\\\"");

    Match m7pp = expand("`expression [ \"\\]'\" ]`");
    assertExpand(m7pp, "expression", "\\\"]'\\\"");

    Match m8 = expand("`expression[ #@ |:|,|;| myArray ]`");
    assertExpand(m8, "expression", "#@|:|,|;|1 ", "myArray");

    Match m9 = expand("`expression[ #@ |\\||\\n|;| myArray ]`");
    assertExpand(m9, "expression", "#@|\\\\||\\\\n|;|1 ", "myArray");

    Match m10 = expand("`expression[ # 'expression [ 1 + 1 ]'  ]`");
    assertExpand(m10, "expression", "#1", PRIMITIVE_QUOTE + "(\"expression\", \"1 + 1\")");

    Match m11 = expand("`expression[ `expression [ ## var ]`  ]`");
    assertExpand(m11, "expression", "`expression [ ##1 ]`", "var");

    Match m12 = expand("`expression[ 'expression [ # var ]' ]`");
    assertExpand(m12, "expression", "'expression [ #1 ]'", "var");

    boolean caught = false;
    try {
      expand("`expression[ ## var ]`");
    }
    catch (Error e) {
      assertTrue(e.getMessage().contains("negative depth"));
      caught = true;
    }
    assertTrue(caught);
  }
}
