package main;

class Testing
{
  /****************************************************************************/
  static void findClassPath()
  {
    ClassLoader loader = ClassLoader.getSystemClassLoader();
    System.out.println(loader.getResource(""));
    System.out.println(Testing.class.getResource("Testing.class"));
  }

  /****************************************************************************/
  public static void main(final String[] args)
  {
  }
}