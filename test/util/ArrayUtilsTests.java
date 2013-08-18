package util;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.*;
import static util.ArrayUtils.*;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class ArrayUtilsTests
{
  @Test public void aa_concat()
  {
    String x = "x", y = "y", z = "z";

    // concat(array, elem)
    assertArrayEquals(arr(x),       concat(arr(),     x));
    assertArrayEquals(arr(x, y),    concat(arr(x),    y));
    assertArrayEquals(arr(x, y, z), concat(arr(x, y), z));

    // concat(array, array)
    assertArrayEquals(arr(),        concat(arr(),     arr()));
    assertArrayEquals(arr(x),       concat(arr(),     arr(x)));
    assertArrayEquals(arr(x),       concat(arr(x),    arr()));
    assertArrayEquals(arr(x, y),    concat(arr(x),    arr(y)));
    assertArrayEquals(arr(x, y, z), concat(arr(x, y), arr(z)));
    assertArrayEquals(arr(x, y, z), concat(arr(x),    arr(y, z)));
  }

  @Test public void ab_cartesian()
  {
    String x = "x", y = "y", z = "z";

    assertArrayEquals(arr2(),           cartesian(arr2(), arr()));
    assertArrayEquals(arr2(),           cartesian(arr2(arr(x)), arr()));
    assertArrayEquals(arr2(arr(x, y)),  cartesian(arr2(arr(x)), arr(y)));

    assertArrayEquals(
      arr2(arr(x, y, z)),               cartesian(arr2(arr(x, y)), arr(z)));
    assertArrayEquals(
      arr2(arr(x, y), arr(x, z)),       cartesian(arr2(arr(x)),    arr(y, z)));

    assertArrayEquals(
      arr2(arr(x, y), arr(x, z), arr(y, y), arr(y, z)),
      cartesian(arr2(arr(x), arr(y)), arr(y, z)));
  }

  @Test public void ac_contains()
  {
    String x = "x", y = "y", z = "z";

    // contains(array, elem)
    assertFalse (contains(arr(),     x));
    assertTrue  (contains(arr(x),    x));
    assertFalse (contains(arr(x),    y));
    assertTrue  (contains(arr(x, y), x));
    assertTrue  (contains(arr(x, y), y));
    assertFalse (contains(arr(x, y), z));

    // contains(array, array)
    assertTrue  (contains(arr(),        arr()));
    assertFalse (contains(arr(),        arr(x)));
    assertTrue  (contains(arr(x),       arr()));
    assertTrue  (contains(arr(x),       arr(x)));
    assertTrue  (contains(arr(x, y),    arr(x)));
    assertFalse (contains(arr(x, y),    arr(z)));
    assertTrue  (contains(arr(x, y),    arr(x, y)));
    assertFalse (contains(arr(x, y),    arr(y, x)));
    assertTrue  (contains(arr(x, y, z), arr(x, y)));
    assertTrue  (contains(arr(x, y, z), arr(y, z)));
    assertTrue  (contains(arr(x, y, z), arr(x, z)));
    assertFalse (contains(arr(x, y, z), arr(z, x)));
  }
}
