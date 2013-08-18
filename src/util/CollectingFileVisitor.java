package util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

/**
 * A file visitor that collects all files whose path are matched by the supplied
 * PathMatcher object.
 */
public class CollectingFileVisitor extends SimpleFileVisitor<Path>
{
  /****************************************************************************/
  public final List<Path> files = new ArrayList<>();

  /****************************************************************************/
  private final PathMatcher pathMatcher;

  /****************************************************************************/
  public CollectingFileVisitor(final PathMatcher pathMatcher)
  {
    this.pathMatcher = pathMatcher;
  }

  /****************************************************************************/
  @Override
  public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
  throws IOException
  {
    if (pathMatcher.matches(file)) {
      files.add(file);
    }
    return FileVisitResult.CONTINUE;
  }

  /****************************************************************************/
  @Override public FileVisitResult visitFileFailed(Path file, IOException exc)
  throws IOException
  {
    return FileVisitResult.CONTINUE;
  }
}