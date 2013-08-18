package util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.util.List;

public class FileUtils
{
  /*****************************************************************************
   * Return a list of files (recursively) below the root directory whose path
   * matches the supplied globbing pattern.
   */
  public static List<Path> glob(final Path root, final String globPattern)
  throws IOException
  {
    final FileSystem fs = FileSystems.getDefault();
    final PathMatcher matcher = fs.getPathMatcher("glob:" + globPattern);
    final CollectingFileVisitor visitor = new CollectingFileVisitor(matcher);
    Files.walkFileTree(root, visitor);
    return visitor.files;
  }

  /*****************************************************************************
   * Recursively deletes a directory.
   * (from http://stackoverflow.com/questions/779519/)
   */
  public static void deleteDir(File f) throws IOException
  {
    if (f.isDirectory()) {
      for (File c : f.listFiles())
        deleteDir(c);
    }

    if (!f.delete()) {
      throw new Error("Could not delete directory " + f + ".");
    }
  }

  /*****************************************************************************
   * Create the file if it does not already exist.
   */
  public static void create(File file) throws IOException
  {
    if (!file.getParentFile().exists())
    if (!file.getParentFile().mkdirs()) {
      throw new Error("Could not create parent directories of " + file + ".");
    }
    file.createNewFile();
  }

  /*****************************************************************************
   * Writes a byte array to a file, using a buffer of the size of the byte array
   * to minimize the number of system calls. If the file does not exist, it will
   * be created, along with its parent directories.
   */
  public static void write(File file, byte[] bytes)
  {
    OutputStream stream = null;
    try  {
      create(file);
       stream = new BufferedOutputStream(
        new FileOutputStream(file), bytes.length);
      stream.write(bytes);
    }
    catch (IOException e){
      throw new Error("I/O error when trying to writes bytes to file: " + file);
    }
    finally {
      try { if (stream != null) { stream.close(); } }
      catch (IOException e) {}
    }
  }

  /*****************************************************************************
   * Writes a string to a file, using a buffer of the size of the string
   * to minimize the number of system calls. If the file does not exist, it will
   * be created, along with its parent directories.
   */
  public static void write(File file, String string)
  {
    /* Using a buffer the size of the string, means there may be more than one
     * write, because character can take up more than one bytes in some
     * encodings. */

    Writer writer = null;
    try {
      create(file);
      writer = new OutputStreamWriter(new BufferedOutputStream(
        new FileOutputStream(file), string.length()));
      writer.write(string);
      writer.close();
    }
    catch (IOException e){
      throw new Error("I/O error when trying to writes string to file: " + file);
    }
    finally {
      try { if (writer != null) { writer.close(); } }
      catch (IOException e) {}
    }
  }
}