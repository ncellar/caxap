package compiler;

import parser.Match;

/**
 * Handles the match tree after parsing:
 *
 * <pre>
 * - applies pre macro expansion callbacks
 * - expands macros
 * - applies post macro expansion callbacks
 * </pre>
 */
public class PostParser
{
  public Match run(Match match)
  {
    match = new PostParseTransformer()      .transform(match);
    match = new MacroExpander()             .transform(match);
    match = new PostExpansionTransformer()  .transform(match);

    return match;
  }
}
