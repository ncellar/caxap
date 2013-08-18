package driver;

import java.util.Stack;

import source.Source;

/**
 * Enables some functions to produce better diagnostics by providing bits of
 * context.
 */
public class Hints
{
  /****************************************************************************/
  private static Hints instance = new Hints();

  /****************************************************************************/
  public static Hints get()
  {
    return instance;
  }

  /****************************************************************************/
  private Stack<Source> sources = new Stack<>();

  /****************************************************************************/
  public Source source()
  {
    return sources.isEmpty() ? null : sources.peek();
  }

  /****************************************************************************/
  public void hintSource(Source source)
  {
    sources.push(source);
  }

  /****************************************************************************/
  public void endHintSource()
  {
    sources.pop();
  }
}
