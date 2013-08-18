package util;

import java.lang.reflect.Method;

import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;

import static org.junit.Assert.*;
import static util.ArrayUtils.*;
import static util.MemberAccessor.*;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class MemberAccessorTests
{
  static class Test1
  {
    /* Different kind of methods: no params, some params, variadic params alone,
     * variadic params with other params, ... */
    public void  test1()                       {}
    public void  test2(Test1 x)                {}
    public void  test3(Test1 x,    Test1 y)    {}
    public void  test4(Test1... x)             {}
    public void  test5(String x,   Test1... y) {}
    public void  test6(Test1 x,    Test1... y) {}

    // Private method.
    @SuppressWarnings("unused")
    private void test7(Test1 x,    Test1... y) {}

    /* Ambiguous overloaded methods: the selected method needs to be
     * disambiguated using an arbiter. */
    public int test8(Test1 x)                   { return 1; }
    public int test8(Test2 x)                   { return 2; }
    public int test9(Test1 x, Test2 y, Test2 z) { return 1; }
    public int test9(Test2 x, Test1 y, Test1 z) { return 2; }

    // Methods taking primitive parameters.
    public void test10(int x)     {}
    public void test11(int... x)  {}
  }

  static class Test2 extends Test1 {}

  static final Class<Test1> klass = Test1.class;

  static Method test1;
  static Method test2;
  static Method test3;
  static Method test4;
  static Method test5;
  static Method test6;

  @BeforeClass public static void setup() throws Exception
  {
    test1 = klass.getMethod("test1");
    test2 = klass.getMethod("test2", Test1.class);
    test3 = klass.getMethod("test3", Test1.class, Test1.class);
    test4 = klass.getMethod("test4", Test1[].class);
    test5 = klass.getMethod("test5", String.class, Test1[].class);
    test6 = klass.getMethod("test6", Test1.class, Test1[].class);
  }

  @Test public void aa_checkParams() throws Exception
  {
    assertTrue  (checkParams(test1, new Class<?>[0]));
    assertFalse (checkParams(test1, arr(Object.class)));
    assertTrue  (checkParams(test2, arr(Test1.class)));
    assertTrue  (checkParams(test2, arr(Test2.class)));
    assertFalse (checkParams(test2, arr(Object.class)));
    assertTrue  (checkParams(test3, arr(Test2.class, Test2.class)));
    assertTrue  (checkParams(test4, new Class<?>[0]));
    assertTrue  (checkParams(test4, arr(Test2.class)));
    assertTrue  (checkParams(test4, arr(Test2.class, Test2.class)));
    assertFalse (checkParams(test4, arr(Object.class)));
    assertTrue  (checkParams(test5, arr(String.class)));
    assertTrue  (checkParams(test5, arr(String.class, Test2.class)));
    assertTrue  (checkParams(test5, arr(String.class, Test2.class, Test2.class)));
    assertTrue  (checkParams(test6, arr(Test2.class)));
    assertTrue  (checkParams(test6, arr(Test2.class, Test2.class)));
    assertTrue  (checkParams(test6, arr(Test2.class, Test2.class, Test2.class)));
    assertFalse (checkParams(test6, new Class<?>[0]));
  }

  @Test public void ab_repackageParams() throws Exception
  {
    Test1 t = new Test1();

    test1.invoke(t, repackageParams(test1, arr()));
    test2.invoke(t, repackageParams(test2, arr(t)));
    test4.invoke(t, repackageParams(test4, arr()));
    test4.invoke(t, repackageParams(test4, arr(t)));
    test4.invoke(t, repackageParams(test4, arr(t, t)));
    test5.invoke(t, repackageParams(test5, arr("x")));
    test5.invoke(t, repackageParams(test5, arr("x", t)));
    test6.invoke(t, repackageParams(test6, arr(t)));
    test6.invoke(t, repackageParams(test6, arr(t, t)));
  }

  @Test public void ac_invoke() throws Exception
  {
    /* Test2 subclasses Test1, which defines the methods. So the invocations
     * will test that the hierarchy is traversed properly. */
    Test1 t = new Test2();

    invoke(t, "test1");
    invoke(t, "test2", t);
    invoke(t, "test3", t, t);
    invoke(t, "test4");
    invoke(t, "test4", t);
    invoke(t, "test4", t, t);
    invoke(t, "test5", "x");
    invoke(t, "test5", "x", t, t);
    invoke(t, "test6", t);
    invoke(t, "test6", t, t, t);
    invoke(t, "test7", t, t, t);

    assertEquals(1, invoke(t, "test8", arr(Test1.class), t));
    assertEquals(2, invoke(t, "test8", arr(Test2.class), t));
    assertEquals(1, invoke(t, "test9", arr(Test2.class, Test2.class),
      t, t, t));
    assertEquals(2, invoke(t, "test9", arr(Test2.class, Test1.class),
      t, t, t));

    invoke(t, "test10", 1);
    invoke(t, "test11");
    invoke(t, "test11", 1, 1, 1);

    try {
      invoke(t, "test11", "x");
      fail("Expected NoSuchMethodException.");
    }
    catch (MemberAccessException e) {
      assertTrue(e.getCause() instanceof NoSuchMethodException);
    }

    try {
      invoke(t, "testDontExist");
      fail("Expected NoSuchMethodException.");
    }
    catch (MemberAccessException e) {
      assertTrue(e.getCause() instanceof NoSuchMethodException);
    }
  }
}
