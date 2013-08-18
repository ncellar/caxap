package util;

import java.util.AbstractList;
import java.util.List;

import java.lang.reflect.Array;

/**
 * ArraySlice for array of unknown type (but containing objects, not primitive
 * types).
 *
 * @author Norswap
 *
 */
public class UArraySlice extends AbstractList<Object> implements List<Object>
{
  /**************************************************************************/
  private final Object array;

  /**************************************************************************/
  private final int start, end;

  /**************************************************************************/
  public UArraySlice(Object array)
  {
    this(array, 0, Array.getLength(array));
  }

  /**************************************************************************/
  public UArraySlice(Object array, int start, int end)
  {
    this.array = array;
    this.start = start;
    this.end   = end;
  }

  /**************************************************************************/
  @Override public int size()
  {
    return end - start;
  }

  /**************************************************************************/
  @Override public Object get(int index)
  {
    return Array.get(array, start + index);
  }

  /**************************************************************************/
  @Override public Object set(int index, Object element)
  {
    Object out = get(index);
    Array.set(array, start + index, element);
    return out;
  }
}
