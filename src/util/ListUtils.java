package util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class ListUtils
{
  /*****************************************************************************
   * Returns a new immutable list containing the given elements.
   */
  @SafeVarargs public static <T> List<T> list(T... elems)
  {
    return Arrays.asList(elems);
  }

  /*****************************************************************************
   * Returns a new mutable array list containing the given elements.
   */
  @SafeVarargs public static <T> List<T> mlist(T... elems)
  {
    return new ArrayList<T>(Arrays.asList(elems));
  }

  /*****************************************************************************
   * Returns a new immutable empty list whose element type is the element type
   * of witness.
   */
  public static <T> List<T> list0(T witness)
  {
    @SuppressWarnings("unchecked")
    List<T> out = (List<T>) Collections.EMPTY_LIST;
    return out;
  }

  /*****************************************************************************
   * Returns a new immutable empty list whose element type is the type of
   * witness.
   */
  public static <T> List<T> list0(T[] witness)
  {
    @SuppressWarnings("unchecked")
    List<T> out = (List<T>) Collections.EMPTY_LIST;
    return out;
  }

  /*****************************************************************************
   * Returns an immutable copy of the given list.
   */
  public static <T> List<T> immutableCopy(List<T> list)
  {
    return Collections.unmodifiableList(new ArrayList<T>(list));
  }

  /*****************************************************************************
   * Returns the last element of $list.
   */
  public static <T> T getLast(List<T> list)
  {
    return list.get(list.size() - 1);
  }

  /*****************************************************************************
   * Removes and returns the last element of $list.
   */
  public static <T> T removeLast(List<T> list)
  {
    return list.remove(list.size() - 1);
  }
}
