package parser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import grammar.Expression;
import source.Source;
import source.SourceStream;

/**
 * This class holds the minimal amount of data that needs to be remembered for
 * each expression graph node traversed while parsing the input. Since the graph
 * can contain cycles, nodes can be traversed multiple times, generating each
 * time a new ParseData object.
 */
public class ParseData
{
  /****************************************************************************/
  final Expression expr;

  /****************************************************************************/
  final Source source;

  /****************************************************************************/
  final int begin;

  /****************************************************************************/
  final boolean atomic;

  /****************************************************************************/
  final List<Match> childs;

  /****************************************************************************/
  final ParseErrors errors;

  /****************************************************************************/
  ParseData(Expression expr, SourceStream stream, boolean atomic)
  {
    this.expr    = expr;
    this.source  = stream.source;
    this.begin   = stream.position;
    this.atomic  = atomic;
    this.errors  = atomic ? null : new ParseErrors(expr, begin);
    this.childs  = atomic
      ? Collections.<Match>emptyList()
      : new ArrayList<Match>();
  }

  /*****************************************************************************
   * Make a ParseData from a Match. Useful to inject matches (as ParseData) into
   * the memoization table.
   */
  ParseData(int position, Match match)
  {
    this.expr      = match.expr;
    this.source    = match.source;
    this.begin     = position;
    this.end       = position;
    this.atomic    = false;
    this.childs    = null;
    this.errors    = new ParseErrors(expr, begin);
    this.match     = match;
    this.succeeded = true;
  }

  /****************************************************************************/
  Match match = null;

  /****************************************************************************/
  int end;

  /****************************************************************************/
  boolean succeeded = false;

  /*****************************************************************************
   * Marks the match as successful.
   */
  void succeed(int position)
  {
    end = position;
    succeeded = true;
    match = new Match(expr, source, begin, end, childs);
    expr.callbacks().parseDo(match);
  }

  /*****************************************************************************
   * Marks the match as unsuccessful and backtrack to the begin of the partially
   * matched expression.
   */
  void fail()
  {
    end = begin;
    succeeded = false;
  }

  /*****************************************************************************
   * Combines the data associated to a sub-expression with this data.
   */
  void merge(ParseData sub)
  {
    if (atomic) { return; }

    if (sub.atomic) {
      errors.merge(sub.begin);
    }
    else {
      errors.merge(sub.errors);
    }

    if (sub.succeeded) {
      childs.add(sub.match);
    }
  }
}
