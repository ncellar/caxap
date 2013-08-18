package compiler;

import static compiler.util.MatchCreator.new_match;

import java.util.ArrayList;
import java.util.List;

import grammar.Expression.MacroRule;
import grammar.Expression.Rule;
import parser.Match;

/**
 * See the sections of my thesis titled "How does caxap expand macros?",
 * "Expansion Strategies" and "Macro Expansion Order & Raw Macros" to learn more
 * about how macro are expanded.
 */
public class MacroExpander extends MatchTreeTransformer
{
  /*****************************************************************************
   * Macro-expand the given match. Returns the expanded match. There is no
   * guarantee that the supplied match will not be modified. The current
   * implementation does modify the supplied match, but you still need to assign
   * the result to account for cases such as a change of the root match.
   */
  @Override public Match transform(Match match)
  {
    if (match.expr instanceof Rule && !match.expr.atomic
    &&  match.child().expr instanceof MacroRule)
    {
      return expandMacro(match, match.child());
    }
    return transformChilds(match);
  }

  /****************************************************************************/
  private Match expandMacro(Match parent, Match macroMatch)
  {
    Macro macro    = ((MacroRule) macroMatch.expr).macro;
    Match oldMatch = macroMatch;

    if (!macro.raw) {
      macroMatch = transformChilds(macroMatch);
    }

    Match expansion = macro.expand(macroMatch);
    expansion = transform(expansion); // recursive expansion

    if (!macro.isCalled()) {
      checkParent((Rule) parent.expr, macro);
    }

    if (macro.isAs()) {
      checkAsExpansion(oldMatch, macro, expansion);
      return expansion;
    }
    else if (macro.isUnder()) {
      List<Match> childs = new ArrayList<>(parent.children());
      childs.set(0, expansion);
      return new_match(parent.expr, childs);
    }
    else /* macro.isReplaces() || macro.isCalled() */ {
      return expansion;
    }
  }

  /****************************************************************************/
  private void checkParent(Rule parent, Macro macro)
  {
    if (!parent.name.equals(macro.parentRule.name))
    {
      throw new Error("Macro \"" + macro
        + "\" appearing under a rule it does not extend: \"" + parent.name
        + "\". Expected rule \"" + macro.parentRule + "\" instead.");
    }
  }

  /****************************************************************************/
  private void checkAsExpansion(Match expanded, Macro macro, Match expansion)
  {
    if (!(expansion.expr instanceof Rule))
    {
      throw new Error("AS Macro \"" + macro
        + "\" did not expand to a rule. (" + expanded.where() + ")");
    }

    Rule rule = (Rule) expansion.expr;

    if(!rule.name.equals(macro.parentRule.name))
    {
      throw new Error("Macro \"" + macro
        + "\" did not expand to an instance of the rule it expands."
        + "(" + expanded.where() + ")");
    }
  }
}
