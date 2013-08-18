package compiler;

import static compiler.util.PEGCompiler.compile;
import static compiler.util.StringMatcher.matchString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static trees.MatchSpec.rule;
import static util.ListUtils.list;

import compiler.macros.MacroInterface;

import compiler.Macro.Strategy;
import driver.Context;
import driver.EntryPoint;
import driver.SourceFile;
import grammar.Expression;
import grammar.Grammar;
import grammar.java._E_MacroDefinitions;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;
import parser.Match;

import static util.MemberAccessor.invoke;

/**
 * Tests the classes {@link MacroCompiler} and {@link MemoryJavaFileManager}.
 */
@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MacroCompilerTests
{
  /****************************************************************************/
  static       Grammar       old;
  static final Grammar       grammar  = new Grammar(_E_MacroDefinitions.class);
  static final MacroExpander expander = new MacroExpander();

  /****************************************************************************/
  @BeforeClass public static void setUp()
  {
    new EntryPoint().setDefaults();
    old = Context.get().grammar();
    invoke(Context.get(), "setGrammar", grammar);
  }

  /****************************************************************************/
  @BeforeClass public static void tearDown()
  {
    if (old != null) invoke(Context.get(), "setGrammar", old);
  }

  /****************************************************************************/
  @Test public void aa_test() throws ClassNotFoundException
  {
    SourceFile file = mock(SourceFile.class);
    when(file.imports()).thenReturn(list(
      "import java.util.List;",
      "import static java.util.Arrays.asList;"
    ));
    Context.get().currentFile = file;

    Expression syntax =
      compile("\"unless\" expr:expression \"{\" stmt:statement \"}\"");

    MacroInterface expander =
      new MacroCompiler().compile("TestMacro", "return stmt[0];");
    Context.get().captureNames.clear();

    Macro macro = new Macro("Unless", "statement", grammar,
      syntax, expander, Strategy.AS, false, false);
    macro.enable();

    Match match = matchString(
      "unless false { System.out.println(\"hello\"); }", "statement");

    match = new MacroExpander().transform(match);
    assertEquals(grammar.rule("statement"), match.expr);
    assertTrue(match.has(rule("methodInvocation")));
  }
}
