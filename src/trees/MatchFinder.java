package trees;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import parser.Match;
import util.ArrayIterator;

import static util.ArrayUtils.arr;

/**
 * The class implements the ability to find sub-match(es) of a match matching
 * certain criteria.
 */
public class MatchFinder
{
  /*****************************************************************************
   * Returns an array containing zero or more sub-matches of $root that match
   * the specification $spec.
   *
   * If $inclusive is true, those matches should be situated "between" a
   * sequence of matches that satisfy the specifications in $before; and a
   * sequence of matches that satisfy the specifications in $after.
   *
   * If $inclusive is false, the matches should be "outside", i.e. not between
   * or part of $before and $after.
   *
   * A Match matched by $spec cannot be a sub-match of any Match used to satisfy
   * the requirements expressed by $before and $after.
   *
   * The matches used to satisfy the specifications in $before and $after don't
   * need to be contiguous, but they need to appear in the same order as in the
   * specification array. If a specification is matched by a Match, no other
   * specification in the same array may be matched by that Match or by one of
   * its sub-matches.
   *
   * The $finder indicates which match(es) of the remaining interval we want to
   * gather.
   */
  public static Match[] find(
    final Match       root,
    final MatchSpec   spec,
    final Finder      finder,
    final MatchSpec[] before,
    final MatchSpec[] after,
    boolean inclusive)
  {
    List<Match> left = trace(root, before, true, !inclusive);
    if (left == null) { return Match.EMPTY; }

    List<Match> right = trace(root, after, false, !inclusive);
    if (right == null) { return Match.EMPTY; }

    BoundedMatchIterator iter = new BoundedMatchIterator(
      root, left, right, finder.leftToRight, inclusive);

    return finder.find(spec, iter);
  }

  /*****************************************************************************
   * Returns a trace (see {@link MatchIterator#trace()}) from $root to the
   * Match matching the last specification (see below) from $Lspecs. Returns an
   * empty trace if $Lspecs is empty. If not all specifications in $Lspecs can
   * be satisfied, returns null instead.
   *
   * If $firstSpec is true, returns the trace for the element matching the first
   * specification; else use the last specification instead. "first" and "last" are
   * relative to the order indicated by $leftToRight.
   */
  private static List<Match> trace(Match root, MatchSpec[] Lspecs,
    boolean leftToRight, boolean firstSpec)
  {
    ArrayIterator<MatchSpec> specs = new ArrayIterator<>(Lspecs, !leftToRight);
    MatchIterator iter = new MatchIterator(root, leftToRight);

    if (!specs.hasNext()) {
      return Collections.emptyList();
    }

    List<Match> trace = null;
    specs.forward();

    while (iter.hasNext() && !specs.pastEdge())
    {
      if (specs.current().matches(iter.next()))
      {
        if (firstSpec && trace == null) {
          trace = iter.trace();
        }

        specs.forward();
      }
    }

    return specs.pastEdge()
      ? trace != null ? trace : iter.trace()
      : null;
  }

  /****************************************************************************/
  public enum Finder
  {
    /**
     * A finder expresses a strategy to gather matches that are between $before
     * and $after, given a specification.
     */

    /**************************************************************************/
    FIRST (true),
    /**************************************************************************/
    LAST  (false),
    /**************************************************************************/
    ALL   (true)
    {
      @Override Match[] find(MatchSpec spec, BoundedMatchIterator iter)
      {
        List<Match> matches = new ArrayList<>();
        for (Match m : iter)
        {
          if (spec.matches(m)) {
            matches.add(m);
            iter.skipChilds();
          }
        }
        return matches.toArray(Match.EMPTY);
      }
    };

    /**************************************************************************/
    final boolean leftToRight;

    /**************************************************************************/
    Finder(boolean leftToRight)
    {
      this.leftToRight = leftToRight;
    }

    /***************************************************************************
     * Implements the FIRST or LAST, strategy, depending on the order of
     * iteration; Finder#leftToRight should be used to select the correct
     * ordering.
     */
    Match[] find(MatchSpec spec, BoundedMatchIterator iter)
    {
      for (Match m : iter)
      {
        if (spec.matches(m)) {
          return arr(m);
        }
      }

      return Match.EMPTY;
    }
  }
}
