package grammar;

import static grammar.Expression.*;

/**
 * Using in this context, the visitor pattern is a way to work around the lack
 * of multimethods in the Java language. I.e. in Java, the specific overloaded
 * method to use is picked at compile time and therefore depends of the static
 * type of its parameters.
 *
 * Implementing this interface allows for a class to get the multimethod
 * behavior (choice of the overloaded variant depending on the runtime type) by
 * doing a call like "expression.accept(this)". The Expression class has the
 * abstract method "accept()" and each of its subclasses must implement the
 * method (with the same body: "visitor.visit(this)"). In each such
 * implementation, a different overloaded variant of visit() will be selected at
 * compile-time.
 */
public interface ExpressionVisitor
{
  void visit(Capture expr);
  void visit(Rule expr);
  void visit(Choice expr);
  void visit(Sequence expr);
  void visit(Not expr);
  void visit(And expr);
  void visit(Star expr);
  void visit(Plus expr);
  void visit(Optional expr);
  void visit(Range expr);
  void visit(CharClass expr);
  void visit(StringLiteral expr);
  void visit(Any expr);
}
