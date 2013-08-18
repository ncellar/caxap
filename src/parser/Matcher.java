package parser;

import source.Source;
import source.SourceStream;
import grammar.Expression;
import grammar.ExpressionVisitor;

/**
 * This class contains the core parsing logic. It parses input from the
 * associated source stream. We use memoization to improve performance.
 *
 * The aim is to build a match tree that indicates how the input matches a given
 * expression and its sub-expressions.
 *
 * TODO explain call stack path
 */
public class Matcher implements ExpressionVisitor
{
  /****************************************************************************/
  private final Source source;

  /****************************************************************************/
  protected final SourceStream stream;

  /****************************************************************************/
  protected final Memo memo;

  /****************************************************************************/
  public Matcher(Source source)
  {
    this.source = source;
    this.stream = new SourceStream(source);
    this.memo   = new LimitedMemo(this);
  }

  /*****************************************************************************
   * true when parsing an atomic expression or one if its sub-expressions.
   */
  private boolean atomic = false;

  /*****************************************************************************
   * The parse for the expression currently being parsed.
   */
  private ParseData data = null;

  /****************************************************************************/
  public Source source()
  {
    return source;
  }

  /****************************************************************************/
  public Memo memo()
  {
    return memo;
  }

  /*****************************************************************************
   * Returns the result of the last call to {@link Matcher#matches(Expression)}.
   */
  public boolean succeeded()
  {
    return data.succeeded;
  }

  /*****************************************************************************
   * The match tree resulting of a successful parse. Call only if the last call
   * to {@link Matcher#matches(Expression)} returned true.
   */
  public Match match()
  {
    return data.match;
  }

  /*****************************************************************************
   * The errors resulting from an unsuccessful parse. Call only if the last call
   * to {@link Matcher#matches(Expression)} returned false.
   */
  public ParseErrors errors()
  {
    return data.errors;
  }

  /*****************************************************************************
   * Parse the source stream using expr as root expression. Returns true if the
   * parse succeeded. If it succeeded, the input position is moved past the
   * matched input.
   */
  public boolean matches(Expression expr)
  {
    data = new ParseData(expr, stream, atomic);
    expr.accept(this);
    return data.succeeded;
  }

  /*****************************************************************************
   * Resets the input position of the matcher. Does not empty the
   * memoization table.
   */
  public void reset()
  {
    stream.reset();
    atomic = false;
    data   = null;
  }

  /*****************************************************************************
   * Dirty hack to allow parsing files where memoization would overflow memory.
   */
  void accept(Expression expr)
  {
    boolean ok = false;
    while (!ok) try {
      expr.accept(this);
      ok = true;
    }
    catch (OutOfMemoryError e) {
      System.out.println("Warning: heap exhausted, memoization table emptied.");
      stream.position = data.begin;
      memo.clear();
      System.gc();
    }
  }

  /****************************************************************************/
  ParseData parse(Expression expr)
  {
    boolean   _atomic = atomic;
    ParseData _data   = data;

    try {
      atomic = atomic || expr.atomic;
      data   = new ParseData(expr, stream, expr.atomic);

      accept(expr);
      // expr.accept(this); // non-hack version

      return data;
    }
    finally {
      atomic = _atomic;
      data   = _data;
    }
  }

  /****************************************************************************/
  boolean visitChild(Expression child)
  {
    ParseData childData = memo.get(stream.position, child);

    stream.position = childData.end; // necessary because of memoization
    data.merge(childData);

    return childData.succeeded;
  }

  /****************************************************************************/
  void succeed()
  {
    data.succeed(stream.position);
  }

  //============================================================================
  // EXPRESSION VISITOR
  //============================================================================

  /****************************************************************************/
  public void visitChoice(Expression expr)
  {
    for (Expression e : expr.children()) {
      if (visitChild(e)) {
        succeed();
        return;
      }
    }
    data.fail();
  }

  /****************************************************************************/
  @Override public void visit(Expression.Choice expr)
  {
    visitChoice(expr);
  }

  /****************************************************************************/
  @Override public void visit(Expression.Rule expr)
  {
    visitChoice(expr);
  }

  /****************************************************************************/
  @Override public void visit(Expression.Sequence expr)
  {
    for (Expression e : expr.children()) {
      if (!visitChild(e)) {
        data.fail();
        return;
      }
    }
    succeed();
  }

  /****************************************************************************/
  @Override public void visit(Expression.And expr)
  {
    if (visitChild(expr.child())) {
      data.succeed(data.begin);
    }
    else {
      data.fail();
    }
  }

  /****************************************************************************/
  @Override public void visit(Expression.Not expr)
  {
    if (visitChild(expr.child())) {
      data.fail();
    }
    else {
      succeed();
    }
  }

  /****************************************************************************/
  @Override public void visit(Expression.Plus expr)
  {
    if (!visitChild(expr.child())) {
      data.fail();
    }
    else {
      while (visitChild(expr.child())) ;
      succeed();
    }
  }

  /****************************************************************************/
  @Override public void visit(Expression.Star expr)
  {
    while (visitChild(expr.child())) ;
    succeed();
  }

  /****************************************************************************/
  @Override public void visit(Expression.Optional expr)
  {
    visitChild(expr.child());
    succeed();
  }

  /****************************************************************************/
  @Override public void visit(Expression.StringLiteral expr)
  {
    String srcString = stream.get(expr.string.length());

    if (!stream.isPastEnd() && srcString.equals(expr.string)) {
      succeed();
    }
    else {
      data.fail();
    }
  }

  /****************************************************************************/
  @Override public void visit(Expression.CharClass expr)
  {
    char c = stream.get();
    int index = expr.chars.indexOf(c);

    if (!stream.isPastEnd()
      && (!expr.negated && index != -1
        || expr.negated && index == -1)) {
      succeed();
    }
    else {
      data.fail();
    }
  }

  /****************************************************************************/
  @Override public void visit(Expression.Range expr)
  {
    char c = stream.get();

    if (!stream.isPastEnd()
      && (!expr.negated && (expr.first <= c && c <= expr.last)
        || expr.negated && (c < expr.first || expr.last < c))
    ) {
      succeed();
    }
    else {
      data.fail();
    }
  }

  /****************************************************************************/
  @Override public void visit(Expression.Any expr)
  {
    stream.get();

    if (!stream.isPastEnd()) {
      succeed();
    }
    else {
      data.fail();
    }
  }

  /*****************************************************************************
   * Delegate to the unique child.
   */
  @Override public void visit(Expression.Capture expr)
  {
    if (visitChild(expr.child())) {
      succeed();
    }
    else {
      data.fail();
    }
  }
}
