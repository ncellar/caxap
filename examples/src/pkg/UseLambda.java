package pkg;

require my_util.Function;
require macro pkg.Lambda;

public class UseLambda
{
  public static void main(String[] args)
  {
    Function<String> func = fn(String x, Integer y) : String { return x + y; };
    System.out.println(func.call("hello", 1));
  }
}