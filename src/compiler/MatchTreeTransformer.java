package compiler;

import static compiler.util.MatchCreator.new_match;

import java.util.ArrayList;
import java.util.List;

import parser.Match;

/**
 * A match tree is an immutable structure. This class holds the logic to apply a
 * recursive transformation on a match tree, and rebuild only the parts of the
 * tree which have changed. The subtrees that have not changed are shared
 * between the new and the old tree.
 */
public abstract class MatchTreeTransformer
{
  /****************************************************************************/
  public abstract Match transform(Match match);

  /*****************************************************************************
   * Usually called from {@link #transform(Match)} to enable recursion.
   */
  protected Match transformChilds(Match match)
  {
    List<Match> childs1 = match.children();
    List<Match> childs2 = null;

    for (int i = 0 ; i < childs1.size() ; ++i)
    {
      Match original  = childs1.get(i);
      Match expansion = transform(original);

      if (original != expansion) {
        childs2 = (childs2 != null) ? childs2 : new ArrayList<>(match.children());
        childs2.set(i, expansion);
      }
    }

    return childs2 != null
      ? new_match(match.expr, childs2)
      : match;
  }
}
