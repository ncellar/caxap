package parser;

import java.util.HashMap;
import java.util.Map;

import parser.Matcher;

import grammar.Expression;
import source.Source;

/*******************************************************************************
 * Analyze the performance of a grammar.
 */
public class StatisticsReporter extends Matcher
{
  /****************************************************************************/
  public StatisticsReporter(final Source source)
  {
    super(source);
  }

  /****************************************************************************/
  public static class ExprInfo
  {
    public int successes;
    public int failures;
    public int memoedSuccesses;
    public int memoedFailures;
  }

  /****************************************************************************/
  public static class PosInfo
  {
    public int invocations;
  }

  /****************************************************************************/
  final public Map<Expression, ExprInfo> exprInfos = new HashMap<>();

  /****************************************************************************/
  final public Map<Integer, PosInfo> posInfos = new HashMap<>();

  /****************************************************************************/
  @Override public boolean visitChild(Expression child)
  {
    ExprInfo exprInfo = exprInfos.get(child);

    if (exprInfo == null) {
      exprInfo = new ExprInfo();
      exprInfos.put(child, exprInfo);
    }

    PosInfo posInfo = posInfos.get(stream.position);

    if (posInfo == null) {
      posInfo = new PosInfo();
      posInfos.put(stream.position, posInfo);
    }

    posInfo.invocations += 1;

    boolean memoed = memo.get(stream.position, child) != null;
    boolean success = super.visitChild(child);

    if (success) {
      exprInfo.successes += 1;
      if (memoed) {
        exprInfo.memoedSuccesses += 1;
      }
    }
    else {
      exprInfo.failures += 1;
      if (memoed) {
        exprInfo.memoedFailures += 1;
      }
    }

    return success;
  }

  /****************************************************************************/
  public void report()
  {
    for (Map.Entry<Expression, ExprInfo> e : exprInfos.entrySet()) {
      ExprInfo info = e.getValue();
      System.out.println(e.getKey()
        + " : " + (info.successes - info.memoedSuccesses)
        + " : " + (info.failures  - info.memoedFailures)
        + " : " + info.memoedSuccesses
        + " : " + info.memoedFailures);
    }

   for (Map.Entry<Integer, PosInfo> e : posInfos.entrySet()) {
      PosInfo info = e.getValue();
      System.out.println(e.getKey()
        + " : " + source().where(e.getKey())
        + " : " + info.invocations
        );
    }
  }
}