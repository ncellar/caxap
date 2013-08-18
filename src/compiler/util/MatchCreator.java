package compiler.util;

import static grammar.GrammarDSL.rule;
import static grammar.GrammarDSL.seq;
import static util.ListUtils.list0;
import static util.ListUtils.mlist;

import java.util.List;

import driver.Context;
import grammar.Expression;
import parser.Match;
import source.Source;
import source.SourceComposed;

/**
 * Class that let's user compose matches. The created match does not correspond
 * to an existing source.
 */
public class MatchCreator
{
  /*****************************************************************************
   * Build a new match for the rule with the give name, with the given childs.
   */
  public static Match new_match(String ruleName, Match... childs)
  {
    Expression[] exprs = new Expression[childs.length];

    for (int i = 0 ; i < childs.length ; ++i) {
      exprs[i] = childs[i].expr;
    }

    Source source   = new SourceComposed(childs);

    List<Match> subMatches =
      childs.length == 0
        ? list0(childs) :
      childs.length == 1
        ? mlist(childs[0])
        : mlist(buildMatch(seq(exprs), source, mlist(childs)));

    return buildMatch(rule(ruleName, seq(exprs)), source, subMatches);
  }

  /*****************************************************************************
   * Builds a new match from a given expression. This version is useful when
   * making a new match from an old one. The childs of the new match will be
   * different from the old, because of macro expansion. Therefore a new source
   * for the match must be created to preserve correctness (especially for
   * {@link Match#string()}).
   */
  public static Match new_match(Expression expr, List<Match> childs)
  {
    Source source = new SourceComposed(childs.toArray(new Match[0]));
    return buildMatch(expr, source, childs);
  }

  /****************************************************************************/
  private static Match buildMatch(Expression expr, Source source,
    List<Match> childs)
  {
    Expression clean = Context.get().grammar().clean(expr);
    return new Match(clean, source, 0, source.end(), childs);
  }
}
