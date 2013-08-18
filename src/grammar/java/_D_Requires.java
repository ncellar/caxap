package grammar.java;

import static grammar.GrammarDSL.*;

import grammar.Expression;
import grammar.Grammar;

/**
 * Grammar rules for require statements.
 *
 * See {@link driver.RequiresParser} for more information about require
 * statement.
 *
 * @see JavaGrammar
 */
public class _D_Requires extends _C_Statements
{
  /****************************************************************************/
  public final Expression require = keyword("require");

  /****************************************************************************/
  public final Expression macro = keyword("macro");

  /****************************************************************************/
  public final Expression starryIdentifier = rule_seq(
    qualifiedIdentifier, opt(dot, star));

  /****************************************************************************/
  public final Expression starryIdentifierOne = rule_seq(
    identifier, dot, choice(star, starryIdentifier));

  /****************************************************************************/
  public final Expression staticRequire = rule_seq(
    require, _static, starryIdentifierOne,
    opt(colon, choice(
      seq(colon, choice(star, starryIdentifier)),
      seq(opt(colon), starryIdentifierOne)
    )),
    semi
  );

  /****************************************************************************/
  public final Expression macroRequire = rule_seq(
    require, macro, starryIdentifierOne,
    opt(colon, choice(colon, star, identifier)), semi
  );

  /****************************************************************************/
  public final Expression regularRequire = rule_seq(
    require, starryIdentifierOne,
    opt(colon, opt(colon), opt(choice(star, starryIdentifier))), semi
  );

  /****************************************************************************/
  public final Expression.Rule requireDeclaration = rule(
    macroRequire, staticRequire, regularRequire);

  /****************************************************************************/
  public void initialize(Grammar grammar)
  {
    grammar.addExistingRuleAlternative(grammar.rule("importDeclaration"),
      requireDeclaration, false);

    prelude.callbacks = new CallbacksPrelude();
  }
}
