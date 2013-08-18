package parser;

import grammar.Expression;

/**
 * The interface provides a method to return the result of parsing a given at
 * given a input position. As the name suggests, the implementing classes are
 * likely to perform some form of memoization to avoid parsing the same
 * expression twice at the same position.
 *
 * TODO Making a tool that can identify expressions that would benefit the most
 * from memoization might be a good idea.
 */
interface Memo
{
  /*****************************************************************************
   * Returns memoized match data for the given expression at the current input
   * position, or call the parser to get a new match.
   */
  ParseData get(int position, Expression expr);

  /*****************************************************************************
   * Forgets all memoized data.
   */
  public void clear();
}
