package trees;

import static trees.BoundedMatchIterator.State.LEFT;
import static trees.BoundedMatchIterator.State.MIDDLE;
import static trees.BoundedMatchIterator.State.RIGHT;

import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import parser.Match;
import util.PeekIterator;

public class BoundedMatchIterator implements Iterator<Match>, Iterable<Match>
{
  /****************************************************************************/
  private final MatchIterator iterator;

  /****************************************************************************/
  private final PeekIterator<Match> leftIter;

  /****************************************************************************/
  private final PeekIterator<Match> rightIter;

  /****************************************************************************/
  private final boolean inclusive;

  /****************************************************************************/
  private final boolean rightOpen;

  /****************************************************************************/
  enum State { LEFT, MIDDLE, RIGHT }

  /****************************************************************************/
  private State state = LEFT;

  /****************************************************************************/
  private Match next;

  /****************************************************************************/
  private Match prev;

  /****************************************************************************/
  BoundedMatchIterator(Match match, List<Match> left, List<Match> right,
    boolean leftToRight, boolean inclusive)
  {
    this.inclusive   = inclusive;
    this.leftIter    = new PeekIterator<>(leftToRight ? left  : right);
    this.rightIter   = new PeekIterator<>(leftToRight ? right : left);
    this.iterator    = new MatchIterator(match, leftToRight);
    this.rightOpen   = !rightIter.hasNext();

    forward();
  }

  /****************************************************************************/
  private void forward()
  {
    switch(state) {
    case LEFT   : leftItem  (); break;
    case MIDDLE : middleItem(); break;
    case RIGHT  : rightItem (); break;
    }
  }

  /****************************************************************************/
  private void leftItem()
  {
    while (leftIter.hasNext())
    {
      assert iterator.hasNext();
      Match match = iterator.next();

      if (match == leftIter.peek()) {
        leftIter.next();

        if (match == rightIter.peek()) {
          rightIter.next();
        }
      }
      else if (!inclusive) {
        next = match;
        break;
      }
    }

    if (!leftIter.hasNext()) {
      state = MIDDLE;
      iterator.skipChilds();
      middleItem();
    }
  }

  /****************************************************************************/
  private void middleItem()
  {
    while (rightIter.hasNext())
    {
      assert iterator.hasNext();
      Match match = iterator.next();

      if (match == rightIter.peek()) {
        rightIter.next();
        continue;
      }
      else if (inclusive) {
        next = match;
        break;
      }
    }

    if (!rightIter.hasNext()) {
      state = RIGHT;
      iterator.skipChilds();
      rightItem();
    }
  }

  /****************************************************************************/
  private void rightItem()
  {
    next = inclusive == rightOpen && iterator.hasNext()
      ? iterator.next()
      : null;
  }

  /*****************************************************************************
   * Ensures that the descendant of the last item returned by next() (if any)
   * will be skipped in the iteration. Behaves as if next() was called for all
   * the descendants.
   */
  public void skipChilds()
  {
    if (prev == null || prev.children().isEmpty()) {
      return;
    }

    int maxChildIndex = prev.children().size() - 1;
    for (int i = 0 ; i < maxChildIndex ; ++i) {
      iterator.skipChilds();
      iterator.next();
    }

    iterator.skipChilds();
    forward();
  }

  /****************************************************************************/
  @Override public boolean hasNext()
  {
    return next != null;
  }

  /****************************************************************************/
  @Override public Match next()
  {
    if (next == null) {
      throw new NoSuchElementException();
    }

    prev = next;
    forward();
    return prev;
  }

  /****************************************************************************/
  @Override public Iterator<Match> iterator()
  {
    return this;
  }

  /****************************************************************************/
  @Override public void remove()
  {
    throw new UnsupportedOperationException();
  }
}
