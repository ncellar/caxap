package compiler;

import grammar.Expression.Rule;
import parser.Match;

/*******************************************************************************
 * Applies all post-parse callbacks that can potentially modify the match tree.
 */
class PostParseTransformer extends MatchTreeTransformer
{
  @Override public Match transform(Match match)
  {
    match = (match.expr instanceof Rule)
      ? ((Rule) match.expr).callbacks().postParseTopDown(match)
      : match;

    match = transformChilds(match);

    match = (match.expr instanceof Rule)
      ? ((Rule) match.expr).callbacks().postParseBottomUp(match)
      : match;

    return match;
  }
}

/*******************************************************************************
 * Applies all post-expansion callbacks that can potentially modify the match
 * tree.
 */
class PostExpansionTransformer extends MatchTreeTransformer
{
  @Override public Match transform(Match match)
  {
    match = (match.expr instanceof Rule)
      ? ((Rule) match.expr).callbacks().postExpansionTopDown(match)
      : match;

    match = transformChilds(match);

    match = (match.expr instanceof Rule)
      ? ((Rule) match.expr).callbacks().postExpansionBottomUp(match)
      : match;

    return match;
  }
}
