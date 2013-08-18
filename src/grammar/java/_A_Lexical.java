package grammar.java;

import static grammar.GrammarDSL.*;

import grammar.Expression;

/**
 * This class defines the lexical structure of the Java language, i.e. how to
 * form the tokens that would be used in a context-free grammar (CFG). In a PEG
 * grammar, the rules for token formation can be included in the grammar itself.
 *
 * Normally Java parses a source file as follows:
 *
 * <pre>
 * - conversion of unicode escape (\\uXXXX) into unicode characters
 * - stripping of white space and comments
 * - tokenization (using longest match)
 * </pre>
 *
 * Currently, unicode escapes are not recognized.
 *
 * This class contains both grammar rules and helper functions, that notably
 * help with whitespace handling.
 *
 * @see JavaGrammar
 */
public class _A_Lexical
{
  //----------------------------------------------------------------------------
  // Helpers
  //----------------------------------------------------------------------------

  /****************************************************************************/
  protected Expression spaced(Expression expr)
  {
    return seq(expr, spacing);
  }

  /****************************************************************************/
  protected Expression literal(Expression expr)
  {
    return spaced(atomic(expr));
  }

  /****************************************************************************/
  protected Expression operator(String str)
  {
    return rule(spaced(str(str)));
  }

  /****************************************************************************/
  protected Expression operator(String str, String notChars)
  {
    return rule(spaced(atomic(seq(
      str(str), not(chars(notChars))))));
  }

  /****************************************************************************/
  protected Expression keyword(Expression expr)
  {
    return rule(spaced(atomic(
      seq(expr, not(letterOrDigit)))));
  }

  /****************************************************************************/
  protected Expression keyword(String keyword)
  {
    return keyword(str(keyword));
  }

  //----------------------------------------------------------------------------
  // Commonly Used Subexpressions
  //----------------------------------------------------------------------------

  public final Expression  underscore  = chars("_");
  public final Expression  floatSuffix = chars("fFdD");
  public final Expression  zeroSeven   = range('0', '7');
  public final Expression  zeroNine    = range('0', '9');

  //----------------------------------------------------------------------------
  // JLS 3.3 (Unicode Escapes)
  //----------------------------------------------------------------------------

  /****************************************************************************/
  public final Expression hexDigit = rule(
    range('a', 'f'),
    range('A', 'F'),
    zeroNine
  );

  /****************************************************************************/
  public final Expression whiteSpace = rule(plus(chars(" \t\r\n\f")));

  /****************************************************************************/
  public final Expression multiLineComment = rule_seq(
    str("/*"),
    until(any, str("*/"))
  );

  /****************************************************************************/
  public final Expression singleLineComment = rule_seq(
    str("//"),
    until(any, chars("\r\n"))
  );

  //----------------------------------------------------------------------------
  // JLS 3.6 (Spacing) & JLS 3.7 (Comments)
  //----------------------------------------------------------------------------

  /*****************************************************************************
   * Optional spacing. Spacing is never mandatory in java, but some tokens
   * cannot be parsed if not separated by spacing (e.g. classSpacing vs class
   * Spacing).
   */
  public final Expression spacing = rule(star(
    choice(atomic(plus(whiteSpace)), multiLineComment, singleLineComment)));

  /*****************************************************************************
   * Atomic spacing.
   */
  public final Expression aspacing = atomic(rule(spacing));

  /*****************************************************************************
   * Mandatory spacing (for use in macro's syntactic specification).
   */
  public final Expression fspacing = atomic(rule(plus(
    choice(whiteSpace, multiLineComment, singleLineComment))));

  /*****************************************************************************
   * Atomic mandatory spacing.
   */
  public final Expression afspacing = atomic(rule(fspacing));

  //----------------------------------------------------------------------------
  // JLS 3.8 (Identifiers) (1: letters and digits)
  //----------------------------------------------------------------------------
  /* These are traditional definitions of letters and digits. JLS defines
   * letters and digits as Unicode characters recognized as such by special Java
   * procedures, which is difficult to express in terms of Parsing Expressions. */
  //----------------------------------------------------------------------------

  /****************************************************************************/
  public final Expression letter = rule(
    range('a', 'z'),
    range('A', 'Z'),
    chars("_$")
  );

  /****************************************************************************/
  public final Expression letterOrDigit = rule(letter, range('0', '9'));

  //----------------------------------------------------------------------------
  // JLS 3.10.2 (Boolean Literals)
  //----------------------------------------------------------------------------

  public final Expression _true  = keyword("true");
  public final Expression _false = keyword("false");

  public final Expression booleanLiteral = rule(_true, _false);

  //----------------------------------------------------------------------------
  // JLS 3.10.7 (The Null Literal)
  //----------------------------------------------------------------------------
  public final Expression _null = keyword("null");

  //----------------------------------------------------------------------------
  // JLS 3.9 (Keywords)
  //----------------------------------------------------------------------------
  /* More precisely: reserved words. According to JLS, "true", "false", and
   * "null" are technically not keywords - but still must not appear as
   * identifiers. Keywords "const" and "goto" are not used; JLS explains the
   * reason. We distinguish primitive types from other keywords. */
  //----------------------------------------------------------------------------

  public final Expression _boolean  = keyword("boolean");
  public final Expression _byte     = keyword("byte");
  public final Expression _char     = keyword("char");
  public final Expression _double   = keyword("double");
  public final Expression _float    = keyword("float");
  public final Expression _int      = keyword("int");
  public final Expression _long     = keyword("long");

  /****************************************************************************/
  public final Expression primitiveType = rule(
    _boolean, _byte, _char, _double, _float, _int, _long);

  /*****************************************************************************
   * Java keywords which are not primitive types, to be reused in other rules.
   * To use keyword in grammar rules, use the fields of this class named
   * "_<keyword_name>" (e.g. "_public").
   *
   * goto and const are not used by Java but are reserved for the compiler to
   * produce better error message if those C/C++ keywords are used.
   */
  public final Expression lexNonTypeKeyword = keyword(choice(
    str("abstract"),    str("assert"),
    str("break"),       str("case"),
    str("catch"),       str("class"),
    str("const"),       str("continue"),
    str("default"),     str("do"),
    str("else"),        str("enum"),
    str("extends"),     str("finally"),
    str("final"),       str("for"),
    str("goto"),        str("if"),
    str("implements"),  str("import"),
    str("interface"),   str("instanceof"),
    str("native"),      str("new"),
    str("package"),     str("private"),
    str("protected"),   str("public"),
    str("return"),      str("static"),
    str("strictfp"),    str("super"),
    str("switch"),      str("synchronized"),
    str("this"),        str("throws"),
    str("throw"),       str("transient"),
    str("try"),         str("void"),
    str("volatile"),    str("while")
  ));

  /*****************************************************************************
   * Words that represent primitive values, to be reused in other rules.
   */
  public final Expression lexLiteralWord = rule(_true, _false, _null);

  /*****************************************************************************
   * Reserved words comprise keywords (primitive type names, other keywords) and
   * literal words. A Java identifier cannot be the same as a reserved word.
   */
  public final Expression keyword = atomic(rule(
    primitiveType,
    lexNonTypeKeyword,
    lexLiteralWord
  ));

  //-------------------------------------------------------------------------
  // JLS 3.8 (Identifiers) (2: identifiers)
  //-------------------------------------------------------------------------
  public final Expression identifier = rule(literal(seq(
    not(keyword),
    letter,
    star(letterOrDigit)
  )));

  //-------------------------------------------------------------------------
  // JLS 3.10.1 (Integer Literals)
  //-------------------------------------------------------------------------

  /****************************************************************************/
  public final Expression hexDigits = rule(list(star(underscore), hexDigit));

  /****************************************************************************/
  public final Expression hexNumeral = rule_seq(
    choice(str("0x"), str("0X")), hexDigits);

  /****************************************************************************/
  public final Expression binaryNumeral = rule_seq(
    choice(str("0b"), str("0B")),
    list(underscore, chars("01"))
  );

  /****************************************************************************/
  public final Expression octalNumeral = rule_seq(
    str("0"), plus(star(underscore), zeroSeven));

  /****************************************************************************/
  public final Expression decimalNumeral = rule(
    str("0"),
    seq(
      range('1', '9'),
      star(star(underscore), zeroNine)
    )
  );

  /****************************************************************************/
  public final Expression integerLiteral = rule(literal(seq(
    choice(
      hexNumeral,
      binaryNumeral,
      octalNumeral,
      decimalNumeral  // May be a prefix of all above ("0")
    ),
    opt(chars("lL"))
  )));

  //----------------------------------------------------------------------------
  // Integer parsable by @link{Integer#parseInt(String, int)}.
  // /!\ This does NOT include trailing whitespace.
  //----------------------------------------------------------------------------
  public final Expression parseIntNumber = rule(seq(
    opt(choice(ref("plus"), ref("minus"))),
    plus(zeroNine)
  ));

  //----------------------------------------------------------------------------
  // JLS 3.10.2 (Floating-Point Literals)
  //----------------------------------------------------------------------------

  /****************************************************************************/
  public final Expression hexSignificand = rule(
    seq(
      choice(str("0x"), str("0X")),
      opt(hexDigits),
      str("."),
      hexDigits
    ),
    seq(hexNumeral, opt(str(".")))  // May be a prefix of above
  );

  /****************************************************************************/
  public final Expression digits = rule(list(star(underscore), zeroNine));

  /****************************************************************************/
  public final Expression binaryExponent = rule_seq(
    chars("pP"), opt(chars("+\\-")), digits);

  /****************************************************************************/
  public final Expression hexFloat = rule_seq(
    hexSignificand, binaryExponent, opt(floatSuffix));

  /****************************************************************************/
  public final Expression exponent = rule_seq(
    chars("eE"), opt(chars("+-")), digits);

  /****************************************************************************/
  public final Expression decimalFloat = rule(
    seq(digits, str("."), opt(digits), opt(exponent), opt(floatSuffix)),
    seq(str("."), digits, opt(exponent), opt(floatSuffix)),
    seq(digits, exponent, opt(floatSuffix)),
    seq(digits, opt(exponent), floatSuffix)
  );

  /****************************************************************************/
  public final Expression floatLiteral = rule(
    literal(choice(hexFloat, decimalFloat)));

  //----------------------------------------------------------------------------
  // JLS 3.10.6 (Escape Sequences for Character and String Literals)
  //----------------------------------------------------------------------------

  /****************************************************************************/
  public final Expression octalEscape = rule(
    seq(range('0', '3'), zeroSeven, zeroSeven),
    seq(zeroSeven, zeroSeven),
    zeroSeven
  );

  /****************************************************************************/
  public final Expression escape = rule_seq(
    str("\\"),
    choice(chars("btnfr\"'\\"), octalEscape)
  );

  //----------------------------------------------------------------------------
  // JLS 3.10.4 (Character Literals)
  //----------------------------------------------------------------------------

  public final Expression charLiteralNoQuotes = rule(
      escape,
      seq(not(chars("'\\\n\r")), any)
    );

  public final Expression charLiteral = rule(literal(seq(
    str("'"), charLiteralNoQuotes, str("'"))));

  //----------------------------------------------------------------------------
  // JLS 3.10.5 (String Literals)
  //----------------------------------------------------------------------------

  public final Expression stringLiteralContent = atomic(rule(star(choice(
    escape,
    seq(not(chars("\"\\\n\r")), any)
  ))));

  public final Expression stringLiteral = rule(literal(seq(
    str("\""), stringLiteralContent, str("\"") )));

  //----------------------------------------------------------------------------
  // JLS 3.10.0 (Literals)
  //----------------------------------------------------------------------------
  public final Expression literal = rule(
    charLiteral,
    floatLiteral,
    integerLiteral,
    stringLiteral,
    booleanLiteral,
    _null
  );

  //----------------------------------------------------------------------------
  // JLS 3.11 (Separators)
  //----------------------------------------------------------------------------
  // lAnBra/rAnBra means left/right angle bracket
  // lcuBra/rCuBra means left/right curly brace
  // lPar/rPar     means left/right parenthesis
  // lSqBra/rSqBra means left/right square bracket
  //----------------------------------------------------------------------------

  public final Expression lAnBra      = operator("<");
  public final Expression lCuBra      = operator("{");
  public final Expression lPar        = operator("(");
  public final Expression lSqBra      = operator("[");

  public final Expression rAnBra      = operator(">");
  public final Expression rCuBra      = operator("}");
  public final Expression rPar        = operator(")");
  public final Expression rSqBra      = operator("]");

  //----------------------------------------------------------------------------
  // JLS 3.12 (Operators) (1: Arithmetic Operators)
  //----------------------------------------------------------------------------

  public final Expression plus        = operator("+", "=+");
  public final Expression minus       = operator("-", "=-");
  public final Expression star        = operator("*", "=");
  public final Expression slash       = operator("/", "=");
  public final Expression percent     = operator("%", "=");

  public final Expression plusEq      = operator("+=");
  public final Expression minusEq     = operator("-=");
  public final Expression starEq      = operator("*=");
  public final Expression slashEq     = operator("/=");
  public final Expression modEq       = operator("%=");

  public final Expression plusPlus    = operator("++");
  public final Expression minusMinus  = operator("--");

  //----------------------------------------------------------------------------
  // JLS 3.12 (Operators) (2: Binary Operators)
  //----------------------------------------------------------------------------
  /* sl, sr and bsr mean "shift right", "shift left" and "binary shift right" */
  //----------------------------------------------------------------------------

  public final Expression pipe        = operator("|",     "=|");
  public final Expression and         = operator("&",     "=&");
  public final Expression hat         = operator("^",     "=");
  public final Expression hatEq       = operator("^=");
  public final Expression sl          = operator("<<",    "=");
  public final Expression sr          = operator(">>",    "=>");
  public final Expression bsr         = operator(">>>",   "=");

  public final Expression pipeEq      = operator("|=");
  public final Expression andEq       = operator("&=");
  public final Expression slEq        = operator("<<=");
  public final Expression srEq        = operator(">>=");
  public final Expression bsrEq       = operator(">>>=");

  public final Expression tilde       = operator("~");

  //----------------------------------------------------------------------------
  // JLS 3.12 (Operators) (3: Logic Operators)
  //----------------------------------------------------------------------------

  public final Expression bang        = operator("!", "=");
  public final Expression andAnd      = operator("&&");
  public final Expression orOr        = operator("||");

  //----------------------------------------------------------------------------
  // JLS 3.12 (Operators) (4: Comparison Operators)
  //----------------------------------------------------------------------------
  /* ge, gt, le, lt mean greater and equal, greater than, lower and equal, .. */
  //----------------------------------------------------------------------------

  public final Expression eqEq        = operator("==");
  public final Expression notEq       = operator("!=");
  public final Expression ge          = operator(">=");
  public final Expression gt          = operator(">", "=>");
  public final Expression le          = operator("<=");
  public final Expression lt          = operator("<", "=>");

  //----------------------------------------------------------------------------
  // JLS 3.12 (Operators) (5: Other Operators)
  //----------------------------------------------------------------------------

  public final Expression at          = operator("@");
  public final Expression colon       = operator(":");
  public final Expression comma       = operator(",");
  public final Expression dot         = operator(".");
  public final Expression ellipsis    = operator("...");
  public final Expression eq          = operator("=", "=");
  public final Expression qMark       = operator("?");
  public final Expression semi        = operator(";");

  //----------------------------------------------------------------------------
  // Keywords
  //----------------------------------------------------------------------------
  /* goto and const are not used by Java but are reserved for the compiler to
   * produce better error message if those C/C++ keywords are used. */
  //----------------------------------------------------------------------------

  public final Expression _abstract       = keyword("abstract");
  public final Expression _assert         = keyword("assert");
  public final Expression _break          = keyword("break");
  public final Expression _case           = keyword("case");
  public final Expression _catch          = keyword("catch");
  public final Expression _class          = keyword("class");
  public final Expression _continue       = keyword("continue");
  public final Expression _default        = keyword("default");
  public final Expression _do             = keyword("do");
  public final Expression _else           = keyword("else");
  public final Expression _enum           = keyword("enum");
  public final Expression _extends        = keyword("extends");
  public final Expression _finally        = keyword("finally");
  public final Expression _final          = keyword("final");
  public final Expression _for            = keyword("for");
  public final Expression _if             = keyword("if");
  public final Expression _implements     = keyword("implements");
  public final Expression _import         = keyword("import");
  public final Expression _interface      = keyword("interface");
  public final Expression _instanceof     = keyword("instanceof");
  public final Expression _native         = keyword("native");
  public final Expression _new            = keyword("new");
  public final Expression _package        = keyword("package");
  public final Expression _private        = keyword("private");
  public final Expression _protected      = keyword("protected");
  public final Expression _public         = keyword("public");
  public final Expression _return         = keyword("return");
  public final Expression _static         = keyword("static");
  public final Expression _strictfp       = keyword("strictfp");
  public final Expression _super          = keyword("super");
  public final Expression _switch         = keyword("switch");
  public final Expression _synchronized   = keyword("synchronized");
  public final Expression _this           = keyword("this");
  public final Expression _throw          = keyword("throw");
  public final Expression _throws         = keyword("throws");
  public final Expression _transient      = keyword("transient");
  public final Expression _try            = keyword("try");
  public final Expression _void           = keyword("void");
  public final Expression _volatile       = keyword("volatile");
  public final Expression _while          = keyword("while");
}