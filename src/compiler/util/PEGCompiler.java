package compiler.util;

import driver.Context;

import grammar.Expression;

/**
 * Compiles a string representation of a PEG expression into an expression
 * graph. Also populates {@link Context#captureNames} with the capture names
 * for the expression. Don't forget to clear that field before parsing a new
 * PEG expression!
 *
 * The compiler uses parse callbacks to build the expression, rather than use a
 * macro-like mechanism to translate the syntax to {@link grammar.GrammarDSL}
 * calls. It's simpler, and it avoids namespace pollution.
 */
public class PEGCompiler
{
  /****************************************************************************/
  public static Expression compile(String string)
  {
    StringMatcher.matchString(string, "parsingExpression");
    return Context.get().grammar().clean(Context.get().expressionStack.pop());
  }
}
