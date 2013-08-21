package pkg;

require macro pkg.Unless;

class UseUnless
{
  public static void main(String[] args)
  {
    unless false {
      System.out.println("Hopla boum!");
    }
  }
}
