package util;

import java.lang.reflect.Array;

public class ArrayUtils
{
  /*****************************************************************************
   * Syntactic sugar: "new T[] {...}" -> "arr(...)".
   *
   * When passed 0 elements, T will default to Object.
   */
  @SafeVarargs public static <T> T[] arr(T... ts)
  {
    return ts;
  }

  /* For nested literal arrays, up to 8 levels of nesting.
   * e.g. arr3(arr2(arr(x, y), arr(z)), arr2(arr(u)) */

  @SafeVarargs public static <T> T[][]              arr2(T[]... ts)
    { return ts; }
  @SafeVarargs public static <T> T[][][]            arr3(T[][]... ts)
    { return ts; }
  @SafeVarargs public static <T> T[][][][]          arr4(T[][][]... ts)
    { return ts; }
  @SafeVarargs public static <T> T[][][][][]        arr5(T[][][][]... ts)
    { return ts; }
  @SafeVarargs public static <T> T[][][][][][]      arr6(T[][][][][]... ts)
    { return ts; }
  @SafeVarargs public static <T> T[][][][][][][]    arr7(T[][][][][][]... ts)
    { return ts; }
  @SafeVarargs public static <T> T[][][][][][][][]  arr8(T[][][][][][][]... ts)
    { return ts; }

  /* Those single-argument versions are needed because Java is dumb at detecting
   * types: arr2(new String[0]) would otherwise result in T = Object, return
   * type = Object[][]. Java actually believes that the string array is an array
   * that substitutes for all variadic parameters, instead of a single variadic
   * parameter itself. */

  public static <T> T[][]              arr2(T[] t)
          { return (T[][])              single(t); }
  public static <T> T[][][]            arr3(T[][] t)
          { return (T[][][])            single(t); }
  public static <T> T[][][][]          arr4(T[][][] t)
          { return (T[][][][])          single(t); }
  public static <T> T[][][][][]        arr5(T[][][][] t)
          { return (T[][][][][])        single(t); }
  public static <T> T[][][][][][]      arr6(T[][][][][] t)
          { return (T[][][][][][])      single(t); }
  public static <T> T[][][][][][][]    arr7(T[][][][][][] t)
          { return (T[][][][][][][])    single(t); }
  public static <T> T[][][][][][][][]  arr8(T[][][][][][][] t)
          { return (T[][][][][][][][])  single(t); }

  /*****************************************************************************
   * Creates a new array whose single element is elem. This is only type-safe
   * when T is an array type (no sub-typing possible).
   */
  private static <T> T[] single(T elem)
  {
    @SuppressWarnings("unchecked")
    T[] out = (T[]) Array.newInstance(elem.getClass(), 1);
    out[0] = elem;
    return out;
  }

  /*****************************************************************************
   * Returns a new empty array whose element type is the type of witness.
   * It's generally cleaner to use "new T[0]" instead.
   *
   * This is type-safe because the array is empty.
   */
  public static <T> T[] arr0(T witness)
  {
    @SuppressWarnings("unchecked")
    T[] out = (T[]) Array.newInstance(witness.getClass(), 0);
    return out;
  }

  /*****************************************************************************
   * Returns the concatenation of one array and one item.
   */
  public static <T> T[] concat(T[] one, T two)
  {
    @SuppressWarnings("unchecked") T[] out =
      (T[]) Array.newInstance(two.getClass(), one.length + 1);

    System.arraycopy(one, 0, out, 0, one.length);
    out[one.length] = two;

    return out;
  }

  /*****************************************************************************
   * Returns the concatenation of two arrays.
   */
  public static <T> T[] concat(T[] one, T[] two)
  {
    @SuppressWarnings("unchecked") T[] out = (T[]) Array.newInstance(
      one.getClass().getComponentType(), one.length + two.length);

    System.arraycopy(one, 0, out, 0, one.length);
    System.arraycopy(two, 0, out, one.length, two.length);

    return out;
  }

  /*****************************************************************************
   * Returns a cartesian product. "one" contains sequences of T. The returned
   * arrays contains a copy of each sequence in "one" for each element in two:
   * that copy had the element of two appended.
   */
  public static <T> T[][] cartesian(T[][] one, T[] two)
  {
    @SuppressWarnings("unchecked") T[][] out =
      (T[][]) Array.newInstance(two.getClass(), one.length * two.length);

    for (int i = 0; i < one.length; ++i) {
      for (int j = 0; j < two.length; ++j) {
        out[i * two.length + j] = ArrayUtils.concat(one[i], two[j]);
      }
    }

    return out;
  }

  /*****************************************************************************
   * Returns true iff "element" is contained within "array".
   */
  public static <T> boolean contains(T[] array, T element)
  {
    return search(array, element) != -1;
  }

  /*****************************************************************************
   * Returns the first index at which "element" appears within array, of -1 if
   * it does not appear.
   */
  public static <T> int search(T[] array, T element)
  {
    for (int i = 0 ; i < array.length ; ++i) {
      if (array[i].equals(element)) {
        return i;
      }
    }

    return -1;
  }

  /*****************************************************************************
   * Returns true iff the supplied elements are contained, in the same order
   * (but not necessarily adjacent) within "array".
   */
  public static <T> boolean contains(T[] array, T[] elements)
  {
    if (elements.length == 0) {
      return true;
    }

    int i = 0;

    for (T e : array) {
      if (e.equals(elements[i]) && ++i == elements.length) {
        return true;
      }
    }

    return false;
  }
}
