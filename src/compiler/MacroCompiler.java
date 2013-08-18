package compiler;

import static util.StringUtils.builderAppend;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import files.RelativeSourcePath;
import compiler.macros.MacroInterface;
import driver.CompilationDriver;
import compiler.java.CompiledClass;
import compiler.java.DynamicJavaCompiler;
import compiler.java.StringJavaFileObject;
import driver.Context;

/**
 * A macro compiler turns the user-defined macro expansion code into a class,
 * and loads this class in order to be able to expand the defined macro.
 *
 * TODO Code might be cleaner if using dynamicQuote().
 */
public class MacroCompiler
{
  /*****************************************************************************
   * The package in which to place macro expander classes.
   */
  public static final String MACRO_PKG = "compiler.macros";

  /****************************************************************************/
  private final DynamicJavaCompiler compiler = DynamicJavaCompiler.get();

  /*****************************************************************************
   * Same as {@link #compile(String, List, List, String)}, but fills in
   * some defaults.
   */
  public MacroInterface compile(String macroName, String macroBody)
  {
    Context ctx = Context.get();
    Set<String> captureNames = ctx.captureNames;
    return compile(
      macroName, ctx.currentFile.imports(), captureNames, macroBody);
  }

  /*****************************************************************************
   * Converts user-defined expansion code into a class. $macroName should be
   * the user-defined macro name. $imports should be a list of fully qualified
   * classes to import.
   */
  private MacroInterface compile(
    String              macroName,
    Collection<String>  imports,
    Collection<String>  captures,
    String              macroBody)
  {
    StringBuilder code = new StringBuilder("package " + MACRO_PKG + ";\n\n");

    for (String imp : imports) {
      builderAppend(code, imp, "\n");
    }

    code.append("import parser.Match;\n");

    code.append("\npublic class ");
    code.append(macroName);
    code.append("Macro implements MacroInterface {\n  ");
    code.append("public Match expand(Match input) {\n");

    for (String captureName : captures)
    {
      builderAppend(code,
        "    Match[] ", captureName,
        " = input.getCaptures(\"", captureName, "\");\n");
    }

    builderAppend(code, macroBody, "\n  }\n}");

    String name = "compiler.macros." + macroName + "Macro";

    List<CompiledClass> compClasses =
      compiler.compile(new StringJavaFileObject(
        RelativeSourcePath.make(name), code.toString()));

    CompiledClass macroCompClass = null;

    for (CompiledClass compClass : compClasses) {
      if (compClass.name.equals(name)) {
        macroCompClass = compClass;
        break;
      }
    }

    CompilationDriver.dumpAndOrLoadClass(macroCompClass);
    Class<?> klass = macroCompClass.klass();

    try {
      return (MacroInterface) klass.newInstance();
    }
    catch (Exception e) {
      throw new Error("Error when instantiating macro expansion code for macro: "
        + macroName + ".", e);
    }
  }
}
