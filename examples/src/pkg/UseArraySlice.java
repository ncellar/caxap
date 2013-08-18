package pkg;

require macro pkg.ArraySlice;

import static java.util.Arrays.asList;

public class UseArraySlice
{
  public static void main(String[] args)
  {
    Integer[][] array = new Integer[][] { new Integer[] { 1, 2, 3, 4 } };
    System.out.println(asList(array[0][1:3]));          // 2, 3
    System.out.println(asList(array[0][1:]));           // 2, 3, 4
    System.out.println(asList(array[0][:3]));           // 1, 2, 3
    System.out.println(asList(array[0][:]));            // 1, 2, 3, 4
    System.out.println(asList(array[0][1:-1]));         // 2, 3
    System.out.println(asList(array[0][-3:3]));         // 2, 3
    System.out.println(array[0][-3:3].toString());      // 2, 3
    System.out.println(asList(array[0:1][0][-3:3]));    // 2, 3
    System.out.println(array[0:1][0][-3:3].toString()); // 2, 3
  }
}