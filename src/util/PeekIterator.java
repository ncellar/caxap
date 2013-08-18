package util;

import java.util.Iterator;

/**
 * Wraps an iterator, allowing to peek the next item. {@link Iterator#remove()}
 * is unsupported.
 */
public class PeekIterator<T> implements Iterator<T>
{
  /****************************************************************************/
  private final Iterator<T> iterator;

  /****************************************************************************/
  private T top;

  /****************************************************************************/
  public PeekIterator(Iterable<T> iterable)
  {
    this.iterator = iterable.iterator();
    advance();
  }

  /****************************************************************************/
  private void advance()
  {
    top = iterator.hasNext() ? iterator.next() : null;
  }

  /****************************************************************************/
  public T peek()
  {
    return top;
  }

  /****************************************************************************/
  @Override public boolean hasNext()
  {
    return top != null;
  }

  /****************************************************************************/
  @Override public T next()
  {
    T old = top;
    advance();
    return old;
  }

  /****************************************************************************/
  @Override public void remove()
  {
    throw new UnsupportedOperationException();
  }
}
