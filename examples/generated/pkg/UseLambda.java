package pkg;

import my_util.Function;
public class UseLambda
{
  public static void main(String[] args)
  {
    Function<String> func = new Function<String> ( String.class,Integer.class ) {
  	  public String call_implem(Object... __params__) {
  	    String x = (String) __params__[0];Integer y = (Integer) __params__[1]; { return x + y; } }
  	};
    System.out.println(func.call("hello", 1));
  }
}