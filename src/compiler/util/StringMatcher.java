package compiler.util;

import static grammar.GrammarDSL.endOfInput;
import static grammar.GrammarDSL.seq;
import static trees.MatchSpec.rule;

import driver.Context;
import grammar.Expression;
import grammar.Grammar;
import parser.Match;
import parser.Matcher;
import source.Source;
import source.SourceString;

/**
 * Utility function to parse strings.
 */
public class StringMatcher
{
  /****************************************************************************/
  public static Match matchString(String string, String rule)
  {
    Source  source  = new SourceString(string);
    Matcher matcher = new Matcher(source);
    Grammar grammar = Context.get().grammar();

    Expression wrap = grammar.clean(seq(grammar.rule(rule), endOfInput));

    if (matcher.matches(wrap)) {
      return matcher.match().first(rule(rule));
    }
    else {
      throw new Error(matcher.errors().report(source));
    }
  }
}
