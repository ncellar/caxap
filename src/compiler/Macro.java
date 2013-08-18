package compiler;

import compiler.macros.MacroInterface;
import grammar.Expression;
import grammar.Expression.MacroRule;
import grammar.Expression.Rule;
import grammar.Grammar;
import parser.Match;

import static util.ListUtils.list;

/**
 * Describes a macro defined in a macro file.
 */
public class Macro
{
  /****************************************************************************/
  public final Rule parentRule;

  /****************************************************************************/
  public final Grammar grammar;

  /****************************************************************************/
  public final Rule rule;

  /****************************************************************************/
  private boolean enabled;

  /****************************************************************************/
  public final boolean raw;

  /****************************************************************************/
  public final boolean prioritary;

  /****************************************************************************/
  private final MacroInterface expander;

  /****************************************************************************/
  public enum Strategy { AS, UNDER, REPLACES, CALLED }

  /****************************************************************************/
  private Strategy strategy;

  /****************************************************************************/
  public Macro(
    String         ruleName,
    Rule           parentRule,
    Grammar        grammar,
    Expression     syntax,
    MacroInterface expander,
    Strategy       strategy,
    boolean        raw,
    boolean        prioritary)
  {
    this.parentRule        = parentRule;
    this.grammar           = grammar;
    this.expander          = expander;
    this.strategy          = strategy;
    this.raw               = raw;
    this.prioritary        = prioritary;
    this.enabled           = false;

    if (expander == null && strategy != Strategy.CALLED)
    {
      throw new Error("No body for macro " + ruleName
        + ". Maybe you meant to use the \"called\" strategy?");
    }

    if ((parentRule == null) != (strategy== Strategy.CALLED))
    {
      throw new Error("Mismatch between strategy " + strategy + " and the "
        + (parentRule == null ? "absence" : "presence")
        + " of a parent rule in macro " + ruleName + ".");
    }

    Rule dirty = expander == null
      ? new Rule(ruleName, list(syntax))
      : new MacroRule(ruleName, this, syntax);

    this.rule = grammar.cleanUnregisteredRule(dirty);
  }

  /****************************************************************************/
  public Macro(
    String         ruleName,
    String         parentRule,
    Grammar        grammar,
    Expression     syntax,
    MacroInterface expander,
    Strategy       strategy,
    boolean        raw,
    boolean        prioritary)
  {
    this(ruleName, parentRule == null ? null : grammar.rule(parentRule),
      grammar, syntax, expander, strategy, raw, prioritary);
  }

  /****************************************************************************/
  @Override public String toString()
  {
    return rule.name;
  }

  /****************************************************************************/
  public Match expand(Match match)
  {
    return expander == null ? match : expander.expand(match);
  }

  /*****************************************************************************
   * Enables the macro, adding it to the grammar.
   */
  public void enable()
  {
    assert !enabled;
    if (parentRule != null) {
      grammar.addRuleAlternative(parentRule, rule, prioritary);
    }
    else {
      grammar.registerRule(rule);
    }
    enabled = true;
  }

  /*****************************************************************************
   * Disables the macro, removing it from the grammar.
   */
  public void disable()
  {
    assert enabled;
    if (parentRule != null) {
      grammar.removeRuleAlternative(parentRule, rule);
    }
    else {
      grammar.unregisterRule(rule);
    }
    enabled = false;
  }

  /****************************************************************************/
  public void ensureDisabled()
  {
    if (enabled) { disable(); }
  }

  /****************************************************************************/
  public Strategy strategy()
  {
    return strategy;
  }

  /****************************************************************************/
  public boolean isAs()
  {
    return strategy == Strategy.AS;
  }

  /****************************************************************************/
  public boolean isUnder()
  {
    return strategy == Strategy.UNDER;
  }

  /****************************************************************************/
  public boolean isReplaces()
  {
    return strategy == Strategy.REPLACES;
  }

  /****************************************************************************/
  public boolean isCalled()
  {
    return strategy == Strategy.CALLED;
  }
}
