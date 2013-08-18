package driver;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import util.FileUtils;
import files.RootedSourcePath;

/**
 * caxap's entry point. Processes command line options and store them in
 * {@link Config}. Finds all source files, the uses {@link DependencyResolver}
 * to order them in compilation order. Uses {@link CompilationDriver} to compile
 * the files.
 */
public class EntryPoint
{
  /****************************************************************************/
  public static void main(String[] args)
  {
    new EntryPoint().run(args);
  }

  /*****************************************************************************
   * Macro source files.
   */
  List<RootedSourcePath> macroPaths = new ArrayList<>();

  /*****************************************************************************
   * Non-macro source files.
   */
  List<RootedSourcePath> sourcePaths = new ArrayList<>();

  /****************************************************************************/
  public void run(String[] args)
  {
    int i = 0;
    while (i < args.length) {
      i = processCommandLineParameter(args, i);
    }
    setDefaults();
    getPaths();

    List<SourceFile> orderedFiles =
      DependencyResolver.resolve(macroPaths, sourcePaths);

    CompilationDriver.drive(orderedFiles);
  }

  /****************************************************************************/
  void getPaths()
  {
    try {
      for (Path root : Config.get().roots)
      {
        for (Path p : FileUtils.glob(root, "**.javam")) {
          macroPaths.add(new RootedSourcePath(root, p, false));
        }
        for (Path p : FileUtils.glob(root, "**.java")) {
          sourcePaths.add(new RootedSourcePath(root, p, false));
        }
      }
    }
    catch (IOException e) {
      throw new Error("Error while obtaining list of macro files.", e);
    }
  }

  /*****************************************************************************
   * Processes the command line parameter at index i, then returns the index
   * of the next argument to process.
   */
  int processCommandLineParameter(final String[] args, final int i)
  {
    switch(args[i])
    {
    case "-charset":
      if (i+1 < args.length) {
        Config.get().charset = Charset.forName(args[i+1]);
        return i + 2;
      }
      break;

    case "-source":
      if (i+1 < args.length) {
        Config.get().roots.add(Paths.get(args[i + 1]));
        return i + 2;
      }
      break;

    case "-binary":
      if (i+1 < args.length) {
        Config.get().targetDir = Paths.get(args[i + 1]);
        return i + 2;
      }
      break;

    case "-output":
      if (i+1 < args.length) {
        Config.get().generatedSrcDir = Paths.get(args[i + 1]);
        return i + 2;
      }
      break;

    case "-cache":
      if (i+1 < args.length) {
        Config.get().cache = Boolean.valueOf(args[i+1]);
        return i + 2;
      }
      break;

    case "-dump":
      if (i+1 < args.length) {
        Config.get().dumpMacroSource = Boolean.valueOf(args[i+1]);
        return i + 2;
      }
      break;

    default:
      System.out.println("Ignoring unknown option: \"" + args[i] + "\"");
      return i + 1;
    }

    System.out.println("Expected an argument to option: \"" + args[i] + "\"");
    return i + 1;
  }

  /****************************************************************************/
  public void setDefaults()
  {
    Config cfg = Config.get();

    if (cfg.roots.isEmpty()) {
      cfg.roots.add(Paths.get("src"));
    }

    if (cfg.targetDir == null) {
      cfg.targetDir = Paths.get("target/classes");
    }

    if (cfg.generatedSrcDir == null) {
      cfg.generatedSrcDir = Paths.get("generated");
    }
  }
}
