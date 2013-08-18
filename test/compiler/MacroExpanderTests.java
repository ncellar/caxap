package compiler;

import static compiler.Macro.Strategy.AS;
import static compiler.Macro.Strategy.REPLACES;
import static compiler.Macro.Strategy.UNDER;
import static compiler.Macro.Strategy.CALLED;
import static compiler.util.MatchCreator.new_match;
import static compiler.util.StringMatcher.matchString;
import static grammar.GrammarDSL.ref;
import static grammar.GrammarDSL.seq;
import static grammar.GrammarDSL.str;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static trees.MatchSpec.rule;
import static util.MemberAccessor.invoke;

import compiler.macros.MacroInterface;
import compiler.util.Quoter;
import driver.Context;
import grammar.Expression;
import grammar.Grammar;
import grammar.java._E_MacroDefinitions;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;
import parser.Match;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MacroExpanderTests
{
  /****************************************************************************/
  static       Grammar       old;
  static final Grammar       grammar  = new Grammar(_E_MacroDefinitions.class);
  static final MacroExpander expander = new MacroExpander();

  /****************************************************************************/
  @BeforeClass public static void setUp()
  {
    old = Context.get().grammar();
    invoke(Context.get(), "setGrammar", grammar);
  }

  /****************************************************************************/
  @BeforeClass public static void tearDown()
  {
    if (old != null) invoke(Context.get(), "setGrammar", old);
  }

  //============================================================================

  class Macro1 implements MacroInterface {
    @Override public Match expand(Match input) { return quote("42"); }
  }

  class Macro2 implements MacroInterface {
    @Override public Match expand(Match input) { return quote("/macro1/"); }
  }

  class Macro3 implements MacroInterface {
    @Override public Match expand(Match input) {
      return new_match("macro3_dummy", quote("42"));
    }
  }

  class Macro4 implements MacroInterface {
    @Override public Match expand(Match input) {
      return new_match("macro4_dummy", quote("/macro3/"));
    }
  }

  class Macro5 implements MacroInterface {
    @Override public Match expand(Match input) {
      return new_match("macro5_dummy", quote("42"));
    }
  }

  class Macro6 implements MacroInterface {
    @Override public Match expand(Match input) {
      return new_match("macro6_dummy", quote("/macro5/"));
    }
  }

  class Macro7 implements MacroInterface {
    @Override public Match expand(Match input) { return quote("/macro2/"); }
  }

  static class Macro8 implements MacroInterface {
    @Override public Match expand(Match input) {
      return input.first(rule("expression"));
    }
  }

  Expression m8s = seq(
    str("/macro8/"), ref("lCuBra"), ref("expression"), ref("rCuBra"));

  static class Macro9 implements MacroInterface {
    @Override public Match expand(Match input) {
      return Quoter.dynamicQuote("expression", "\"#1\"",
        input.first(rule("expression")).string());
    }
  }

  Expression m9s = seq(
    str("/macro9/"), ref("lCuBra"), ref("expression"), ref("rCuBra"));

  Expression m11s = seq(str("/macro11/"), ref("macro10"));

  static class Macro12 implements MacroInterface {
    @Override public Match expand(Match input) {
      return Quoter.dynamicQuote("expression", "\"#1\"",
        input.first(rule("macro10")).string());
    }
  }

  Expression m12s = seq(str("/macro12/"), ref("macro10"));

  /****************************************************************************/
  Match quote(String code)
  {
    return Quoter.dynamicQuote("expression", code);
  }

  /****************************************************************************/
  Macro macro(int n, Expression syntax, MacroInterface iface,
    Macro.Strategy strat, boolean raw)
  {
    return new Macro("macro" + n, strat == CALLED ? null : "expression",
      grammar, syntax, iface, strat, raw, false);
  }

  /****************************************************************************/
  Macro smacro(int n, MacroInterface iface, Macro.Strategy type, boolean raw)
  {
    return macro(n, str("/macro" + n + "/"), iface, type, raw);
  }

  /****************************************************************************/
  Match testParse(int n)
  {
    return testParse(n, "/macro" + n + "/");
  }

  /****************************************************************************/
  Match testParse(int n, String mstr)
  {
    Match m = matchString(mstr, "expression");
    assertEquals(mstr, m.string());
    assertTrue(m.is (rule("expression")));
    assertTrue(m.child().is(rule("macro" + n)));
    return m;
  }

  /****************************************************************************/
  @Test public void aa_test()
  {
    Macro macro1 = smacro(1, new Macro1(), AS,       false);
    Macro macro2 = smacro(2, new Macro2(), AS,       false);
    Macro macro3 = smacro(3, new Macro3(), UNDER,    false);
    Macro macro4 = smacro(4, new Macro4(), UNDER,    false);
    Macro macro5 = smacro(5, new Macro5(), REPLACES, false);
    Macro macro6 = smacro(6, new Macro6(), REPLACES, false);
    Macro macro7 = smacro(7, new Macro7(), AS,       false);
    Macro macro8  = macro(8, m8s, new Macro8(), AS,  false);
    Macro macro8r = macro(8, m8s, new Macro8(), AS,  true);
    Macro macro9  = macro(9, m9s, new Macro9(), AS,  true);

    Macro macro10 = smacro(10, new Macro1(), CALLED, false);
    macro10.enable();
    Macro macro11 = macro(11,  m11s, new Macro1(),  AS, false);
    Macro macro12 = macro(12,  m12s, new Macro12(), AS, false);

    Match m1, m2;

    macro1.enable();
    m1 = testParse(1);

    m2 = expander.transform(m1);
    assertEquals("42" , m2.string());
    assertTrue (m2.is (rule("expression")));
    assertFalse(m2.has(rule("macro1")));

    macro2.enable();
    m1 = testParse(2);

    m2 = expander.transform(m1);
    assertEquals("42", m2.string());
    assertTrue (m2.is (rule("expression")));
    assertFalse(m2.has(rule("macro1")));
    assertFalse(m2.has(rule("macro2")));

    macro3.enable();
    m1 = testParse(3);

    m2 = expander.transform(m1);
    assertEquals("42", m2.string());
    assertTrue(m2.is (rule("expression")));
    assertTrue(m2.child().is(rule("macro3_dummy")));

    macro4.enable();
    m1 = testParse(4);

    m2 = expander.transform(m1);
    assertEquals("42", m2.string());
    assertTrue (m2.is (rule("expression")));
    assertTrue (m2.child().is(rule("macro4_dummy")));
    assertTrue (m2.child().child().is(rule("expression")));
    assertTrue (m2.child().child().child().is(rule("macro3_dummy")));
    assertFalse(m2.has(rule("macro3")));
    assertFalse(m2.has(rule("macro4")));

    macro5.enable();
    m1 = testParse(5);

    m2 = expander.transform(m1);
    assertEquals("42", m2.string());
    assertTrue (m2.is(rule("macro5_dummy")));
    assertFalse(m2.has(rule("macro5")));

    macro6.enable();
    m1 = testParse(6);

    m2 = expander.transform(m1);
    assertEquals("42", m2.string());
    assertTrue (m2.is (rule("macro6_dummy")));
    assertTrue (m2.child().is(rule("macro5_dummy")));
    assertFalse(m2.has(rule("macro5")));
    assertFalse(m2.has(rule("macro6")));

    macro7.enable();
    m1 = testParse(7);

    m2 = expander.transform(m1);
    assertEquals("42", m2.string());
    assertTrue (m2.is (rule("expression")));
    assertFalse(m2.has(rule("macro7")));
    assertFalse(m2.has(rule("macro1")));

    macro8.enable();
    m1 = testParse(8, "/macro8/{/macro1/}");

    m2 = expander.transform(m1);
    assertEquals("42", m2.string());
    assertTrue (m2.is(rule("expression")));
    assertFalse(m2.has(rule("macro8")));
    assertFalse(m2.has(rule("macro1")));

    m1 = testParse(8, "/macro8/{/macro8/{/macro1/}}");
    m2 = expander.transform(m1);
    assertEquals("42", m2.string());
    assertTrue (m2.is(rule("expression")));
    assertFalse(m2.has(rule("macro8")));
    assertFalse(m2.has(rule("macro1")));

    macro8.disable();
    macro8r.enable();
    m1 = testParse(8, "/macro8/{/macro1/}");

    m2 = expander.transform(m1);
    assertEquals("42", m2.string());
    assertTrue (m2.is(rule("expression")));
    assertFalse(m2.has(rule("macro8")));
    assertFalse(m2.has(rule("macro1")));

    m1 = testParse(8, "/macro8/{/macro8/{/macro1/}}");
    m2 = expander.transform(m1);
    assertEquals("42", m2.string());
    assertTrue (m2.is(rule("expression")));
    assertFalse(m2.has(rule("macro8")));
    assertFalse(m2.has(rule("macro1")));

    macro9.enable();
    m1 = testParse(9, "/macro9/{/macro1/}");

    m2 = expander.transform(m1);
    assertEquals("\"/macro1/\"", m2.string());
    assertTrue(m2.child().has(rule("stringLiteral")));

    macro11.enable();
    m1 = testParse(11, "/macro11//macro10/");
    m2 = expander.transform(m1);
    assertEquals("42", m2.string());

    macro12.enable();
    m1 = testParse(12, "/macro12//macro10/");
    m2 = expander.transform(m1);
    assertEquals("\"/macro10/\"", m2.string());
  }
}
