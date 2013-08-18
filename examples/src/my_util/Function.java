package my_util;

/**
 * Represents an anonymous function.
 */
public abstract class Function<OutType>
{
  public final Class<?>[] paramTypes;
  
  public Function(Class<?>... paramTypes)
  {
    this.paramTypes = paramTypes;
  }
  
  protected abstract OutType call_implem(Object... params);
  
  public OutType call(Object... params)
  {
    if (params.length != paramTypes.length)
    {
      throw new IllegalArgumentException("Wrong number of parameters: expected "
        + paramTypes.length + ", but got " + params.length + ".");
    }
    
    for (int i = 0 ; i < params.length ; ++i) {
      if (!paramTypes[i].isAssignableFrom(params[i].getClass()))
      {
        throw new IllegalArgumentException(i + "th parameter is of type "
          + params[i].getClass() + ". Expected " + paramTypes[i]
          + " or a subclass.");
      }
    }
    
    return call_implem(params);
  }
  
  public OutType callFast(Object... params)
  {
    return call_implem(params);
  }
}