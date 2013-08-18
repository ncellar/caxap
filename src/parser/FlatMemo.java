package parser;

import java.util.HashMap;
import java.util.Map;

import grammar.Expression;
import grammar.Expression.Rule;

/**
 * Memoizes every rule in a flat hash map.
 */
public class FlatMemo implements Memo
{
  /****************************************************************************/
  private Map<Key, ParseData> memo = new HashMap<>();

  /****************************************************************************/
  private final Matcher matcher;

  /****************************************************************************/
  FlatMemo(Matcher matcher)
  {
    this.matcher = matcher;
  }

  /****************************************************************************/
  private static class Key
  {
    private final int position;
    private final Rule rule;

    Key(int position, Rule rule)
    {
      this.position = position;
      this.rule = rule;
    }

    @Override public int hashCode()
    {
      return (rule.id << 16) + position;
    }

    @Override public boolean equals(Object _that)
    {
      Key that = (Key) _that;
      return this.position == that.position && this.rule.equals(that.rule);
    }
  }

  /****************************************************************************/
  @Override public ParseData get(int position, Expression expr)
  {
    if (!(expr instanceof Rule)) {
      return matcher.parse(expr);
    }

    Key key = new Key(position, (Rule)expr);
    ParseData out = memo.get(key);

    if (out == null) {
      out = matcher.parse(expr);
      memo.put(key, out);
    }

    return out;
  }

  /****************************************************************************/
  @Override public void clear()
  {
    memo = new HashMap<>();
  }
}
