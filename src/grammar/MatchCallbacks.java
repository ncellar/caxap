package grammar;

import parser.Match;

/**
 * A set of callbacks invoked at different times during the lifetime of a Match.
 *
 * During the parse, parseDo() is called bottom-up: the method is first invoked
 * with the leaf matches as parameter, then with their parents, etc...
 *
 * The only purpose of parseDo() is to modify the way the source gets parsed. It
 * is notably the mechanism by which new macros are registered. The problem is
 * that the presence of state that modifies the way the source is parsed breaks
 * memoization. To avoid this issue, parseDo() should only be defined for rules
 * that will make it to the final match tree once parsed (unless an parse error
 * is encountered).
 *
 * Callbacks that allow to replace a match after parsing and after macro expansion
 * is also present, both in bottom-up and top-down flavors.
 */
public class MatchCallbacks
{
  /****************************************************************************/
  public static MatchCallbacks DEFAULT = new MatchCallbacks();

  /*****************************************************************************
   * Called after a match was successfully constructed during the parse.
   */
  public void parseDo(Match match) {}

  /*****************************************************************************
   * Called after parsing but before macro expansion. Called before the calling
   * {@link #postParseTopDown(Match)} and {@link #postParseBottomUp(Match)} for
   * the sub-matches.
   */
  public Match postParseTopDown(Match match) { return match; }

  /*****************************************************************************
   * Called after parsing but before macro expansion. Called after the calling
   * {@link #postParseTopDown(Match)} and {@link #postParseBottomUp(Match)} for
   * the sub-matches.
   */
  public Match postParseBottomUp(Match match) { return match; }

    /*****************************************************************************
   * Called after macro expansion. Called before the calling
   * {@link #postParseTopDown(Match)} and {@link #postParseBottomUp(Match)} for
   * the sub-matches.
   */
  public Match postExpansionTopDown(Match match) { return match; }

  /*****************************************************************************
   * Called after macro expansion. Called after the calling
   * {@link #postParseTopDown(Match)} and {@link #postParseBottomUp(Match)} for
   * the sub-matches.
   */
  public Match postExpansionBottomUp(Match match) { return match; }
}
