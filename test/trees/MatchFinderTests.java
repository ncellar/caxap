package trees;

import static org.junit.Assert.assertArrayEquals;
import static trees.MatchFinder.find;
import static trees.MatchFinder.Finder.ALL;
import static trees.MatchFinder.Finder.FIRST;
import static trees.MatchFinder.Finder.LAST;
import static trees.MatchSpec.anySpec;
import static util.ArrayUtils.arr;

import java.util.Arrays;

import source.SourceString;

import grammar.Expression;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

import parser.Match;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MatchFinderTests
{
  Match match(String name, Match... childs)
  {
    Expression.Rule rule = new Expression.Rule(name, null);
    return new Match(rule, new SourceString(""), 0, 0, Arrays.asList(childs));
  }

  MatchSpec s1 = new MatchSpec.RuleSpec("1");
  MatchSpec s2 = new MatchSpec.RuleSpec("2");
  MatchSpec s3 = new MatchSpec.RuleSpec("3");
  MatchSpec s4 = new MatchSpec.RuleSpec("4");
  MatchSpec s5 = new MatchSpec.RuleSpec("5");
  MatchSpec s6 = new MatchSpec.RuleSpec("6");
  MatchSpec s7 = new MatchSpec.RuleSpec("7");
  MatchSpec s8 = new MatchSpec.RuleSpec("8");

  Match m7 = match("7");
  Match m6 = match("6");
  Match m5 = match("5");
  Match m4 = match("4");
  Match m3 = match("3", m6, m7);
  Match m2 = match("2", m4, m5);
  Match m1 = match("1", m2, m3);

  @Test public void aa_find_inclusive()
  {
    assertArrayEquals(arr(m7),
      find(m7, s7, FIRST, new MatchSpec[0], new MatchSpec[0], true));

    assertArrayEquals(new Match[0],
      find(m7, s7, FIRST, arr(anySpec), new MatchSpec[0], true));

    assertArrayEquals(new Match[0],
      find(m7, s7, FIRST, new MatchSpec[0], arr(anySpec), true));

    assertArrayEquals(arr(m7),
      find(m7, s7, LAST, new MatchSpec[0], new MatchSpec[0], true));

    assertArrayEquals(new Match[0],
      find(m7, s7, LAST, arr(anySpec), new MatchSpec[0], true));

    assertArrayEquals(new Match[0],
      find(m7, s7, LAST, new MatchSpec[0], arr(anySpec), true));

    assertArrayEquals(arr(m1),
      find(m1, anySpec, ALL, new MatchSpec[0], new MatchSpec[0], true));

    assertArrayEquals(arr(m5, m3),
      find(m1, anySpec, ALL, arr(s4), new MatchSpec[0], true));

    assertArrayEquals(arr(m2, m6),
      find(m1, anySpec, ALL, new MatchSpec[0], arr(s7), true));

    assertArrayEquals(arr(m5, m6),
      find(m1, anySpec, ALL, arr(s4), arr(s7), true));

    assertArrayEquals(arr(m3),
      find(m1, anySpec, ALL, arr(s4, s5), new MatchSpec[0], true));

    assertArrayEquals(arr(m2),
      find(m1, anySpec, ALL, new MatchSpec[0], arr(s6, s7), true));

    assertArrayEquals(new Match[0],
      find(m1, anySpec, ALL, arr(s4, s5), arr(s6, s7), true));

    assertArrayEquals(arr(m7),
      find(m1, anySpec, ALL, arr(s4, s6), new MatchSpec[0], true));

    assertArrayEquals(arr(m3),
      find(m1, anySpec, ALL, arr(s2), new MatchSpec[0], true));

    assertArrayEquals(new Match[0],
      find(m1, s8, FIRST, new MatchSpec[0], new MatchSpec[0], true));

    assertArrayEquals(new Match[0],
      find(m1, s1, FIRST, arr(s3), new MatchSpec[0], true));

    assertArrayEquals(new Match[0],
      find(m1, anySpec, FIRST, arr(s5), arr(s6), true));

    assertArrayEquals(arr(m6),
      find(m1, anySpec, LAST, new MatchSpec[0], arr(s7), true));

    assertArrayEquals(arr(m3),
      find(m1, anySpec, LAST, arr(s4), new MatchSpec[0], true));

    assertArrayEquals(arr(m6),
      find(m1, anySpec, LAST, arr(s4), arr(s7), true));
  }

  @Test public void aa_find_exclusive()
  {
    assertArrayEquals(new Match[0],
      find(m7, s7, FIRST, new MatchSpec[0], new MatchSpec[0], false));

    assertArrayEquals(new Match[0],
      find(m7, s7, FIRST, arr(anySpec), new MatchSpec[0], false));

    assertArrayEquals(new Match[0],
      find(m7, s7, FIRST, new MatchSpec[0], arr(anySpec), false));

    assertArrayEquals(new Match[0],
      find(m7, s7, LAST, new MatchSpec[0], new MatchSpec[0], false));

    assertArrayEquals(new Match[0],
      find(m7, s7, LAST, arr(anySpec), new MatchSpec[0], false));

    assertArrayEquals(new Match[0],
      find(m7, s7, LAST, new MatchSpec[0], arr(anySpec), false));

    assertArrayEquals(new Match[0],
      find(m1, anySpec, ALL, new MatchSpec[0], new MatchSpec[0], false));

    assertArrayEquals(arr(m2, m6),
      find(m1, anySpec, ALL, arr(s7), new MatchSpec[0], false));

    assertArrayEquals(arr(m5, m3),
      find(m1, anySpec, ALL, new MatchSpec[0], arr(s4), false));

    assertArrayEquals(arr(m4, m7),
      find(m1, anySpec, ALL, arr(s5), arr(s6), false));

    assertArrayEquals(arr(m4),
      find(m1, anySpec, ALL, arr(s5), new MatchSpec[0], false));

    assertArrayEquals(arr(m4),
      find(m1, anySpec, ALL, arr(s5, s6), new MatchSpec[0], false));

    assertArrayEquals(new Match[0],
      find(m1, anySpec, ALL, new MatchSpec[0], arr(s6, s7), false));

    assertArrayEquals(new Match[0],
      find(m1, anySpec, ALL, arr(s4, s5), arr(s6, s7), false));

    assertArrayEquals(new Match[0],
      find(m1, anySpec, ALL, arr(s4, s6), new MatchSpec[0], false));

    assertArrayEquals(arr(m3),
      find(m1, anySpec, ALL, new MatchSpec[0], arr(s4, s5), false));

    assertArrayEquals(arr(m3),
      find(m1, anySpec, ALL, new MatchSpec[0], arr(s2), false));

    assertArrayEquals(new Match[0],
      find(m1, s8, FIRST, arr(s5), arr(s6), false));

    assertArrayEquals(new Match[0],
      find(m1, s1, FIRST, arr(s5), new MatchSpec[0], false));

    assertArrayEquals(new Match[0],
      find(m1, anySpec, FIRST, arr(s4), arr(s7), false));

    assertArrayEquals(arr(m6),
      find(m1, anySpec, LAST, arr(s7), new MatchSpec[0], false));

    assertArrayEquals(arr(m3),
      find(m1, anySpec, LAST, new MatchSpec[0], arr(s4), false));

    assertArrayEquals(arr(m7),
      find(m1, anySpec, LAST, arr(s5), arr(s6), false));
  }
}
