package grammar;

import grammar.Expression.And;
import grammar.Expression.Any;
import grammar.Expression.Capture;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import util.StringUtils;

/**
 * Performs a pass over the expression tree that resolves reference expressions
 * (turning our tree into a graph), computes the textual representations of
 * expressions, and compacts the graph so as to avoid duplicate expressions. It
 * also sets a reference to the grammar for each expression.
 *
 * Obtaining a graph without duplicate expressions serves to make memoization
 * slightly more efficient.
 *
 * Despite the fact that the expression graph can contain loops, the textual
 * representation of an expression is always bounded due to the use of rule
 * names whenever possible.
 *
 * The cleaner always returns an expression of the same type it receives,
 * excepted when the exception is of type Reference in which case it returns an
 * expression of type Rule.
 */
public class ExpressionTreeCleaner implements ExpressionVisitor
{
  /*****************************************************************************
   * The grammar from which the expression tree originates.
   */
  private final Grammar grammar;

  /*****************************************************************************
   * Unique ID to assign to the next rule.
   */
  private int ruleCounter = 0;

  /*****************************************************************************
   * A map from textual representation of expressions to the canonical expression
   * for that representation. This is used in order to remove duplicate
   * expressions in the expression graph.
   */
  private final Map<String, Expression> canonicals = new HashMap<>();

  /****************************************************************************/
  ExpressionTreeCleaner(Grammar grammar)
  {
    this.grammar = grammar;
  }

  /*****************************************************************************
   * @see ExpressionTreeCleaner
   */
  public Expression clean(Expression expr)
  {
    if (expr instanceof Reference) {
      expr = resolveRef((Reference) expr);
    }

    if (expr.repr != null)
    {
      /* clean() has already been called on the the expression. This can happen
       * if there are common subtrees (e.g. two rules references a same rule),
       * or with recursive rules. */

      return standardize(expr);
    }

    if (expr instanceof Rule)
    {
      /* Setting the representation of rules needs to be done done now in order
       * to be able to compute the representation of child expressions that
       * recurse. It is also necessary in order for standardize() to work
       * properly in case of recursion. */

      if ((expr.repr = ((Rule) expr).name) == null) {
        throw new Error("Rule with name not set.");
      }

      // Assign a unique ID to each rule.
      if (expr instanceof Rule) {
        ((Rule)expr).id = ruleCounter++;
      }
    }

    expr.grammar = grammar;

    List<Expression> children = expr.children();
    for (int i = 0 ; i < children.size() ; ++i)
    {
      children.set(i, clean(children.get(i)));
    }

    // Compute the textual representation of the expression.
    expr.accept(this);

    return standardize(expr);
  }

  /*****************************************************************************
   * Resolves the reference specified by the given expression.
   */
  Expression resolveRef(Reference ref)
  {
    String referencedRule = ref.referencedRule;
    Expression referenced = grammar.maybeRule(referencedRule);

   if (referenced == null)
   {
      throw new RuntimeException("Reference to non-existant rule "
        + referencedRule);
    }

    return referenced;
  }

  /*****************************************************************************
   * Standardize the expression: avoids the presence of duplicate non-rule
   * expressions in the tree. The textual representation of expr needs to have
   * been computed beforehand.
   *
   * We don't standardize rule for two reasons. First, this allows macros
   * defined in different files to have the same name. Second, there is not much
   * to gain from it anyway. Rules can never be created from within an
   * expression, and there is already a rule repository in {@link Grammar},
   * which reference resolution uses.
   */
  Expression standardize(Expression expr)
  {
    if (expr instanceof Rule) { return expr; }

    // Eliminate unnecessary nodes.
    if (expr instanceof Sequence || expr instanceof Choice)
    if (expr.children().size() == 1) {
      return expr.child();
    }

    Expression canonical = canonicals.get(expr.repr);

    if (canonical == null) {
      canonical = expr;
      canonicals.put(expr.repr, expr);
    }

    return canonical;
  }

  /*****************************************************************************
   * Potentially wraps (with parens) the textual representation of expr, to make
   * it suitable for use in a parent with given precedence.
   */
  static String wrap(int precedence, Expression expr)
  {
    if (expr.precedence >= precedence) {
      return expr.repr;
    }

    return "(" + expr.repr + ")";
  }

  //----------------------------------------------------------------------------
  // VISITOR
  //----------------------------------------------------------------------------
  /* Computes the textual representation of the expression assuming the textual
   * representation of its children has already been computed. */
  //----------------------------------------------------------------------------

  /****************************************************************************/
  @Override public void visit(Choice expr)
  {
    StringBuilder str = new StringBuilder();

    for (Expression e : expr.children()) {
      str.append(e);
      str.append(" | ");
    }

    if (str.length() != 0) {
      str.delete(str.length() - 3, str.length());
    }
    else {
      str.append("<empty>");
    }

    expr.repr = str.toString();
  }

  /****************************************************************************/
  @Override public void visit(Sequence expr)
  {
    StringBuilder str = new StringBuilder();

    for (Expression e : expr.children()) {
      str.append(wrap(expr.precedence, e));
      str.append(" ");
    }

    /* The trailing space is important: it differentiates the String for a
     * sequence containing a single element, from the string for the element by
     * itself. */

    if (str.length() == 0) {
      str.append("<empty>");
    }

    expr.repr = str.toString();
  }

  /****************************************************************************/
  @Override public void visit(Not expr)
  {
    expr.repr = "!" + wrap(expr.precedence, expr.child());
  }

  /****************************************************************************/
  @Override public void visit(And expr)
  {
    expr.repr = "&" + wrap(expr.precedence, expr.child());
  }

  /****************************************************************************/
  @Override public void visit(Star expr)
  {
    expr.repr = wrap(expr.precedence, expr.child()) + "*";
  }

  /****************************************************************************/
  @Override public void visit(Plus expr)
  {
    expr.repr = wrap(expr.precedence, expr.child()) + "+";
  }

  /****************************************************************************/
  @Override public void visit(Optional expr)
  {
    expr.repr = wrap(expr.precedence, expr.child()) + "?";
  }

  /****************************************************************************/
  @Override public void visit(Range expr)
  {
    expr.repr = (expr.negated ? "^[" : "[") + StringUtils.escape(expr.first) + "-"
      + StringUtils.escape(expr.last) + "]";
  }

  /****************************************************************************/
  @Override public void visit(CharClass expr)
  {
    expr.repr = (expr.negated ? "^[" : "[") + StringUtils.escape(expr.chars) + "]";
  }

  /****************************************************************************/
  @Override public void visit(StringLiteral expr)
  {
    expr.repr = "\"" + StringUtils.escape(expr.string) + "\"";
  }

  /****************************************************************************/
  @Override public void visit(Any expr)
  {
    expr.repr = "_";
  }

  /****************************************************************************/
  @Override public void visit(Capture expr)
  {
    expr.repr = expr.captureName + ":"
      + wrap(expr.precedence, expr.child());
  }

  /****************************************************************************/
  @Override public void visit(Rule expr)
  {
    /* Done in clean(). Not done in the Rule constructor because the rule name
     * can be bound late by the Grammar class. */
  }
}
