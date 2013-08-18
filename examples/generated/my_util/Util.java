package my_util;

/**
 * Various utility functions used by macros.
 */

public class Util
{
  public static String capitalize(String string)
  {
    return string.substring(0, 1).toUpperCase() + string.substring(1);
  }

  public static String repeat(int n, String string)
  {
    return new String(new char[n]).replace("\0", string);
  }
}
