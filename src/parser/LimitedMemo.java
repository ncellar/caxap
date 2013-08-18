package parser;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import util.ArrayUtils;

import grammar.Expression;
import grammar.Expression.Rule;

/**
 * Memoizes only the last {@link #AMOUNT} parses for each rule.
 */
public class LimitedMemo implements Memo
{
  /****************************************************************************/
  private final Map<Expression, Memoed> memo = new HashMap<>();

  /****************************************************************************/
  private final Matcher matcher;

  /****************************************************************************/
  public static final int AMOUNT = 10;

  /****************************************************************************/
  LimitedMemo(Matcher matcher)
  {
    this.matcher = matcher;
  }

  /****************************************************************************/
  private static class Memoed
  {
    private final Integer   [] positions = new Integer   [AMOUNT];
    private final ParseData [] pdatas    = new ParseData [AMOUNT];

    int last = 0;

    {
      Arrays.fill(positions, -1);
    }

    ParseData atPosition(int position)
    {
      int index = ArrayUtils.search(positions, position);
      return index >= 0 ? pdatas[index] : null;
    }

    void insert(int position, ParseData pdata)
    {
      positions [last] = position;
      pdatas    [last] = pdata;

      last = ++last % AMOUNT;
    }
  }

  /****************************************************************************/
  @Override public ParseData get(int position, Expression expr)
  {
    if (!(expr instanceof Rule)) {
      return matcher.parse(expr);
    }

    Memoed memoed = memo.get(expr);

    if (memoed == null) {
      memoed = new Memoed();
      memo.put(expr, memoed);
    }

    ParseData out = memoed.atPosition(position);

    if (out == null) {
      out = matcher.parse(expr);
      memoed.insert(position, out);
    }

    return out;
  }

  /****************************************************************************/
  @Override public void clear()
  {
    memo.clear();
  }
}
