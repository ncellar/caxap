package compiler.java;

import java.io.Reader;
import java.io.StringReader;
import java.net.URI;
import java.nio.CharBuffer;

import javax.tools.SimpleJavaFileObject;

import files.RelativeSourcePath;

/**
 * A JavaFileObject that uses code from a string.
 */
public class StringJavaFileObject extends SimpleJavaFileObject
{
  /****************************************************************************/
  private final String code;

  /****************************************************************************/
  private final RelativeSourcePath path;

  /*****************************************************************************
   * $name should be the name of single public class defined in $code, if there
   * is one.
   */
  public StringJavaFileObject(RelativeSourcePath path, String code)
  {
    super(URI.create("string:///" + path.fileName()), Kind.SOURCE);
    this.code = code;
    this.path = path;
  }

  /****************************************************************************/
  @Override public CharBuffer getCharContent(boolean ignoreEncErrors)
  {
    return CharBuffer.wrap(code);
  }

  /****************************************************************************/
  @Override public Reader openReader(boolean ignoreEncErrors)
  {
    return new StringReader(code);
  }

  /****************************************************************************/
  public String code()
  {
    return code;
  }

  /****************************************************************************/
  public RelativeSourcePath path()
  {
    return path;
  }
}
