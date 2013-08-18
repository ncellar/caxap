package trees;

import java.util.Arrays;

import driver.Hints;

import grammar.Expression;
import grammar.Expression.Rule;
import parser.Match;
import source.Source;

import static util.StringUtils.escape;
import static util.StringUtils.join;

/**
 * A specification that can match Match objects (yes, this is confusing).
 */
public abstract class MatchSpec
{
  /****************************************************************************/
  public abstract boolean matches(Match match);

  /*****************************************************************************
   * Matches Match objects whose source is a given string.
   */
  public static class StringSpec extends MatchSpec
  {
    private final String str;

    public StringSpec(String str)
    {
      this.str = str;
    }

    @Override public boolean matches(Match match)
    {
      return match.string().equals(str);
    }

    @Override public String toString()
    {
      return "match string \"" + escape(str) + "\"";
    }
  }

  /****************************************************************************/
  public static StringSpec str(String str)
  {
    return new StringSpec(str);
  }

  /*****************************************************************************
   * Matches Match objects matching a given grammar rule.
   */
  public static class RuleSpec extends MatchSpec
  {
    private final String rule;

    public RuleSpec(String rule)
    {
      this.rule = rule;
    }

    public RuleSpec(Rule rule)
    {
      this.rule = rule.name;
    }

    @Override public boolean matches(Match match)
    {
      return match.expr instanceof Rule
        && ((Rule)match.expr).name.equals(rule);
    }

    @Override public String toString()
    {
      return "match rule \"" + rule + "\"";
    }
  }

  /****************************************************************************/
  public static RuleSpec rule(String rule)
  {
    return new RuleSpec(rule);
  }

  /****************************************************************************/
  public static RuleSpec rule(Rule rule)
  {
    return new RuleSpec(rule);
  }

  /*****************************************************************************
   * Matches Match objects that are capture with the given name. You usually
   * want to use Match#get() instead of this.
   */
  public static class CaptureSpec extends MatchSpec
  {
    public final String captureName;

    CaptureSpec(String captureName)
    {
      this.captureName = captureName;
    }

    @Override public boolean matches(Match match)
    {
      return match.expr instanceof Expression.Capture
        && ((Expression.Capture)match.expr).captureName.equals(captureName);
    }

    @Override public String toString()
    {
      return "match capture \"" + captureName + "\"";
    }
  }

  /****************************************************************************/
  public static CaptureSpec capture(String rule)
  {
    return new CaptureSpec(rule);
  }

  /*****************************************************************************
   * Matches Match objects matching a given expression. The expression can't be
   * a rule, because rules don't have a canonical representation (see
   * ExpressionTreeCleaner for specifics). Use RuleSpec to match rules.
   */
  public static class ExprSpec extends MatchSpec
  {
    private final Expression expr;

    public ExprSpec(Expression expr)
    {
      this.expr = expr;
    }

    @Override public boolean matches(Match match)
    {
      /* Calling the cleaner now enables the spec to be used with multiple
       * grammars, although it isn't the case now. It also avoid having to pass
       * the grammar to the spec constructor. */

      return match.expr == match.expr.grammar.clean(expr);
    }

    @Override public String toString()
    {
      return "match expression [" + expr + "]";
    }
  }

  /****************************************************************************/
  public static ExprSpec expr(Expression expr)
  {
    return new ExprSpec(expr);
  }

  /*****************************************************************************
   * Matches Match objects matched by one of its child specifications.
   */
  public static class OrSpec extends MatchSpec
  {
    private final MatchSpec[] specs;

    public OrSpec(MatchSpec... specs)
    {
      this.specs = specs;
    }

    @Override public boolean matches(Match match)
    {
      for (MatchSpec spec : specs) {
        if (spec.matches(match)) { return true; }
      }
      return false;
    }

    @Override public String toString()
    {
      return join(Arrays.asList(specs), " or ");
    }
  }

  /****************************************************************************/
  public static OrSpec or(MatchSpec... specs)
  {
    return new OrSpec(specs);
  }

  /*****************************************************************************
   * Matches Match objects matched by all of its child specifications.
   */
  public static class AndSpec extends MatchSpec
  {
    private final MatchSpec[] specs;

    public AndSpec(MatchSpec... specs)
    {
      this.specs = specs;
    }

    @Override public boolean matches(Match match)
    {
      for (MatchSpec spec : specs) {
        if (!spec.matches(match)) { return false; }
      }
      return true;
    }

    @Override public String toString()
    {
      return join(Arrays.asList(specs), " and ");
    }
  }

  /****************************************************************************/
  public static AndSpec and(MatchSpec... specs)
  {
    return new AndSpec(specs);
  }

  /*****************************************************************************
   * Matches Match objects not matched by its child specification.
   */
  public static class NotSpec extends MatchSpec
  {
    private final MatchSpec spec;

    public NotSpec(MatchSpec spec)
    {
      this.spec = spec;
    }

    @Override public boolean matches(Match match)
    {
      return !spec.matches(match);
    }

    @Override public String toString()
    {
      return "not " + spec.toString();
    }
  }

  /****************************************************************************/
  public static NotSpec not(MatchSpec spec)
  {
    return new NotSpec(spec);
  }

  /*****************************************************************************
   * Matches Match objects that have been matched at the given position.
   */
  public static class PositionSpec extends MatchSpec
  {
    private final int position;

    public PositionSpec(int position)
    {
      this.position = position;
    }

    @Override public boolean matches(Match match)
    {
      return match.begin == position;
    }

    @Override public String toString()
    {
      Source source = Hints.get().source();

      return source == null
        ? "at position " + position
        : source.where(position);
    }
  }

  /****************************************************************************/
  public static PositionSpec at(int position)
  {
    return new PositionSpec(position);
  }

  /*****************************************************************************
   * Matches Match objects that have a sub-match matched by the child
   * specification.
   */
  public static class HasSpec extends MatchSpec
  {
    private final MatchSpec spec;

    public HasSpec(MatchSpec spec)
    {
      this.spec = spec;
    }

    @Override public boolean matches(Match match)
    {
      return match.has(spec);
    }

    @Override public String toString()
    {
      return "has submatch matching (" + spec.toString() + ")";
    }
  }

  /****************************************************************************/
  public static HasSpec has(MatchSpec spec)
  {
    return new HasSpec(spec);
  }

  /*****************************************************************************
   * Matches all expressions.
   */
  public static class AnySpec extends MatchSpec
  {
    @Override public boolean matches(Match match)
    {
      return true;
    }

    @Override public String toString()
    {
      return "match anything";
    }
  }

  /****************************************************************************/
  public static final AnySpec anySpec = new AnySpec();

  /*****************************************************************************
   * Returns a MatchSpec matching Match object having a sub-match with given
   * expression at the given position.
   */
  public static MatchSpec hasExprAtPos(Expression expr, int position)
  {
    return has(and(at(position), expr(expr)));
  }

  /*****************************************************************************
   * Returns a MatchSpec matching Match object having a sub-match similar to
   * the one supplied at the given position.
   */
  public static MatchSpec hasMatchAtPos(Match match, int position)
  {
    return has(and(at(position), expr(match.expr), str(match.string())));
  }
}
