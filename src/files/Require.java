package files;

import java.util.List;

import util.StringUtils;

/**
 * Represents a require statement made from a source file.
 *
 * A require statement is an import statement that specifies explicitly which
 * source file is being depended upon. See {@link driver.RequiresParser} for
 * more information about require statements.
 *
 * There are three flavors of requires: Regular requires import a class
 * (possibly nested) from a source file. Static requires import a class member
 * from a source file (the member can be a method or a nested class, although a
 * static require for a nested class is similar to a regular require for the
 * same class). Macro requires import a macro from a macro source file.
 *
 * For static and macro requires, the member or macro name (respectively) can
 * also be a star ("*"), meaning all members or macros should be imported.
 *
 * For regular import, the class chain may consist of a single star ("*"),
 * meaning that all top-level classes in the file are required.
 *
 * The class chain may also end with a star, meaning that all the static inner
 * classes of the preceding class chain are required.
 */
public class Require
{
  /****************************************************************************/
  private final RelativeSourcePath path;

  /****************************************************************************/
  private final List<String> classes;

  /****************************************************************************/
  private final String member;

  /*****************************************************************************
   * $path is a relative path to the file containing the required items.
   *
   * $classes is a list of class names forming the path to an imported
   * class (a nested class if classe.size() > 1). null for a macro require.
   *
   * $member is the name of the imported member in a static require, or
   * the name of macro in a macro require. null for a regular require.
   */
  public Require(RelativeSourcePath path, List<String> classes, String member)
  {
    classes = classes != null && classes.isEmpty() ? null : classes;
    member  = member  != null && member .isEmpty() ? null : member;

    this.path    = path;
    this.classes = classes;
    this.member  = member;
  }

  /****************************************************************************/
  public List<String> classes()
  {
    return classes;
  }

  /****************************************************************************/
  public String member()
  {
    return member;
  }

  /****************************************************************************/
  public String macro()
  {
    return member;
  }

  /****************************************************************************/
  public RelativeSourcePath relativePath()
  {
    return path;
  }

  /****************************************************************************/
  public boolean isStatic()
  {
    return member != null && classes != null;
  }

  /****************************************************************************/
  public boolean isMacro()
  {
    return member != null && classes == null;
  }

  /****************************************************************************/
  public boolean isRegular()
  {
    return member == null && classes != null;
  }

  /*****************************************************************************
   * Only for static requires: indicate if all static members in the file
   * are required by this require statement.
   */
  public boolean requiresAllMembers()
  {
    return member.equals("*");
  }

  /*****************************************************************************
   * Only for regular requires: indicates if all top-level classes in the file
   * are required by this require statement.
   */
  public boolean requiresAllClasses()
  {
    return classes.get(0).equals("*");
  }

  /*****************************************************************************
   * A string containing the class names returned by classes(), joined by
   * dots.
   */
  public String classChain()
  {
    if (classes == null) { return ""; }
    return StringUtils.join(classes, ".");
  }

  /*****************************************************************************
   * Returns a String presenting the require as an import statement, if applicable.
   * If not, returns the empty string.
   */
  public String importString()
  {
    if (isMacro()) { return ""; }

    String middle = path.pkg().name() + "." + classChain();

    return isStatic()
      ? "import static " + middle + "." + member + ";"
      : "import " + middle + ";";
  }

  /****************************************************************************/
  @Override public String toString()
  {
    String keyword =
      isMacro()  ? "macro "  :
      isStatic() ? "static " : "";

    String pkgString    = path.pkgString();
    String separator    = isStatic() ? "." : "";
    String memberString = member != null ? member : "";

    return keyword + pkgString + ":" + classChain() + separator + memberString;
  }

  /****************************************************************************/
  @Override public boolean equals(Object o)
  {
    if (!(o instanceof Require)) { return false; }
    Require other = (Require) o;

    boolean cEqual = classes != null
      ? classes.equals(other.classes)
      : other.classes == null;

    boolean mEqual = member != null
      ? member.equals(other.member)
      : other.member == null;

    return path.equals(other.path) && cEqual && mEqual;
  }

  /****************************************************************************/
  @Override public int hashCode()
  {
    int cCode = classes == null ? 0 : classes.hashCode();
    int mCode = member  == null ? 0 : member .hashCode();
    return path.hashCode() ^ cCode ^ mCode;
  }
}
