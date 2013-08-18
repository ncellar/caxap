package grammar.java;

import static grammar.GrammarDSL.*;

import grammar.Expression;

/**
 * Grammar rules for Java expressions.
 *
 * @see JavaGrammar
 */
public class _B_Expressions extends _A_Lexical
{
  //----------------------------------------------------------------------------
  // COMMONLY USED SUBEXPRESSIONS
  //----------------------------------------------------------------------------

  public final Expression square = rule_seq(lSqBra, rSqBra);

  public final Expression diamond = rule_seq(lAnBra, rAnBra);

  public final Expression qualifiedIdentifier = rule_seq(list(dot, identifier));

  //----------------------------------------------------------------------------
  // TYPES
  //----------------------------------------------------------------------------

  /*****************************************************************************
   * An array type name is a class or primitive type name followed by one or
   * more squares ("[]"). Each square introduces a new dimension in the array.
   * Let x be "int[][] x = new int[2][3];", then "x[0]" is an integer array of
   * size 3.
   */
  public final Expression arrayType = rule_seq(
    choice(primitiveType, ref("classType")),
    plus(square)
  );

  /****************************************************************************/
  public final Expression referenceType = rule(arrayType, ref("classType"));

  /*****************************************************************************
   * The next few sections deal with Java generics. The idea is that some
   * classes take types as parameter upon instantiation. The class definition
   * may reference those types. Similarly, some methods take types as
   * parameters upon method call.
   *
   * We distinguish between type arguments, which are passed upon
   * instantiation/method call, but are also used in types inside variable
   * declarations (at large); and type parameters which specify which types
   * we expect to be supplied.
   */

  /*****************************************************************************
   * A type argument is a reference type or wildcard (question mark). The
   * wildcard can optionally be bounded by super- or sub-types. Wildcards can
   * be used in type names, but not as parameter to a constructor or method
   * call, hence the nonWildcardTypeArguments rule.
   */
  public final Expression typeArgument = rule(
    referenceType,
    seq(qMark, opt(choice(_extends, _super), referenceType))
  );

  /****************************************************************************/
  public final Expression typeArguments = rule_seq(
    lAnBra, list(comma, typeArgument), rAnBra);

  /****************************************************************************/
  public final Expression nonWildcardTypeArguments = rule_seq(
    lAnBra, list(comma, referenceType), rAnBra);

  /****************************************************************************/
  public final Expression classType = rule_seq(list(dot,
    seq(identifier, opt(typeArguments))));

  /****************************************************************************/
  public final Expression nonGenericClassType = rule(qualifiedIdentifier);

  /*****************************************************************************
   * A type bound is an "and" (&) separated list of classes/interfaces that the
   * type argument given for this parameter should extend/implement.
   */
  public final Expression typeBound = rule_seq(list(and, classType));

  /*****************************************************************************
   * Type parameters are given a name (identifier) and an optional bound.
   */
  public final Expression typeParameter = rule_seq(
    identifier, opt(_extends, typeBound));

  /****************************************************************************/
  public final Expression typeParameters = rule_seq(
      lAnBra, list(comma, typeParameter), rAnBra);

  /*****************************************************************************
   * primitiveType has to go second, as it can be a prefix of a reference type.
   * e.g. "int" is a prefix of "int[]".
   */
  public final Expression type = rule(referenceType, primitiveType);

  //----------------------------------------------------------------------------
  // EXPRESSIONS
  //----------------------------------------------------------------------------

  /*****************************************************************************
   * A parenthesized expression.
   */
  public final Expression parExpression = rule_seq(
    lPar, ref("expression"), rPar);

  /*****************************************************************************
   * A comma separated list of expression enclosed in parenthesis, used as
   * argument list for a method or constructor.
   */
  public final Expression arguments = rule_seq(
    lPar, opt(list(comma, ref("expression"))), rPar);

  //----------------------------------------------------------------------------
  // METHOD INVOCATION, CONSTRUCTOR INVOCATION & FIELD ACCESS
  //----------------------------------------------------------------------------

  /*****************************************************************************
   * Either a regular qualified identifier or a qualified identifier with a
   * type argument list before the last identifier.
   */
  public final Expression qualifiedMethodName = rule(
    seq(
      opt(qualifiedIdentifier, dot),
      nonWildcardTypeArguments,
      identifier
    ),
    qualifiedIdentifier  // can be a prefix of previous alternative
  );

  /*****************************************************************************
   * Used as a prefix to invoke methods or access fields on an enclosing
   * class instance ("this"), potentially treating it as an instance of its
   * superclass ("super").
   */
  public final Expression instanceSpecifier = rule_seq(
    opt(nonGenericClassType, dot),
    choice(
      seq(_this, opt(_super)),
      _super
    )
  );

  /*****************************************************************************
   * Used as prefix to inner constructors (see later).
   */
  public final Expression thisInstanceSpecifier = rule_seq(
    opt(nonGenericClassType, dot), _this);

  /****************************************************************************/
  public final Expression instantiatedMethodName = rule_seq(
    opt(instanceSpecifier, dot), qualifiedMethodName);

  /*****************************************************************************
   * If we did not separate primary method invocations from the general case, we
   * would sometimes have to rely on a qualifiedIdentifier as primary
   * expression. The suffix for method invocation would then directly be the
   * method arguments. This is different than for the other primary
   * expressions, where the suffix is something like <dot> <identifier>
   * <arguments>.
   * *************************************************************************
   */
  public final Expression primaryMethodInvocation = rule_seq(
    instantiatedMethodName, arguments);

  /*****************************************************************************
   * This form is only allowed in inner classes: the primary expression will be
   * the instance of the enclosing class for the newly created inner class.
   */
  public final Expression qualifiedSuperConstructorInvocation = rule_seq(
    ref("suffixedPrimaryExpression"), dot,
    opt(nonWildcardTypeArguments), _super, arguments);

  /****************************************************************************/
  public final Expression unqualifiedConstructorInvocation = rule_seq(
    opt(nonWildcardTypeArguments),
    choice(_this, _super),
    arguments
  );

  /*****************************************************************************
   * Constructor invocation. This can only appear as first statement in a
   * constructor body. This is not enforced by the grammar.
   */
  public final Expression constructorInvocation = rule(
    unqualifiedConstructorInvocation,
    qualifiedSuperConstructorInvocation
  );

  /*****************************************************************************
   * Access to a field (class or instance variable) through an explicit "this"
   * or "super" keyword.
   */
  public final Expression instantiatedFieldAccess = rule_seq(
    instanceSpecifier, dot, qualifiedIdentifier);

  /*****************************************************************************
   * Access to a class field, or an instance field through an implicit "this".
   */
  public final Expression regularFieldAccess = rule(qualifiedIdentifier);

  //----------------------------------------------------------------------------
  // CREATORS
  //----------------------------------------------------------------------------
  /* A creator is an expression that follows the "new" keyword. */
  //----------------------------------------------------------------------------

  public final Expression variableInitializer = rule(
    ref("expression"), ref("arrayInitializer"));

  /*****************************************************************************
   * A way to express array as literal values. It can appear on the right hand
   * side of an assignment to an array variable, or following an array
   * constructor call. An array initializer can have an optional trailing
   * comma.
   */
  public final Expression arrayInitializer = rule_seq(
    lCuBra, opt(list(comma, variableInitializer)), opt(comma), rCuBra);

  /****************************************************************************/
  public final Expression squareExpr = rule_seq(lSqBra, ref("expression"), rSqBra);

  /*****************************************************************************
   * In an array creator, either an array initializer must be supplied, or at
   * least the size of the first dimension must be supplied.
   */
  public final Expression arrayCreator = rule_seq(
    choice(classType, primitiveType),
    choice(
      seq(plus(square), arrayInitializer),
      seq(plus(squareExpr), star(square))
    )
  );

  /*****************************************************************************
   * A non-array creator can make use of the diamond to automatically infer the
   * value of the class type parameters. If you use diamond inference, you must
   * use automatic constructor type parameters inference (i.e. if there are
   * constructor type parameters, you can't supply them manually).
   */
  public final Expression diamondCreator = rule_seq(
    nonGenericClassType,
    diamond,
    arguments,
    opt(ref("classBody"))
  );

  /*****************************************************************************
   * A non-array creator can have two sets of type parameters: one is specific
   * to the class and comes after the class name, the other is specific to the
   * constructor and comes before the class name. If omitted, the constructor
   * type parameters (*not* class type parameters) will be inferred
   * automatically. This also applies to methods with type parameters.
   */
  public final Expression nonDiamondCreator = rule_seq(
    opt(nonWildcardTypeArguments),
    nonGenericClassType,
    opt(nonWildcardTypeArguments),
    arguments,
    opt(ref("classBody"))
  );

  /****************************************************************************/
  public final Expression nonArrayCreator = rule(
    diamondCreator, nonDiamondCreator);

  /****************************************************************************/
  public final Expression creator = rule(nonArrayCreator, arrayCreator);

  //----------------------------------------------------------------------------
  // PRIMARY EXPRESSION
  //----------------------------------------------------------------------------
  /* The basic building block from which unary expression are made. */
  //----------------------------------------------------------------------------

  /****************************************************************************/
  public final Expression voidClass = rule_seq(_void, dot, _class);

  /****************************************************************************/
  public final Expression typeClass = rule_seq(type, dot, _class);

  /*****************************************************************************
   * This does not correspond to the JLS "primary" grammar rule (the equivalent
   * is suffixedPrimaryExpression). Note that "super" is not a valid expression
   * by itself.
   */
  public final Expression primaryExpression = rule(
    parExpression,
    seq(_new, creator),
    literal,
    typeClass,
    voidClass,
    primaryMethodInvocation,
    instantiatedFieldAccess,    // prefix: primaryMethodInvocation
    thisInstanceSpecifier,      // prefix: instantiatedFieldAccess, innerCreator
    regularFieldAccess          // prefix: thisInstanceSpecifier
  );

  //----------------------------------------------------------------------------
  // UNARY EXPRESSIONS
  //----------------------------------------------------------------------------
  /* Unary expressions are expressions that can be used as operand to binary
   * and ternary operators. They are formed by appending suffixes and
   * prepending prefixes to primary expressions.
   *
   * In this section and the next one, we will often name the rule in the
   * form xxxExpression. This does not mean that text matching the rule is
   * necessarily an instance of the xxx concept, but rather that it could be,
   * but could also be an instance of any concept with higher precedence.
   * For instance, the methodInvocationExpression rule can match array
   * accesses. */
  //----------------------------------------------------------------------------

  /*****************************************************************************
   * UNREACHABLE
   */
  public final Expression arrayAccessExpression = rule(lrecur(
    primaryExpression, squareExpr));

  /*****************************************************************************
   * Suffix to a primary expression denoting a method invocation.
   */
  public final Expression methodInvocationSuffix = rule_seq(
    dot,
    qualifiedMethodName,
    arguments
  );

  /*****************************************************************************
   * An inner creator is a creator preceded by the new keyword positioned after
   * an expression. It us used to instantiate member classes (the prefix
   * expression being an instance of the enclosing class).
   */
  public final Expression innerCreatorSuffix = rule_seq(dot, _new, nonArrayCreator);

  /*****************************************************************************
   * Suffixes applied to primary expression to make some of the unary
   * expressions. The third alternative represents an inner creator.
   */
  public final Expression primaryExpressionSuffix = rule(
    methodInvocationSuffix,
    seq(dot, identifier), // prefix of methodInvocationSuffix
    innerCreatorSuffix,
    squareExpr
  );

  /*****************************************************************************
   * An expression formed by suffixing (potentially nothing) to a primary
   * expression. This corresponds to the JLS "primary" grammar rule.
   */
  public final Expression suffixedPrimaryExpression = rule(lrecur(
    primaryExpression,
    primaryExpressionSuffix
  ));

  /*****************************************************************************
   * This is a suffixPrimaryExpression that must end with an inner creator.
   */
  public final Expression innerCreator = rule(lrecurPlus(
      primaryExpression,
      endsWith(innerCreatorSuffix, primaryExpressionSuffix)
  ));

  /*****************************************************************************
   * A method invocation that is not a primary expression. This is a
   * suffixPrimaryExpression that must end with a method call.
   */
  public final Expression nonPrimaryMethodInvocation = rule(lrecurPlus(
      primaryExpression,
      endsWith(methodInvocationSuffix, primaryExpressionSuffix)
  ));

  /*****************************************************************************
   * A left-hand side made from an suffixed primaryExpression. This is a
   * suffixedPrimaryExpression that must end with an identifier or array access.
   */
  public final Expression nonPrimaryLeftHand = rule(lrecurPlus(
    primaryExpression,
    endsWith(
      choice(
        seq(dot, identifier, not(arguments)),
        squareExpr
      ),
      primaryExpressionSuffix
    )
  ));

  /*****************************************************************************
   * An expression that can appear on the left-hand side of an assignment
   * operator, or be prefixed or suffixed with an increment operator.
   *
   * Either a field access, a local variable, or an array element access.
   * Those expressions can also be parenthesized.
   *
   * The two last alternative could be prefix of the first one.
   */
  public final Expression leftHandSide = rule(
    nonPrimaryLeftHand,
    seq(lPar, ref("leftHandSide"), rPar), // can be a prefix of previous
    instantiatedFieldAccess,
    regularFieldAccess
  );

  /****************************************************************************/
  public final Expression preIncrementExpression = rule_seq(plusPlus, leftHandSide);

  /****************************************************************************/
  public final Expression preDecrementExpression = rule_seq(minusMinus, leftHandSide);

  /****************************************************************************/
  public final Expression postIncrementExpression = rule_seq(
    leftHandSide, plusPlus);

  /****************************************************************************/
  public final Expression postDecrementExpression = rule_seq(
    leftHandSide, minusMinus);

  /****************************************************************************/
  public final Expression incrementExpression = rule(
    preIncrementExpression,
    preDecrementExpression,
    postIncrementExpression,
    postDecrementExpression
  );

  /****************************************************************************/
  public final Expression typeCast = rule_seq(lPar, type, rPar, ref("unaryExpression"));

  /*****************************************************************************
   * Note that "+++x" is not valid, because Java matches tokens by longest
   * prefix match, which means the expression will always parse as the invalid
   * "++(+x)" and never as the valid "+(++x)". The same principle applies in
   * other situations, so keep it in mind.
   */
  public final Expression unaryExpression = rule(
    typeCast,
    incrementExpression,
    seq(plus,  ref("unaryExpression")),
    seq(minus, ref("unaryExpression")),
    seq(tilde, ref("unaryExpression")),
    seq(bang,  ref("unaryExpression")),
    suffixedPrimaryExpression
  );

  //----------------------------------------------------------------------------
  // BINARY & TERNARY OPERATORS
  //----------------------------------------------------------------------------
  /* Formations of expression using unary Expressions as basic building
   * blocks. Those grammar rules depend heavily on the rules for associativity
   * and precedence for Java operators.
   *
   * Rules starting with "lrecur" are left-associative while those finishing
   * with a reference to the rule itself (right recursion) are
   * right-associative.
   *
   * The operators (and associated expression types) appear in decreasing
   * order of precedence.
   *
   * The name of the rules reflects the name given to operator in the JLS. */
  //----------------------------------------------------------------------------

  /****************************************************************************/
  public final Expression multiplicativeOperator = rule(star, percent, slash);

  /****************************************************************************/
  public final Expression multiplicativeExpression = rule(lrecur(
    unaryExpression,
    seq(multiplicativeOperator, unaryExpression)
  ));

  /****************************************************************************/
  public final Expression additiveOperator = rule(plus, minus);

  /****************************************************************************/
  public final Expression additiveExpression = rule(lrecur(
    multiplicativeExpression,
    seq(additiveOperator, multiplicativeExpression)
  ));

  /****************************************************************************/
  public final Expression shiftOperator = rule(sl, sr, bsr);

  /****************************************************************************/
  public final Expression shiftExpression = rule(lrecur(
    additiveExpression,
    seq(shiftOperator, additiveExpression)
  ));

  /****************************************************************************/
  public final Expression comparisonOperator = rule(le, ge, lt, gt);

  /****************************************************************************/
  public final Expression relationalExpression = rule(lrecur(
    shiftExpression,
    choice(
      seq(comparisonOperator, shiftExpression),
      seq(_instanceof, referenceType)
    )
  ));

  /****************************************************************************/
  public final Expression equalityOperator = rule(eqEq, notEq);

  /****************************************************************************/
  public final Expression equalityExpression = rule(lrecur(
    relationalExpression,
    seq(equalityOperator, relationalExpression)
  ));

  /****************************************************************************/
  public final Expression bitwiseAndExpression = rule(lrecur(
    equalityExpression,
    seq(and, equalityExpression)
  ));

  /****************************************************************************/
  public final Expression bitwiseExclusiveOrExpression = rule(lrecur(
    bitwiseAndExpression,
    seq(hat, bitwiseAndExpression)
  ));

  /****************************************************************************/
  public final Expression bitwiseOrExpression = rule(lrecur(
    bitwiseExclusiveOrExpression,
    seq(pipe, bitwiseExclusiveOrExpression)
  ));

  /****************************************************************************/
  public final Expression conditionalAndExpression = rule(lrecur(
    bitwiseOrExpression,
    seq(andAnd, bitwiseOrExpression)
  ));

  /****************************************************************************/
  public final Expression conditionalOrExpression = rule(lrecur(
    conditionalAndExpression,
    seq(orOr, conditionalAndExpression)
  ));

  /*****************************************************************************
   * aka ternary expression
   */
  public final Expression conditionalExpression = rule_seq(
      conditionalOrExpression,
      opt(qMark, ref("expression"), colon, ref("conditionalExpression"))
  );

  /****************************************************************************/

  public final Expression assignmentOperator = rule(
    eq, plusEq, minusEq, starEq, slashEq, andEq, pipeEq, hatEq,
    modEq, slEq, srEq, bsrEq);

  /****************************************************************************/
  public final Expression assignment = rule_seq(
    leftHandSide, assignmentOperator, ref("expression"));

  /*****************************************************************************
   * Note that this rule is right-recursive through the assignment rule.
   */
  public final Expression assignmentExpression = rule(
    assignment, conditionalExpression);

  /****************************************************************************/
  public final Expression expression = rule(assignmentExpression);

  //----------------------------------------------------------------------------
  // ANNOTATIONS (USAGE)
  //----------------------------------------------------------------------------
  /* The syntax for annotation usage. The syntax to define new annotations will
   * come later. */
  //----------------------------------------------------------------------------

  /*****************************************************************************
   * Similar to a regular array initializer, excepted an annotation is
   * permitted in place of an expression, and expressions can't contain
   * assignments.
   */
  public final Expression elementValueArrayInitializer = rule_seq(
    lCuBra, opt(list(comma, ref("elementValue"))), opt(comma), rCuBra);

  /*****************************************************************************
   * Annotations values can't contain assignments. This condition is not
   * totally enforced by the grammar: some subrules of conditional expressions
   * have references to the expression rule, which can lead to invalid
   * expressions.
   */
  public final Expression elementValue = rule(
    conditionalExpression,
    ref("annotation"),
    elementValueArrayInitializer
  );

  /*****************************************************************************
   * The identifier references an attribute (method) of the selected
   * annotation.
   */
  public final Expression elementValuePair = rule_seq(identifier, eq, elementValue);

  /****************************************************************************/
  public final Expression normalAnnotation = rule_seq(
    at, qualifiedIdentifier, lPar, opt(list(comma, elementValuePair)), rPar);

  /*****************************************************************************
   * @MyAnnotation("x") is a shorthand for @MyAnnotation(value = "x")
   */
  public final Expression singleElementAnnotation = rule_seq(
    at, qualifiedIdentifier, lPar, elementValue, rPar);

  /*****************************************************************************
   * @MyAnnotation is a shorthand for @MyAnnotation()
   */
  public final Expression markerAnnotation = rule_seq(at, qualifiedIdentifier);

  /****************************************************************************/
  public final Expression annotation = rule(
    normalAnnotation,
    singleElementAnnotation,
    markerAnnotation  // can be a prefix of all preceding
  );
}
