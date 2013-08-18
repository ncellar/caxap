package my_util;

import static java.util.Arrays.copyOfRange;

/**
 * Holds the function that array slices (pkg.ArraySlice) expand to.
 */
public class ArraySlice
{
  public static <T> T[] slice(T[] array, int start, Integer stop)
  {
    stop =
      stop == null ? array.length :
      stop < 0     ? array.length + stop :
                     stop;
    
    start = start < 0 ? array.length + start : start;
    
    return copyOfRange(array, start, stop);
  }
}