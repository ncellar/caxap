package util;

/**
 * An iterator to walk through arrays (or array subsets) in left-to-right or
 * right-to-left order.
 */
public class ArrayIterator<T>
  extends AbstractBiIterator<T> implements Iterable<T>, Cloneable
{
  /****************************************************************************/
  private final T[] array;

  /****************************************************************************/
  private int index;

  /****************************************************************************/
  private final int start, end;

  /****************************************************************************/
  private final int dir;

  /****************************************************************************/
  public ArrayIterator(T[] array, int start, int end)
  {
    this.dir   = start < end ? 1 : -1;
    this.array = array;
    this.index = start - dir;
    this.start = start;
    this.end   = end;
  }

  /****************************************************************************/
  public ArrayIterator(T[] array, boolean reverse)
  {
    this(array,
      !reverse ? 0            : array.length - 1,
      !reverse ? array.length : -1);
  }

  /****************************************************************************/
  public ArrayIterator(T[] array)
  {
    this(array, false);
  }

  /****************************************************************************/
  @Override public boolean pastEdge()
  {
    return dir * index < dir * start || dir * end <= dir * index;
  }

  /****************************************************************************/
  @Override public T current()
  {
    return pastEdge() ? null : array[index];
  }

  /****************************************************************************/
  @Override public void forward()
  {
    index += dir;
  }

  /****************************************************************************/
  @Override public void backward()
  {
    index -= dir;
  }

  /****************************************************************************/
  @Override public boolean hasNext()
  {
    return dir * index < dir * end - 1;
  }

  /****************************************************************************/
  @Override public boolean hasPrev()
  {
    return dir * start < dir * index;
  }

  /*****************************************************************************
   * Returns the array index of the item below the reading head, or -1 if
   * the head is past the edge of the sequence.
   */
  public int index()
  {
    return pastEdge() ? -1 : index;
  }

  /*****************************************************************************
   * Returns the index of the item below the reading head in the sequence, or -1
   * if the head is past the edge of the sequence.
   */
  public int iterIndex()
  {
    return pastEdge() ? -1 : dir * (index - start);
  }

  /****************************************************************************/
  public void replace(T replacement)
  {
    array[index] = replacement;
  }

  /****************************************************************************/
  @Override public void remove()
  {
    throw new UnsupportedOperationException();
  }

  /****************************************************************************/
  @Override public ArrayIterator<T> iterator()
  {
    return this;
  }

  /****************************************************************************/
  public ArrayIterator<T> newIterator()
  {
    return new ArrayIterator<>(array, start, end);
  }

  /****************************************************************************/
  @SuppressWarnings("unchecked")
  @Override public ArrayIterator<T> clone()
  {
    try {
      return (ArrayIterator<T>) super.clone();
    }
    catch (CloneNotSupportedException e) {
      throw new Error("unreachable error");
    }
  }
}
