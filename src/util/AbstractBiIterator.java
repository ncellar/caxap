package util;

/**
 * Implement default behavior for {@link BiIterator#next()} and
 * {@link BiIterator#prev()}.
 */
public abstract class AbstractBiIterator<T> implements BiIterator<T>
{
  @Override public T next()
  {
    forward();
    return current();
  }

  @Override public T prev()
  {
    backward();
    return current();
  }
}
