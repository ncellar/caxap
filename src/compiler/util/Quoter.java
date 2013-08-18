package compiler.util;

import static grammar.GrammarDSL.endOfInput;
import static grammar.GrammarDSL.seq;
import static trees.MatchSpec.expr;
import static trees.MatchSpec.hasMatchAtPos;
import static trees.MatchSpec.rule;
import static util.StringUtils.unescape;

import java.util.ArrayList;
import java.util.List;

import driver.Context;
import driver.Hints;
import grammar.Expression;
import grammar.Expression.Rule;
import grammar.Grammar;
import parser.Match;
import parser.Matcher;
import source.Source;
import source.SourceString;
import trees.MatchSpec;
import util.ArraySlice;

/**
 * See the section of my thesis titled "(Quasi)quotation" to learn more about
 * quotation and quasiquotation.
 */
public class Quoter
{
  /*****************************************************************************
   * This call is meant to be made from Java code and does not require
   * preliminary source preprocessing. The contents of $code should follow the
   * syntax of {@link grammar.java._E_MacroDefinitions#dynamicSourceFragment} (specifying
   * inserts with "#1", "#2", etc; where the number is an index into $matches).
   */
  public static Match dynamicQuote(String rule, String code, Object... inserts)
  {
    return new DynamicQuoteMethod().dynamicQuote(rule, code, inserts);
  }

  /****************************************************************************/
  private static class DynamicQuoteMethod
  {
    // // GLOBALS
    // ===============================================================

    String code;            // [final] original quote string
    Object[] inserts;       // [final] objects to be inserted
    List<MatchSpec> specs;  // verify that inserted matches do parse back

    // index diff for current marker between $code and $expansion
    int diff = 0;

    // // PER MARKER
    // ============================================================

    Match marker;       // current marker
    int number;         // insert (of $inserts) index by $marker
    boolean isSplice;   // is the marker for splicing (match array insertion)?
    Object insert;      // $inserts[$number]
    String insertion;   // $insert, converted to string according to $marker

    /******************************************************************h********/
    Match dynamicQuote(String rule, String code, Object... inserts)
    {
      StringBuffer expansion = new StringBuffer(code);
      Matcher matcher = new Matcher(new SourceString(code));

      this.code = code;
      this.inserts = inserts;
      this.specs = new ArrayList<>(inserts.length);

      // The rule can match any string.
      matcher.matches(Context.get().grammar().rule("dynamicSourceFragment"));

      for (Match marker : matcher.match().all(rule("insertMarker")))
      if (!marker.has(rule("backslash"))) // escaped marker
      {
        updatePerMarkerVariables(marker);
        expansion.replace(marker.begin + diff, marker.end + diff, insertion);
        diff = diff - marker.length() + insertion.length();
      }

      return primitiveQuote(rule, expansion.toString(), specs);
    }

    /**************************************************************************/
    void updatePerMarkerVariables(Match marker)
    {
      number = Integer.parseInt(marker.first(rule("parseIntNumber")).string());

      this.marker = marker;
      checkNumber();
      String str = marker.string();

      isSplice = str.startsWith("#@");
      insert = inserts[number - 1];

      checkType();
      insertion = getInsertionString();
    }

    /**************************************************************************/
    void checkType()
    {
      if (isSplice && !insert.getClass().isArray()) {
        throw new Error("The parameter supplied to dynamicQuote() for the "
          + "splice marker " + marker.string() + " should be an array, in ["
          + code + "].");
      }
    }

    /**************************************************************************/
    void checkNumber()
    {
      if (number > inserts.length) {
        throw new Error("Insert number in source fragment is too high: "
          + number + " when the max is " + inserts.length + ".");
      }
    }

    /*****************************************************************************
     * Returns $insert, converted to string according to $marker. Adds items in
     * $specs for $insert.
     */
    String getInsertionString()
    {
      return isSplice
        ? getSpliceString((Object[]) insert, getDelimiters(marker))
        : insertToString(insert);
    }

    /***************************************************************************
     * Returns the delimiters of a spliced inserts.
     */
    private String[] getDelimiters(Match marker)
    {
      assert isSplice;

      Match[] delimMatches = marker.all(rule("spliceDelimiter"));
      String[] delimiters = new String[3];

      for (int i = 0; i < 3; ++i)
      {
        delimiters[i] = unescape(delimMatches[i].string());
        delimiters[i] = delimiters[i].replaceAll("\\\\\\|", "|");
        // We replace \|, but this is a regexp inside a java string.
      }

      return delimiters;
    }

    /***************************************************************************
     * Converts an inserted object to a string holding the code it represents.
     * Uses {@link Match#string()} for matches, {@link Object#toString()}
     * otherwise.
     */
    private String insertToString(Object insert)
    {
      if (insert instanceof Match) {
        Match match = (Match) insert;
        specs.add(hasMatchAtPos(match, marker.begin + diff));
        return match.string();
      }
      else {
        return insert.toString();
      }
    }

    /***************************************************************************
     * Like {@link #getInsertionString()}, but specialized for match splices.
     */
    String getSpliceString(Object[] inserts, String[] seps)
    {
      if (inserts.length == 0) {
        return "";
      }

      // Keep $diff accurate for $specs.
      int old_diff = diff;
      diff += seps[0].length();

      StringBuilder builder = new StringBuilder(seps[0]);

      for (Object insert : inserts)
      {
        String str = insertToString(insert);
        builder.append(str);
        builder.append(seps[1]);
        diff += str.length() + seps[1].length();
      }

      diff = old_diff;

      builder.delete(builder.length() - seps[1].length(), builder.length());
      builder.append(seps[2]);
      return builder.toString();
    }
  }

  /*****************************************************************************
   * Primitive form of quotation. Parses $code according to the rule named
   * $ruleName. Verifies that the resulting match satisfies the specifications
   * in $specs.
   */
  public static Match primitiveQuote(String ruleName, String code,
    List<MatchSpec> specs)
  {
    Grammar grammar = Context.get().grammar();
    Rule rule = grammar.rule(ruleName);
    Expression wrap = grammar.clean(seq(rule, endOfInput));
    Source source = new SourceString(code);
    Matcher matcher = new Matcher(source);

    if (!matcher.matches(wrap)) {
      throw new Error("The quotation '" + rule + " [" + code
        + "]' does not yield a valid parse.");
    }

    Match result = matcher.match();
    Hints.get().hintSource(source);

    for (MatchSpec spec : specs) {
      if (!spec.matches(result)) {
        throw new Error("The match tree obtained by parsing \"" + code
          + "\" did not match the specification:\n" + spec);
      }
    }
    Hints.get().endHintSource();

    return result.first(expr(rule));
  }

  /****************************************************************************/
  public static Match primitiveQuote(String ruleName, String code,
    MatchSpec... specs)
  {
    return primitiveQuote(ruleName, code, new ArraySlice<>(specs));
  }
}
