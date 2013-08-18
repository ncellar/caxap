package grammar;

import static util.MemberAccessor.setFinal;
import grammar.Expression.Rule;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * A grammar is a set of named parsing expressions. One of the rule is the root
 * rule, which is the entry point to the grammar.
 */
public class Grammar
{
  /*****************************************************************************
   * A mapping from rule names to the corresponding parsing expression.
   */
  private final Map<String, Rule> rules = new HashMap<>();

  /****************************************************************************/
  public final ExpressionTreeCleaner cleaner = new ExpressionTreeCleaner(this);

  /*****************************************************************************
   * Builds the grammar from a class with parsing expression fields. The fields
   * whose type is Expression (or one of its subclasses) are converted to rules,
   * using the field name as rule name. The root rule needs to be held in a
   * field named "root".
   *
   * Mostly, building a grammar means building a proper expression graph from
   * the class. When modeling a grammar as a Java class, reference to other
   * rules can be implemented as reference to the field that holds the rule, but
   * because of recursion, that alone doesn't suffice. Hence the presence of
   * reference expressions, to allow referencing a rule before its field gets
   * declared.
   *
   * With reference expressions, expressions form a graph. Once reference are
   * resolved, this tree becomes a graph. If there is some recursion in the
   * grammar, this graph will contain loops.
   *
   * @see Expression.tidy()
   */
  public Grammar(Class<?> klass)
  {
    try {
      Object grammar = klass.newInstance();

      for (int i = 0 ; i < 2 ; ++i)
      for (Field field : klass.getFields())
      {
        Object o = field.get(grammar);

        if (Rule.class.isAssignableFrom(o.getClass()))
        {
          Rule rule = (Rule) o;

          if (i == 1) {
            // always returns $rule
            cleaner.clean(rule);
          }
          else {
            if (rule.name == null) {
              setFinal(o, "name", field.getName());
            }
            registerRule(rule);
          }
        }
      }

      // If there is an initialize(grammar) method, call it.
      Method method = klass.getMethod("initialize", Grammar.class);
      method.invoke(grammar, this);
    }
    catch (NoSuchMethodException e) {
      /* ignore  (from getMethod()) */
    }
    catch (Exception e) {
      e.printStackTrace();
      throw new Error("Problem while extracting grammar rules "
        + "from class " + klass, e);
    }
  }

  /*****************************************************************************
   * Adds an alternative (which is also a rule) to a rule of the base grammar.
   * The added rule should have its name set, and should have gone through the
   * cleaner. No rule with the same name as the added rule should be in the
   * grammar.
   */
  public void addRuleAlternative(
    Rule extendedRule, Rule rule, boolean prioritary)
  {
    registerRule(rule);
    addExistingRuleAlternative(extendedRule, rule, prioritary);
  }

  /*****************************************************************************
   * Same as {@link #addRuleAlternative(Rule, Rule)}, but allows the alternative
   * to already be known by the grammar.
   */
  public void addExistingRuleAlternative(
    Rule extendedRule, Rule rule, boolean prioritary)
  {
    if (prioritary) {
      extendedRule.children().add(0, rule);
    }
    else {
      extendedRule.children().add(rule);
    }
  }

  /****************************************************************************/
  public void removeRuleAlternative(Rule extendedRule, Rule rule)
  {
    unregisterRule(rule);
    extendedRule.children().remove(rule);
  }

  /*****************************************************************************
   * Registers a rule with the grammar. Throws an error if there is already a
   * rule with the same name.
   */
  public void registerRule(Rule rule)
  {
    if (rules.put(rule.name, rule) != null)
    {
      throw new Error("Trying to register a rule"
        + " with an already registered name: \"" + rule.name + "\".");
    }
  }

  /*****************************************************************************
   * Makes the grammar forget about the given rule. Throws an error if the
   * grammar does not know about the rule.
   */
  public void unregisterRule(Rule rule)
  {
    if (rules.remove(rule.name) == null)
    {
      throw new Error("Trying to unregister an unknown rule: \""
        + rule.name + "<\".");
    }
  }

  /*****************************************************************************
   * Retrieves the rule with the given name. Exits the program with an error
   * if the rule does not exist.
   */
  public Rule rule(String name)
  {
    Rule rule = rules.get(name);

    if (rule == null) {
      throw new Error("No rule named \"" + name + "\".");
    }

    return rule;
  }

  /*****************************************************************************
   * Returns the rule with the given name if one exists, or null.
   */
  Rule maybeRule(String name)
  {
    return rules.get(name);
  }

  /*****************************************************************************
   * Runs a new expression through the cleaner, in order to make its use valid.
   */
  public Expression clean(Expression expr)
  {
    return cleaner.clean(expr);
  }

  /*****************************************************************************
   * Clean a rule which is not registered. This method is necessary to allow
   * for recursive rules.
   */
  public Rule cleanUnregisteredRule(Rule rule)
  {
    registerRule(rule);
    Rule out = (Rule) clean(rule);
    unregisterRule(rule);
    return out;
  }
}
