package grammar;

import grammar.Expression.And;
import grammar.Expression.Any;
import grammar.Expression.CharClass;
import grammar.Expression.Choice;
import grammar.Expression.Not;
import grammar.Expression.Optional;
import grammar.Expression.Plus;
import grammar.Expression.Range;
import grammar.Expression.Reference;
import grammar.Expression.Rule;
import grammar.Expression.Sequence;
import grammar.Expression.Star;
import grammar.Expression.StringLiteral;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * A Domain Specific Language (DSL) to clearly express PEG grammar rules.
 *
 * Some of the construct directly map to a subclass of Expression. In those
 * case, report to the documentation of the appropriate class. If you are
 * familiar with PEG, it should all be pretty self-evident.
 */
public class GrammarDSL
{
  /****************************************************************************/
  public static Choice choice(Expression... exprs)
  {
    return new Choice(Arrays.asList(exprs));
  }

  /****************************************************************************/
  public static Sequence seq(Expression... exprs)
  {
    return new Sequence(Arrays.asList(exprs));
  }

  /****************************************************************************/
  public static And and(Expression expr)
  {
    return new And(expr);
  }

  /****************************************************************************/
  public static And and(Expression... exprs)
  {
    return new And(new Sequence(Arrays.asList(exprs)));
  }

  /****************************************************************************/
  public static Not not(Expression expr)
  {
    return new Not(expr);
  }

  /****************************************************************************/
  public static Not not(Expression... exprs)
  {
    return new Not(new Sequence(Arrays.asList(exprs)));
  }

  /****************************************************************************/
  public static Plus plus(Expression expr)
  {
    return new Plus(expr);
  }

  /****************************************************************************/
  public static Plus plus(Expression... exprs)
  {
    return new Plus(new Sequence(Arrays.asList(exprs)));
  }

  /****************************************************************************/
  public static Star star(Expression expr)
  {
    return new Star(expr);
  }

  /****************************************************************************/
  public static Star star(Expression... exprs)
  {
    return new Star(new Sequence(Arrays.asList(exprs)));
  }

  /****************************************************************************/
  public static Optional opt(Expression expr)
  {
    return new Optional(expr);
  }

  /****************************************************************************/
  public static Optional opt(Expression... exprs)
  {
    return new Optional(new Sequence(Arrays.asList(exprs)));
  }

  /****************************************************************************/
  public static Reference ref(String name)
  {
    return new Reference(name);
  }

  /****************************************************************************/
  public static StringLiteral str(String str)
  {
    return new StringLiteral(str);
  }

  /****************************************************************************/
  public static CharClass chars(String str)
  {
    return new CharClass(str, false);
  }

  /****************************************************************************/
  public static CharClass notChars(String str)
  {
    return new CharClass(str, true);
  }

  /****************************************************************************/
  public static Range range(char start, char end)
  {
    return new Range(start, end, false);
  }

    /****************************************************************************/
  public static Range notRange(char start, char end)
  {
    return new Range(start, end, true);
  }

  /*****************************************************************************
   * Iterates iterateThis until untilThis is encountered.
   */
  public static Sequence until(Expression iterateThis, Expression untilThis)
  {
    return seq(star(not(untilThis), iterateThis), untilThis);
  }

  /*****************************************************************************
   * Iterates iterateThis until untilThis is encountered. iterateThis must
   * appear at least one time.
   */
  public static Sequence untilOnce(Expression iterateThis, Expression untilThis)
  {
    return seq(star(not(untilThis), iterateThis), untilThis);
  }

  /*****************************************************************************
   * A non-empty sequence of expr, separated by sep.
   */
  public static Sequence list(Expression sep, Expression expr)
  {
    return seq(expr, star(sep, expr));
  }

  /*****************************************************************************
   * A rule to express direct (within the same rule) left recursion. The first
   * parameter is the base (non-left-recursive) expression. The second parameter
   * is an implicitly left-recursive alternative. Said otherwise, the second
   * parameter represents all possible suffix that can be applied to the base
   * expression.
   *
   * Currently, the left recursion is translated to right recursion. The
   * construct is at the very least useful to clearly mark uses of
   * left-recursion in the code for what they are.
   */
  public static Sequence lrecur(Expression base, Expression suffix)
  {
    return seq(base, star(suffix));
  }

  /*****************************************************************************
   * Direct left recursion, with at least one suffix.
   */
  public static Sequence lrecurPlus(Expression base, Expression suffix)
  {
    return seq(base, plus(suffix));
  }

  /*****************************************************************************
   * Apply to an item "repeat" that is inside a repeating construct to ensure
   * that the last repeated item does not match "end".
   *
   * e.g. star(doesntEndWith(c, choice(a, b, c))) will match any sequence of a,
   * b or c that does not end with a c.
   *
   * Usually "repeat" is a choice from which we want to exclude an alternative.
   * In those cases, make sure that "end" is not a potential prefix of some
   * alternatives you don't want to exclude.
   */
  public static final Expression doesntEndWith(Expression end, Expression repeat)
  {
    // If this didn't to work in lRecur, it would have been:
    // return star/plus(star(end), repeat);
    return seq(not(end, not(repeat)), repeat);
  }

  /*****************************************************************************
   * Apply to an item "repeat" that is inside a repeating construct to ensure
   * that the last repeated item does match "end".
   *
   * e.g. star(endsWith(c, choice(a, b, c))) will match any sequence of a, b or
   * c that does end with a c.
   *
   * Same remark as in {@link #doesntEndWith}.
   */
  public static final Expression endsWith(Expression end, Expression repeat)
  {
    // If this didn't to work in lRecur, it would have been:
    // return star/plus(until(repeat, end));
    return seq(not(not(end), repeat, not(repeat)), repeat);
  }

  /*****************************************************************************
   * Marks the expression as atomic.
   * @see Expression
   */
  public static final Expression atomic(Expression expr)
  {
    expr.atomic = true;
    return expr;
  }

  /*****************************************************************************
   * Trick: If you want to make an alias for a rule name, just define a new rule
   * like below. But be aware that the old rule will still appear in the match
   * tree.
   *
   * Rule rule2 = rule(newName, rule1);
   */
  public static final Rule rule(String name, Expression... exprs)
  {
    return new Rule(name, new ArrayList<Expression>(Arrays.asList(exprs)));
  }

  /*****************************************************************************
   * The point of omitting the rule name (by setting it to null) is to affect it
   * later (in grammar.Grammar, through reflection since the field is final), to
   * the name of the class field containing the returned Rule. So always affect
   * the result of this method to a public class field.
   */
  public static final Rule rule(Expression... exprs)
  {
    return new Rule(null, new ArrayList<Expression>(Arrays.asList(exprs)));
  }

  /*****************************************************************************
   * Makes a new rule with a single alternative which is a sequence of the
   * passed expressions.
   */
  public static final Rule rule_seq(String name, Expression... exprs)
  {
    return rule(name, seq(exprs));
  }

  /*****************************************************************************
   * Like rule_seq(String, Expression...), but omitting the rule name for the
   * same reason as rule(Expression...).
   */
  public static final Rule rule_seq(Expression... exprs)
  {
    /** Same remark as rule(Expression...) */
    return rule(seq(exprs));
  }

  /****************************************************************************/
  public static final Any any = Any.GET;

  /****************************************************************************/
  public static final Expression endOfInput = new Not(any);
}
