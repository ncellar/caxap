package driver;

import java.util.List;

import files.Require;
import parser.Match;
import parser.Matcher;
import util.Result;

/**
 * Oversees the parsing of a source file.
 *
 * Methods of this class will behave whatever the order they are called in or
 * whether they are called multiple times.
 */
public class SourceParseManager
{
  /****************************************************************************/
  private final Matcher matcher;

  /****************************************************************************/
  private final SourceFile sourceFile;

  /****************************************************************************/
  SourceParseManager(SourceFile sourceFile)
  {
    this.sourceFile = sourceFile;
    this.matcher = new Matcher(sourceFile.source());
  }

  /*****************************************************************************
   * Parse the require and import statement, storing them as Require and String
   * objects (respectively) in $requires and $imports.
   */
  void parseRequires(List<Require> requires, List<String> imports)
  {
    matcher.reset();
    Result<String> pkg = new Result<>();
    new RequiresParser().parseRequires(matcher, pkg, requires, imports);
    checkPackage(pkg.get());

  }

  /****************************************************************************/
  private void checkPackage(String parsedPkg)
  {
    String expectedPkg = sourceFile.path().pkg().name();

    if (!parsedPkg.equals(expectedPkg)) {
      throw new Error("Declared package does not correspond to directory "
        + "layout in file " + sourceFile + ". \"" + parsedPkg
        + "\" was declared but \"" + expectedPkg + "\" was expected.");
    }
  }

  /*****************************************************************************
   * Parses the whole file.
   */
  public Match parse()
  {
    matcher.reset();

    if (!matcher.matches(Context.get().grammar().rule("compilationUnit")))
    {
      throw new Error(
        "Parsing error in "
          + matcher.source()
          + " :\n\n"
          + matcher.errors().report(matcher.source()));
    }

    return matcher.match();
  }
}
