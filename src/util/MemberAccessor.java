package util;

import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Provide facilities to access class members. Useful to access hidden class
 * members, especially to perform tests.
 *
 * TODO There is currently one situation in which getMethod() (and therefore
 * invoke() too) may not be able to retrieve the desired method: if two method
 * differ only by the parameter type of some of their argument having generic
 * types. It can be fixed: http://stackoverflow.com/questions/3609799
 *
 * TODO getMethod() does not play nice with null.
 */
public class MemberAccessor
{
  /*****************************************************************************
   * Set the final field with name $field on object $o to $value.
   * Beware that the final field should not be initialized to an observable
   * compile-time constant, or this might not work.
   */
  public static void setFinal(Object o, String field, Object value)
  {
    try {
      Field f = o.getClass().getField(field);
      f.setAccessible(true);
      f.set(o, value);
    }
    catch (NoSuchFieldException | IllegalAccessException e) {
      throw new MemberAccessException(e);
    }
  }

  /*****************************************************************************
   * Invokes the method with the given name on the receiver, supplying it the
   * passed parameters. Useful to call non-public methods.
   *
   * If there are multiple method applicable for the supplied parameters, just
   * runs the first one it encounters. No ordering of methods is guaranteed,
   * neither is the fact that the ordering stays the same.
   */
  public static Object invoke(
    Object receiver, String selector, Object... params)
  {
    return invoke(receiver, selector, null, params);
  }

  /*****************************************************************************
   * Same as {@link #invoke(Object, Method, Object...), but takes an additional
   * $arbiter parameter to distinguish between multiple applicable methods.
   *
   * The first applicable method whose parameter classes include those in
   * "arbiter", in the same order (but not necessarily adjacent) is run.
   */
  public static Object invoke(
    Object receiver, String selector, Class<?>[] arbiter, Object... params)
  {
    Method method = getMethod(receiver, selector, arbiter, params);

    if (method == null) {
      throw new MemberAccessException(
        new NoSuchMethodException("No method named \"" + selector
          + "\" matching the supplied parameters."));
    }

    return invoke(receiver, method, params);
  }

  /*****************************************************************************
   * Invokes the given method on the given object and with the given parameters.
   *
   * The parameters are passed as though this was the regular the method. No
   * trickery is needed, as would be the case with {@link Method#invoke()}. See
   * {@link #repackageParams()}) to know exactly what you're avoiding.
   *
   * The method is rendered accessible before invoking it, and stays that way.
   */
  public static Object invoke(
    Object receiver, Method method, Object... params)
  {
    method.setAccessible(true);
    try {
      return method.invoke(receiver, repackageParams(method, params));
    }
    catch (IllegalAccessException | InvocationTargetException e) {
      throw new MemberAccessException(e);
    }
  }

  /****************************************************************************
   * Return the Method that would be invoked if calling
   * {@link #invoke(Object, String, Class[], Object...) with the same
   * parameters. Allows for more efficient and terse code if the method is to be
   * invoked multiple times.
   */
  public static Method getMethod(
    Object receiver, String selector, Class<?>[] arbiter, Object... params)
  {
    // Iterate on class hierarchy, from bottom to top.
    for ( Class<?> klass = receiver.getClass()
        ; klass != null
        ; klass = klass.getSuperclass())
    {
      Method m = getMethod(klass, receiver, selector, arbiter, params);
      if (m != null) {
        return m;
      }
    }

    return null;
  }

  /*****************************************************************************
   * Same as {@link #getMethod(Object, String, Class[], Object...)}, but only
   * search the methods declared in $klass, not in the rest of the hierarchy of
   * the receiver's class.
   */
  private static Method getMethod(Class<?> klass,
    Object receiver, String selector, Class<?>[] arbiter, Object[] params)
  {
    final List<Method> named = new ArrayList<>();

    // Find all methods in the class with the given name.
    for (final Method m : klass.getDeclaredMethods())
    {
      if (m.getName().equals(selector)) {
        named.add(m);
      }
    }

    // Return the first method with the given name that matches the given
    // parameters and the arbiter, if any.
    for (final Method m : named)
    {
      final Class<?>[] actualClasses = getParamClasses(params);
      final Class<?>[] formalClasses = m.getParameterTypes();

      if (checkParams(m, actualClasses)
        && (arbiter == null || ArrayUtils.contains(formalClasses, arbiter)))
      {
        return m;
      }
    }

    return null;
  }

  /*****************************************************************************
   * Checks that the supplied method is able to accept parameters with the
   * supplied classes.
   *
   * Protected for testing.
   */
  protected static boolean checkParams(Method m, Class<?>[] actuals)
  {
    final Class<?>[] formals = m.getParameterTypes();

    int upper = formals.length;

    if (m.isVarArgs())
    {
      if (actuals.length < formals.length - 1) {
        return false;
      }

      final Class<?> varClass = formals[--upper].getComponentType();

      for (int i = upper ; i < actuals.length ; ++i) {
        if (!isAssignableFrom(varClass, actuals[i])) {
          return false;
        }
      }
    }
    else if (actuals.length != formals.length)
    {
      return false;
    }

    for (int i = 0 ; i < upper ; ++i)
    {
      if (!isAssignableFrom(formals[i], actuals[i])) {
        return false;
      }
    }

    return true;
  }

  /*****************************************************************************
   * Returns true if a variable of type "one" can be assigned from a value of
   * type "two". This does basically the same thing as the
   * Class#isAssignableFrom(Class) method, but this also checks if "one" is a
   * primitive class (int.class, etc) and "two" a boxed class (Integer.class,
   * etc). The reverse ("one" boxed and "two" primitive), while valid in the
   * sense of the above definition, is not checked.
   */
  private static boolean isAssignableFrom(Class<?> one, Class<?> two)
  {
    if( (one == byte.class    && two == Byte.class)
     || (one == short.class   && two == Short.class)
     || (one == int.class     && two == Integer.class)
     || (one == long.class    && two == Long.class)
     || (one == char.class    && two == Character.class)
     || (one == float.class   && two == Float.class)
     || (one == double.class  && two == Double.class)
     || (one == boolean.class && two == Boolean.class))
    {
      return true;
    }
    else {
      return one.isAssignableFrom(two);
    }
  }

  /*****************************************************************************
   * Repackage the parameters supplied to invoke() in order to make them
   * agreeable to Method#invoke(). The problem is that Method#invoke() expects
   * the variadic arguments to be passed as an array of the appropriate type.
   *
   * Protected for testing.
   */
  protected static Object[] repackageParams(Method m, Object[] params)
  {
    if (!m.isVarArgs())
    {
      return params;
    }
    else
    {
      final Class<?>[] paramClasses = m.getParameterTypes();
      final int argCount = paramClasses.length;
      final Object[] out = new Object[argCount];

      // Gather all non-variadic parameters.
      for (int i = 0 ; i < argCount - 1 ; ++i)
      {
        out[i] = params[i];
      }

      /* Package the variadic parameters in an array. If there are none, use an
       * empty array instead. */
      if (params.length == argCount - 1)
      {
        out[argCount - 1] = Array.newInstance(
          paramClasses[argCount - 1].getComponentType(), 0);
      }
      else
      {
        final int last   = argCount - 1;
        final int length = params.length - last;

        out[last] = Array.newInstance(
          paramClasses[last].getComponentType(), length);

        for (int i = 0 ; i < length ; ++i)
        {
          Array.set(out[last], i, params[last + i]);
        }
      }

      return out;
    }
  }

  /*****************************************************************************
   * Returns an array containing the classes of the objects passed as
   * parameters.
   */
  private static Class<?>[] getParamClasses(Object... params)
  {
    final Class<?> classes[] = new Class<?>[params.length];

    for (int i = 0 ; i < params.length ; ++i)
    {
      classes[i] = params[i].getClass();
    }

    return classes;
  }
}
