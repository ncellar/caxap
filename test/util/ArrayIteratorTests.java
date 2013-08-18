package util;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static util.ArrayUtils.arr;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ArrayIteratorTests
{

  void test(int dir, Integer[] a, int start, int end)
  {
    ArrayIterator<Integer> iter = new ArrayIterator<>(a, start, end);
    int i = start;
    assertTrue(iter.current() ==  null);
    assertEquals(-1, iter.index());
    assertEquals(-1, iter.iterIndex());
    for (int x : iter) {
      assertEquals(i, iter.index());
      assertEquals((long) dir * (i - start), iter.iterIndex());
      assertEquals(i, x);
      i += dir;
    }
    iter.forward();
    assertTrue(iter.current() ==  null);
    assertEquals(-1, iter.index());
    assertEquals(-1, iter.iterIndex());
  }

  @Test public void aa_iterator()
  {
    Integer[] a = arr(0, 1, 2, 3, 4, 5, 6);

    test(1, a, 0, a.length);
    test(-1, a, a.length - 1, -1);
    test(1, a, 2, 5);
    test(-1, a, 5, 2);
  }
}
