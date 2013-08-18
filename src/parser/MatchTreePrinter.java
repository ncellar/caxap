package parser;

import java.io.PrintStream;

public class MatchTreePrinter
{
  /****************************************************************************/
  private final Match root;

  /****************************************************************************/
  private final PrintStream out;

  /****************************************************************************/
  public MatchTreePrinter(Match root, PrintStream out)
  {
    this.root = root;
    this.out = out;
  }

  /****************************************************************************/
  private int indentation = 0;

  /****************************************************************************/
  public void print()
  {
    indentation = 0;
    print(root);
  }

  /****************************************************************************/
  private void print(Match match)
  {
    if (match == null) {
      out.println("Parse failed.");
      return;
    }

    printIndentation();
    out.println(match.expr);

    if (!match.expr.atomic) {
      indentation++;
      for (Match m : match.children()) {
        print(m);
      }
      indentation--;
    }
  }

  /****************************************************************************/
  private void printIndentation()
  {
    for (int i = 1 ; i <= indentation ; ++i) {
      out.print(i % 10 + " ");
    }
  }
}
