package grammar.java;

import static trees.MatchSpec.anySpec;
import static trees.MatchSpec.or;
import static trees.MatchSpec.rule;
import static trees.MatchSpec.str;
import static util.StringUtils.unescape;

import java.util.Arrays;
import java.util.List;
import java.util.Stack;

import driver.Context;
import grammar.Expression;
import grammar.GrammarDSL;
import grammar.MatchCallbacks;
import parser.Match;
import trees.MatchSpec;

/*******************************************************************************
 * The base class for callbacks made upon encountering a parsing expression.
 *
 * All the callbacks for the different kind of expressions notations are meant
 * to share a single expression stack ({@link Context#expressionStack}). When
 * the callback for an expression notation is invoked, it will pop the
 * expression specified by its children from the stack, then push the resulting
 * expression on the stack.
 *
 * How spacing works: unless a trailing dash ("-") is present, optional spacing
 * is inserted after all string literals. References to Java non-literals retain
 * any associated whitespace they might have. The rules "spacing" (optional
 * whitespace) and "fspacing" (mandatory whitespace) can also be used.
 * Whitespace inside string literals is matched literally. Whitespace between
 * expression and operators is non-significant.
 */
class CallbacksExpression extends MatchCallbacks
{
  /****************************************************************************/
  Stack<Expression> stack()
  {
    return Context.get().expressionStack;
  }

  /*****************************************************************************
   * Pick the childs that are placed in reverse order on the stack.
   */
  List<Expression> getChilds(int n)
  {
    Expression[] childs = new Expression[n];

    for (int i = n - 1 ; i >= 0 ; --i) {
      childs[i] = stack().pop();
    }

    return Arrays.asList(childs);
  }

  /****************************************************************************/
  public static String unescapeChars(String chars)
  {
    return unescape(chars.replaceAll("\\\\]", "]"));
  }
}
////////////////////////////////////////////////////////////////////////////////

/******************************************************************************/
class ChoiceCallbacks extends CallbacksExpression
{
  @Override public void parseDo(Match match)
  {
    int n = match.all(rule("sequenceParsingExpression")).length;
    stack().push(new Expression.Choice(getChilds(n)));
  }
}

/******************************************************************************/
class SequenceCallbacks extends CallbacksExpression
{
  @Override public void parseDo(Match match)
  {
    int n = match.all(rule("binaryParsingExpression")).length;
    if (n == 1) { return; }

    stack().push(new Expression.Sequence(getChilds(n)));
  }
}

/******************************************************************************/
class AndCallbacks extends CallbacksExpression
{
  @Override public void parseDo(Match match)
  {
    stack().push(new Expression.And(stack().pop()));
  }
}

/******************************************************************************/
class NotCallbacks extends CallbacksExpression
{
  @Override public void parseDo(Match match)
  {
    stack().push(new Expression.Not(stack().pop()));
  }
}

/******************************************************************************/
class CaptureCallbacks extends CallbacksExpression
{
  @Override public void parseDo(Match match)
  {
    Match captureNameM = match.firstBeforeFirst(rule("identifier"), str(":"));

    String captureName = captureNameM != null
      ? captureNameM.string()
      : match.first(rule("referenceParsingExpression")).string();

    Context.get().captureNames.add(captureName);
    stack().push(new Expression.Capture(captureName, stack().pop()));
  }
}

/******************************************************************************/
class UntilCallbacks extends CallbacksExpression
{
  @Override public void parseDo(Match match)
  {
    Expression two = stack().pop(), one = stack().pop();
    stack().push(GrammarDSL.until(one, two));
  }
}

/******************************************************************************/
class UntilOnceCallbacks extends CallbacksExpression
{
  @Override public void parseDo(Match match)
  {
    Expression two = stack().pop(), one = stack().pop();
    stack().push(GrammarDSL.untilOnce(one, two));
  }
}

/******************************************************************************/
class ListCallbacks extends CallbacksExpression
{
  @Override public void parseDo(Match match)
  {
    Expression two = stack().pop(), one = stack().pop();
    stack().push(GrammarDSL.list(two, one));
  }
}

/******************************************************************************/
class SuffixCallbacks extends CallbacksExpression
{
  @Override public void parseDo(Match match)
  {
    Expression expr = stack().pop();

    Match suffix = match.firstAfterFirst(
      or(rule("pegStar"), rule("pegPlus"), rule("qMark")),
      rule("primaryParsingExpression"));

    if (suffix != null)
    switch(suffix.expr.toString())
    {
    case "pegStar":
      expr = new Expression.Star(expr);
      break;
    case "pegPlus":
      expr = new Expression.Plus(expr);
      break;
    case "qMark":
      expr = new Expression.Optional(expr);
      break;
    }

    stack().push(expr);
  }
}

/******************************************************************************/
class AnyCallbacks extends CallbacksExpression
{
  @Override public void parseDo(Match match)
  {
    stack().push(Expression.Any.GET);
  }
}

/******************************************************************************/
class LiteralCallbacks extends CallbacksExpression
{
  @Override public void parseDo(Match match)
  {
    String withQuotes = match.first(rule("stringLiteral")).string();
    String noQuotes = withQuotes.substring(1, withQuotes.length() - 1);
    stack().push(new Expression.StringLiteral(unescape(noQuotes)));

    if (!match.has(MatchSpec.str("-"))) {
      stack().push(new Expression.Reference("spacing"));
      stack().push(new Expression.Sequence(getChilds(2)));
    }
  }
}

/******************************************************************************/
class ReferenceCallbacks extends CallbacksExpression
{
  @Override public void parseDo(Match match)
  {
    stack().push(new Expression.Reference(match.string()));
  }
}

/******************************************************************************/
class CharClassCallbacks extends CallbacksExpression
{
  @Override public void parseDo(Match match)
  {
    boolean negated = match.has(match.firstBeforeFirst(
      str("^"), rule("lSqBra")));

    String chars = match.firstAfterFirst(anySpec, rule("lSqBra")).string();
    chars = unescapeChars(chars);

    stack().push(new Expression.CharClass(chars, negated));
  }
}

/******************************************************************************/
class CharRangeCallbacks extends CallbacksExpression
{
  @Override public void parseDo(Match match)
  {
    boolean negated = match.has(match.first(str("^")));

    char start = unescapeChars(match.firstAfterFirst(
      rule("pegChar"), rule("lSqBra")).string()).charAt(0);

    char end = unescapeChars(match.lastBeforeFirst(
      rule("pegChar"), rule("rSqBra")).string()).charAt(0);

    stack().push(new Expression.Range(start, end, negated));
  }
}
