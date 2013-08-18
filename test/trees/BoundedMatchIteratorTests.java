package trees;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertEquals;
import static util.ArrayUtils.arr;
import static util.ListUtils.list;
import static util.ListUtils.list0;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

import parser.Match;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class BoundedMatchIteratorTests
{
  Match match(Match... childs)
  {
    return new Match(null, null, 0, 0, Arrays.asList(childs));
  }

  // A depth 3 complete binary tree.
  Match o = match();
  Match n = match();
  Match m = match();
  Match l = match();
  Match k = match();
  Match j = match();
  Match i = match();
  Match h = match();
  Match g = match(n, o);
  Match f = match(l, m);
  Match e = match(j, k);
  Match d = match(h, i);
  Match c = match(f, g);
  Match b = match(d, e);
  Match a = match(b, c);

  List<Match> empty   = Collections.emptyList();
  Match[]     noBound = arr(a, b, d, h, i, e, j, k, c, f, l, m, g, n, o);

  List<Match> left,       right;
  Match[]     leftRight,  rightLeft, skip;

  void tests(boolean inclusive)
  {
    int cnt = 0;
    for (Match match: new BoundedMatchIterator(o, empty, empty, true, inclusive))
    {
      assertTrue(match == o);
      ++cnt;
    }
    assertEquals((inclusive ? 1 : 0), cnt);

    cnt = 0;
    for (Match match : new BoundedMatchIterator(a, left, right, true, inclusive))
    {
      assertTrue(match == leftRight[cnt++]);
    }
    assertEquals(leftRight.length, cnt);

    cnt = 0;
    BoundedMatchIterator iter =
      new BoundedMatchIterator(a, left, right, true, inclusive);
    for (Match match : iter)
    {
      assertTrue(match == skip[cnt++]);
      iter.skipChilds();
    }
    assertEquals(skip.length, cnt);

    cnt = 0;
    for (Match match : new BoundedMatchIterator(a, left, right, false, inclusive))
    {
      assertEquals(rightLeft[cnt++], match);
    }
    assertEquals(rightLeft.length, cnt);

    cnt = 0;
    for (Match match : new BoundedMatchIterator(a, list0(a), list0(a), true, inclusive))
    {
      assertTrue(match == noBound[cnt++]);
    }
    assertEquals((inclusive ? noBound.length : 0), cnt);
  }

  @Test public void inclusive()
  {
    left  = list(a, b, d, i);
    right = list(a, c, g, n);
    leftRight = arr(e, j, k, f, l, m);
    rightLeft = arr(f, m, l, e, k, j);
    skip      = arr(e, f);
    tests(true);

    left = list(a, b, d);
    right = list(a, c, g);
    tests(true);
  }

  @Test public void exclusive()
  {
    left  = list(a, b, e, j);
    right = list(a, c, f, m);
    leftRight = arr(d, h, i, g, n, o);
    rightLeft = arr(g, o, n, d, i, h);
    skip      = arr(d, g);
    tests(false);

    left  = list(a, b, e);
    right = list(a, c, f);
    tests(false);
  }
}
