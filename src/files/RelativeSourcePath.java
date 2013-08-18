package files;

import static util.ListUtils.removeLast;
import static util.StringUtils.getStemAndExtension;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

/**
 * A path to a source file, relative to a root source directory. This path
 * includes all directories corresponding to package name components (if any).
 */
public class RelativeSourcePath
{
  /****************************************************************************/
  private final Package pkg;

  /****************************************************************************/
  private final String fileStem;

  /****************************************************************************/
  private final String fileExt;

  /****************************************************************************/
  private final Path relativePath;

  /****************************************************************************/
  public static RelativeSourcePath make(Package pkg, String fileName)
  {
    return new RelativeSourcePath(pkg, fileName);
  }

  /*****************************************************************************
   * Makes a path for the regular source file corresponding to the given
   * package string (the fully qualified dot-separated name of a class).
   */
  public static RelativeSourcePath make(String pkgStr)
  {
    int dot = pkgStr.lastIndexOf('.');

    return dot == -1
      ? make(new Package(""), pkgStr)
      : make(new Package(pkgStr.substring(0, dot)),
                         pkgStr.substring(dot + 1) + ".java");
  }

//  TODO This is cleaner than the current constructor, but cannot be
//    inherited by RootedSourcePath.
//
//  /****************************************************************************/
//  public static RelativeSourcePath make(Path relative)
//  {
//    int lastName = relative.getNameCount() - 1;
//    String name    = relative.getName(lastName).toString();
//    String pkgPath = relative.subpath(0, lastName).toString();
//
//    return make(new Package(pkgPath), name);
//  }

  /****************************************************************************/
  public RelativeSourcePath(Package pkg, String fileName)
  {
    String[] stemAndExt = getStemAndExtension(fileName);

    this.pkg         = pkg;
    this.fileStem    = stemAndExt[0];
    this.fileExt     = stemAndExt[1];
    this.relativePath = pkg.relativePath().resolve(Paths.get(fileName));
  }

  /****************************************************************************/
  public RelativeSourcePath(Path relative)
  {
    List<String> components = new ArrayList<>(relative.getNameCount() - 1);

    for (Path p : relative) {
      components.add(p.toString());
    }

    String[] stemAndExt = getStemAndExtension(removeLast(components));

    this.relativePath = relative;
    this.pkg          = new Package(components);
    this.fileStem     = stemAndExt[0];
    this.fileExt      = stemAndExt[1];
  }

  /****************************************************************************/
  public Package pkg()
  {
    return pkg;
  }

  /****************************************************************************/
  public String fileStem()
  {
    return fileStem;
  }

  /****************************************************************************/
  public String fileExt()
  {
    return fileExt;
  }

  /****************************************************************************/
  public String fileName()
  {
    return fileExt.isEmpty() ? fileStem : fileStem + "." + fileExt;
  }

  /****************************************************************************/
  public Path relativePath()
  {
    return relativePath;
  }

  /*****************************************************************************
   * Returns a "package string" to the class that has the same name than
   * this file.
   */
  public String pkgString()
  {
    String pkgName = pkg.toString();
    return pkgName.isEmpty() ? fileStem : pkgName + "." + fileStem;
  }

  /****************************************************************************/
  @Override public String toString()
  {
    return relativePath.toString();
  }

  /****************************************************************************/
  @Override public boolean equals(Object o)
  {
    if (!(o instanceof RelativeSourcePath)) { return false; }
    RelativeSourcePath other = (RelativeSourcePath) o;
    return other.canEqual(this) && relativePath.equals(other.relativePath);
  }

  /****************************************************************************/
  public boolean canEqual(Object other)
  {
    return (other instanceof RelativeSourcePath);
  }

  /****************************************************************************/
  @Override public int hashCode()
  {
    return toString().hashCode();
  }
}
