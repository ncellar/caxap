package grammar.java;

import static grammar.GrammarDSL.*;

import compiler.QuotationMacro;

import compiler.Macro;

import grammar.Expression;
import grammar.Expression.Rule;
import grammar.Grammar;

/**
 * Grammar rules for macro definitions and quotation.
 *
 * @see JavaGramar
 */
public class _E_MacroDefinitions extends _D_Requires
{
  //============================================================================
  // MACRO SYNTACTIC SPECIFICATION
  //============================================================================

  /*****************************************************************************
   * The right square bracket needs to be escaped in characters classes and
   * ranges.
   */
  public final Expression pegChar = atomic(rule_seq(
    seq(not(rSqBra), choice(str("\\]"), charLiteralNoQuotes))
  ));

  /****************************************************************************/
  public final Expression pegStar = operator("*", "+");

 /****************************************************************************/
  public final Expression pegPlus = operator("+", "+/");

  /****************************************************************************/
  public final Expression charClassParsingExpression = rule_seq(
    opt(str("^")), lSqBra, star(not(rSqBra), pegChar), rSqBra);

  /****************************************************************************/
  public final Expression charRangeParsingExpression = rule_seq(
    opt(str("^")),
    lSqBra, charLiteralNoQuotes, str("-"), pegChar, rSqBra);

  /****************************************************************************/
  public final Expression underscorePasingExpression = rule(spaced(str("_")));

  /****************************************************************************/
  public final Expression literalParsingExpression = rule_seq(
    stringLiteral, opt(minus));

  /****************************************************************************/
  public final Expression referenceParsingExpression = rule(identifier);

  /****************************************************************************/
  public final Expression parenParsingExpression = rule_seq(
    lPar, ref("parsingExpression"), rPar);

  /****************************************************************************/
  public final Expression primaryParsingExpression = rule(
    underscorePasingExpression,
    literalParsingExpression,
    referenceParsingExpression,
    charClassParsingExpression,
    charRangeParsingExpression,
    parenParsingExpression
  );

  /****************************************************************************/
  public final Expression suffixParsingExpression = rule_seq(
    primaryParsingExpression,
    opt(choice(pegStar, pegPlus, qMark))
  );

  /****************************************************************************/
  public final Expression starPlus = rule(spaced(str("*+")));

  /****************************************************************************/
  public final Expression plusSlash = rule(spaced(str("+/")));

  /****************************************************************************/
  public final Expression notParsingExpression = rule_seq(
    bang, suffixParsingExpression);

  /****************************************************************************/
  public final Expression andParsingExpression = rule_seq(
    and, suffixParsingExpression);

  /*****************************************************************************
   * Slightly tricky because of the need to disallow spaces around the colon.
   * Need to simplify.
   */
  public final Expression captureExpression = rule(
    seq(
      not(star(letterOrDigit), fspacing), identifier,
      str(":"), ref("prefixParsingExpression")
    ),
    seq(str(":"), referenceParsingExpression)
  );

  /****************************************************************************/
  public final Expression prefixParsingExpression = rule(
    andParsingExpression,
    notParsingExpression,
    captureExpression,
    suffixParsingExpression
  );

  /****************************************************************************/
  public final Expression untilParsingExpression = rule_seq(
    prefixParsingExpression, starPlus, prefixParsingExpression);

  /****************************************************************************/
  public final Expression untilOnceParsingExpression = rule_seq(
    prefixParsingExpression, plusPlus, prefixParsingExpression);

  /****************************************************************************/
  public final Expression listParsingExpression = rule_seq(
    prefixParsingExpression, plusSlash, prefixParsingExpression);

  /****************************************************************************/
  public final Expression binaryParsingExpression = rule(
    untilParsingExpression,
    untilOnceParsingExpression,
    listParsingExpression,
    prefixParsingExpression
  );

  /****************************************************************************/
  public final Expression sequenceParsingExpression = rule(plus(
    binaryParsingExpression));

  /****************************************************************************/
  public final Expression parsingExpression = rule(list(
    pipe, sequenceParsingExpression));

  //============================================================================
  // QUOTATION SYNTAX
  //============================================================================

  /****************************************************************************/
  public final Expression hash = operator("#");

  /****************************************************************************/
  public final Expression hashat = operator("#@");

  /****************************************************************************/
  public final Expression quote = operator("'");

  /****************************************************************************/
  public final Expression backquote = operator("`");

  /*****************************************************************************
   * Does not include trailing space.
   */
  public final Expression backslash = rule(str("\\"));

  /*****************************************************************************
   * Upon interpreting the quote syntax, instances of the "insert" rule preceded
   * by a backslash have to be filtered out. See {@link compiler.util.Quoter}.
   */
  public final Expression regularUnquotation = rule_seq(
    opt(backslash), hash,
    choice(ref("unquotation"), unaryExpression));

  /****************************************************************************/
  public final Expression spliceDelimiter = rule(star(choice(
    escape,
    str("\\|"),
    seq(not(str("|")), any)
   )));

  /****************************************************************************/
  public final Expression spliceDelimiters = rule_seq(str("|"),
    spliceDelimiter, str("|"),
    spliceDelimiter, str("|"),
    spliceDelimiter, str("|")
  );

  /****************************************************************************/
  public final Expression splicePrefix = rule_seq(
    opt(backslash), hashat, spliceDelimiters, spacing
  );

  /****************************************************************************/
  public final Expression splice = rule_seq(splicePrefix, unaryExpression);

  /****************************************************************************/
  public final Expression unquotation = rule(regularUnquotation, splice);

  /****************************************************************************/
  public final Expression qEndMarker  = seq(rSqBra, choice(quote, backquote));

  /****************************************************************************/
  public final Expression escapedQEndMarker = rule_seq(backslash, qEndMarker);

  /****************************************************************************/
  public final Expression sourceFragmentChar = seq(not(qEndMarker), any);

  /****************************************************************************/
  public final Expression sourceFragment = rule_seq(
    star(until(sourceFragmentChar,
      choice(ref("quotation"), unquotation, escapedQEndMarker))),
    star(sourceFragmentChar));

  /*****************************************************************************
   * Note that because lSqBra consumes trailing whitespace, quotation cannot
   * be used to quote whitespace.
   */
  public final Rule simpleQuotation = rule_seq(
    quote, identifier, lSqBra, sourceFragment, rSqBra, quote);

  /****************************************************************************/
  public final Rule quasiquotation = rule_seq(
    backquote, identifier, lSqBra, sourceFragment, rSqBra, backquote);

  /****************************************************************************/
  public final Rule quotation = rule(simpleQuotation, quasiquotation);

  //============================================================================
  // DYNAMIC QUOTATION
  //============================================================================
  /* Quotation syntax that can embedded within a Java string to be able to quote
   * in pure java (without the need to preprocess any file). */
  //============================================================================

  /*****************************************************************************
   * @see {@link #unquotation}
   */
  public final Expression insertMarker = rule_seq(
    choice(splicePrefix, seq(opt(backslash), hash)),
    parseIntNumber);

  /****************************************************************************/
  public final Expression dynamicSourceFragment = rule_seq(
    star(until(any, insertMarker)), star(any));

  //============================================================================
  // MACRO DECLARATION / DEFINITION
  //============================================================================
  // To be inserted in classMemberDeclaration.
  //============================================================================

  /****************************************************************************/
  public final Expression raw = keyword("raw");

  /****************************************************************************/
  public final Expression prioritary = keyword("prioritary");

  /****************************************************************************/
  public final Expression as = keyword("as");

  /****************************************************************************/
  public final Expression under = keyword("under");

  /****************************************************************************/
  public final Expression replaces = keyword("replaces");

  /****************************************************************************/
  public final Expression called = keyword("called");

  /****************************************************************************/
  public final Expression strategy = rule(choice(as, under, replaces, called));

  /****************************************************************************/
  public final Rule macroDefinition = rule_seq("macroDefinition",
    opt(raw), opt(prioritary), macro, identifier, strategy, opt(identifier),
    colon, parsingExpression, choice(block, semi));

  //============================================================================

  @Override public void initialize(Grammar grammar)
  {
    super.initialize(grammar);

    /* Currently, a macro file can contain type definitions, which will simply
     * be ignored. */

    grammar.addExistingRuleAlternative(grammar.rule("topLevelTypeDeclaration"),
      macroDefinition, false);

    Macro quotations = new Macro("quotationm", "unaryExpression",
      grammar, quotation, new QuotationMacro(), Macro.Strategy.AS, false, false);

    quotations.enable();

    macroDefinition.callbacks = new CallbacksMacroDefinition(grammar);

    charClassParsingExpression .callbacks = new CharClassCallbacks();
    charRangeParsingExpression .callbacks = new CharRangeCallbacks();
    underscorePasingExpression .callbacks = new AnyCallbacks();
    literalParsingExpression   .callbacks = new LiteralCallbacks();
    referenceParsingExpression .callbacks = new ReferenceCallbacks();
    suffixParsingExpression    .callbacks = new SuffixCallbacks();
    untilParsingExpression     .callbacks = new UntilCallbacks();
    untilOnceParsingExpression .callbacks = new UntilOnceCallbacks();
    listParsingExpression      .callbacks = new ListCallbacks();
    notParsingExpression       .callbacks = new NotCallbacks();
    andParsingExpression       .callbacks = new AndCallbacks();
    captureExpression     .callbacks = new CaptureCallbacks();
    sequenceParsingExpression  .callbacks = new SequenceCallbacks();
    parsingExpression          .callbacks = new ChoiceCallbacks();
  }
}
