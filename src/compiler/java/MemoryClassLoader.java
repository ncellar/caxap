package compiler.java;

import java.security.SecureClassLoader;
import java.util.HashMap;
import java.util.Map;

/**
 * A class loader that exposes a function to define classes from bytecode.
 * Functions defined as such are not dumped to disk and live in memory. We also
 * save their bytecode, since there is no way to get it back from the Java
 * compiler.
 *
 * The class names supplied to the functions should the full class names as
 * returned by {@link Class#getName()} (e.g. "java.util.List" or
 * "java.util.AbstractMap$SimpleEntry").
 */
public class MemoryClassLoader extends SecureClassLoader
{
  /****************************************************************************/
  private static final MemoryClassLoader instance = new MemoryClassLoader();
  private MemoryClassLoader() {}
  public static MemoryClassLoader get() { return instance; }

  /****************************************************************************/
  private Map<String, byte[]> bytecodes = new HashMap<>();

  /****************************************************************************/
  public Class<?> defineClass(String name, byte[] bytecode)
  {
    /* The class has already been defined. This can happen because a class is
     * compiled but then output as result of the compilation of another class
     * (that's just how the Java compiler works - compiling whole directories
     * and shit). Maybe this should be handled more cleanly. */
    if (bytecodes.get(name) != null) {
      try {
        return loadClass(name);
      }
      catch (ClassNotFoundException e) {
        throw new Error("programming error");
      }
    }

    bytecodes.put(name, bytecode);
    return defineClass(name, bytecode, 0, bytecode.length);
  }

  /****************************************************************************/
  public byte[] getBytecode(String name)
  {
    return bytecodes.get(name);
  }
}
