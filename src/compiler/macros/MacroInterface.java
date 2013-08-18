package compiler.macros;

import parser.Match;

/**
 * The generated macro classes implement this interface.
 */
public interface MacroInterface
{
  /*****************************************************************************
   * Returns a new match tree, using the match tree for the input matching the
   * macro syntax as input.
   *
   * The macro compiler will generate code to make the captures specified by the
   * syntactic definition available under their given names, as local variables.
   */
  Match expand(Match input);
}
