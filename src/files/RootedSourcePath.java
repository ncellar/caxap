package files;

import java.io.File;
import java.nio.file.Path;

/**
 * A path to a source file. This is the composition of a RelativeSourcePath
 * and a root Path.
 */
public class RootedSourcePath extends RelativeSourcePath
{
  /****************************************************************************/
  private final Path root;

  /****************************************************************************/
  private final Path absolutePath;

  /****************************************************************************/
  public RootedSourcePath(Path root, Path path, boolean relative)
  {
    super(relative ? path : root.relativize(path));

    this.root         = root;
    this.absolutePath = relative ? root.resolve(path) : path;
  }

  /****************************************************************************/
  public RootedSourcePath(Path root, Package pkg, String fileName)
  {
    super(pkg, fileName);

    this.root         = root;
    this.absolutePath = root.resolve(relativePath());
  }

  /****************************************************************************/
  public Path root()
  {
    return root;
  }

  /****************************************************************************/
  public Path absolutePath()
  {
    return absolutePath;
  }

  /****************************************************************************/
  public File file()
  {
    return absolutePath.toFile();
  }

  /****************************************************************************/
  @Override public String toString()
  {
    return absolutePath.toString();
  }

  /****************************************************************************/
  @Override public boolean equals(Object o)
  {
    if (!(o instanceof RootedSourcePath)) { return false; }

    RootedSourcePath other = (RootedSourcePath) o;
    return other.canEqual(this) && root.equals(other.root)
      && relativePath().equals(other.relativePath());
  }

  /****************************************************************************/
  @Override public boolean canEqual(Object other)
  {
    return (other instanceof RootedSourcePath);
  }

  /****************************************************************************/
  @Override public int hashCode()
  {
    return toString().hashCode();
  }
}
