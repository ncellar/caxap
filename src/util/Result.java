package util;

public class Result<T>
{
  private T t;
  public T set(T t) { this.t = t; return t; }
  public T get()    {             return t; }
}
