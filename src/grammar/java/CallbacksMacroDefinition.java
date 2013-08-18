package grammar.java;

import static trees.MatchSpec.rule;

import compiler.Macro;
import compiler.MacroCompiler;
import compiler.PostParser;
import compiler.macros.MacroInterface;
import driver.Context;
import grammar.Grammar;
import grammar.MatchCallbacks;
import parser.Match;

import static util.ArrayUtils.arr;

/*******************************************************************************
 * Callback for macro declarations (the syntactic specification of a macro). The
 * callback picks up an expression built by other callbacks and adds it to the
 * grammar.
 */
public class CallbacksMacroDefinition extends MatchCallbacks
{
  /****************************************************************************/
  private static final MacroCompiler compiler = new MacroCompiler();

  /****************************************************************************/
  private static final PostParser postParser = new PostParser();

  /*****************************************************************************
   * The grammar the macros will be adding to.
   */
  private final Grammar grammar;

  /****************************************************************************/
  CallbacksMacroDefinition(Grammar grammar)
  {
    this.grammar = grammar;
  }

  /*****************************************************************************
   * Registers a new macro
   */
  @Override public void parseDo(Match match)
  {
    String ruleName = match.first(rule("identifier")).string();

    Match parentRule = match.firstBetween(
      rule("identifier"), arr(rule("strategy")), arr(rule("parsingExpression")));

    String parentRuleName = match.has(parentRule) ? parentRule.string() : null;

    Macro.Strategy strategy =
      match.has(rule("as"))         ? Macro.Strategy.AS:
      match.has(rule("under"))      ? Macro.Strategy.UNDER:
      match.has(rule("replaces"))   ? Macro.Strategy.REPLACES:
    /*match.has(rule("called"))*/     Macro.Strategy.CALLED;

    boolean raw = match.has(rule("raw"));
    boolean prioritary = match.has(rule("prioritary"));

    Match block = match.first(rule("block"));

    MacroInterface expander = match.has(block)
      ? compiler.compile(ruleName, postParser.run(block).string())
      : null;

    Macro macro = new Macro(ruleName, parentRuleName, grammar,
      Context.get().expressionStack.pop(), expander, strategy, raw, prioritary);

    Context.get().currentFile.addMacro(macro);
    macro.enable();

    Context.get().captureNames.clear();
  }
}