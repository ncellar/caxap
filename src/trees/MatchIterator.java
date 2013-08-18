package trees;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Stack;

import parser.Match;
import util.ArrayIterator;

/**
 * Returns the matches of the tree by doing a depth-first prefix walk of the
 * tree, from left to right or from right to left.
 */
public class MatchIterator implements Iterable<Match>, Iterator<Match>
{
  /**
   * The position in the match tree is kept as a "trace": a stack of NodeState.
   * The last item returned by next() is always at the top of the stack (we use
   * some "if" magic for the first invocation of next()).
   */

  /****************************************************************************/
  private class NodeState
  {
    final Match match;
    final boolean upperHasNext;
    final ArrayIterator<Match> iter;

    NodeState(Match match, NodeState parent)
    {
      this.match        = match;
      this.upperHasNext = parent == null ? false : parent.treeHasNext();
      Match[] childs    = match.children().toArray(Match.EMPTY);
      this.iter         = new ArrayIterator<>(childs, !leftToRight);
    }

    boolean treeHasNext()
    {
      return upperHasNext || iter.hasNext();
    }
  }
  //////////////////////////////////////////////////////////////////////////////

  /****************************************************************************/
  private final Match tree;

  /****************************************************************************/
  private Stack<NodeState> stack = new Stack<>();

  /*****************************************************************************
   * Indicates whether the elements of the match tree are returned in a
   * left-to-right (true) or right-to-left order (false).
   */
  private final boolean leftToRight;

  /****************************************************************************/
  public MatchIterator(Match tree)
  {
    this(tree, true);
  }

  /****************************************************************************/
  public MatchIterator(Match tree, boolean leftToRight)
  {
    this.tree        = tree;
    this.leftToRight = leftToRight;
  }

  /****************************************************************************/
  @Override public boolean hasNext()
  {
    return stack.isEmpty() || stack.peek().treeHasNext();
  }

  /****************************************************************************/
  @Override public Match next()
  {
    forward();
    return stack.peek().match;
  }

  /*****************************************************************************
   * Puts the next item at the top of the stack.
   */
  private void forward()
  {
    if (stack.isEmpty()) {
      stack.push(new NodeState(tree, null));
      return;
    }

    if (!hasNext()) {
      throw new NoSuchElementException();
    }

    NodeState top = stack.peek();

    while (!top.iter.hasNext()) {
      stack.pop();
      top = stack.peek();
    }

    stack.push(new NodeState(top.iter.next(), top));
  }

  /*****************************************************************************
   * Ensures that the descendant of the last item returned by next() (if any)
   * will be skipped in the iteration. Behaves as if next() was called for all
   * the descendants.
   */
  public void skipChilds()
  {
    if (stack.isEmpty() || !hasNext()) {
      return;
    }

    NodeState top = stack.peek();
    while (top.iter.hasNext()) {
      top.iter.next();
    }
  }

  /*****************************************************************************
   * Returns a list of matches that forms the path between the root of the match
   * tree and the last item returned by next().
   */
  public List<Match> trace()
  {
    List<Match> list = new ArrayList<>();

    for (NodeState ns : stack) {
      list.add(ns.match);
    }

    return list;
  }

  /****************************************************************************/
  @Override public void remove()
  {
    throw new UnsupportedOperationException();
  }

  /****************************************************************************/
  @Override public Iterator<Match> iterator()
  {
    return this;
  }
}
