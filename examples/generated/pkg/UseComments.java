package pkg;

public class UseComments
{
  /* This is a /@ nested @/
    /@
      /@ comment! @/
    @/ */public static void test()
  {
    // 42 !
    int x = /* -(4 * 3 + (2 + 7)) */42;
  }
}