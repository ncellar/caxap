package util;

import java.util.Iterator;

/**
 * UNUSED
 *
 * A bidirectional iterator, able to walk in both directions, not unlike
 * {@link java.util.ListIterator}.
 *
 * Contrary to a ListIterator, the "reading head" of the iterator is always on
 * an item, excepted when the iterator is exhausted in one direction. In those
 * cases, the head is one past the edge of the sequence.
 */
public interface BiIterator<T> extends Iterator<T>
{
  /*****************************************************************************
   * Return the item under the reading head, or null if the head is one past
   * an edge of the sequence.
   */
  T current();

  /*****************************************************************************
   * Moves the reading head one position towards the right.
   */
  void forward();

  /*****************************************************************************
   * Moves the reading head one position towards the left.
   */
  void backward();

  /*****************************************************************************
   * Like {@link BiIterator#forward()} then {@link BiIterator#current()}.
   */
  @Override T next();

  /*****************************************************************************
   * Like {@link BiIterator#backward()} then {@link BiIterator#current()}.
   */
  T prev();

  /*****************************************************************************
   * True if the position to the right is not past the edge of the sequence.
   */
  @Override boolean hasNext();

  /*****************************************************************************
   * True if the position to the left is not past the edge of the sequence.
   */
  boolean hasPrev();

  /*****************************************************************************
   * Removes the element under the reading head.
   */
  @Override void remove();

  /*****************************************************************************
   * Indicates if the reading head is past the edge of the sequence.
   */
  boolean pastEdge();
}
