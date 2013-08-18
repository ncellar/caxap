package grammar;

import java.util.Collections;
import java.util.List;

import compiler.Macro;

import static java.util.Arrays.asList;

/**
 * This class was greatly inspired by the Expr class from the parser generator
 * Mouse, by Roman R. Redziejowski (www.romanredz.se).
 *
 * This abstract class represent a parsing expression (the kind that underpins
 * parsing expression grammars (PEG)).
 *
 * Its concrete (inner) subclasses represent all the kinds of parsing expression
 * supported by this framework (syntactic sugar notwithstanding).
 *
 * The class concerns itself with modeling the relation between expressions and
 * sub-expressions, keeping track of which expression are also named grammar
 * rules and building textual representation for expressions.
 */
public abstract class Expression
{
  //============================================================================
  // FIELDS
  //============================================================================

  /*****************************************************************************
   * The grammar this expression belongs to.
   */
  public Grammar grammar;

  /*****************************************************************************
   * Holds a textual representation of the expression, generated using rule
   * names or textual representations of sub-expressions. Expressions whose
   * textual representation are equivalent are guaranteed to be semantically
   * equivalent.
   */
  protected String repr;

  /*****************************************************************************
   * Array of sub-expressions. null for expressions that have no children, and
   * potentially empty for expressions with a variable number of children.
   */
  private final List<Expression> children;

  /*****************************************************************************
   * Operator precedence, higher means more precedence.
   */
  final int precedence;

  /*****************************************************************************
   * Is the expression atomic? An atomic expression does generate a leaf in the
   * match tree, the errors encountered while parsing it are not logged, leaving
   * a single error at the start of the whole expression in case it fails to
   * parse.
   *
   * Said otherwise, an atomic expression is parsed as tough the parser was a
   * Recognizer, even if it isn't.
   *
   * Literals expressions (characters and strings) are naturally atomic. So is
   * the Not expression. Composite expressions can also be made atomic, in order
   * to avoid polluting the match tree with irrelevant information.
   */
  public boolean atomic = false;

  /****************************************************************************/
  public MatchCallbacks callbacks;

  //============================================================================
  // CONSTRUCTOR
  //============================================================================

  /****************************************************************************/
  Expression(int precedence)
  {
    this.precedence = precedence;
    this.children = Collections.<Expression>emptyList();
  }

  /****************************************************************************/
  Expression(int precedence, List<Expression> children)
  {
    this.precedence = precedence;
    this.children = children;
  }

  //============================================================================
  // METHODS
  //============================================================================

  /*****************************************************************************
   * Returns the rule name if the expression is a rule, else a generated name.
   * Expressions whose name are equivalent are guaranteed to be semantically
   * equivalent.
   */
  public String toString()
  {
    return repr;
  }

  /****************************************************************************/
  public MatchCallbacks callbacks()
  {
    return (callbacks == null) ? MatchCallbacks.DEFAULT : callbacks;
  }

  /*****************************************************************************
   * @see ExpressionVisitor
   */
  public abstract void accept(ExpressionVisitor visitor);

  /****************************************************************************/
  public List<Expression> children()
  {
    return children;
  }

  /****************************************************************************/
  public Expression child()
  {
    return children.get(0);
  }

  //============================================================================
  // CLASSES
  //============================================================================

  /*****************************************************************************
   * Represents a reference to a named grammar rule: '<' <ruleName> '>'
   */
  public static class Reference extends Expression
  {
    public final String referencedRule;

    public Reference(final String referencedRule)
    {
      super(4);
      this.referencedRule = referencedRule;
    }

    // References get eliminated, so we don't want to visit them.
    public void accept(ExpressionVisitor visitor) { }
  }

  /*****************************************************************************
   * Represent a grammar rule. A rule works just like a choice in that that it
   * can have multiple alternatives. This enables modifications of the grammar
   * at compile time.
   */
  public static class Rule extends Expression
  {
    /* Final field might be changed by grammar.Grammar. */
    public final String name;

    /** Unique ID */
    public int id;

    public Rule(String ruleName, List<Expression> children)
    {
      super(4, children);
      this.name = ruleName;
    }

    public void accept(ExpressionVisitor visitor) { visitor.visit(this); }
  }

  /*****************************************************************************
   * A rule generated by a macro definition.
   */
  public static class MacroRule extends Rule
  {
    public final Macro macro;

    public MacroRule(String name, Macro macro, Expression child)
    {
      super(name, asList(child));
      this.macro = macro;
    }

    public void accept(ExpressionVisitor visitor) { visitor.visit(this); }
  }

  /*****************************************************************************
   * Represent a named-capture of an expression in a user-specified rule.
   */
  public static class Capture extends Expression
  {
    public final String captureName;

    public Capture(final String captureName, final Expression child)
    {
      super(3 , asList(child));
      this.captureName = captureName;
    }

    public void accept(ExpressionVisitor visitor) { visitor.visit(this); }
  }

  /*****************************************************************************
   * Represents a choice amongst alternatives: <expr> (| <expr>)*
   */
  public static class Choice extends Expression
  {
    public Choice(final List<Expression> children)
    {
      super(0, children);
    }

    public void accept(ExpressionVisitor visitor) { visitor.visit(this); }
  }

  /*****************************************************************************
   * Represents a sequence of expressions: <expr>*
   */
  public static class Sequence extends Expression
  {
    public Sequence(final List<Expression> children)
    {
      super(1, children);
    }

    public void accept(ExpressionVisitor visitor) { visitor.visit(this); }
  }

  /*****************************************************************************
   * Represents an additional check for expression conformance: &<expr>
   */
  public static class And extends Expression
  {
    public And(final Expression child)
    {
      super(2, asList(child));
    }

    public void accept(ExpressionVisitor visitor) { visitor.visit(this); }
  }

  /*****************************************************************************
   * Represents a check for lack of conformance to an expression: !<expr>
   */
  public static class Not extends Expression
  {
    public Not(final Expression child)
    {
      super(2, asList(child));
      this.atomic = true;
    }

    public void accept(ExpressionVisitor visitor) { visitor.visit(this); }
  }

  /*****************************************************************************
   * Represents a sequence of one or more expression of the same kind: <expr>+
   */
  public static class Plus extends Expression
  {
    public Plus(final Expression child)
    {
      super(3, asList(child));
    }

    public void accept(ExpressionVisitor visitor) { visitor.visit(this); }
  }

  /*****************************************************************************
   * Represents a sequence of zero or more expression of the same kind: <expr>*
   */
  public static class Star extends Expression
  {
    public Star(final Expression child)
    {
      super(3, asList(child));
    }

    public void accept(ExpressionVisitor visitor) { visitor.visit(this); }
  }

  /*****************************************************************************
   * Represents an optional expression: <expr>?
   */
  public static class Optional extends Expression
  {
    public Optional(final Expression child)
    {
      super(3, asList(child));
    }

    public void accept(ExpressionVisitor visitor) { visitor.visit(this); }
  }

  /*****************************************************************************
   * Represents a String literal, such as "potato"
   */
  public static class StringLiteral extends Expression
  {
    public final String string;

    public StringLiteral(final String string)
    {
      super(4);
      this.atomic = true;
      this.string = string;
    }

    public void accept(ExpressionVisitor visitor) { visitor.visit(this); }
  }

  /*****************************************************************************
   * Represents a character range, such as [a - z]
   */
  public static class Range extends Expression
  {
    public final char first;
    public final char last;
    public final boolean negated;

    public Range(final char first, final char last, final boolean negated)
    {
      super(4);
      this.atomic  = true;
      this.first   = first;
      this.last    = last;
      this.negated = negated;
    }

    public void accept(ExpressionVisitor visitor) { visitor.visit(this); }
  }

  /*****************************************************************************
   * Represents a character class, such as [aeiouy] or [^aeiouy]. The leading
   * caret (^) indicates negation, so the class represented is the one that
   * contains all the characters that do not follow the caret.
   */
  public static class CharClass extends Expression
  {
    public final String chars;
    public final boolean negated;

    public CharClass(final String chars, final boolean negated)
    {
      super(4);
      this.atomic  = true;
      this.chars   = chars;
      this.negated = negated;
    }

    public void accept(ExpressionVisitor visitor) { visitor.visit(this); }
  }

  /*****************************************************************************
   * Represents any character.
   */
  public static class Any extends Expression
  {
    public static Any GET = new Any();

    private Any()
    {
      super(4);
      this.atomic = true;
    }

    public void accept(ExpressionVisitor visitor) { visitor.visit(this); }
  }
}
