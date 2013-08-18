package grammar.java;

/**
 * This package contains a full Parsing Expression Grammar (PEG) for Java,
 * according the Java Language Specification (JLS) version 7, and the behavior
 * of the Eclipse Java Compiler.
 *
 * To keep the sources manageable, the actual Java grammar is split across three
 * classes: _A_Lexical, _B_Expressions and _C_Statements. The letters in the
 * class name emphasize the inheritance hierarchy.
 *
 * Additionally, the classes _D_Requires and _E_Macros define additional grammar
 * rules for use in macro-enabled Java code.
 *
 * The grammar is optimized for legibility and understandability.
 *
 * There might be some bugs left in extremely rare edge cases. Notify me at
 * norswap@gmail.com. Don't be afraid, even if you're not sure!
 */
public class JavaGrammar extends _E_MacroDefinitions {}