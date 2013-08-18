package parser;

import static trees.MatchFinder.find;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import grammar.Expression;
import grammar.Expression.Capture;
import grammar.Expression.Rule;
import source.Source;
import trees.MatchFinder.Finder;
import trees.MatchSpec;

/**
 * The parser constructs a match tree. Each node in the tree represents a match
 * between a parsing expression and a chunk of input.
 *
 * The match tree is what is usually called the parse tree. It is also sometimes
 * abusively called AST (Abstract Syntax Tree). This is no AST, because the tree
 * is not "abstract", it is closely tied to the way the syntax of the language
 * was specified in the grammar.
 *
 * When using the finders, remember that Not-expressions are atomic (the
 * sub-matches of their matches don't show up in the match tree), while
 * And-expressions are not.
 */
public class Match
{
  /*****************************************************************************
   * Empty Match array to be used as type witness or to signal the lack of
   * result in a find operation.
   */
  public static final Match[] EMPTY = new Match[0];

  /*****************************************************************************
   * Converts an array of objects to strings, using the {@link #string()} method
   * for any instance of {@link Match}, {@link Object#toString()} otherwise.
   */
  public static String[] strings(Object[] objects)
  {
    String[] out = new String[objects.length];

    for (int i = 0 ; i < objects.length ; ++i)
    {
      out[i] = objects[i] instanceof Match
        ? ((Match)objects[i]).string()
        : objects[i].toString();
    }

    return out;
  }

  /*****************************************************************************
   * Matched expression.
   */
  public final Expression expr;

  /*****************************************************************************
   * Input position of the begin of the match.
   */
  public final int begin;

  /*****************************************************************************
   * Input position of the end of the match.
   */
  public final int end;

  /*****************************************************************************
   * Matches of the sub-expressions of expr.
   */
  private final List<Match> children;

  /*****************************************************************************
   * The source whose input is matched.
   */
  public final Source source;

  /****************************************************************************/
  public Match(Expression expr, Source src, int begin, int end)
  {
    this(expr, src, begin, end, Collections.<Match>emptyList());
  }

  /****************************************************************************/
  public Match(Expression expr, Source source, int begin,int end,
    List<Match> children)
  {
    this.expr = expr;
    this.source = source;
    this.begin = begin;
    this.end = end;
    this.children = Collections.unmodifiableList(children);
  }

  /****************************************************************************/
  @Override public String toString()
  {
    return "match for [" + expr + "] " + source.where(begin)
      + " (len: " + length() + ")";
  }

  /****************************************************************************/
  public List<Match> children()
  {
    return children;
  }

  /****************************************************************************/
  public Match child()
  {
    return children.get(0);
  }

  /*****************************************************************************
   * Returns true if the match has no children. This happens if the match is
   * atomic or if the match is an optional expression (?, *) matching nothing.
   */
  public boolean empty()
  {
    return children.isEmpty();
  }

  /****************************************************************************/
  public int length()
  {
    return end - begin;
  }

  /*****************************************************************************
   * Returns a string describing the location of the match (the source and
   * the position in the source).
   */
  public String where()
  {
    return "in [" + source + "] at [" + source.where(begin) + "]";
  }

  /****************************************************************************/
  public Expression expression()
  {
    return expr;
  }

  /*****************************************************************************
   * Return the matched string, trimmed of leading and trailing whitespace, of
   * all comments, and with whitespace sequences compressed to a single space.
   */
  public String string()
  {
    return source.at(begin, end).trim();
  }

  /*****************************************************************************
   * Return the matched string, but do not trim or compress whitespace.
   */
  public String originalString()
  {
    return source.at(begin, end);
  }

  /*****************************************************************************
   * Indicates if this match is matched by the supplied specification.
   */
  public boolean is(MatchSpec spec)
  {
    return spec.matches(this);
  }

  /*****************************************************************************
   * Indicates if the match has a descendant matching the supplied
   * specification.
   */
  public boolean has(MatchSpec spec)
  {
    return first(spec) != null;
  }

  /*****************************************************************************
   * Indicates if a previous call to a finder method yielded a result (the
   * supplied argument should be the return value from such a call).
   */
  public boolean has(Match[] matchs)
  {
    return matchs.length > 0;
  }

   /*****************************************************************************
   * Indicates if a previous call to a finder method yielded a result (the
   * supplied argument should be the return value from such a call).
   */
  public boolean has(Match match)
  {
    return match != null;
  }

  /*****************************************************************************
   * Get all the captures with the given name in this match tree. The recursion
   * is cut off when a matching capture is encountered, or when a sub-rule is
   * encountered.
   *
   * If the match this method is called on is itself a capture with the given
   * name, it won't be returned.
   */
  public Match[] getCaptures(String captureName)
  {
    List<Match> out = new ArrayList<>();

    /* Do the first recursion step here, allows this method to be called
     * on rules. */
    for (Match m : children) {
      m.getCaptures(captureName, out);
    }

    return out.toArray(new Match[out.size()]);
  }

  /****************************************************************************/
  private void getCaptures(String captureName, List<Match> captures)
  {
    if (expr instanceof Capture
    && ((Expression.Capture)expr).captureName.equals(captureName))
    {
      captures.add(child());
    }
    else if (!(expr instanceof Rule))
    {
      for (Match m : children) {
        m.getCaptures(captureName, captures);
      }
    }
  }

  //============================================================================
  // FINDERS
  //============================================================================

  /*****************************************************************************
   * Used as default parameter to some methods. MSA for MatchSpec Array.
   */
  private static final MatchSpec[] EMPTY_MSA = new MatchSpec[0];

  /****************************************************************************/
  private static Match single(Match[] result)
  {
    return result.length == 1 ? result[0] : null;
  }

  /****************************************************************************/
  public Match first(MatchSpec spec)
  {
    return single(find(this, spec, Finder.FIRST, EMPTY_MSA, EMPTY_MSA, true));
  }

  /****************************************************************************/
  public Match firstAfterFirst(MatchSpec spec, MatchSpec... before)
  {
    return single(find(this, spec, Finder.FIRST, before, EMPTY_MSA, true));
  }

  /****************************************************************************/
  public Match firstAfterLast(MatchSpec spec, MatchSpec... before)
  {
    return single(find(this, spec, Finder.FIRST, EMPTY_MSA, before, false));
  }

  /****************************************************************************/
  public Match firstBeforeLast(MatchSpec spec, MatchSpec... after)
  {
    return single(find(this, spec, Finder.FIRST, EMPTY_MSA, after, true));
  }

  /****************************************************************************/
  public Match firstBeforeFirst(MatchSpec spec, MatchSpec... after)
  {
    return single(find(this, spec, Finder.FIRST, after, EMPTY_MSA, false));
  }

  /****************************************************************************/
  public Match firstBetween(
    MatchSpec spec, MatchSpec[] before, MatchSpec[] after)
  {
    return single(find(this, spec, Finder.FIRST, before, after, true));
  }

  /****************************************************************************/
  public Match firstOutside(
    MatchSpec spec, MatchSpec[] before, MatchSpec[] after)
  {
    return single(find(this, spec, Finder.FIRST, before, after, false));
  }

  /****************************************************************************/
  public Match last(MatchSpec spec)
  {
    return single(find(this, spec, Finder.LAST, EMPTY_MSA, EMPTY_MSA, true));
  }

  /****************************************************************************/
  public Match lastAfterFirst(MatchSpec spec, MatchSpec... before)
  {
    return single(find(this, spec, Finder.LAST, before, EMPTY_MSA, true));
  }

  /****************************************************************************/
  public Match lastAfterLast(MatchSpec spec, MatchSpec... before)
  {
    return single(find(this, spec, Finder.LAST, EMPTY_MSA, before, false));
  }

  /****************************************************************************/
  public Match lastBeforeLast(MatchSpec spec, MatchSpec... after)
  {
    return single(find(this, spec, Finder.LAST, EMPTY_MSA, after, true));
  }

  /****************************************************************************/
  public Match lastBeforeFirst(MatchSpec spec, MatchSpec... after)
  {
    return single(find(this, spec, Finder.LAST, after, EMPTY_MSA, false));
  }

  /****************************************************************************/
  public Match lastBetween(
    MatchSpec spec, MatchSpec[] before, MatchSpec[] after)
  {
    return single(find(this, spec, Finder.LAST, before, after, true));
  }

  /****************************************************************************/
  public Match lastOutside(
    MatchSpec spec, MatchSpec[] before, MatchSpec[] after)
  {
    return single(find(this, spec, Finder.LAST, before, after, false));
  }

  /****************************************************************************/
  public Match[] all(MatchSpec spec)
  {
    return find(this, spec, Finder.ALL, EMPTY_MSA, EMPTY_MSA, true);
  }

  /****************************************************************************/
  public Match[] allAfterFirst(MatchSpec spec, MatchSpec... before)
  {
    return find(this, spec, Finder.ALL, before, EMPTY_MSA, true);
  }

  /****************************************************************************/
  public Match[] allAfterLast(MatchSpec spec, MatchSpec... before)
  {
    return find(this, spec, Finder.ALL, EMPTY_MSA, before, true);
  }

  /****************************************************************************/
  public Match[] allBeforeLast(MatchSpec spec, MatchSpec... after)
  {
    return find(this, spec, Finder.ALL, EMPTY_MSA, after, true);
  }

  /****************************************************************************/
  public Match[] allBeforeFirst(MatchSpec spec, MatchSpec... after)
  {
    return find(this, spec, Finder.ALL, after, EMPTY_MSA, false);
  }

  /****************************************************************************/
  public Match[] allBetween(
    MatchSpec spec, MatchSpec[] before, MatchSpec[] after)
  {
    return find(this, spec, Finder.ALL, before, after, true);
  }

  /****************************************************************************/
  public Match[] allOutside(
    MatchSpec spec, MatchSpec[] before, MatchSpec[] after)
  {
    return find(this, spec, Finder.ALL, before, after, false);
  }
}
