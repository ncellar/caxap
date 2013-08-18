package driver;

import java.nio.charset.Charset;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * A singleton that gives user-defined options for the application and a few
 * global constants. Some defaults are set in {@link EntryPoint#setDefaults()}.
 */
public class Config
{
  /****************************************************************************/
  private static Config instance = new Config();

  /****************************************************************************/
  public static Config get()
  {
    return instance;
  }

  /****************************************************************************/
  private Config() {}

  /****************************************************************************/
  public final Path curDir = Paths.get(System.getProperty("user.dir"));

  /*****************************************************************************
   * Charset used for source files. Can be customized via the command line
   * option "-charset".
   */
  Charset charset = Charset.forName("UTF-8");

  /*****************************************************************************
   * Root source directories (package names are relative to one of those
   * directories). Can be customized with the command line option "-root"
   * (multiple such options allowed).
   */
  List<Path> roots = new ArrayList<>();

  /*****************************************************************************
   * Cache the result of compiling macros and macro-using files (.class files).
   * Can be customized with the command line option "-cache".
   *
   * In the future, this will also controls lazy recompilation (don't recompile
   * what couldn't have changed).
   */
  boolean cache = true;

  /*****************************************************************************
   * Output the macro expander sources under the {@link #generatedSrcDir}
   * directory. Can be customized with the command line option "-dump".
   */
  boolean dumpMacroSource = true;

  /*****************************************************************************
   * Where output .class files (if $cache == true). Can be customized with the
   * command line option "-target".
   */
  Path targetDir = null;

  /*****************************************************************************
   * Root source directory for generated java source (if $outputSource == true).
   * Can be customized with the command line option "-generated".
   */
  Path generatedSrcDir = null;

  /****************************************************************************/
  public boolean cache()
  {
    return cache;
  }

  /****************************************************************************/
  public Path targetDir()
  {
    return targetDir;
  }

  /****************************************************************************/
  public Path generatedSrcDir()
  {
    return generatedSrcDir;
  }

  /****************************************************************************/
  public Charset charset()
  {
    return charset;
  }
}
