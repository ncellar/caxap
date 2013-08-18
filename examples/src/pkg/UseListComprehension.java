package pkg;

import java.util.List;

require macro pkg.ListComprehension:*;

public class UseListComprehension
{
  public static void main(String[] args)
  {
    // [ "a", "b", "c" ]
    System.out.println([String x
      for String x in new String[]{ "a", "b", "c" } ]);

    // [ "a", "b", "c" ]
    System.out.println([String x
      for String x in new String[]{ "a", "", "b", "c", "" } if !x.isEmpty() ]);

    // [ad, bd, cd, ae, be, ce, af, bf, cf]
    System.out.println([String x + y
      for String x in new String[]{ "a", "b", "c"}
      for String y in new String[]{ "d", "e", "f"} ]);

    // [ad, bd, cd, ae, be, ce, af, bf, cf]
    System.out.println([String x + y
      for String x in new String[]{ "a", "", "b", "c", "" } if !x.isEmpty()
      for String y in new String[]{ "d", "", "e", "f", "" } if !y.isEmpty() ]);
  }
}
