package compiler;

import static trees.MatchSpec.or;
import static trees.MatchSpec.rule;
import static util.StringUtils.endsWithWhitespace;
import static util.StringUtils.escape;

import java.util.ArrayList;
import java.util.List;

import compiler.macros.MacroInterface;
import compiler.util.Quoter;
import parser.Match;
import trees.MatchSpec;

/**
 * Expanding the "quotation" rule (holding either a simple quotation or a
 * quasiquotation).
 */
public class QuotationMacro implements MacroInterface
{
  /****************************************************************************/
  public Match expand(Match quotation)
  {
    return new QuotationMacroImplem().expand(quotation);
  }
}

/******************************************************************************/
class QuotationMacroImplem
{
  // Don't recreate the match specification each time.
  private static final MatchSpec UNQUOTATION     = rule("unquotation");
  private static final MatchSpec QUASIQUOTATION  = rule("quasiquotation");
  private static final MatchSpec SQUOTATION      = rule("simpleQuotation");
  private static final MatchSpec ESCAPED_QEND    = rule("escapedQEndMarker");
  private static final MatchSpec UNARY           = rule("unaryExpression");
  private static final MatchSpec BACKSLASH       = rule("backslash");
  private static final MatchSpec UNQUOT_OR_UNARY = or(UNQUOTATION, UNARY);
  private static final MatchSpec SOURCE_SUB  =
    or(SQUOTATION, QUASIQUOTATION, UNQUOTATION, ESCAPED_QEND);
  private static final MatchSpec HASH_LIKE       =
    or(rule("hash"), rule("hashat"));


  /****************************************************************************/
  static final String PRIMITIVE_QUOTE = "compiler.util.Quoter.primitiveQuote";

  /****************************************************************************/
  static final String DYNAMIC_QUOTE = "compiler.util.Quoter.dynamicQuote";

  /*****************************************************************************
   * The expansion of the macro.
   */
  private StringBuilder expansion;

  /*****************************************************************************
   * Difference in index between the expansion and the source the quotation was
   * taken from, for the next unquotation to be processed.
   */
  private int diff;

  /*****************************************************************************
   * Index for the next insert marker to be inserted into the expansion.
   */
  private int insertMarkerCounter = 1;

  /*****************************************************************************
   * Indicates if nesting level of quotations/unquotations. Used to determine
   * if we are directly under the outermost quotation to remove escapes there.
   * Not be confused with depth.
   */
  private int nesting = 0;

  /*****************************************************************************
   * Inserts extracted from the source fragment.
   */
  private final List<Match> inserts = new ArrayList<>();

  /****************************************************************************/
  public Match expand(Match quotation)
  {
    String ruleName = quotation.first(rule("identifier")).string();

    Match sourceFragment = quotation.first(rule("sourceFragment"));

    /* Need originalString() to use Match#begin and Match#end as indications.
     * Trimming removes end whitespace (there is no starting whitespace because
     * of the grammar rule). */
    expansion = new StringBuilder(sourceFragment.originalString().trim());
    diff = - sourceFragment.begin;

    replaceUnquotations(quotation, 0);

    /* Escape the expansion to make it suitable to appear within a string
     * literal. */
    expansion.replace(0, expansion.length(), escape(expansion.toString()));

    String expandFunc = inserts.isEmpty() ? PRIMITIVE_QUOTE : DYNAMIC_QUOTE;
    expansion.insert(0, expandFunc + "(\"" + ruleName + "\", \"");
    expansion.append("\"");

    for (Match insert : inserts) {
      expansion.append(", (Object)"); // avoids ambiguity with only one insert
      expansion.append(insert.string());
    }
    expansion.append(")");

    return Quoter.primitiveQuote("unaryExpression", expansion.toString());
  }

  /*****************************************************************************
   * Recursively replace unquotations in the source fragment with insertion
   * markers.
   */
  private void replaceUnquotations(Match match, int depth)
  {
    for (Match sub : match.all(SOURCE_SUB))
    {
      /* We use child() to recurse. It is always appropriate since simpleQuotation,
       * quasiquotation and unquotation are all rules. */

      ++nesting;

      if (sub.is(QUASIQUOTATION)) {
        replaceUnquotations(sub.child(), depth + 1);
      }
      else if (sub.is(SQUOTATION)) {
        if (depth > 0) {
          replaceUnquotations(sub.child(), depth);
        }
        else /* depth == 0 */ {
          if  (nesting == 1)
          for (Match escape : sub.child().all(SOURCE_SUB))
          if  (escape.is(ESCAPED_QEND)) {
            unescapeEscapedEndMarker(escape);
          }
        }
      }
      else if (sub.is(ESCAPED_QEND)) {
        if (nesting == 2) {
          unescapeEscapedEndMarker(sub);
        }
      }
      else /* UNQUOTATION */ {
        processUnquotation(sub.child(), depth - 1);
      }

      --nesting;
    }
  }

  /****************************************************************************/
  private void unescapeEscapedEndMarker(Match escape)
  {
    expansion.replace(escape.begin + diff, escape.end + diff,
      escape.string().substring(1));
    diff -= 1;
  }

  /*****************************************************************************
   * If $unquotation is at depth 0, replace it by an insertion marker.
   */
  private void processUnquotation(Match unquotation, int depth)
  {
    boolean escaped = unquotation.has(
      unquotation.firstBeforeFirst(BACKSLASH, HASH_LIKE));

    if (escaped) { depth += 1; }

    if (depth == 0) {
      applyUnquotation(unquotation);
      /* The unquoted expression will appear as variadic parameter to
       * dynamicQuote(). If the expression contains nested
       * quotations/unquotations, those will be handled recursively by the macro
       * expander. */
    }
    else {
      replaceUnquotations(unquotation, depth);
    }
  }

  /*****************************************************************************
   * Add the unquoted expression to {@link #inserts} and replace the unquotation
   * by an insertion marker.
   */
  private void applyUnquotation(Match unquot)
  {
    Match unary = unquot.first(UNQUOT_OR_UNARY);

    if (unary.is(UNQUOTATION)) {
      throw new Error("Unquotation with negative depth.");
    }

    inserts.add(unary);

    String str = unquot.string();
    boolean isSplice = str.startsWith("#@");

    String hashLike = isSplice
      ? "#@" + unquot.first(rule("spliceDelimiters")).string()
      : "#";

    boolean spaced = endsWithWhitespace(unquot.originalString());
    String marker = hashLike + insertMarkerCounter++ + (spaced ? " " : "");

    expansion.replace(unquot.begin + diff, unquot.end + diff, marker);

    diff = diff - unquot.length() + marker.length();
  }
}
