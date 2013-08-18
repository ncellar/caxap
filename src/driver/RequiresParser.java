package driver;

import static trees.MatchSpec.or;
import static trees.MatchSpec.rule;
import static trees.MatchSpec.str;
import static util.ArrayUtils.arr;
import static util.ListUtils.list;
import static util.ListUtils.removeLast;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import files.Package;
import files.RelativeSourcePath;
import files.Require;
import grammar.java._D_Requires;
import parser.Match;
import parser.Matcher;
import trees.MatchSpec;
import util.Result;

/**
 * See section of the section titled "The require Statement: Importing Macros
 * and Specifying Dependencies" in my thesis for a comprehensive description of
 * the require statement.
 *
 * For the grammar of the require statement, see
 * {@link _D_Requires#requireDeclaration}.
 */
public class RequiresParser
{
  /*****************************************************************************
   * {@see {@link SourceParseManager#parseRequires(List, List)}}
   *
   * This function will advance the matcher past the file's package, import and
   * require statements, or throws a runtime error if it encounters a parse
   * error on that segment.
   */
  void parseRequires(Matcher matcher, Result<String> pkg, List<Require> requires, List<String> imports)
  {
    if (matcher.matches(Context.get().grammar().rule("fullPrelude")))
    {
      Match prelude = matcher.match();
      pkg.set(prelude.first(rule("qualifiedIdentifier")).string());
      parseRequires(prelude, requires, imports);
    }
    else
    {
      throw new Error(
        "Parsing error while reading package/import/require statements:\n\n"
        + matcher.errors().report(matcher.source()));
    }
  }

  /****************************************************************************/
  private void parseRequires(Match prelude,
    List<Require> requires, List<String> imports)
  {
    for (Match m : prelude.all(rule("importDeclaration")))
    {
      if (m.has(rule("requireDeclaration")))
      {
        parseRequire(requires, m);
      }
      else /* import */
      {
        imports.add(m.string());
      }

      /* In the case of an "import", we could check if the .class file exists
       * here, but it seems complicated to do in practice (need to write own
       * code to traverse directories, .jar and .zip files). */
    }
  }

  /****************************************************************************/
  private void parseRequire(List<Require> out, Match match)
  {
    final boolean isMacro  = match.has(rule("macro"));
    final boolean isStatic = match.has(rule("_static"));
    final int     nbColons = match.all(rule("colon")).length;

    List<String> chain1 = new ArrayList<>();
    List<String> chain2 = new ArrayList<>();

    MatchSpec idOrStar    = or(rule("identifier"), str("*"));
    MatchSpec semiOrColon = or(rule("colon"),      rule("semi"));

    Match[] chain1m = match.allBeforeFirst(idOrStar, semiOrColon);
    Match[] chain2m = match.allBetween(idOrStar,
      arr(rule("colon")), arr(rule("semi")));

    for (Match m : chain1m) { chain1.add(m.string()); }
    for (Match m : chain2m) { chain2.add(m.string()); }

    String fileStem = removeLast(chain1);

    if (nbColons == 0 && fileStem.equals("*"))  // Package-Wide Imports
    {
      out.addAll(processPackageRequire(chain1, isMacro));
      return;
    }
    else if (nbColons == 0 || nbColons == 2)    // Syntactic Sugars
    {
      chain2.add(0, fileStem);
    }

    if (!isMacro && !chain2.isEmpty() && chain2.get(0).equals("*"))
    {
      throw new Error("Can't have file-wide imports for regular imports.");
    }

    RelativeSourcePath relative = new RelativeSourcePath(
      new Package(chain1),
      fileStem + '.' + (isMacro ? SourceFile.MACRO_EXT : "java"));

    String member = isMacro || isStatic ? removeLast(chain2) : null;

    out.add(new Require(relative, chain2, member));
  }

  /*****************************************************************************
   * When a package-wide require is encountered, we use the
   * {@link SourceRepository} to ensure the {@link SourceFile} for each source
   * file in the package is loaded (either regular source files, or macro source
   * files).
   *
   * Note we do create {@link SourceFile} for files depended upon via
   * package-wide requires, but not for other kind of requires. The reason is to
   * preserve the separation of concerns as much as possible. Since package-wide
   * require require a file-system scan to know the files depended upon, we might
   * as well ensure they are loaded while we're at it.
   */
  private Collection<Require> processPackageRequire(
    List<String> pkgCompo, boolean isMacro)
  {
    Package pkg = new Package(pkgCompo);
    String  ext = isMacro ? SourceFile.MACRO_EXT : "java";

    Collection<SourceFile> files = Context.get().repo.findAll(pkg, ext);
    Collection<Require>    out   = new ArrayList<>(files.size());

    for (SourceFile file : files)
    {
      List<String> classes = isMacro ? null : list("*");
      String       member  = isMacro ? "*"  : null;

      out.add(new Require(file.path(), classes, member));
    }

    return out;
  }
}
