package driver;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

import compiler.MacroCompiler;

import grammar.Expression;
import grammar.Grammar;
import grammar.java.JavaGrammar;

/**
 * A singleton that allows to share global data between places where that might
 * otherwise be difficult (e.g. callbacks).
 */
public class Context
{
  /****************************************************************************/
  public static final Grammar MACRO_GRAMMAR = new Grammar(JavaGrammar.class);

  /*****************************************************************************
   * (Must be after other static members.)
   */
  private static final Context instance = new Context();

  /****************************************************************************/
  public static Context get()
  {
    return instance;
  }

  /****************************************************************************/
  public final SourceRepository repo = new SourceRepository();

  /*****************************************************************************
   * The compile-time file currently being parsed, or null.
   */
  public SourceFile currentFile;

  /*****************************************************************************
   * The stack used to compile the string representation of a PEG expression
   * into an Expression graph.
   */
  public Stack<Expression> expressionStack;

  /*****************************************************************************
   * A set of capture names found in the expressions that are in
   * $expressionStack.
   *
   * Set by {@link grammar.java.CaptureCallbacks} and consumed by
   * {@link MacroCompiler#compile()}.
   */
  public Set<String> captureNames;

  /*****************************************************************************
   * Grammar currently used.
   */
  private Grammar grammar;

  /****************************************************************************/
  private Context()
  {
    initialize();
  }

  /****************************************************************************/
  public void initialize()
  {
    expressionStack = new Stack<>();
    captureNames    = new HashSet<>();
    grammar         = MACRO_GRAMMAR;
  }

  /****************************************************************************/
  public Grammar grammar()
  {
    return grammar;
  }

  /****************************************************************************/
  void setGrammar(Grammar grammar)
  {
    this.grammar = grammar;
  }
}
