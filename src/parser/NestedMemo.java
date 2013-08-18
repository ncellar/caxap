package parser;

import grammar.Expression;
import grammar.Expression.Rule;

import java.util.HashMap;
import java.util.Map;

/**
 * Memoizes every rule in a nested hash map: one level for positions, and one
 * level for rules.
 */
public class NestedMemo implements Memo
{
  /*****************************************************************************
   * Maps (input position, parsing expression) pairs to ParseData objects.
   */
  private final Map<Integer, Map<Expression, ParseData>> memo = new HashMap<>();

  /****************************************************************************/
  private final Matcher matcher;

  /****************************************************************************/
  NestedMemo(Matcher matcher)
  {
    this.matcher = matcher;
  }

  /****************************************************************************/
  @Override public ParseData get(int position, Expression expr)
  {
    if (!(expr instanceof Rule)) {
      return matcher.parse(expr);
    }

    Map<Expression, ParseData> inner = memo.get(position);

    ParseData out = inner != null ? inner.get(expr) : null;

    if (out == null) {
      out = matcher.parse(expr);
      set(out);
    }

    return out;
  }

  /*****************************************************************************
   * Memoizes the given match data at the given position.
   */
  private void set(ParseData data)
  {
    Map<Expression, ParseData> inner = memo.get(data.begin);

    if (inner == null) {
      inner = new HashMap<>();
      memo.put(data.begin, inner);
    }

    inner.put(data.expr, data);
  }

  /****************************************************************************/
  @Override public void clear()
  {
    memo.clear();
  }
}
