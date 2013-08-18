package grammar.java;

import static grammar.GrammarDSL.*;
import grammar.Expression;

/**
 * Grammar rules for Java statements and declarations.
 *
 * @see JavaGrammar
 */
public class _C_Statements extends _B_Expressions
{
  //============================================================================
  // STATEMENTS
  //============================================================================

  //----------------------------------------------------------------------------
  // VARIABLE DECLARATION
  //----------------------------------------------------------------------------

  {
    (this).toString();
  }

  /*****************************************************************************
   * A variable name. Not simply an identifier because Java is "backward
   * compatible" with the C syntax for array declaration (putting square
   * brackets after the variable name instead of after the type).
   */
  public final Expression variableDeclaratorId = rule_seq(
    identifier, star(square));

  /*****************************************************************************
   * A prefix that appears in the declarations of non-field variables.
   */
  public final Expression variableDeclarationPrefix = rule_seq(
     star(annotation), opt(_final, star(annotation)), type);

  /*****************************************************************************
   * A (variable name, value) pair, used in local variable declarations.
   */
  public final Expression variableDeclarator = rule_seq(
    variableDeclaratorId, opt(eq, variableInitializer));

  /****************************************************************************/
  public final Expression localVariableDeclaration = rule_seq(
    variableDeclarationPrefix,
    list(comma, variableDeclarator)
  );

  //----------------------------------------------------------------------------
  // BLOCKS AND STATEMENT EXPRESSIONS
  //----------------------------------------------------------------------------

  /****************************************************************************/
  public final Expression methodInvocation = rule(
    nonPrimaryMethodInvocation,
    primaryMethodInvocation // prefix of nonPrimaryMethodInfocation
  );

  /*****************************************************************************
   * Only expression that may have side effects can be used as statements.
   */
  public final Expression statementExpression = rule(
    assignment,
    incrementExpression,
    methodInvocation,
    innerCreator,
    seq(_new, creator),
    innerCreator
  );

  // TODO strictpf is valid here (and for abstract classes too)

  /*****************************************************************************
   * This is the prefix for classes defined inside a block. Notice the
   * similarity with variableDeclarationPrefix.
   */
  public final Expression classDeclarationPrefix = rule_seq(
     star(annotation), opt(choice(_final, _abstract)), star(annotation));

  /*****************************************************************************
   * Prefix for abstract classes defined inside blocks.
   */
  public final Expression abstractClassDeclarationPrefix = rule_seq(
   star(annotation), opt(_abstract), star(annotation));

  /*****************************************************************************
   * Things that can go in a block. This is a superset of simple statements.
   * The additional items are declarations. Declarations wouldn't make sense
   * in simple statement, because those statement can go directly after
   * an "if" or "while" (not inside a block).
   */
  public final Expression blockStatement = rule(
    seq(localVariableDeclaration, semi),
    seq(classDeclarationPrefix, ref("classDeclaration")),
    seq(abstractClassDeclarationPrefix, ref("abstractClassDeclaration")),
    ref("statement")
  );

  /****************************************************************************/
  public final Expression block = rule_seq(
    lCuBra, star(blockStatement), rCuBra);

  //----------------------------------------------------------------------------
  // LOOP STATEMENTS
  //----------------------------------------------------------------------------

  /*****************************************************************************
   * A parameter declaration, used in method/constructor declarations, for/try
   * statements, ...
   */
  public final Expression formalParameter = rule_seq(
    variableDeclarationPrefix,
    variableDeclaratorId
  );

  /****************************************************************************/
  public final Expression forInit = rule(
    localVariableDeclaration,
    list(comma, statementExpression)
  );

  /****************************************************************************/
  public final Expression forUpdate = rule(list(comma, statementExpression));

  /****************************************************************************/
  public final Expression forStatement = rule_seq(
    _for, lPar, opt(forInit), semi, opt(expression), semi, opt(forUpdate),
    rPar, ref("statement"));

  /****************************************************************************/
  public final Expression forEachStatement = rule_seq(
    _for, lPar, formalParameter, colon, expression, rPar, ref("statement"));

  /****************************************************************************/
  public final Expression whileStatement = rule_seq(
    _while, parExpression, ref("statement"));

  /****************************************************************************/
  public final Expression doWhileStatement = rule_seq(
    _do, ref("statement"), _while, parExpression, semi);

  //----------------------------------------------------------------------------
  // TRY STATEMENTS
  //----------------------------------------------------------------------------

  /****************************************************************************/
  public final Expression catchBlock = rule_seq(
    _catch, lPar,
    variableDeclarationPrefix, star(pipe, type), variableDeclaratorId,
    rPar, block
  );

  /****************************************************************************/
  public final Expression finallyBlock = rule_seq(_finally, block);

  /****************************************************************************/
  public final Expression tryStatement = rule_seq(
    _try, block,
    choice(
      seq(plus(catchBlock), opt(finallyBlock)),
      finallyBlock
    )
  );

  /*****************************************************************************
   * A resource is a local variable used in a try-with-resources statement.
   * Its type must implement the java.lang.AutoCloseable interface.
   */
  public final Expression resourceDeclaration = rule_seq(
    formalParameter, eq, expression
  );

  /*****************************************************************************
   * Contrary to the regular try statement, there does not need to be a
   * catch or finally block following the try block.
   */
  public final Expression tryWithResourcesStatement = rule_seq(
    _try, lPar, list(semi, resourceDeclaration), opt(semi), rPar,
    block, star(catchBlock), opt(finallyBlock)
  );

  //----------------------------------------------------------------------------
  // OTHER STATEMENTS
  //----------------------------------------------------------------------------

  /*****************************************************************************
   * An assertion checks that the first (boolean) expression evaluates to true.
   * If it doesn't an AssertionError is thrown, and the second expression is
   * passed to its constructor, if present.
   */
  public final Expression assertion = rule_seq(
    _assert, expression, opt(colon, expression), semi);

  /****************************************************************************/
  public final Expression ifStatement = rule_seq(
    _if, parExpression, ref("statement"), opt(_else, ref("statement")));

  /*****************************************************************************
   * Compile-time constant expression. Value of a primitive type or String
   * whose value is known at compile-time, as defined in JLS 15.28. To get an
   * idea why we can't use the grammar to specify those types further, consider
   * that "true ? (int) (1 + 1.0) : 2" is a constant expression.
   */
  public final Expression constantExpression = rule_seq(expression);

  /****************************************************************************/
  public final Expression enumConstantName = rule_seq(identifier);

  /****************************************************************************/
  public final Expression switchLabel = rule_seq(
    choice(
      seq(_case, choice(constantExpression, enumConstantName)),
      _default
    ),
    colon
  );

  /****************************************************************************/
  public final Expression switchBlockStmtGroup = rule_seq(
    switchLabel, star(blockStatement));

  /****************************************************************************/
  public final Expression switchStatement = rule_seq(
    _switch, parExpression, lCuBra, star(switchBlockStmtGroup), rCuBra);

  /****************************************************************************/
  public final Expression synchronizedBlock = rule_seq(
    _synchronized, parExpression, block);

  /****************************************************************************/
  public final Expression returnStatement = rule_seq(
    _return, opt(expression), semi);

  /****************************************************************************/
  public final Expression throwStatement = rule_seq(_throw, expression, semi);

  /*****************************************************************************
   * The "break;" form is only valid as or inside loop bodies, while the
   * "break <label>;" form is valid inside any statement that can have
   * sub-statements (loops, if, switch and blocks, but not anonymous classes).
   *
   * It turns out we could enforce those conditions within the grammar, but it
   * is more trouble than it is worth since the conditions can be trivially
   * checked afterwards. Especially for a real compiler which will need to
   * verify that the referenced labels exist anyway. For the curious, the
   * solution would entail redefining block, if, switch, ... for use
   * within loop statements, leading to some unwelcome duplication.
   */
  public final Expression breakStatement = rule_seq(
    _break, opt(identifier), semi);

  /*****************************************************************************
   * Continue statements are only valid as or inside loop bodies.
   * The remark from breakStatement applies.
   */
  public final Expression continueStatement = rule_seq(
    _continue, opt(identifier), semi);

  /*****************************************************************************
   * A statement preceded by a label, which will allow a quick exit from the
   * statement. A label can be applied to any statement, although it is only
   * ever useful on statements that can have "sub-statements", like blocks,
   * loops, etc.. One the sub-statements can be "break <label>;" which then
   * causes the specified enclosed statement to abort.
   */
  public final Expression labeledStatement = rule_seq(
    identifier, colon, ref("statement"));

  //----------------------------------------------------------------------------
  // STATEMENT
  //----------------------------------------------------------------------------
  public final Expression statement = rule(
    block,
    assertion,
    ifStatement,
    forStatement,
    forEachStatement,
    whileStatement,
    doWhileStatement,
    tryStatement,
    tryWithResourcesStatement,
    switchStatement,
    synchronizedBlock,
    returnStatement,
    throwStatement,
    breakStatement,
    continueStatement,
    semi,
    labeledStatement,
    seq(statementExpression, semi)
  );

  //============================================================================
  // TYPE DECLARATIONS
  //============================================================================
  /* A type is an interface, an annotation type, a class or an enum.
   *
   * This sections concerns itself with modifiers (annotations and keywords such
   * as public, final, ...) a lot. Often we will specify that a declaration is
   * preceded by zero or more modifiers selected amongst those that are valid
   * for the declared element. We could theoretically encode the fact that each
   * keyword modifier cannot appear more than once in the grammar. We chose not
   * to, as it would entail listing every possible keyword ordering. Annotation
   * have a similar restriction, which cannot be encoded in the grammar since
   * annotation names are arbitrary identifiers. */
  //============================================================================

  //----------------------------------------------------------------------------
  // METHOD, CONSTRUCTOR & FIELD DECLARATION
  //----------------------------------------------------------------------------
  /* The base forms of declarations, to be augmented with modifiers
   * (annotations and keywords) in specific kind of types. */
   //----------------------------------------------------------------------------

  /*****************************************************************************
   * The last parameter may be an ellipsis (allows passing variable number
   * of arguments which will be bundled as an array).
   *
   * Surprisingly "int[]... x" is allowed while "int... x[]" is not.
   */
  public final Expression ellipsisParameter = rule_seq(
    variableDeclarationPrefix,
    ellipsis,
    identifier
  );

  /*****************************************************************************
   * Parameter lists for methods and constructors.
   */
  public final Expression formalParameterList = rule_seq(
    lPar,
    opt(choice(
      seq(list(comma, formalParameter), opt(comma, ellipsisParameter)),
      ellipsisParameter
    )),
    rPar
  );

  /****************************************************************************/
  public final Expression methodBody = rule_seq(block);

  /*****************************************************************************
   * The optional squares are used to specify array return types in something
   * that vaguely resembles C-style: "int test() [] ;" is the same as
   * "int[] test() ;".
   */
  public final Expression methodDeclarationPrefix = rule_seq(
    opt(typeParameters),
    choice(
      seq(_void, identifier, formalParameterList),
      seq(type, identifier, formalParameterList, star(square))
    ),
    opt(_throws, list(comma, classType))
  );

  /****************************************************************************/
  public final Expression methodDeclaration = rule_seq(
    methodDeclarationPrefix, methodBody);

  /*****************************************************************************
   * This form is only acceptable in abstract classes and interfaces.
   */
  public final Expression abstractMethodDeclaration = rule_seq(
    methodDeclarationPrefix, semi);

  /*****************************************************************************
   * The first statement in a constructor can be an optional call to another
   * constructor of the same class or to a super-constructor.
   *
   * e.g. plain "super()" is equivalent to "EnclosingClass.this.super()".
   */
  public final Expression constructorBody = rule_seq(
    lCuBra,
    opt(constructorInvocation),
    star(blockStatement),
    rCuBra
  );

  /****************************************************************************/
  public final Expression constructorDeclaration = rule_seq(
    opt(typeParameters),
    identifier,
    formalParameterList,
    opt(_throws, list(comma, classType)),
    constructorBody
  );

  /****************************************************************************/
  public final Expression fieldDeclaration = rule_seq(
    type, list(comma, variableDeclarator), semi);

  //----------------------------------------------------------------------------
  // TYPE DECLARATION MODIFIERS
  //----------------------------------------------------------------------------
  /* Accessibility modifier keywords (public, private, protected) are mutually
   * exclusive. The abstract and final keyword are mutually exclusive.
   *
   * Top-level types have the same modifiers as nested types, except they can't
   * be private, protected or static. All top level types are visible within
   * their package, while public top-level types are visible everywhere. */
  //----------------------------------------------------------------------------

  /****************************************************************************/
  public final Expression accessibilityModifier = rule(
    _public, _protected, _private);

  /****************************************************************************/
  public final Expression accessibilityRestricter = rule(
    _protected, _private);

  /****************************************************************************/
  public final Expression nestedEnumModifier = rule(
   annotation, accessibilityModifier, _static, _strictfp);

  /****************************************************************************/
  public final Expression topLevelEnumModifier = rule(
    annotation, _public, _strictfp);

  /*****************************************************************************
   * The abstract modifier is redundant for an interface. The static modifier is
   * redundant for nested interfaces. They shouldn't be used.
   */
  public final Expression nestedInterfaceModifier = rule(
    annotation, accessibilityModifier, _static, _strictfp, _abstract);

  /****************************************************************************/
  public final Expression topLevelInterfaceModifier = rule(
    annotation, _public, _strictfp, _abstract);

  /*****************************************************************************
   * We omit the abstract modifier from this list, because we treat abstract
   * classes separately from regular classes later on.
   */
  public final Expression nestedClassModifier = rule(
    annotation, accessibilityModifier, _static, _strictfp,  _final);

  /****************************************************************************/
  public final Expression topLevelClassModifier = rule(
    annotation, _public, _strictfp, _final);

  /****************************************************************************/
  public final Expression nestedAbstractClassModifier = rule(
    annotation, accessibilityModifier, _static, _strictfp);

  /****************************************************************************/
  public final Expression topLevelAbstractClassModifier = rule(
    annotation, _public, _strictfp);

  //----------------------------------------------------------------------------
  /* A nested type declared in an interface is implicitly public and static. As
   * a result, such types can't have accessibility restricters as modifiers. */
  //----------------------------------------------------------------------------

  /****************************************************************************/
  public final Expression interfaceEnumModifier = rule_seq(
    not(accessibilityRestricter), nestedEnumModifier);

  /****************************************************************************/
  public final Expression interfaceInterfaceModifier = rule_seq(
    not(accessibilityRestricter), nestedInterfaceModifier);

  /****************************************************************************/
  public final Expression interfaceClassModifier = rule_seq(
    not(accessibilityRestricter), nestedClassModifier);

  /****************************************************************************/
  public final Expression interfaceAbstractClassModifier = rule_seq(
    not(accessibilityRestricter), nestedAbstractClassModifier);

  //----------------------------------------------------------------------------
  /* The following rules for abstract classes modifier lists ensure that the
   * abstract keyword appears only once. */
  //----------------------------------------------------------------------------

  /****************************************************************************/
  public final Expression nestedAbstractClassModifiers = rule_seq(
    star(nestedAbstractClassModifier),
    _abstract,
    star(nestedAbstractClassModifier)
  );

  /****************************************************************************/
  public final Expression interfaceAbstractClassModifiers = rule_seq(
    star(interfaceAbstractClassModifier),
    _abstract,
    star(interfaceAbstractClassModifier)
  );

  /****************************************************************************/
  public final Expression topLevelAbstractClassModifiers = rule_seq(
    star(topLevelAbstractClassModifier),
    _abstract,
    star(topLevelAbstractClassModifier)
  );

  //----------------------------------------------------------------------------
  // TYPE DECLARATION
  //----------------------------------------------------------------------------
  /* Rules for the declaration of types in various context (the modifiers vary
   * according to the context). The common (non-modifier) part of each
   * declaration will be defined later. */
  //----------------------------------------------------------------------------

  /*****************************************************************************
   * Note that non-static nested types are called inner types. Note that
   * static members are not allowed in non-static inner types (not enforced by
   * the grammar).
   */
  public final Expression nestedTypeDeclaration = rule(
    seq(star(nestedInterfaceModifier),     ref("interfaceDeclaration")),
    seq(star(nestedInterfaceModifier),     ref("annotationTypeDeclaration")),
    seq(star(nestedEnumModifier),          ref("enumDeclaration")),
    seq(star(nestedClassModifier),         ref("classDeclaration")),
    seq(nestedAbstractClassModifiers,      ref("abstractClassDeclaration"))
  );

  /****************************************************************************/
  public final Expression interfaceTypeDeclaration = rule(
    seq(star(interfaceInterfaceModifier), ref("interfaceDeclaration")),
    seq(star(interfaceInterfaceModifier), ref("annotationTypeDeclaration")),
    seq(star(interfaceEnumModifier),      ref("enumDeclaration")),
    seq(star(interfaceClassModifier),     ref("classDeclaration")),
    seq(interfaceAbstractClassModifiers,  ref("abstractClassDeclaration"))
  );

  /****************************************************************************/
  public final Expression topLevelTypeDeclaration = rule(
    semi,
    seq(star(topLevelInterfaceModifier),  ref("interfaceDeclaration")),
    seq(star(topLevelInterfaceModifier),  ref("annotationTypeDeclaration")),
    seq(star(topLevelEnumModifier),       ref("enumDeclaration")),
    seq(star(topLevelClassModifier),      ref("classDeclaration")),
    seq(topLevelAbstractClassModifiers,   ref("abstractClassDeclaration"))
  );

  //----------------------------------------------------------------------------
  // INTERFACE DECLARATION
  //----------------------------------------------------------------------------

  /*****************************************************************************
   * Interface fields are implicitly public, static and final, because
   * interfaces are only intended to give a specification of some behavior.
   * Thus all of these modifiers are redundant.
   */
  public final Expression interfaceFieldModifier = rule(
    annotation, _public, _static, _final);

  /*****************************************************************************
   * As a consequence from the immutability of interface fields (see
   * interfaceFieldModifiers), the interface fields initializers should be
   * compile-time constants (as described in JLS 14.8).
   */
  public final Expression interfaceFieldDeclaration = rule_seq(
    star(interfaceFieldModifier), fieldDeclaration);

  /*****************************************************************************
   * Interface methods are public and abstract by default, making those
   * modifiers redundant and discouraged by the JLS as a matter of style.
   */
  public final Expression interfaceMethodModifier = rule(
    annotation, _public, _abstract);

  /****************************************************************************/
  public final Expression interfaceMethodDeclaration = rule_seq(
    star(interfaceMethodModifier), abstractMethodDeclaration);

  /*****************************************************************************
   * Non-method class members are implicitly static.
   * A semicolon is not a member, but can appear in their stead.
   */
  public final Expression interfaceMemberDeclaration = rule(
    semi,
    interfaceMethodDeclaration,
    interfaceFieldDeclaration,
    interfaceTypeDeclaration
  );

  /****************************************************************************/
  public final Expression interfaceBody = rule_seq(
    lCuBra, star(interfaceMemberDeclaration), rCuBra);

  /****************************************************************************/
  public final Expression interfaceDeclaration = rule_seq(
    _interface,
    identifier,
    opt(typeParameters),
    opt(_extends, list(comma, classType)),
    interfaceBody
  );

  //----------------------------------------------------------------------------
  // ANNOTATION DECLARATION
  //----------------------------------------------------------------------------
  /* An annotation type declaration makes a new annotation available for use.
   * The parameters that must/can be passed to the annotation are defined by the
   * attributes (methods) of the annotation type. You can also have a class
   * implement the annotation type. This is a feature best left alone as it has
   * no practical use cases. */
  //----------------------------------------------------------------------------

  /*****************************************************************************
   * A method of an annotation is called an "attribute". Each attribute
   * represent a parameter that must (or can, if a default value is specified)
   * be passed to an annotation when used (see ANNOTATION USAGE).
   *
   * Like interface methods, attributes are public and abstract by default.
   *
   * As per section 9.6.1 of the JLS, an attribute's return types must be one
   * of: primitive types (the boxed version are allowed), String, any
   * parameterized version of Class, an enum type, enumeration type; or
   * one-dimensional array of the aforementioned. This is no enforced by the
   * grammar.
   */
  public final Expression annotationAttributeDeclaration = rule_seq(
    star(interfaceMethodModifier),
    type,
    identifier, lPar, rPar,
    opt(_default, elementValue),
    semi
  );

  /*****************************************************************************
   * Member other than attributes in an annotation declaration follow the same
   * rules as the corresponding members in an interface.
   */
  public final Expression annotationTypeMemberDeclaration = rule(
    semi,
    annotationAttributeDeclaration,
    interfaceFieldDeclaration,
    interfaceTypeDeclaration
  );

  /****************************************************************************/
  public final Expression annotationTypeBody = rule_seq(
    lCuBra, star(annotationTypeMemberDeclaration), rCuBra);

  /****************************************************************************/
  public final Expression annotationTypeDeclaration = rule_seq(
    at, _interface, identifier, annotationTypeBody);

  //----------------------------------------------------------------------------
  // CLASS DECLARATION
  //----------------------------------------------------------------------------

  /****************************************************************************/
  public final Expression classFieldModifier = rule(
    annotation, accessibilityModifier, _static, _final, _transient, _volatile);

  /****************************************************************************/
  public final Expression classFieldDeclaration = rule_seq(
    star(classFieldModifier), fieldDeclaration);

  /****************************************************************************/
  public final Expression classMethodModifier = rule(
    annotation, accessibilityModifier, _final, _static, _strictfp,
    _synchronized, _transient, _volatile);

  /****************************************************************************/
  public final Expression classMethodDeclaration = rule_seq(
    star(classMethodModifier), methodDeclaration);

  /****************************************************************************/
  public final Expression nativeMethodDeclaration = rule_seq(
    star(classMethodModifier), _native, star(classMethodModifier),
    abstractMethodDeclaration);

  /*****************************************************************************
   * The way the rule is setup ensures there will only be a single accessibility
   * modifier.
   */
  public final Expression classConstructorDeclaration = rule_seq(
    star(annotation), opt(accessibilityModifier), star(annotation),
    constructorDeclaration);

  /*****************************************************************************
   * A class-level block is not a class member, but appears at the same place
   * as class members.
   */
  public final Expression classMemberDeclaration = rule(
    semi,
    seq(opt(_static), block),
    nestedTypeDeclaration,
    classMethodDeclaration,
    nativeMethodDeclaration,
    classConstructorDeclaration,
    classFieldDeclaration
  );

  /****************************************************************************/
  public final Expression classBody = rule_seq(
    lCuBra, star(classMemberDeclaration), rCuBra);

  /****************************************************************************/
  public final Expression classDeclaration = rule_seq(
    _class, identifier,
    opt(typeParameters),
    opt(_extends, classType),
    opt(_implements, list(comma, classType)),
    classBody
  );

  //----------------------------------------------------------------------------
  // ABSTRACT CLASS DECLARATION
  //----------------------------------------------------------------------------

  /*****************************************************************************
   * An abstract method can't be private since subclasses have to see the
   * method in order to implement it.
   */
  public final Expression abstractMethodModifier = rule(
    annotation, _public, _protected);

  /****************************************************************************/
  public final Expression abstractMethodDeclarationWithModifiers = rule_seq(
    star(abstractMethodModifier),
    opt(_abstract, star(abstractMethodModifier)),
    abstractMethodDeclaration
  );

  /*****************************************************************************
   * A class-level block is not a class member, but appears at the same place
   * as class members.
   */
  public final Expression abstractClassMemberDeclaration = rule(
    classMemberDeclaration,
    abstractMethodDeclarationWithModifiers
  );

  /****************************************************************************/
  public final Expression abstractClassBody = rule_seq(
    lCuBra, star(abstractClassMemberDeclaration), rCuBra);

  /****************************************************************************/
  public final Expression abstractClassDeclaration = rule_seq(
    _class, identifier,
    opt(typeParameters),
    opt(_extends, classType),
    opt(_implements, list(comma, classType)),
    abstractClassBody
  );

  //----------------------------------------------------------------------------
  // ENUM DECLARATION
  //----------------------------------------------------------------------------
  /* An enum is basically an abstract class which has singleton nested classes
   * extending itself. The nested classes are called enumeration constants. */
  //----------------------------------------------------------------------------

  /*****************************************************************************
   * An enumeration constant declaration can be as simple as an identifier This
   * identifier can also be followed by parameters, which correspond to the
   * parameters to one of the enum's constructors. This can also be followed by
   * a whole class body (and needs to, if the enum itself has abstract
   * methods).
   */
  public final Expression enumConstant = rule_seq(
    star(annotation), identifier, opt(arguments), opt(classBody)
  );

  /*****************************************************************************
   * enum constructor are implicitly private, as they are only meant to be
   * used by enum constants.
   */
  public final Expression enumConstructorDeclaration = rule_seq(
    star(annotation),
    opt(_private, star(annotation)),
    constructorDeclaration
  );

  /****************************************************************************/
  public final Expression enumMemberDeclaration = rule(
    enumConstructorDeclaration,
    seq(not(classConstructorDeclaration), abstractClassMemberDeclaration)
  );

  /****************************************************************************/
  public final Expression enumBody = rule_seq(
    lCuBra,
    opt(list(comma, enumConstant)),
    opt(comma),
    opt(semi, star(enumMemberDeclaration)),
    rCuBra);

  /****************************************************************************/
  public final Expression enumDeclaration = rule_seq(
    _enum, identifier, opt(_implements, list(comma, classType)), enumBody);

  //============================================================================
  // COMPILATION UNIT (TOP LEVEL)
  //============================================================================

  /*****************************************************************************
   * Packages may be annotated. There can be at most one annotated package
   * declaration for any given package. The preferred way is to put this
   * declaration in the file "package-info.java" directly under the package
   * directory. This file does not declare a class. This file can also contain
   * package-level comments: insert a comment before the package declaration.
   */
  public final Expression packageDeclaration = rule_seq(
    star(annotation), _package, qualifiedIdentifier, semi);

  /****************************************************************************/
  public final Expression importDeclaration = rule_seq(
    _import, opt(_static), qualifiedIdentifier, opt(dot, star), semi);

  /****************************************************************************/
  public final Expression prelude = rule_seq(
    spacing,
    opt(packageDeclaration),
    star(importDeclaration)
  );

  /*****************************************************************************
   * A version of the prelude rule that ensures that the whole prelude is parsed
   * and not cut short in case of errors in import declarations.
   */
  public final Expression fullPrelude = rule_seq(
    prelude,
    and(choice(_static, _private, _public, _strictfp, _abstract, _final, _class,
      _interface, _enum, seq(at, _interface), annotation, ref("macro"),
      ref("raw"), ref("prioritary"), endOfInput))
  );

  /****************************************************************************/
  public final Expression compilationUnit = rule_seq(
    prelude,
    star(topLevelTypeDeclaration),
    endOfInput
  );
}
