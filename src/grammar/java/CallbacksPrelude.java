package grammar.java;

import static trees.MatchSpec.rule;
import static util.StringUtils.builderAppend;
import static compiler.util.Quoter.primitiveQuote;

import grammar.MatchCallbacks;

import driver.Context;
import parser.Match;

/**
 * Expands require statements into nothing (macro requires) or import statements.
 * Note that when we expand macros for the first time, the import and requires
 * statement have already been parsed once.
 */
public class CallbacksPrelude extends MatchCallbacks
{
  /****************************************************************************/
  @Override public Match postParseTopDown(Match input)
  {
    Match pkg = input.first(rule("packageDeclaration"));

    StringBuilder code = new StringBuilder(
      input.has(pkg) ? pkg.string() + "\n" : "");

    for (String imp : Context.get().currentFile.imports()) {
      builderAppend(code, "\n", imp);
    }
    code.append("\n");

    return primitiveQuote("prelude", code.toString());
  }
}
