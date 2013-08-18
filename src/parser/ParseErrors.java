package parser;

import grammar.Expression;

import java.util.ArrayList;
import java.util.List;

import source.Source;
import util.StringUtils;

/*******************************************************************************
 * There is currently a single way to handle parse errors. The system will
 * report only a single parse error. This error will be the one that happens the
 * farthest ahead in the input stream.
 *
 * You might be wondering why we can reach this farthest point if we encountered
 * errors earlier in the input. The answer is that the errors could be
 * encountered when parsing alternatives of a choice expression. If the first
 * alternative fails, we carry on by trying the second one, etc. The errors
 * could also happen when failing to match an optional expression (in ?, *, +).
 *
 * Actually, the system may report more than one error if more than one error
 * happen at the farthest error position. This means that at any given point
 * during a parse we must keep the farthest input position and a list of
 * expressions that fail there. This data is held in the ErrorGroup class.
 *
 * "at any given point during a parse" ? Can't we discard the current error
 * information once we have successfully parsed an expression? Unfortunately no.
 * Consider the following example:
 *
 * <pre>
 * input = A, B, E
 * expr  = seq(choice(seq(A, B, C),
 *                    A),
 *             D)
 * </pre>
 *
 * Despite the fact that the choice succeeds with its second alternative (A),
 * the error in the first alternative is still the most relevant to the failure
 * to parse the input sequence. The error on expression C happens at input
 * position 3 (E), while the error on D happens at input position 2 (B).
 *
 * -----------------------------------------------------------------------------
 *
 * A few details about particular expressions:
 *
 * Not: Since a parse error in what is expected in a Not expression, all parse
 * errors are discarded on success. This is achieved as a side effect of having
 * Not expression being atomic.
 *
 * And: Despite resetting the input position, And is treated like any other
 * expression with regards to error handling.
 *
 * Opt, Star and Plus: The failure to match an optional expression does register
 * an error.
 *
 * -----------------------------------------------------------------------------
 *
 * We record the path in the expression to a failing expression by building a
 * graph of ParseErrors objects. Each ParseErrors object refers to the Errors
 * object for its sub-expressions.
 *
 * ParseErrors are built regardless of whether there was or not an error.
 */
public class ParseErrors
{
  /*****************************************************************************
   * Expression to which this error group belongs.
   */
  final Expression expression;

  /*****************************************************************************
   * Position corresponding to this error group. -1 if the error group is empty.
   */
  int position;

  /*****************************************************************************
   * Errors in the sub-expressions, or null if all errors happen at the begin of
   * the expression.
   */
  private List<ParseErrors> subs = new ArrayList<>(1);

  /*****************************************************************************
   * Begin of the match for the expression.
   */
  private final int begin;

  /****************************************************************************/
  ParseErrors(final Expression expression, final int begin)
  {
    this.expression = expression;
    this.begin = begin;
    this.position = begin - 1;
  }

  /*****************************************************************************
   * Indicates an error at given input position for the current expression. If
   * the error happened in a sub-expression, and information about this error is
   * available, merge(ErroGroup) should be used instead.
   */
  void merge(final int position)
  {
    if (position > this.position) {
      this.position = position;
      subs.clear();
    }
  }

  /****************************************************************************
   * Merges the error group for a sub-expression to this error group. Called on
   * error groups corresponding to non-leaf expressions.
   *
   * The procedure works as explained in the class comment. In particular, only
   * the farthest errors are kept. The error list is truncated if all
   * sub-expression fail at the beginning of the expression.
   */
  void merge(final ParseErrors childErrors)
  {
    if (childErrors.position == this.begin && this.position <= this.begin)
    {
      this.position = this.begin;
    }
    else if (childErrors.position > this.position)
    {
      this.position = childErrors.position;
      subs.clear();
      subs.add(childErrors);
    }
    else if (childErrors.position == this.position)
    {
      subs.add(childErrors);
    }
  }

  /*****************************************************************************
   * Reports all errors in this group on the standard output.
   */
  public String report(Source src)
  {
    StringBuilder builder = new StringBuilder();
    builder.append("Error at input position ");
    builder.append(position);
    builder.append(" (");
    builder.append(src.where(position));
    builder.append(")");

    // append an excerpt around the error site

    int excerptStart = Math.max(0, position - 50);
    int excerptStop = Math.min(position + 50, src.end());

    builder.append("\n\n[start excerpt]\n");
    builder.append(src.at(excerptStart, position));
    builder.append("\n[the error is at the begin of next line]\n");
    builder.append(src.at(position, excerptStop));
    builder.append("\n[end excerpt]\n\n");

    // print path

    builder.append("in ");
    trace(0, builder);

    return builder.toString();
  }

  /*****************************************************************************
   * Appends the whole path to the faulty expression(s) to $builder.
   */
  private void trace(int indent, StringBuilder builder)
  {
    builder.append(expression);

    if (subs.size() == 1) {
      builder.append(" > ");
      subs.get(0).trace(indent, builder);
    }
    else {
      indent += 2;

      for (ParseErrors e : subs) {
        builder.append("\n");
        builder.append(StringUtils.repeat(" ", indent));
        builder.append(">> ");
        e.trace(indent, builder);
      }
    }
  }

//  UNUSED CODE
//
//  /*****************************************************************************
//   * Indicates whether there was an error while parsing the expression.
//   */
//  public boolean erroneous()
//  {
//    return this.position != this.begin - 1;
//  }
}

