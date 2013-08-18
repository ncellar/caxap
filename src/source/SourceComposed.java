package source;

import parser.Match;

/**
 * A virtual source, used to back matches that do not belong to a "regular"
 * source. Those are matches created by a user (usually in order to produce the
 * output of a macro); or matches that result from macro-expansion.
 *
 * @see {@link compiler.util.MatchCreator}
 */
public class SourceComposed extends SourceString
{
  /****************************************************************************/
  public SourceComposed(Match... matchs)
  {
    super(strFromMatches(matchs));
  }

  /****************************************************************************/
  public static String strFromMatches(Match[] matches)
  {
    StringBuilder builder = new StringBuilder();

    for (Match match : matches) {
      builder.append(match.originalString());
    }

    return builder.toString();
  }

  /****************************************************************************/
  @Override public String where(int p)
  {
    return super.where(p) + " (in composed source)";
  }

  /****************************************************************************/
  @Override public String toString()
  {
    return "composed source (" + Integer.toHexString(text.hashCode()) + ")";
  }
}
