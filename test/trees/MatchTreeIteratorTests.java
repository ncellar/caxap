package trees;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static util.ArrayUtils.arr;

import java.util.Arrays;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

import parser.Match;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MatchTreeIteratorTests
{
  Match match(Match... childs)
  {
    return new Match(null, null, 0, 0, Arrays.asList(childs));
  }

  @Test public void treeIterator()
  {
    Match a = match();
    Match b = match();
    Match c = match(a, b);
    Match d = match();
    Match e = match();
    Match f = match(d, e);
    Match g = match(c, f);
    Match h = match();
    Match i = match(g, h);

    Match[] leftRight = arr(i, g, c, a, b, f, d, e, h);
    Match[] rightLeft = arr(i, h, g, f, e, d, c, b, a);
    Match[] skipping  = arr(i, g, c, a, b, f, h);

    int j = 0;
    for (Match m : new MatchIterator(i)) {
      assertTrue(m == leftRight[j++]);
    }
    assertEquals(leftRight.length, j);

    j = 0;
    for (Match m : new MatchIterator(i, false)) {
      assertTrue(m == rightLeft[j++]);
    }
    assertEquals(rightLeft.length, j);

    // skipping childs of the root match
    j = 0;
    MatchIterator iter = new MatchIterator(i);
    iter.skipChilds(); // should do nothing
    iter.next();
    iter.skipChilds();
    assertTrue(!iter.hasNext());

    // skipping childs of a non-leaf, non-root match
    j = 0;
    iter = new MatchIterator(i);
    while(iter.hasNext())
    {
      Match m = iter.next();
      assertTrue (m == skipping[j++]);
      if (m == f) {
        iter.skipChilds();
      }
    }
    assertEquals(skipping.length, j);

    // skipping childs of a leaf match
    j = 0;
    iter = new MatchIterator(i);
    while(iter.hasNext())
    {
      Match m = iter.next();
      assertTrue (m == leftRight[j++]);
      if (m == a) {
        iter.skipChilds();
      }
    }
    assertEquals(leftRight.length, j);
  }
}
