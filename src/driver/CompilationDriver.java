package driver;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import files.BackedRequire;
import compiler.PostParser;
import compiler.java.CompiledClass;
import compiler.java.DynamicJavaCompiler;
import compiler.java.MemoryClassLoader;
import compiler.java.StringJavaFileObject;
import parser.Match;
import util.FileUtils;

/**
 * Using an ordered list of source files as input, compiles everything and
 * ensures that the framework's products are placed at the appropriate location.
 */
public class CompilationDriver
{
  /*****************************************************************************
   * @see {@link CompilationDriver}
   */
  public static void drive(List<SourceFile> files)
  {
    new CompilationDriver().run(files);
  }

  /****************************************************************************/
  private static final PostParser postParser = new PostParser();

  /****************************************************************************/
  private static final DynamicJavaCompiler compiler = DynamicJavaCompiler.get();

  /****************************************************************************/
  private void run(List<SourceFile> files)
  {
    for (SourceFile file : files)
    {
      Context.get().currentFile = file;
      enableRequiredMacros(file);

      String code = expandFile(file);

      /* Macros defined in the file are compiled during the parse, via
       * callbacks. This is needed because subsequent macros in the file might
       * use macros defined earlier. The callback will also add the macro to
       * $file. See {@link grammar.java.CallbacksMacroDefinition}. */

      if (!file.isMacro() && file.isCompileTimeDependency())
      {
        List<CompiledClass> classes = compileFile(file, code);
        dumpAndOrLoadClasses(classes);
      }

      if (!file.isMacro() || Config.get().dumpMacroSource) {
        writeGeneratedSource(file, code);
      }

      disableMacros(file);
    }

    Context.get().currentFile = null;
  }

  /*****************************************************************************
   * Returns the macro-expansion of $file.
   */
  private String expandFile(SourceFile file)
  {
    Match match = file.parser().parse();
    return postParser.run(match).originalString();
  }

  /*****************************************************************************
   * Returns the compiled classes for the classes defined in $code, which should
   * be the result of macro-expanding the code from $file. $file should not be a
   * macro file.
   */
  private List<CompiledClass> compileFile(SourceFile file, String code)
  {
    return
      compiler.compile(new StringJavaFileObject(file.path(), code));
  }


  /****************************************************************************/
  public static void dumpAndOrLoadClasses(List<CompiledClass> classes)
  {
    dumpAndOrLoadClasses(classes, MemoryClassLoader.get(),
      Config.get().targetDir(), true, Config.get().cache());
  }

  /******************************************************************************
   * Load $classes with $classLoader if $load, write them in $targetDir if $dump.
   */
  public static void dumpAndOrLoadClasses(List<CompiledClass> classes,
    MemoryClassLoader loader, Path targetDir, boolean load, boolean dump)
  {
    for (CompiledClass klass : classes) {
      dumpAndOrLoadClass(klass, loader, targetDir, load, dump);
    }
  }

  /****************************************************************************/
  public static void dumpAndOrLoadClass(CompiledClass klass)
  {
    dumpAndOrLoadClass(klass, MemoryClassLoader.get(),
      Config.get().targetDir(), true, Config.get().cache());
  }

  /****************************************************************************/
  public static void dumpAndOrLoadClass(CompiledClass klass,
    MemoryClassLoader loader, Path targetDir, boolean load, boolean dump)
  {
    if (load) { klass.load(loader); }
    if (dump) { klass.dump(targetDir); }
  }

  /****************************************************************************/
  private void writeGeneratedSource(SourceFile file, String code)
  {
    File dest = Config.get().generatedSrcDir()
      .resolve(file.path().relativePath()).toFile();

    FileUtils.write(dest, code);
  }

  /****************************************************************************/
  private void enableRequiredMacros(SourceFile file)
  {
    for (BackedRequire bReq : file.requires().get()) {
      bReq.enableRequiredMacros();
    }
  }

  /****************************************************************************/
  private void disableRequiredMacros(SourceFile file)
  {
    for (BackedRequire bReq : file.requires().get()) {
      bReq.disableRequiredMacros();
    }
  }

  /*****************************************************************************
   * Disable the macros required by $file, and defined by it.
   */
  private void disableMacros(SourceFile file)
  {
    file.disableMacros();
    disableRequiredMacros(file);
  }
}