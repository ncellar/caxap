package util;

import java.util.List;

public class StringUtils
{
  /*****************************************************************************
   * Returns a string where the escapable characters in the input string have
   * been replaced by the corresponding escapes. The inserted escapes are: \b,
   * \t, \n, \f, \r, \", \\. Albeit \' is a valid escape, we do not escape it
   * since the single quote is valid within string literals.
   */
  public static String escape(String string)
  {
    final StringBuilder result = new StringBuilder(string.length());

    for (char c : string.toCharArray()) {
      if (c == '\'') { result.append('\''); }
      else           { result.append(escape(c)); }
    }

    return result.toString();
  }

  /*****************************************************************************
   * Returns a string that corresponds to the escape sequence for character c.
   * The returns string will either only contain the character c, or one of the
   * sequence \b, \t, \n, \f, \r, \", \', \\.
   *
   * So for instance, the following expressions are true:
   *
   * <pre>
   * escape('\n').equals(&quot;\\n&quot;);
   * escape('x').equals(&quot;x&quot;);
   * </pre>
   */
  public static String escape(char c)
  {
    switch(c)
    {
    case '\b': return "\\b";
    case '\t': return "\\t";
    case '\n': return "\\n";
    case '\f': return "\\f";
    case '\r': return "\\r";
    case '\"': return "\\\"";
    case '\'': return "\\'";
    case '\\': return "\\\\";
    default:   return "" + c;
    }
//    if ("\b\t\n\f\r\"\'\\".indexOf(c) != -1) {
//        return "\\" + c;
//    }
//    else {
//      return "" + c;
//    }
  }

  /*****************************************************************************
   * Converts the single-character escapes to the character they represent.
   * These single-character escapes are: \b, \t, \n, \f, \r, \", \', \\. Octal
   * or unicode escape are not handled.
   */
  public static String unescape(String string)
  {
    StringBuilder builder = new StringBuilder(string);

    for (int i = 0 ; i < builder.length() ; ++i)
    {
      if (builder.charAt(i) == '\\')
      {
        switch (builder.charAt(i+1)) {
        case 'b' : builder.replace(i, i+2, "\b"); break;
        case 't' : builder.replace(i, i+2, "\t"); break;
        case 'n' : builder.replace(i, i+2, "\n"); break;
        case 'f' : builder.replace(i, i+2, "\f"); break;
        case 'r' : builder.replace(i, i+2, "\r"); break;
        case '"' : builder.replace(i, i+2, "\""); break;
        case '\'': builder.replace(i, i+2, "\'"); break;
        case '\\': builder.replace(i, i+2, "\\"); break;
        }
      }
    }

    return builder.toString();
  }

  /*****************************************************************************
   * Returns a string containing the string str, repeated n times.
   * from http://stackoverflow.com/questions/1235179/
   */
  public static String repeat(String str, int n)
  {
    return new String(new char[n]).replace("\0", str);
  }

  /*****************************************************************************
   * Returns a version of the string with leading and trailing whitespace removed,
   * all comments removed, and whitespace sequences compressed to a single space.
   *
   * UNUSED
   */
  public static String trimJavaWhitespace(String str)
  {
    return trimJavaWhitespaceNoComment(
      // (?s) is a flag that makes dots also match newlines.
      str .replaceAll("(?s)/\\*.*\\*/", " ") // /* comments -> space
          .replaceAll("//.*\n", " "));       // line comments -> space
  }

  /*****************************************************************************
   * Same as {@link #trimJavaWhitespace()} but keeps comments (replaces line
   * comments by multi-line comments).
   *
   * UNUSED
   */
  public static String trimJavaWhitespaceNoComment(String str)
  {
    return str .replaceAll("//(.*)\n", "/*$1 */")
               .replaceAll("\\s+", " ") /* compress whitespace */
               .trim();
  }

  /*****************************************************************************
   * Returns an array whose first element is the stem of the given file name
   * (the file name without the extension); and whose second element is the file
   * extension (without the dot).
   */
  public static String[] getStemAndExtension(String fileName)
  {
    int j = fileName.indexOf('.');
    String fileStem = j > 0 ? fileName.substring(0, j) : fileName;
    String fileExt = j > 0 ? fileName.substring(j + 1) : null;
    return new String[] { fileStem, fileExt };
  }

  /*****************************************************************************
   * Joins the string representations of the objects in $objs by concatenating
   * them with $sep inserted in between.
   */
  public static String join(List<? extends Object> objs, String sep)
  {
    if (objs.isEmpty()) { return ""; }

    StringBuilder builder = new StringBuilder();

    for (Object obj : objs)
    {
      builder.append(obj);
      builder.append(sep);
    }
    builder.delete(builder.length() - sep.length(), builder.length());

    return builder.toString();
  }

  /****************************************************************************/
  public static String replaceCharAt(String string, int i, String replacement)
  {
    return string.substring(0, i) + replacement
      + string.substring(i + 1, string.length());
  }

  /*****************************************************************************
   * Shortcut for multiple builder.append() calls.
   */
  public static void builderAppend(StringBuilder builder, Object... objs)
  {
    for (Object obj : objs) {
      builder.append(obj.toString());
    }
  }

  /*****************************************************************************
   * Indicates if the string ends in Java whitespace.
   */
  public static boolean endsWithWhitespace(String string)
  {
    return (" \t\r\n\f".indexOf(string.charAt(string.length() - 1)) != -1)
      || string.endsWith("*/") || string.matches("//.*$");
  }
}
