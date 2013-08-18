package main;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * A list of class of the OpenJDK source to be ignored when testing the caxap
 * parser.
 */
public class OpenJDKExcludes
{
  private final static String[] arrayExcludes = new String[]
    {
// Manually Inspected
"C:\\h\\desk\\openjdk\\jdk\\test\\java\\util\\WeakHashMap\\GCDuringIteration.java",             // (missing semicolon)
"C:\\h\\desk\\openjdk\\langtools\\test\\com\\sun\\javadoc\\testClassTree\\pkg\\Coin.java",      // (public enum constructor + enum constants not using the constructor)
"C:\\h\\desk\\openjdk\\langtools\\test\\com\\sun\\javadoc\\testIndex\\pkg\\Coin.java",          // (same)
"C:\\h\\desk\\openjdk\\langtools\\test\\com\\sun\\javadoc\\testLinkTaglet\\checkPkg\\B.java",   // (abstract method in regular class)
"C:\\h\\desk\\openjdk\\langtools\\test\\com\\sun\\javadoc\\testSourceTab\\DoubleTab\\C.java",   // (/t in code)
"C:\\h\\desk\\openjdk\\langtools\\test\\com\\sun\\javadoc\\testSourceTab\\SingleTab\\C.java",   // (same)
"C:\\h\\desk\\openjdk\\langtools\\test\\com\\sun\\javadoc\\testSupplementary\\C.java",          // (identifier starting with invalid unicode chars)
"C:\\h\\desk\\openjdk\\langtools\\test\\com\\sun\\javadoc\\testUnnamedPackage\\BadSource.java", // (text and no code)
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\4846262\\Test.java",                      // (semicolon missing)
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\6440583\\A.java",                         // (addition as statement)
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\annotations\\neg\\AnnComma.java",         // (comment is incorrect, it is only for array initializers in annotations)
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\annotations\\neg\\Syntax1.java",          // (meant to be an ill-formed annotation)
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\annotations\\neg\\Z12.java",              // (void annotation attribute type)
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\annotations\\neg\\Z13.java",              // (unauthorized throw)
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\annotations\\neg\\Z14.java",              // (parameterized annotation)
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\6302184\\T6302184.java",                  // (8859 encoding)

// Blindly Excluded
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\annotations\\neg\\Z2.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\annotations\\neg\\Z3.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\annotations\\neg\\Z5.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\annotations\\neg\\Z8.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\annotations\\neg\\Z9.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\api\\T6265137a.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\BadAnnotation.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\BadHexConstant.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\DefiniteAssignment\\ConstantInfiniteWhile.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\AbstractMethodCantHaveBody.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\AnnotationMustBeNameValue.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\ArrayDimMissing.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\AssertAsIdentifier.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\AssertAsIdentifier2.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\CallMustBeFirst.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\CannotCreateArrayWithTypeArgs.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\CantExtendIntfAnno.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\CatchWithoutTry.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\DefaultAllowedInIntfAnnotationMember.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\DiamondAndExplicitParams.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\DotClassExpected.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\ElseWithoutIf.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\EmptyCharLiteral.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\EnumAsIdentifier.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\EnumAsIdentifier2.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\Expected2.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\Expected3.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\FinallyWithoutTry.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\IdentifierExpected.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\IllegalChar.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\IllegalEscapeChar.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\IllegalLineEndInCharLit.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\IllegalNonAsciiDigit.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\IllegalStartOfExpr.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\IllegalUnderscore.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\IllegalUnicodeEscape.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\InterfaceNotAllowed.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\IntfAnnotationCantHaveTypeParams.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\IntfAnnotationsCantHaveParams.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\IntfAnnotationsCantHaveTypeParams.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\IntfMethodCantHaveBody.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\InvalidBinaryNumber.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\InvalidHexNumber.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\LocalEnum.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\MalformedFpLit.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\MissingMethodBody.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\ModifierNotAllowed.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\NativeMethodCantHaveBody.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\Orphaned.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\PrematureEOF.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\ProcessorWrongType\\ProcessorWrongType.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\ThrowsNotAllowedInAnno.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\TryWithoutCatchOrFinally.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\TryWithoutCatchOrFinallyOrResource.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\TypeReqClassArray.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\UnclosedCharLiteral.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\UnclosedComment.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\UnclosedStringLiteral.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\UnsupportedBinaryLiteral.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\diags\\examples\\VarargsAndOldArraySyntax.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\Digits.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\enum\\6384542\\T6384542.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\enum\\6384542\\T6384542a.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\enum\\EnumProtectedConstructor.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\enum\\EnumPublicConstructor.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\enum\\ExplicitlyAbstractEnum1.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\enum\\ExplicitlyAbstractEnum2.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\enum\\ExplicitlyFinalEnum1.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\enum\\ExplicitlyFinalEnum2.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\enum\\FauxSpecialEnum1.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\enum\\FauxSpecialEnum2.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\enum\\LocalEnum.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\EOI.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\ExtendArray.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\ExtraneousEquals.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\failover\\FailOver01.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\FloatingPointChanges\\BadConstructorModifiers.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\FloatingPointChanges\\Test.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\generics\\6413682\\T6413682.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\generics\\6946618\\T6946618c.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\generics\\diamond\\neg\\Neg03.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\generics\\diamond\\pos\\Pos03.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\generics\\syntax\\6318240\\BarNeg1a.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\generics\\syntax\\6318240\\BarNeg2a.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\generics\\typevars\\5060485\\Compatibility02.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\generics\\typevars\\6680106\\T6680106.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\IllegalAnnotation.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\implicitThis\\6541876\\T6541876a.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\InterfaceMemberClassModifiers.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\javazip\\bad\\B.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\LabeledDeclaration.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\literals\\BadBinaryLiterals.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\literals\\BadUnderscoreLiterals.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\literals\\BinaryLiterals.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\literals\\T6891079.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\literals\\UnderscoreLiterals.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\multicatch\\Pos10.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\Parens2.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\Parens3.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\ParseConditional.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\policy\\test3\\A.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\processing\\6994946\\SyntaxErrorTest.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\processing\\errors\\TestParseErrors\\ParseErrors.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\quid\\T6999438.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\rawDiags\\Error.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\StandaloneQualifiedSuper.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\StoreClass.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\StringsInSwitch\\StringSwitches.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\SynchronizedClass.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\T4994049\\T4994049.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\T6882235.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\tree\\T6963934.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\tree\\TestAnnotatedAnonClass.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\TryWithResources\\BadTwrSyntax.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\TryWithResources\\PlainTry.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\typeAnnotations\\newlocations\\BasicTest.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\unicode\\NonasciiDigit.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\unicode\\NonasciiDigit2.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\unicode\\SupplementaryJavaID1.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\unicode\\SupplementaryJavaID2.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\unicode\\SupplementaryJavaID3.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\unicode\\SupplementaryJavaID4.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\unicode\\SupplementaryJavaID5.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\unicode\\SupplementaryJavaID6.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\unicode\\TripleQuote.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\UseEnum.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\varargs\\6569633\\T6569633.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javac\\VoidArray.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javadoc\\6964914\\Error.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javadoc\\6964914\\JavacWarning.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javadoc\\enum\\docComments\\pkg1\\Operation.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javadoc\\sourceOption\\p\\A.java",
"C:\\h\\desk\\openjdk\\langtools\\test\\tools\\javadoc\\T4994049\\FileWithTabs.java",
  };

  public final static Set<String> excludes = new HashSet<String>(
    Arrays.asList(arrayExcludes));
}
