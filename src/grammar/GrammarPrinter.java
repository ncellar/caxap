package grammar;

import java.io.PrintStream;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import util.StringUtils;

import driver.Context;
import grammar.Expression.Rule;
import grammar.java._A_Lexical;
import grammar.java._B_Expressions;
import grammar.java._C_Statements;
import grammar.java._D_Requires;
import grammar.java._E_MacroDefinitions;

/**
 * Pretty prints the caxap Grammar, for inclusion in a latex document.
 */
public class GrammarPrinter
{
  private final Grammar grammar;
  private final List<Class<?>> classes;

  public GrammarPrinter(Grammar grammar, List<Class<?>> classes)
  {
    this.grammar = grammar;
    this.classes = classes;
  }

  public void print(PrintStream out)
  {
    for (Class<?> klass : classes) {
      out.println("\\begin{lstlisting}[breaklines=true]");
      for (Field field : klass.getDeclaredFields()) {
        Rule rule = grammar.maybeRule(field.getName());
        if (rule != null) {
          out.println(ruleString(rule));
        }
      }
      out.println("\\end{lstlisting}");
    }
  }

  public String ruleString(Rule rule)
  {
    return rule.name + " ::= "+ StringUtils.join(rule.children(), " | ") + "\n";
  }

  public static void main(String[] args)
  {
    new GrammarPrinter(Context.get().grammar(), Arrays.asList(new Class<?>[]{
      _A_Lexical.class,
      _B_Expressions.class,
      _C_Statements.class,
      _D_Requires.class,
      _E_MacroDefinitions.class
    })).print(System.out);
  }
}
