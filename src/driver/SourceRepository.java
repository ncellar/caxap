package driver;

import static util.FileUtils.glob;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import files.RootedSourcePath;
import files.Package;
import files.RelativeSourcePath;

/**
 * Handles the association between relative paths and {@link SourceFile}. Each
 * {@link SourceFile} instance is uniquely identified by a set of
 * {@link RelativeSourcePath} instances. Those instances might however not
 * compare equal, due to subclassing.
 *
 * All {@link SourceFile} instances should be obtained from the instance of this
 * class held in {@Context#repo}, which is the only one who
 * should create them.
 */
public class SourceRepository
{
  /****************************************************************************/
  private final Map<Path, SourceFile> files = new HashMap<>();

  /*****************************************************************************
   * Associate relative paths to corresponding rooted source paths known to
   * exist.
   */
  private final Map<Path, RootedSourcePath> hints = new HashMap<>();

  /*****************************************************************************
   * Returns the CTimeFile associated to the given path, creating it if needed.
   */
  SourceFile get(RelativeSourcePath path)
  {
    return get(path.relativePath());
  }

  /*****************************************************************************
   * Returns the CTimeFile associated to the given path, creating it if needed.
   */
  private SourceFile get(Path path)
  {
    SourceFile out = files.get(path);

    if (out == null) {
      out = createNewFile(path);
    }

    return out;
  }

  /*****************************************************************************
   * Indicates that the given paths are known to exists.
   */
  void hint(List<RootedSourcePath> paths)
  {
    for (RootedSourcePath rpath : paths)
    {
      assert rpath.absolutePath().toFile().exists();

      RootedSourcePath old = hints.put(rpath.relativePath(), rpath);

      if (old != null && !old.equals(rpath)) {
        reportAmbiguity(
          rpath.relativePath(), old.absolutePath(), rpath.absolutePath());
      }
    }
  }

  /*****************************************************************************
   * Creates a new SourceFile from the given path. Throws an error if no source
   * file can be found matching the path, or if the relative path matches
   * multiple files.
   */
  private SourceFile createNewFile(Path relative)
  {
    RootedSourcePath rpath = hints.get(relative);

    if  (rpath == null) {
      rpath = findRootedPathFor(relative);

      if (rpath == null) {
        throw new Error("No file matching relative path \"" + relative + "\"");
      }
    }

    SourceFile out = new SourceFile(rpath);
    files.put(relative, out);
    return out;
  }

  /*****************************************************************************
   * Search among {@link Config#roots} for a file matching $relative.
   */
  private RootedSourcePath findRootedPathFor(Path relative)
  {
    RootedSourcePath rpath = null;

    for (Path root : Config.get().roots) {
      Path absolute = root.resolve(relative);

      if (absolute.toFile().exists())
      {
        RootedSourcePath rpath2 = new RootedSourcePath(root, absolute, false);
        if (rpath != null) {
          reportAmbiguity(rpath, rpath2);
        }
        rpath = rpath2;
      }
    }

    return rpath;
  }

  /****************************************************************************/
  private void reportAmbiguity(RootedSourcePath rpath1, RootedSourcePath rpath2)
  {
    reportAmbiguity(
      rpath1.relativePath(), rpath1.absolutePath(), rpath2.absolutePath());
  }

  /****************************************************************************/
  private void reportAmbiguity(Path rel, Path absOne, Path absTwo)
  {
    throw new Error("Multiple file matching relative path \"" + rel
    + "\" : \n - " + absOne + "\n - " + absTwo);
  }

  /*****************************************************************************
   * Find all source files with the given extension under the given directory
   * (not recursively) and return them.
   */
  Collection<SourceFile> findAll(Package pkg, String extension)
  {
    Path pkgPath = pkg.relativePath();
    Set<SourceFile> out = new HashSet<>();

    for (Path root : Config.get().roots)
    {
      List<Path> paths;
      try {
        paths = glob(root.resolve(pkgPath), "*." + extension);
      }
      catch (IOException e) {
        throw new Error(
          "Error while retrieving source files from package " + pkg, e);
      }

      for (Path path : paths)
      {
        Path relative = pkgPath.resolve(path.toFile().getName());
        SourceFile file = get(relative);

        out.add(file);
      }
    }

    return out;
  }
}
