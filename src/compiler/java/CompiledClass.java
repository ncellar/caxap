package compiler.java;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.regex.Matcher;

import util.FileUtils;

/**
 * A compiled class: name + bytecode.
 */
public class CompiledClass
{
  /*****************************************************************************
   * Class name as returned by {@link Class#getName()} (e.g. "java.util.List" or
   * "java.util.AbstractMap$SimpleEntry").
   */
  public final String name;

  /****************************************************************************/
  public final byte[] bytecode;

  /****************************************************************************/
  public Class<?> klass = null;

  /****************************************************************************/
  public CompiledClass(String name, byte[] bytecode)
  {
    this.name = name;
    this.bytecode = bytecode;
  }

  /****************************************************************************/
  public boolean loaded()
  {
    return klass != null;
  }

  /****************************************************************************/
  public Class<?> klass()
  {
    if (!loaded()) {
      throw new Error("Trying the get the class object of an unloaded "
        + "compiled class: " + this);
    }

    return klass;
  }

  /****************************************************************************/
  @Override public String toString()
  {
    return name;
  }

  /*****************************************************************************
   * Dump the bytecode into a file whose matches the structure of the class
   * name, under the $root directory.
   */
  public void dump(Path root)
  {
    String relative = name.replaceAll("\\.",
      Matcher.quoteReplacement(File.separator)) + ".class";

    File output = root.resolve(relative).toFile();

    try {
      FileUtils.create(output);
      OutputStream stream = new BufferedOutputStream(
        new FileOutputStream(output), bytecode.length);
      stream.write(bytecode);
      stream.close();
    }
    catch (IOException e){
      throw new Error("I/O error when trying to dump bytecode for class "
        + name + " to file " + output + ".", e);
    }
  }

  /*****************************************************************************
   * Load this class in the supplied class loader, and returns the resulting
   * Class object.
   */
  public Class<?> load(MemoryClassLoader loader)
  {
    return (klass = loader.defineClass(name, bytecode));
  }
}
