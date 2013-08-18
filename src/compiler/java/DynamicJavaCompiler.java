package compiler.java;

import static util.ListUtils.list;

import java.util.List;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import compiler.util.ThrowingDiagnosticListener;

/**
 * A class that supports dynamic compilation of Java code. I.e. compiling code
 * from within your program.
 */
public class DynamicJavaCompiler
{
  /****************************************************************************/
  private static final DynamicJavaCompiler instance = new DynamicJavaCompiler();
  private DynamicJavaCompiler() {}
  public static DynamicJavaCompiler get() { return instance; }

  /****************************************************************************/
  private final JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();

  /****************************************************************************/
  private final CollectingJavaFileManager fm = new CollectingJavaFileManager(
      compiler.getStandardFileManager(null, null, null));


  /*****************************************************************************
   * Single parameter version of {@link #compile(List, boolean)}.
   */
  public List<CompiledClass> compile(JavaFileObject source)
  {
    return compile(list(source));
  }

  /*****************************************************************************
   * Compile the supplied source objects. A source object represents
   * a source file.
   */
  public List<CompiledClass> compile(List<JavaFileObject> sources)
  {
    compiler.getTask(null, fm, new ThrowingDiagnosticListener(),
      null, null, sources).call();

    return fm.classes;
  }
}
